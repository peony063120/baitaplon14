package com.auction.client.service;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.LoginResponse;
import com.auction.common.dto.UserDTO;
import com.auction.common.entity.BidTransaction;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MockDataProviderTest {

  @Test
  public void testGetAuctions_ShouldReturnPopulatedList() {
    List<AuctionDTO> auctions = MockDataProvider.getAuctions();

    assertNotNull(auctions, "Danh sách đấu giá không được null");
    assertEquals(6, auctions.size(), "Danh sách đấu giá mock phải có đúng 6 phần tử");
    assertEquals("mock_auc_1", auctions.get(0).getId());
  }

  @Test
  public void testGetAuctionDetail_ShouldReturnCorrectDetail() {
    // Kiểm tra chi tiết đấu giá có trả về đúng ID truyền vào không
    String targetId = "test_id_123";
    AuctionDTO detail = MockDataProvider.getAuctionDetail(targetId);

    assertNotNull(detail, "Chi tiết đấu giá không được null");
    assertEquals(targetId, detail.getId(), "ID của chi tiết đấu giá phải khớp với ID truyền vào");
  }

  @Test
  public void testGetBidHistory_ShouldReturnFiveTransactions() {
    // Kiểm tra lịch sử đặt giá có trả về đúng 5 giao dịch không
    List<BidTransaction> history = MockDataProvider.getBidHistory("auc_123");

    assertNotNull(history, "Lịch sử đặt giá không được null");
    assertEquals(5, history.size(), "Lịch sử đặt giá phải có đúng 5 phần tử");
  }

  @Test
  public void testGetLoginResponse_ShouldReturnValidData() {
    // Kiểm tra phản hồi đăng nhập mock có thành công không
    LoginResponse response = MockDataProvider.getLoginResponse();

    assertNotNull(response);
    assertTrue(response.isSuccess(), "Trạng thái đăng nhập phải là true");
    assertEquals("mock_session_123", response.getSessionToken());
  }

  @Test
  public void testGetCurrentUser_ShouldReturnValidUser() {
    // Kiểm tra thông tin người dùng hiện tại có chính xác không
    UserDTO user = MockDataProvider.getCurrentUser();

    assertNotNull(user);
    assertEquals("user_demo", user.getId());
    assertEquals("BIDDER", user.getRole());
  }

  @Test
  public void testGetMyAuctions_ShouldReturnThreeItems() {
    // Kiểm tra danh sách sản phẩm có đúng 3 phần tử không
    List<AuctionDTO> myAuctions = MockDataProvider.getMyAuctions("seller_123");

    assertNotNull(myAuctions);
    assertEquals(3, myAuctions.size());
    assertTrue(myAuctions.get(0).getId().startsWith("my_auc_"));
  }
}