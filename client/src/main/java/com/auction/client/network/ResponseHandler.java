package com.auction.client.network;

import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ResponseHandler — Lớp tiện ích tĩnh chịu trách nhiệm giải mã (parse)
 * các chuỗi JSON thô thành các đối tượng Java có kiểu dữ liệu cụ thể.
 */
public final class ResponseHandler {

  private static final Logger LOGGER = Logger.getLogger(ResponseHandler.class.getName());

  private ResponseHandler() {}

  /**
   * Kiểm tra xem phản hồi từ Server có thành công hay không.
   * @param rawJson Chuỗi JSON thô nhận từ Server
   * @return {@code true} nếu phản hồi là thành công
   */
  public static boolean isSuccess(String rawJson) {
    try {
      Map<String, Object> envelope = protocol().decodeToMap(rawJson);
      String type = protocol().getMessageType(envelope);
      return !MessageProtocol.TYPE_ERROR.equals(type);
    } catch (Exception e) {
      LOGGER.warning("ResponseHandler.isSuccess: Không thể phân tích phản hồi — " + e.getMessage());
      return false;
    }
  }

  /**
   * Trích xuất thông điệp lỗi từ envelope phản hồi.
   * Nếu payload là chuỗi thì trả về trực tiếp; nếu là Map thì tìm trường "message".
   *
   * @param rawJson Chuỗi JSON thô nhận từ Server
   * @return Nội dung thông báo lỗi, hoặc chuỗi mặc định nếu không tìm thấy
   */
  public static String extractErrorMessage(String rawJson) {
    try {
      Map<String, Object> envelope = protocol().decodeToMap(rawJson);
      Object payload = envelope.get("payload");
      if (payload instanceof String) {
        return (String) payload;
      }
      if (payload instanceof Map) {
        Object msg = ((Map<?, ?>) payload).get("message");
        return msg != null ? msg.toString() : "Lỗi không xác định từ máy chủ.";
      }
      return "Lỗi không xác định từ máy chủ.";
    } catch (Exception e) {
      LOGGER.warning("ResponseHandler.extractErrorMessage: " + e.getMessage());
      return "Lỗi không thể đọc phản hồi từ máy chủ.";
    }
  }

  /**
   * Kết quả trả về sau khi đăng nhập hoặc đăng ký thành công.
   *
   * @param rawJson Chuỗi JSON thô nhận từ Server
   * @return Bản đồ thông tin người dùng (userId, username, token, v.v.), hoặc rỗng nếu lỗi
   */
  public static Map<String, Object> parseAuthPayload(String rawJson) {
    try {
      Map<String, Object> envelope = protocol().decodeToMap(rawJson);
      Object payload = envelope.get("payload");
      if (payload instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) payload;
        return result;
      }
      return Collections.emptyMap();
    } catch (Exception e) {
      LOGGER.warning("ResponseHandler.parseAuthPayload: " + e.getMessage());
      return Collections.emptyMap();
    }
  }

  /**
   * Giải mã danh sách phiên đấu giá từ phản hồi GET_AUCTIONS.
   *
   * @param rawJson Chuỗi JSON thô nhận từ Server
   * @return Danh sách {@link Auction}, hoặc danh sách rỗng nếu thất bại
   */
  public static List<Auction> parseAuctionList(String rawJson) {
    try {
      Map<String, Object> envelope = protocol().decodeToMap(rawJson);
      Object payload = envelope.get("payload");

      // Server có thể trả về trực tiếp danh sách hoặc bọc thêm trong object
      if (payload instanceof List) {
        @SuppressWarnings("unchecked")
        List<Object> rawList = (List<Object>) payload;
        return rawList.stream()
            .map(item -> protocol().convertValue(item, Auction.class))
            .collect(java.util.stream.Collectors.toList());
      }

      // Trường hợp payload là Map có trường "auctions" hoặc "content" (pagination)
      if (payload instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> payloadMap = (Map<String, Object>) payload;
        Object list = payloadMap.containsKey("content")
            ? payloadMap.get("content")
            : payloadMap.get("auctions");
        if (list instanceof List) {
          @SuppressWarnings("unchecked")
          List<Object> rawList = (List<Object>) list;
          return rawList.stream()
              .map(item -> protocol().convertValue(item, Auction.class))
              .collect(java.util.stream.Collectors.toList());
        }
      }

      LOGGER.warning("ResponseHandler.parseAuctionList: Cấu trúc payload không nhận dạng được.");
      return Collections.emptyList();
    } catch (Exception e) {
      LOGGER.warning("ResponseHandler.parseAuctionList: " + e.getMessage());
      return Collections.emptyList();
    }
  }

  /**
   * Giải mã chi tiết một phiên đấu giá từ phản hồi GET_AUCTION.
   *
   * @param rawJson Chuỗi JSON thô nhận từ Server
   * @return Đối tượng {@link Auction} đã giải mã, hoặc {@code null} nếu thất bại
   */
  public static Auction parseAuction(String rawJson) {
    try {
      Map<String, Object> envelope = protocol().decodeToMap(rawJson);
      Object payload = envelope.get("payload");
      if (payload == null) return null;
      return protocol().convertValue(payload, Auction.class);
    } catch (Exception e) {
      LOGGER.warning("ResponseHandler.parseAuction: " + e.getMessage());
      return null;
    }
  }

  /**
   * Giải mã kết quả giao dịch đặt giá từ phản hồi PLACE_BID.
   *
   * @param rawJson Chuỗi JSON thô nhận từ Server
   * @return Đối tượng {@link BidTransaction} đã giải mã, hoặc {@code null} nếu thất bại
   */
  public static BidTransaction parseBidTransaction(String rawJson) {
    try {
      Map<String, Object> envelope = protocol().decodeToMap(rawJson);
      Object payload = envelope.get("payload");
      if (payload == null) return null;
      return protocol().convertValue(payload, BidTransaction.class);
    } catch (Exception e) {
      LOGGER.warning("ResponseHandler.parseBidTransaction: " + e.getMessage());
      return null;
    }
  }

  /**
   * Giải mã danh sách lịch sử đặt giá từ phản hồi BID_HISTORY.
   *
   * @param rawJson Chuỗi JSON thô nhận từ Server
   * @return Danh sách {@link BidTransaction}, hoặc danh sách rỗng nếu thất bại
   */
  public static List<BidTransaction> parseBidHistory(String rawJson) {
    try {
      Map<String, Object> envelope = protocol().decodeToMap(rawJson);
      Object payload = envelope.get("payload");

      if (payload instanceof List) {
        @SuppressWarnings("unchecked")
        List<Object> rawList = (List<Object>) payload;
        return rawList.stream()
            .map(item -> protocol().convertValue(item, BidTransaction.class))
            .collect(java.util.stream.Collectors.toList());
      }

      LOGGER.warning("ResponseHandler.parseBidHistory: Cấu trúc payload không phải danh sách.");
      return Collections.emptyList();
    } catch (Exception e) {
      LOGGER.warning("ResponseHandler.parseBidHistory: " + e.getMessage());
      return Collections.emptyList();
    }
  }

  private static MessageProtocol protocol() {
    return ServerConnection.getInstance().getProtocol();
  }
}
