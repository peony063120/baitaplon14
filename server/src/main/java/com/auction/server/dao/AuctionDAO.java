package com.auction.server.dao;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AuctionDAO (Singleton)
 * Quản lý lưu trữ và truy xuất dữ liệu phiên đấu giá.
 * Sử dụng in-memory cache (ConcurrentHashMap) thay thế database.
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
      throw new IllegalArgumentException("Auction hoặc auction ID không được null");
    }
    auctionCache.put(auction.getId(), auction);
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
      throw new IllegalArgumentException("Auction ID không được null");
    }
    auctionCache.remove(id);
  }

  public List<Auction> getAuctionsByStatus(AuctionStatus status) {
    if (status == null) return new ArrayList<>();
    return auctionCache.values().stream()
        .filter(auction -> status.equals(auction.getStatus()))
        .collect(Collectors.toList());
  }

  public List<Auction> getAuctionsBySeller(String sellerId) {
    if (sellerId == null) return new ArrayList<>();
    return auctionCache.values().stream()
        .filter(auction -> sellerId.equals(auction.getSellerId()))
        .collect(Collectors.toList());
  }
}