package com.auction.server.dao;

import com.auction.common.entity.BidTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BidTransactionDAO
 * Manages bid transaction history storage and retrieval.
 * Uses Singleton pattern.
 */
public class BidTransactionDAO {

  private static BidTransactionDAO instance;
  private final List<BidTransaction> transactionList;

  // Private constructor for Singleton
  private BidTransactionDAO() {
    this.transactionList = new ArrayList<>();
  }

  public synchronized void clear() {
    transactionList.clear();
  }

  // getInstance method (Singleton)
  public static synchronized BidTransactionDAO getInstance() {
    if (instance == null) {
      instance = new BidTransactionDAO();
    }
    return instance;
  }

  public synchronized void saveBidTransaction(BidTransaction tx) {
    if (tx == null) {
      throw new IllegalArgumentException("BidTransaction must not be null");
    }
    transactionList.add(tx);
  }

  public synchronized List<BidTransaction> getBidHistory(String auctionId) {
    if (auctionId == null) return new ArrayList<>();
    return transactionList.stream()
            .filter(tx -> auctionId.equals(tx.getAuctionId()))
            .collect(Collectors.toList());
  }

  public synchronized List<BidTransaction> getBidsByUser(String userId) {
    if (userId == null) return new ArrayList<>();
    return transactionList.stream()
            .filter(tx -> userId.equals(tx.getBidderId()))
            .collect(Collectors.toList());
  }
}