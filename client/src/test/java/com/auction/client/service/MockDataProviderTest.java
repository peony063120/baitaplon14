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

    assertNotNull(auctions);
    assertEquals(6, auctions.size());
    assertEquals("mock_auc_1", auctions.get(0).getId());
  }

  @Test
  public void testGetAuctionDetail_ShouldReturnCorrectDetail() {
    AuctionDTO detail = MockDataProvider.getAuctionDetail("auc-004");

    assertNotNull(detail);
    assertEquals("Auction Detail", detail.getItemName());
  }

  @Test
  public void testGetBidHistory_ShouldReturnTransactionCount() {
    List<BidTransaction> history = MockDataProvider.getBidHistory("auc-004");

    assertNotNull(history);
    assertEquals(5, history.size());
  }

  @Test
  public void testGetLoginResponse_ShouldReturnValidData() {
    LoginResponse response = MockDataProvider.getLoginResponse();

    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertEquals("user_bidder_mock", response.getUserId());
  }

  @Test
  public void testGetCurrentUser_ShouldReturnValidUser() {
    UserDTO user = MockDataProvider.getCurrentUser();

    assertNotNull(user);
    assertEquals("user_demo", user.getId());
    assertEquals("BIDDER", user.getRole());
  }

  @Test
  public void testGetMyAuctions_ShouldReturnSellerItems() {
    List<AuctionDTO> myAuctions = MockDataProvider.getMyAuctions("user-006");

    assertNotNull(myAuctions);
    assertEquals(3, myAuctions.size());
  }
}
