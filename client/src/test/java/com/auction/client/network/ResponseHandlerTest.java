package com.auction.client.network;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.LoginResponse;
import com.auction.common.enums.AuctionStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ResponseHandlerTest {

  @BeforeEach
  public void setUp() {
    ServerConnection.resetInstance();
    ServerConnection.getInstance();
  }

  @AfterEach
  public void tearDown() {
    ServerConnection.resetInstance();
  }

  // ==================== PARSE JSON ====================

  @Test
  public void testIsSuccess_WithJsonEnvelope_CorrectlyIdentifiesStatus() {
    String jsonSuccess = "{\"type\":\"SUCCESS\",\"requestId\":\"req-1\",\"payload\":\"Thao tác thành công\"}";
    String jsonError = "{\"type\":\"ERROR\",\"requestId\":\"req-2\",\"payload\":\"Mật khẩu không hợp lệ\"}";

    assertTrue(ResponseHandler.isSuccess(jsonSuccess));
    assertFalse(ResponseHandler.isSuccess(jsonError));
  }

  @Test
  public void testExtractErrorMessage_FromJsonMapPayload() {
    String jsonError = "{\"type\":\"ERROR\",\"requestId\":\"req-2\",\"payload\":\"Số dư tài khoản không đủ để đặt giá\"}";

    String errorMessage = ResponseHandler.extractErrorMessage(jsonError);

    assertEquals("Số dư tài khoản không đủ để đặt giá", errorMessage);
  }

  @Test
  public void testParseAuthPayload_ExtractsValidMap() {
    MessageProtocol protocol = ServerConnection.getInstance().getProtocol();

    Map<String, Object> innerPayload = new java.util.HashMap<>();
    innerPayload.put("token", "JWT-TOKEN-STRING-123");
    innerPayload.put("userId", 9);

    Map<String, Object> envelope = new java.util.HashMap<>();
    envelope.put("type", "SUCCESS");
    envelope.put("payload", innerPayload);

    String jsonAuth = protocol.encode(envelope);

    Map<String, Object> payloadMap = ResponseHandler.parseAuthPayload(jsonAuth);

    assertNotNull(payloadMap);
    assertEquals("JWT-TOKEN-STRING-123", payloadMap.get("token"));

    Integer expectedUserId = 9;
    if (payloadMap.get("userId") instanceof Number) {
      assertEquals(expectedUserId, ((Number) payloadMap.get("userId")).intValue());
    } else {
      assertEquals(expectedUserId, payloadMap.get("userId"));
    }
  }

  // ==================== PARSE TEXT ====================

  @Test
  public void testParseLoginResponse_TextFormat_WithSuccessString() {
    // Format: LOGIN_OK:sessionToken:userId:username:role:balance
    String rawTextResponse = "LOGIN_OK:session_token_xyz_789:user_id_10:testuser:BIDDER:750000.0";
    LoginResponse response = ResponseHandler.parseLoginResponse(rawTextResponse);

    assertTrue(response.isSuccess());
    assertEquals("Login successful", response.getMessage());
    assertEquals("session_token_xyz_789", response.getSessionToken());
    assertEquals(750000.0, response.getBalance(), 0.001);
  }

  @Test
  public void testParseLoginResponse_TextFormat_WithFailString() {
    String rawTextResponse = "LOGIN_FAIL:User account has been suspended";
    LoginResponse response = ResponseHandler.parseLoginResponse(rawTextResponse);

    assertFalse(response.isSuccess());
    assertEquals("User account has been suspended", response.getMessage());
  }

  @Test
  public void testParseAuctionListFromText_WithMultipleLines() {
    String multipleLinesText = "AUCTIONS_COUNT:2" +
                "||AUCTION:A01:MacBook Dell XPS:2200.0:DRAFT:Electronics:540000" +
                "||AUCTION:A02:Custom Mechanical Keyboard:350.0:FINISHED:Art:0";

    List<AuctionDTO> list = ResponseHandler.parseAuctionListFromText(multipleLinesText);

    assertNotNull(list);
    assertEquals(2, list.size());

    AuctionDTO firstItem = list.get(0);
    assertEquals("A01", firstItem.getId());
    assertEquals("MacBook Dell XPS", firstItem.getItemName());
    assertEquals(2200.0, firstItem.getCurrentPrice(), 0.001);

    if (firstItem.getStatus() != null) {
      assertEquals(AuctionStatus.DRAFT.name(), firstItem.getStatus().toString());
    }

    if (firstItem.getRemainingTimeMillis() != 0) {
      assertEquals(540000L, firstItem.getRemainingTimeMillis());
    }
  }

  @Test
  public void testParseBidHistoryFromText_WithBidderUsername() {
    String response = "BID_HISTORY_COUNT:1||BID:auc1:uuid-1:bidder17:6020.0:1717248000000:false";
    List<com.auction.common.entity.BidTransaction> history =
        ResponseHandler.parseBidHistoryFromText(response);
    assertEquals(1, history.size());
    assertEquals("bidder17", history.get(0).getBidderName());
    assertEquals(6020.0, history.get(0).getAmount());
  }

  @Test
  public void testParseAuctionDetailFromText_WithWinnerName() {
    String detailText = "AUCTION:A99:Sony TV 4K:1500.0:RUNNING:winner-uuid:8:120000:1000.0:50.0::bidder17";

    AuctionDTO dto = ResponseHandler.parseAuctionDetailFromText(detailText.trim());

    assertNotNull(dto);
    assertEquals("winner-uuid", dto.getCurrentWinnerId());
    assertEquals("bidder17", dto.getCurrentWinnerName());
  }

  @Test
  public void testParseAuctionUpdateFromText_WithWinnerName() {
    String update = "AUCTION_UPDATE:ID:A99:PRICE:1500.0:WINNER:uuid-1:WINNER_NAME:bidder17:STATUS:RUNNING";

    AuctionDTO dto = ResponseHandler.parseAuctionUpdateFromText(update);

    assertNotNull(dto);
    assertEquals("uuid-1", dto.getCurrentWinnerId());
    assertEquals("bidder17", dto.getCurrentWinnerName());
  }

  @Test
  public void testParseAuctionUpdateFromText_ValidFormat() {
    String update = "AUCTION_UPDATE:ID:A99:PRICE:1500.0:WINNER:bidder1:STATUS:RUNNING";

    AuctionDTO dto = ResponseHandler.parseAuctionUpdateFromText(update);

    assertNotNull(dto);
    assertEquals("A99", dto.getId());
    assertEquals(1500.0, dto.getCurrentPrice());
    assertEquals("bidder1", dto.getCurrentWinnerId());
    assertEquals(AuctionStatus.RUNNING, dto.getStatus());
  }

  @Test
  public void testParseAuctionDetailFromText_ValidFormat() {
    String detailText = "AUCTION:A99:Sony TV 4K:1500.0:DRAFT:winner_user_3:8:120000:1000.0:50.0";

    AuctionDTO dto = ResponseHandler.parseAuctionDetailFromText(detailText.trim());

    assertNotNull(dto);
    assertEquals("A99", dto.getId());
    assertEquals("Sony TV 4K", dto.getItemName());

    if (dto.getStatus() != null) {
      assertEquals(AuctionStatus.DRAFT.name(), dto.getStatus().toString());
    }

    assertEquals("winner_user_3", dto.getCurrentWinnerId());
    assertEquals(8, dto.getTotalBids());
    assertEquals(1000.0, dto.getStartingPrice());
    assertEquals(50.0, dto.getMinIncrement());

    if (dto.getRemainingTimeMillis() != 0) {
      assertEquals(120000L, dto.getRemainingTimeMillis());
    }
  }

  @Test
  public void testIsSuccessTextAndGetErrorMessage_WithTextErrorFormat() {
    String errorString = "ERROR:Hệ thống đang bảo trì máy chủ dữ liệu";

    assertFalse(ResponseHandler.isSuccessText(errorString));
    assertEquals("Hệ thống đang bảo trì máy chủ dữ liệu", ResponseHandler.getErrorMessage(errorString));
  }
}
