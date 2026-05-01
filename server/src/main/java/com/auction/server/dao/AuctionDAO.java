package com.auction.server.dao;

import com.auction.common.entity.Auction;
import java.util.Map;

public class AuctionDAO {
  private static volatile AuctionDAO instance;
  private final Map<String, Auction> auctionCache;

  private AuctionDAO() {
    this.auctionCache = DatabaseConnection.getInstance().getConnection().getTable("AUCTIONS");
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

  /**
   * Lưu hoặc cập nhật phiên đấu giá.
   * Xử lý trường hợp hàng triệu người cùng đấu giá (Concurrent Bidding).
   */
  public void saveAuction(Auction auction) {
    if (auction == null || auction.getId() == null) {
      return;
    }

    /**
     * Khóa theo ID phiên đấu giá để đảm bảo tính tuần tự khi cập nhật giá.
     * Ngăn chặn việc giá thấp ghi đè lên giá cao do trễ mạng.
     */
    synchronized (auction.getId().intern()) {
      Auction currentInDb = auctionCache.get(auction.getId());

      // Nếu là phiên đấu giá mới hoặc có giá cao hơn giá hiện tại trong DB.
      if (currentInDb == null || auction.getCurrentPrice() > currentInDb.getCurrentPrice()) {
        auctionCache.put(auction.getId(), auction);
      }
      // Nếu giá mới thấp hơn hoặc bằng giá hiện tại, ta không làm gì để bảo vệ dữ liệu.
    }
  }

  /**
   * Lấy thông tin phiên đấu giá từ kho lưu trữ tập trung.
   */
  public Auction getAuction(String id) {
    if (id == null) {
      return null;
    }
    return auctionCache.get(id);
  }
}