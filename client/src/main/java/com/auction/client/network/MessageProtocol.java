package com.auction.client.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * MessageProtocol — Chịu trách nhiệm mã hóa các đối tượng Java
 * thành chuỗi JSON và giải mã ngược lại từ JSON thành đối tượng Java
 * phục vụ cho việc giao tiếp giữa Client và Server.
 */
public class MessageProtocol {

  // Các hằng số định nghĩa loại thông điệp (Message Type)

  public static final String TYPE_LOGIN           = "LOGIN";
  public static final String TYPE_REGISTER        = "REGISTER";
  public static final String TYPE_PLACE_BID       = "PLACE_BID";
  public static final String TYPE_GET_AUCTIONS    = "GET_AUCTIONS";
  public static final String TYPE_GET_AUCTION     = "GET_AUCTION";
  public static final String TYPE_CREATE_AUCTION  = "CREATE_AUCTION";
  public static final String TYPE_BID_UPDATE      = "BID_UPDATE";
  public static final String TYPE_AUCTION_UPDATE  = "AUCTION_UPDATE";
  public static final String TYPE_AUCTION_ENDED   = "AUCTION_ENDED";
  public static final String TYPE_ERROR           = "ERROR";
  public static final String TYPE_SUCCESS         = "SUCCESS";
  public static final String TYPE_AUTO_BID_CONFIG = "AUTO_BID_CONFIG";
  public static final String TYPE_BID_HISTORY     = "BID_HISTORY";

  private final ObjectMapper objectMapper;

  public MessageProtocol() {
    this.objectMapper = new ObjectMapper();
    // Đăng ký module để hỗ trợ định dạng thời gian Java 8 (LocalDateTime)
    this.objectMapper.registerModule(new JavaTimeModule());
    // Cấu hình không xuất thời gian dưới dạng timestamp số nguyên mà dạng chuỗi ISO chuẩn
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  /**
   * Mã hóa một đối tượng Java thành một chuỗi JSON.
   *
   * @param request Đối tượng cần mã hóa
   * @return Chuỗi JSON sau khi mã hóa
   */
  public String encode(Object request) {
    try {
      return objectMapper.writeValueAsString(request);
    } catch (Exception e) {
      // CHANGED: "Mã hóa đối tượng thất bại -> " -> "Object encoding failed -> "
      throw new RuntimeException("MessageProtocol: Object encoding failed -> " + e.getMessage(), e);
    }
  }

  /**
   * Giải mã chuỗi JSON thành một cấu trúc Map cấu hình chung.
   *
   * @param json Chuỗi JSON thô
   * @return Bản đồ cấu trúc dữ liệu Map khóa-giá trị
   */
  public Map<String, Object> decodeToMap(String json) {
    try {
      return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
      // CHANGED: "Giải mã chuỗi JSON thành Map thất bại -> " -> "Decoding JSON string to Map failed -> "
      throw new RuntimeException("MessageProtocol: Decoding JSON string to Map failed -> " + e.getMessage(), e);
    }
  }

  /**
   * Giải mã chuỗi JSON thành một lớp đối tượng cụ thể (POJO).
   *
   * @param json      Chuỗi JSON thô
   * @param valueType Lớp đối tượng đích cần giải mã về
   * @return Đối tượng thuộc kiểu dữ liệu được chỉ định
   */
  public <T> T decodeAs(String json, Class<T> valueType) {
    try {
      return objectMapper.readValue(json, valueType);
    } catch (Exception e) {
      // CHANGED: "Giải mã JSON sang lớp " ... " thất bại -> " -> "Decoding JSON to class " ... " failed -> "
      throw new RuntimeException(
              "MessageProtocol: Decoding JSON to class " + valueType.getSimpleName() + " failed -> " + e.getMessage(), e);
    }
  }

  /**
   * Chuyển đổi một đối tượng sang kiểu khác thông qua ObjectMapper.
   * Thay thế an toàn cho getObjectMapper() — tránh expose ObjectMapper ra ngoài.
   *
   * @param from   Đối tượng nguồn (thường là Map hoặc LinkedHashMap từ JSON)
   * @param toType Lớp đích cần chuyển đổi sang
   * @return Đối tượng đã được chuyển đổi
   */
  public <T> T convertValue(Object from, Class<T> toType) {
    try {
      return objectMapper.convertValue(from, toType);
    } catch (Exception e) {
      // CHANGED: "Chuyển đổi đối tượng sang " ... " thất bại -> " -> "Converting value to " ... " failed -> "
      throw new RuntimeException(
              "MessageProtocol: Converting value to " + toType.getSimpleName() + " failed -> " + e.getMessage(), e);
    }
  }

  /**
   * Serialize một đối tượng thành chuỗi JSON rồi giải mã thành Map.
   * Phù hợp khi cần lấy payload lồng nhau (nested object) từ envelope.
   *
   * @param value Đối tượng cần serialize thành JSON trung gian
   * @return Chuỗi JSON của đối tượng đó
   */
  public String writeValueAsString(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception e) {
      // CHANGED: "writeValueAsString thất bại -> " -> "writeValueAsString failed -> "
      throw new RuntimeException("MessageProtocol: writeValueAsString failed -> " + e.getMessage(), e);
    }
  }

  /**
   * Lấy giá trị trường định danh loại thông điệp "type" từ bản đồ dữ liệu.
   *
   * @param envelope Bản đồ thông điệp dạng bao bì (envelope map)
   * @return Chuỗi định danh loại thông điệp, hoặc "UNKNOWN" nếu không tìm thấy
   */
  public String getMessageType(Map<String, Object> envelope) {
    Object type = envelope.get("type");
    return type != null ? type.toString() : "UNKNOWN";
  }

  /**
   * Khởi tạo một cấu trúc thông điệp chuẩn (Envelope Map) tích hợp kèm mã định danh
   * tự động 'requestId' nhằm phục vụ cho cơ chế theo dõi phản hồi đồng bộ.
   *
   * @param type    Loại thông điệp hằng số (Ví dụ: LOGIN, PLACE_BID)
   * @param payload Dữ liệu cốt lõi đính kèm bên trong thông điệp
   * @return Bản đồ cấu trúc gói tin chuẩn chỉnh bao gồm type, requestId, và payload
   */
  public Map<String, Object> buildRequestMessage(String type, Object payload) {
    Map<String, Object> message = new HashMap<>();
    message.put("type", type);
    message.put("requestId", UUID.randomUUID().toString());
    message.put("payload", payload);
    return message;
  }
}