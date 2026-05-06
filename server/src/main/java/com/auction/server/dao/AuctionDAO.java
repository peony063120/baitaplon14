package com.auction.server.dao;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AuctionDAO {
  private static volatile AuctionDAO instance;
  private final Map<String, Auction> auctionCache;

  private AuctionDAO() {
    this.auctionCache = DatabaseConnection.getInstance().getTable("AUCTIONS");
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
    if (auction == null || auction.getId() == null) return;
    synchronized (auction.getId().intern()) {
      Auction current = auctionCache.get(auction.getId());
      if (current == null || auction.getCurrentPrice() > current.getCurrentPrice()) {
        auctionCache.put(auction.getId(), auction);
      }
    }
  }

  public Auction getAuction(String id) {
    if (id == null) return null;
    return auctionCache.get(id);
  }

  public Collection<Auction> getAllAuctions() {
    return auctionCache.values();
  }

  public void deleteAuction(String id) {
    if (id != null) auctionCache.remove(id);
  }

  public List<Auction> getAuctionsByStatus(AuctionStatus status) {
    List<Auction> result = new ArrayList<>();
    for (Auction auction : auctionCache.values()) {
      if (auction.getStatus() == status) {
        result.add(auction);
      }
    }
    return result;
  }
}