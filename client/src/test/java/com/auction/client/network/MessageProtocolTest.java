package com.auction.client.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MessageProtocolTest {

  private MessageProtocol protocol;

  @BeforeEach
  public void setUp() {
    protocol = new MessageProtocol();
  }

  @Test
  public void testEncodeAndDecodeToMap_Success() {
    // GIVEN: Tạo một bản đồ Map dữ liệu mẫu
    Map<String, Object> innerData = new HashMap<>();
    innerData.put("title", "Sản phẩm thử nghiệm");

    Map<String, Object> originalMap = new HashMap<>();
    originalMap.put("type", MessageProtocol.TYPE_LOGIN);
    originalMap.put("payload", innerData);

    // WHEN: Tiến hành mã hóa thành JSON và giải mã ngược lại
    String json = protocol.encode(originalMap);
    assertNotNull(json, "Chuỗi JSON đầu ra không được null");

    Map<String, Object> decodedMap = protocol.decodeToMap(json);

    // THEN: Xác thực tính toàn vẹn của dữ liệu sau khi giải mã
    assertEquals(MessageProtocol.TYPE_LOGIN, protocol.getMessageType(decodedMap));

    @SuppressWarnings("unchecked")
    Map<String, Object> decodedPayload = (Map<String, Object>) decodedMap.get("payload");
    assertEquals("Sản phẩm thử nghiệm", decodedPayload.get("title"));
  }

  @Test
  public void testBuildRequestMessage_ContainsRequiredFields() {
    // GIVEN: Chuẩn bị dữ liệu payload mẫu
    String payloadData = "Test Payload String";

    // WHEN: Tạo request qua cấu trúc envelope chuẩn của giao thức
    Map<String, Object> message = protocol.buildRequestMessage(MessageProtocol.TYPE_PLACE_BID, payloadData);

    // THEN: Đảm bảo có đầy đủ cấu trúc: type, requestId (UUID) và payload
    assertNotNull(message);
    assertEquals(MessageProtocol.TYPE_PLACE_BID, message.get("type"));
    assertNotNull(message.get("requestId"), "Hệ thống phải tự động sinh chuỗi UUID cho requestId");
    assertEquals(payloadData, message.get("payload"));
  }

  @Test
  public void testGetMessageType_WithUnknownOrNullType() {
    // GIVEN: Chuẩn bị envelope không hợp lệ hoặc thiếu thuộc tính type
    Map<String, Object> emptyEnvelope = new HashMap<>();
    Map<String, Object> nullTypeEnvelope = new HashMap<>();
    nullTypeEnvelope.put("type", null);

    // WHEN & THEN: Hệ thống phải nhận định là "UNKNOWN"
    assertEquals("UNKNOWN", protocol.getMessageType(emptyEnvelope));
    assertEquals("UNKNOWN", protocol.getMessageType(nullTypeEnvelope));
  }

  @Test
  public void testConvertValue_Success() {
    // GIVEN: Một bản đồ Map biểu diễn cấu trúc thuộc tính đơn giản
    Map<String, Object> rawData = new HashMap<>();
    rawData.put("testKey", "testValue");

    // WHEN: Chuyển đổi kiểu dữ liệu trung gian qua Jackson sang Map đích
    Map<?, ?> result = protocol.convertValue(rawData, Map.class);

    // THEN: Dữ liệu phải khớp hoàn toàn
    assertNotNull(result);
    assertEquals("testValue", result.get("testKey"));
  }
}