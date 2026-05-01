package com.auction.server.dao;

import com.auction.common.entity.BidTransaction;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BidTransactionDAO {
  private static class Holder {
    private static final BidTransactionDAO INSTANCE = new BidTransactionDAO();
  }
  public static BidTransactionDAO getInstance() { return Holder.INSTANCE; }
  private BidTransactionDAO() {}

  public void saveBidTransaction(BidTransaction tx) {
    if (tx == null || tx.getAuctionId() == null || tx.getBidderId() == null) return;

    Map<String, Queue<BidTransaction>> storage = DatabaseConnection.getInstance().getTable("BID_HISTORY");
    Map<String, Queue<BidTransaction>> index = DatabaseConnection.getInstance().getTable("BIDDER_INDEX");

    // Clone để tránh bị sửa đổi sau khi lưu
    BidTransaction cloned = new BidTransaction(
            tx.getAuctionId(), tx.getBidderId(), tx.getAmount(), tx.getBidTime(), tx.isAutoBid()
    );
    // Có thể cần set TransactionId nếu có
    cloned.setTransactionId(tx.getTransactionId());

    storage.computeIfAbsent(cloned.getAuctionId(), k -> new ConcurrentLinkedQueue<>()).add(cloned);
    index.computeIfAbsent(cloned.getBidderId(), k -> new ConcurrentLinkedQueue<>()).add(cloned);
  }

  public List<BidTransaction> getBidHistory(String auctionId) {
    Map<String, Queue<BidTransaction>> storage = DatabaseConnection.getInstance().getTable("BID_HISTORY");
    Queue<BidTransaction> queue = storage.get(auctionId);
    if (queue == null) return Collections.emptyList();

    List<BidTransaction> result = new ArrayList<>();
    for (BidTransaction tx : queue) {
      result.add(new BidTransaction(
              tx.getAuctionId(), tx.getBidderId(), tx.getAmount(), tx.getBidTime(), tx.isAutoBid()
      ));
    }
    return result;
  }

  public List<BidTransaction> getBidsByUser(String userId) {
    Map<String, Queue<BidTransaction>> index = DatabaseConnection.getInstance().getTable("BIDDER_INDEX");
    Queue<BidTransaction> queue = index.get(userId);
    if (queue == null) return Collections.emptyList();
    return new ArrayList<>(queue);
  }
}