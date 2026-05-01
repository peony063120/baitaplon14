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

    Map<String, Queue<BidTransaction>> storage = DatabaseConnection.getInstance().getConnection().getTable("BID_HISTORY");
    Map<String, Queue<BidTransaction>> index = DatabaseConnection.getInstance().getConnection().getTable("BIDDER_INDEX");

    // Defensive Copy: Bảo vệ dữ liệu gốc khỏi việc bị sửa đổi bên ngoài DAO.
    BidTransaction cloned = new BidTransaction(
        tx.getAuctionId(), tx.getBidderId(), tx.getAmount(), tx.getBidTime(), tx.isAutoBid()
    );

    // Lưu lịch sử theo sản phẩm và theo người dùng.
    storage.computeIfAbsent(cloned.getAuctionId(), k -> new ConcurrentLinkedQueue<>()).add(cloned);
    index.computeIfAbsent(cloned.getBidderId(), k -> new ConcurrentLinkedQueue<>()).add(cloned);
  }

  public List<BidTransaction> getBidHistory(String auctionId) {
    Map<String, Queue<BidTransaction>> storage = DatabaseConnection.getInstance().getConnection().getTable("BID_HISTORY");
    Queue<BidTransaction> queue = storage.get(auctionId);
    if (queue == null) return Collections.emptyList();

    // Trả về bản sao để bên ngoài (như UI) không can thiệp được vào dữ liệu gốc.
    List<BidTransaction> result = new ArrayList<>();
    for (BidTransaction tx : queue) {
      result.add(new BidTransaction(tx.getAuctionId(), tx.getBidderId(), tx.getAmount(), tx.getBidTime(), tx.isAutoBid()));
    }
    return result;
  }
}