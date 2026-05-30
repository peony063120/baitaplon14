package com.auction.client.model;

import com.auction.common.entity.BidTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BidHistoryModelTest {

  private BidHistoryModel bidHistoryModel;

  @BeforeEach
  void setUp() {
    bidHistoryModel = new BidHistoryModel();
  }

  @Test
  void testAddBidSuccess() {
    BidTransaction bid = new BidTransaction("auc-1", "bidder-1", 100.0, LocalDateTime.now(), false);
    bidHistoryModel.addBid(bid);

    assertEquals(1, bidHistoryModel.getBids().size());
  }

  @Test
  void testAddBidNull() {
    // Thêm đối tượng null không làm thay đổi kích thước danh sách
    bidHistoryModel.addBid(null);
    assertEquals(0, bidHistoryModel.getBids().size());
  }

  @Test
  void testGetBidsReturnsUnmodifiableList() {
    // Khởi tạo một giao dịch hợp lệ
    BidTransaction bid = new BidTransaction("auc-1", "bidder-1", 100.0, LocalDateTime.now(), false);
    bidHistoryModel.addBid(bid);

    List<BidTransaction> bids = bidHistoryModel.getBids();
    // Kiểm tra xem danh sách trả về có phải unmodifiable không (chặn sửa đổi trực tiếp từ bên ngoài)
    assertThrows(UnsupportedOperationException.class, () -> bids.add(
        new BidTransaction("auc-2", "bidder-2", 200.0, LocalDateTime.now(), false)
    ));
  }

  @Test
  void testGetBidsForAuction() {
    // Truyền trực tiếp auctionId mong muốn vào constructor thay vì gọi hàm setAuctionId
    BidTransaction bid1 = new BidTransaction("auc-1", "bidder-1", 100.0, LocalDateTime.now(), false);
    BidTransaction bid2 = new BidTransaction("auc-2", "bidder-2", 200.0, LocalDateTime.now(), false);

    bidHistoryModel.addBid(bid1);
    bidHistoryModel.addBid(bid2);

    // Kiểm tra tính năng lọc danh sách đấu giá theo mã auctionId cụ thể
    List<BidTransaction> auction1Bids = bidHistoryModel.getBidsForAuction("auc-1");
    assertEquals(1, auction1Bids.size());
    assertEquals("auc-1", auction1Bids.get(0).getAuctionId());
  }

  @Test
  void testGetBidsForAuctionWithNullId() {
    // Kiểm tra khi truyền vào auctionId là null, hệ thống phải trả về danh sách rỗng thay vì crash
    List<BidTransaction> results = bidHistoryModel.getBidsForAuction(null);
    assertNotNull(results);
    assertTrue(results.isEmpty());
  }

  @Test
  void testClearHistory() {
    // Kiểm tra tính năng xóa sạch lịch sử
    BidTransaction bid = new BidTransaction("auc-1", "bidder-1", 100.0, LocalDateTime.now(), false);
    bidHistoryModel.addBid(bid);

    bidHistoryModel.clear();
    assertEquals(0, bidHistoryModel.getBids().size());
  }
}