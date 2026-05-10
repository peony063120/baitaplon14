package com.auction.server.dao;

import com.auction.common.entity.BidTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BidTransactionDAO
 * Quản lý lưu trữ và truy xuất lịch sử giao dịch đấu giá.
 */
public class BidTransactionDAO {

  private final List<BidTransaction> transactionList;

  public BidTransactionDAO() {
    this.transactionList = new ArrayList<>();
  }

  public synchronized void saveBidTransaction(BidTransaction tx) {
    if (tx == null) {
      throw new IllegalArgumentException("BidTransaction không được null");
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