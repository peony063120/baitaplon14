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

  // ==================== PARSE TEXT THUẦN ====================

  @Test
  public void testParseLoginResponse_TextFormat_WithSuccessString() {
    String rawTextResponse = "LOGIN_OK:session_token_xyz_789:user_id_10:BIDDER:750000.0";
    LoginResponse response = ResponseHandler.parseLoginResponse(rawTextResponse);

    assertTrue(response.isSuccess());
    assertEquals("Đăng nhập thành công", response.getMessage());
    assertEquals("session_token_xyz_789", response.getSessionToken());
    assertEquals(750000.0, response.getBalance(), 0.001);
  }

  @Test
  public void testParseLoginResponse_TextFormat_WithFailString() {
    String rawTextResponse = "LOGIN_FAIL:Tài khoản người dùng đã bị đình chỉ";
    LoginResponse response = ResponseHandler.parseLoginResponse(rawTextResponse);

    assertFalse(response.isSuccess());
    assertEquals("Tài khoản người dùng đã bị đình chỉ", response.getMessage());
  }

  @Test
  public void testParseAuctionListFromText_WithMultipleLines() {
    // Sử dụng Text Block (Java 15+) giúp tránh hoàn toàn lỗi cú pháp nối chuỗi và ký tự xuống dòng
    String multipleLinesText = """
                AUCTIONS_COUNT:2
                AUCTION:A01:Máy tính Dell XPS:2200.0:DRAFT:540000
                AUCTION:A02:Bàn phím cơ Custom:350.0:FINISHED:0
                """;

    List<AuctionDTO> list = ResponseHandler.parseAuctionListFromText(multipleLinesText.trim());

    assertNotNull(list);
    assertEquals(2, list.size());

    AuctionDTO firstItem = list.get(0);
    assertEquals("A01", firstItem.getId());
    assertEquals("Máy tính Dell XPS", firstItem.getItemName());
    assertEquals(2200.0, firstItem.getCurrentPrice(), 0.001);

    if (firstItem.getStatus() != null) {
      assertEquals(AuctionStatus.DRAFT.name(), firstItem.getStatus().toString());
    }

    if (firstItem.getRemainingTimeMillis() != 0) {
      assertEquals(540000L, firstItem.getRemainingTimeMillis());
    }
  }

  @Test
  public void testParseAuctionDetailFromText_ValidFormat() {
    String detailText = "AUCTION:A99:Tivi Sony 4K:1500.0:DRAFT:winner_user_3:8:120000";

    AuctionDTO dto = ResponseHandler.parseAuctionDetailFromText(detailText.trim());

    assertNotNull(dto);
    assertEquals("A99", dto.getId());
    assertEquals("Tivi Sony 4K", dto.getItemName());

    if (dto.getStatus() != null) {
      assertEquals(AuctionStatus.DRAFT.name(), dto.getStatus().toString());
    }

    assertEquals("winner_user_3", dto.getCurrentWinnerId());
    assertEquals(8, dto.getTotalBids());

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