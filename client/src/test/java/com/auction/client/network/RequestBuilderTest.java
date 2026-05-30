package com.auction.client.network;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RequestBuilderTest {

  @BeforeEach
  public void setUp() throws Exception {
    // Reset trạng thái Singleton để thiết lập môi trường Mock ảo cho ServerConnection
    ServerConnection.resetInstance();
    ServerConnection connectionInstance = ServerConnection.getInstance();

  }

  @AfterEach
  public void tearDown() {
    ServerConnection.resetInstance();
  }

  @Test
  public void testLoginRequest_BuildsCorrectPayload() {
    // WHEN: Gọi phương thức tiện ích xây dựng gói tin đăng nhập
    Map<String, Object> request = RequestBuilder.login("user_test", "password_123");

    // THEN: Xác thực cấu trúc định dạng bao bì gói tin gửi lên Server
    assertEquals(MessageProtocol.TYPE_LOGIN, request.get("type"));
    assertNotNull(request.get("requestId"), "Gói tin bắt buộc phải đi kèm mã định danh requestId");

    @SuppressWarnings("unchecked")
    Map<String, Object> payload = (Map<String, Object>) request.get("payload");
    assertNotNull(payload, "Payload dữ liệu không được null");
    assertEquals("user_test", payload.get("username"));
    assertEquals("password_123", payload.get("password"));
  }

  @Test
  public void testCreateAuctionRequest_BuildsCorrectPayload() {
    BigDecimal startingPrice = new BigDecimal("1000.50");
    String endTimeIso = "2026-12-31T23:59:59Z";

    // WHEN: Tạo cấu trúc yêu cầu mở phiên đấu giá mới
    Map<String, Object> request = RequestBuilder.createAuction(
        "Đồng hồ cổ", "Hàng hiếm năm 1990", startingPrice, endTimeIso, 45L
    );

    // THEN: Kiểm tra tính chính xác của các thuộc tính được ánh xạ vào JSON Payload
    assertEquals(MessageProtocol.TYPE_CREATE_AUCTION, request.get("type"));

    @SuppressWarnings("unchecked")
    Map<String, Object> payload = (Map<String, Object>) request.get("payload");
    assertEquals("Đồng hồ cổ", payload.get("title"));
    assertEquals("Hàng hiếm năm 1990", payload.get("description"));
    assertEquals(startingPrice, payload.get("startingPrice"));
    assertEquals(endTimeIso, payload.get("endTime"));
    assertEquals(45L, payload.get("sellerId"));
  }

  @Test
  public void testConfigAutoBidRequest_BuildsCorrectPayload() {
    BigDecimal maxAmount = new BigDecimal("5000.00");
    BigDecimal bidStep = new BigDecimal("200.00");

    // WHEN: Thiết lập cấu hình hệ thống tự động trả giá (Auto-Bid)
    Map<String, Object> request = RequestBuilder.configAutoBid(101L, 12L, maxAmount, bidStep, true);

    // THEN: Kiểm tra các giá trị logic biểu thị trạng thái kích hoạt hệ thống
    @SuppressWarnings("unchecked")
    Map<String, Object> payload = (Map<String, Object>) request.get("payload");
    assertNotNull(payload);
    assertEquals(101L, payload.get("auctionId"));
    assertEquals(12L, payload.get("bidderId"));
    assertEquals(maxAmount, payload.get("maxAmount"));
    assertEquals(bidStep, payload.get("bidStep"));
    assertEquals(true, payload.get("enabled"));
  }
}