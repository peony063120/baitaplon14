package com.auction.server.dao;

import com.auction.common.entity.Auction;
import com.auction.common.entity.User;
import com.auction.common.enums.AuctionStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AuctionDAO (Singleton)
 * In-memory cache with H2 persistence (same pattern as UserDAO).
 */
public class AuctionDAO {

  private static volatile AuctionDAO instance;
  private final Map<String, Auction> auctionCache;

  private AuctionDAO() {
    this.auctionCache = new ConcurrentHashMap<>();
  }

  public static AuctionDAO getInstance() {
    if (instance == null) {
      synchronized (AuctionDAO.class) {
        if (instance == null) {
          instance = new AuctionDAO();
        }
      }
    }
    return instance;
  }

  public void saveAuction(Auction auction) {
    if (auction == null || auction.getId() == null) {
      throw new IllegalArgumentException("Auction or auction ID must not be null");
    }
    normalizeItemId(auction);
    auctionCache.put(auction.getId(), auction);
    persistAuctionToDatabase(auction);
  }

  public Auction getAuction(String id) {
    if (id == null) return null;
    return auctionCache.get(id);
  }

  public List<Auction> getAllAuctions() {
    return new ArrayList<>(auctionCache.values());
  }

  public void deleteAuction(String id) {
    if (id == null) {
      throw new IllegalArgumentException("Auction ID must not be null");
    }
    Auction removed = auctionCache.remove(id);
    deleteAuctionFromDatabase(id, removed != null ? removed.getItemId() : null);
  }

  public List<Auction> getAuctionsByStatus(AuctionStatus status) {
    if (status == null) return new ArrayList<>();
    return auctionCache.values().stream()
        .filter(auction -> status.equals(auction.getStatus()))
        .collect(Collectors.toList());
  }

  public List<Auction> getAuctionsBySeller(String sellerId) {
    if (sellerId == null) return new ArrayList<>();
    String sellerUsername = resolveSellerRef(sellerId);
    return auctionCache.values().stream()
        .filter(auction -> sellerId.equals(auction.getSellerId())
                || (sellerUsername != null && sellerUsername.equals(auction.getSellerId()))
                || sellerId.equals(resolveSellerRef(auction.getSellerId())))
        .collect(Collectors.toList());
  }

  private static void normalizeItemId(Auction auction) {
    String itemId = auction.getItemId();
    if (itemId == null || itemId.isBlank() || "unknown".equalsIgnoreCase(itemId)) {
      auction.setItemId(auction.getId());
    }
  }

  private static String resolveSellerRef(String sellerRef) {
    if (sellerRef == null || sellerRef.isBlank()) {
      return null;
    }
    UserDAO userDAO = UserDAO.getInstance();
    User byId = userDAO.findUserById(sellerRef);
    if (byId != null) {
      return byId.getUsername();
    }
    User byName = userDAO.findUserByUsername(sellerRef);
    return byName != null ? byName.getUsername() : sellerRef;
  }

  private String resolveSellerDbId(String sellerRef) {
    if (sellerRef == null || sellerRef.isBlank()) {
      return null;
    }
    UserDAO userDAO = UserDAO.getInstance();
    User byId = userDAO.findUserById(sellerRef);
    if (byId != null) {
      return byId.getId();
    }
    User byName = userDAO.findUserByUsername(sellerRef);
    return byName != null ? byName.getId() : null;
  }

  private void persistAuctionToDatabase(Auction auction) {
    String sellerDbId = resolveSellerDbId(auction.getSellerId());
    if (sellerDbId == null) {
      System.err.println("[AuctionDAO] Skip DB persist — unknown seller: " + auction.getSellerId());
      return;
    }

    try {
      Connection conn = DatabaseConnection.getInstance().getConnection();
      String itemId = auction.getItemId();
      ensureItemExists(conn, auction, sellerDbId, itemId);

      boolean exists = auctionExistsInDatabase(conn, auction.getId());
      String status = auction.getStatus() != null ? auction.getStatus().name() : AuctionStatus.DRAFT.name();
      double startingPrice = auction.getStartingPrice() > 0 ? auction.getStartingPrice() : auction.getCurrentPrice();
      Timestamp startTs = Timestamp.valueOf(
              auction.getStartTime() != null ? auction.getStartTime() : java.time.LocalDateTime.now());
      Timestamp endTs = Timestamp.valueOf(
              auction.getEndTime() != null ? auction.getEndTime() : java.time.LocalDateTime.now().plusDays(1));

      if (exists) {
        String sql = "UPDATE auctions SET item_id = ?, seller_id = ?, start_time = ?, end_time = ?, "
                + "starting_price = ?, current_price = ?, current_winner_id = ?, min_increment = ?, "
                + "status = ?, anti_sniping_enabled = ?, anti_sniping_extension_seconds = ?, "
                + "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
          stmt.setString(1, itemId);
          stmt.setString(2, sellerDbId);
          stmt.setTimestamp(3, startTs);
          stmt.setTimestamp(4, endTs);
          stmt.setDouble(5, startingPrice);
          stmt.setDouble(6, auction.getCurrentPrice());
          setNullableString(stmt, 7, auction.getCurrentWinnerId());
          stmt.setDouble(8, auction.getMinIncrement());
          stmt.setString(9, status);
          stmt.setBoolean(10, auction.isAntiSnipingEnabled());
          stmt.setInt(11, (int) auction.getAntiSnipingExtensionSeconds());
          stmt.setString(12, auction.getId());
          stmt.executeUpdate();
        }
      } else {
        String sql = "INSERT INTO auctions (id, item_id, seller_id, start_time, end_time, starting_price, "
                + "current_price, current_winner_id, min_increment, status, anti_sniping_enabled, "
                + "anti_sniping_extension_seconds, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
          stmt.setString(1, auction.getId());
          stmt.setString(2, itemId);
          stmt.setString(3, sellerDbId);
          stmt.setTimestamp(4, startTs);
          stmt.setTimestamp(5, endTs);
          stmt.setDouble(6, startingPrice);
          stmt.setDouble(7, auction.getCurrentPrice());
          setNullableString(stmt, 8, auction.getCurrentWinnerId());
          stmt.setDouble(9, auction.getMinIncrement());
          stmt.setString(10, status);
          stmt.setBoolean(11, auction.isAntiSnipingEnabled());
          stmt.setInt(12, (int) auction.getAntiSnipingExtensionSeconds());
          stmt.executeUpdate();
        }
      }
    } catch (SQLException e) {
      System.err.println("[AuctionDAO] Failed to persist auction to database: " + e.getMessage());
    }
  }

  private void ensureItemExists(Connection conn, Auction auction, String sellerDbId, String itemId)
          throws SQLException {
    if (itemExistsInDatabase(conn, itemId)) {
      return;
    }
    String name = auction.getItemName() != null && !auction.getItemName().isBlank()
            ? auction.getItemName() : "Auction Item";
    String description = auction.getItemDescription() != null ? auction.getItemDescription() : "";
    String category = auction.getCategory() != null ? auction.getCategory() : "";
    String itemType = mapItemType(category);
    String sql = "INSERT INTO items (id, seller_id, name, description, category, item_type, attributes, created_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, NULL, CURRENT_TIMESTAMP)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, itemId);
      stmt.setString(2, sellerDbId);
      stmt.setString(3, name);
      stmt.setString(4, description);
      stmt.setString(5, category);
      stmt.setString(6, itemType);
      stmt.executeUpdate();
    }
  }

  private static String mapItemType(String category) {
    if (category == null) {
      return "ELECTRONICS";
    }
    String c = category.toLowerCase();
    if (c.contains("vehicle") || c.contains("xe")) {
      return "VEHICLE";
    }
    if (c.contains("art")) {
      return "ART";
    }
    return "ELECTRONICS";
  }

  private static boolean itemExistsInDatabase(Connection conn, String itemId) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM items WHERE id = ?")) {
      stmt.setString(1, itemId);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    }
  }

  private static boolean auctionExistsInDatabase(Connection conn, String auctionId) throws SQLException {
    try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM auctions WHERE id = ?")) {
      stmt.setString(1, auctionId);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    }
  }

  private void deleteAuctionFromDatabase(String auctionId, String itemId) {
    try {
      Connection conn = DatabaseConnection.getInstance().getConnection();

      try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM auctions WHERE id = ?")) {
        stmt.setString(1, auctionId);
        stmt.executeUpdate();
      }
      if (itemId != null) {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM items WHERE id = ?")) {
          stmt.setString(1, itemId);
          stmt.executeUpdate();
        }
      }
    } catch (SQLException e) {
      System.err.println("[AuctionDAO] Failed to delete auction from database: " + e.getMessage());
    }
  }

  private static void setNullableString(PreparedStatement stmt, int index, String value) throws SQLException {
    if (value == null || value.isBlank()) {
      stmt.setNull(index, java.sql.Types.VARCHAR);
    } else {
      stmt.setString(index, value);
    }
  }
}
