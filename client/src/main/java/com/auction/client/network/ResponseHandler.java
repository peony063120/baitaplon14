package com.auction.client.network;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.LoginResponse;
import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ResponseHandler - Xử lý phản hồi từ server
 * Hỗ trợ cả 2 định dạng: JSON (qua MessageProtocol) và TEXT thuần
 */
public final class ResponseHandler {

  private static final Logger LOGGER = Logger.getLogger(ResponseHandler.class.getName());

  private ResponseHandler() {}

  // ==================== PHẦN 1: PARSE JSON (Dùng MessageProtocol) ====================

  public static boolean isSuccess(String rawJson) {
    try {
      Map<String, Object> envelope = protocol().decodeToMap(rawJson);
      String type = protocol().getMessageType(envelope);
      return !MessageProtocol.TYPE_ERROR.equals(type);
    } catch (Exception e) {
      LOGGER.warning("isSuccess: " + e.getMessage());
      return false;
    }
  }

  public static String extractErrorMessage(String rawJson) {
    try {
      Map<String, Object> envelope = protocol().decodeToMap(rawJson);
      Object payload = envelope.get("payload");
      if (payload instanceof String) return (String) payload;
      if (payload instanceof Map) {
        Object msg = ((Map<?, ?>) payload).get("message");
        // CHANGED: "Lỗi không xác định" -> "Unknown error"
        return msg != null ? msg.toString() : "Unknown error";
      }
      // CHANGED: "Lỗi không xác định" -> "Unknown error"
      return "Unknown error";
    } catch (Exception e) {
      // CHANGED: "Lỗi đọc phản hồi: " -> "Failed to read response: "
      return "Failed to read response: " + e.getMessage();
    }
  }

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
      LOGGER.warning("parseAuthPayload: " + e.getMessage());
      return Collections.emptyMap();
    }
  }

  public static List<Auction> parseAuctionList(String rawJson) {
    try {
      Map<String, Object> envelope = protocol().decodeToMap(rawJson);
      Object payload = envelope.get("payload");
      if (payload instanceof List) {
        @SuppressWarnings("unchecked")
        List<Object> rawList = (List<Object>) payload;
        return rawList.stream()
                .map(item -> protocol().convertValue(item, Auction.class))
                .collect(java.util.stream.Collectors.toList());
      }
      if (payload instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> payloadMap = (Map<String, Object>) payload;
        Object list = payloadMap.containsKey("content") ? payloadMap.get("content") : payloadMap.get("auctions");
        if (list instanceof List) {
          @SuppressWarnings("unchecked")
          List<Object> rawList = (List<Object>) list;
          return rawList.stream()
                  .map(item -> protocol().convertValue(item, Auction.class))
                  .collect(java.util.stream.Collectors.toList());
        }
      }
      return Collections.emptyList();
    } catch (Exception e) {
      LOGGER.warning("parseAuctionList: " + e.getMessage());
      return Collections.emptyList();
    }
  }

  public static Auction parseAuction(String rawJson) {
    try {
      Map<String, Object> envelope = protocol().decodeToMap(rawJson);
      Object payload = envelope.get("payload");
      return payload == null ? null : protocol().convertValue(payload, Auction.class);
    } catch (Exception e) {
      LOGGER.warning("parseAuction: " + e.getMessage());
      return null;
    }
  }

  public static BidTransaction parseBidTransaction(String rawJson) {
    try {
      Map<String, Object> envelope = protocol().decodeToMap(rawJson);
      Object payload = envelope.get("payload");
      return payload == null ? null : protocol().convertValue(payload, BidTransaction.class);
    } catch (Exception e) {
      LOGGER.warning("parseBidTransaction: " + e.getMessage());
      return null;
    }
  }

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
      return Collections.emptyList();
    } catch (Exception e) {
      LOGGER.warning("parseBidHistory: " + e.getMessage());
      return Collections.emptyList();
    }
  }

  // ==================== PHẦN 2: PARSE TEXT THUẦN (Dùng cho server text format) ====================

  /**
   * Parse danh sách AuctionDTO từ response dạng text
   * Format: AUCTIONS_COUNT:2\nAUCTION:id:name:price:status:time\n...
   */
  public static List<AuctionDTO> parseAuctionListFromText(String response) {
    List<AuctionDTO> result = new ArrayList<>();
    if (response == null || response.isEmpty()) return result;

    // Tách chuỗi bằng ký hiệu || thay vì \n
    String[] lines = response.split("\\|\\|");
    for (String line : lines) {
      if (line.startsWith("AUCTION:")) {
        String[] parts = line.substring(8).split(":");
        if (parts.length >= 4) {
          AuctionDTO dto = new AuctionDTO();
          dto.setId(parts[0]);
          dto.setItemName(parts[1]);
          dto.setCurrentPrice(Double.parseDouble(parts[2]));
          try {
            dto.setStatus(AuctionStatus.valueOf(parts[3]));
          } catch (IllegalArgumentException e) {
            dto.setStatus(AuctionStatus.DRAFT);
          }
          if (parts.length > 4) {
            try {
              java.lang.reflect.Method m = dto.getClass().getMethod("setRemainingTimeMillis", long.class);
              m.invoke(dto, Long.parseLong(parts[4]));
            } catch (Exception ex) {
              // Bỏ qua lỗi reflection
            }
          }
          result.add(dto);
        }
      }
    }
    return result;
  }

  /**
   * Parse chi tiết một AuctionDTO từ response dạng text
   * Format: AUCTION:id:name:price:status:winnerId:totalBids:remainingTime
   */
  public static AuctionDTO parseAuctionDetailFromText(String response) {
    if (response == null || !response.startsWith("AUCTION:")) return null;

    String[] parts = response.substring(8).split(":");
    if (parts.length >= 7) {
      AuctionDTO dto = new AuctionDTO();
      dto.setId(parts[0]);
      dto.setItemName(parts[1]);
      dto.setCurrentPrice(Double.parseDouble(parts[2]));
      try {
        dto.setStatus(AuctionStatus.valueOf(parts[3]));
      } catch (IllegalArgumentException e) {
        dto.setStatus(AuctionStatus.DRAFT);
      }
      dto.setCurrentWinnerId(parts[4]);
      dto.setTotalBids(Integer.parseInt(parts[5]));
      try {
        java.lang.reflect.Method m = dto.getClass().getMethod("setRemainingTimeMillis", long.class);
        m.invoke(dto, Long.parseLong(parts[6]));
      } catch (NoSuchMethodException ns) {
        // method not available on compiled DTO version — ignore
      } catch (Exception ex) {
        LOGGER.warning("Failed to set remainingTimeMillis via reflection: " + ex.getMessage());
      }
      return dto;
    }
    return null;
  }

  /**
   * Parse lịch sử đặt giá từ response dạng text
   * Format: BID_HISTORY_COUNT:2\nBID:bidderId:amount:time:isAutoBid\n...
   */
  public static List<BidTransaction> parseBidHistoryFromText(String response) {
    List<BidTransaction> result = new ArrayList<>();
    if (response == null || response.isEmpty()) return result;

    // Tách chuỗi bằng ký hiệu || thay vì \n
    String[] lines = response.split("\\|\\|");
    for (String line : lines) {
      if (line.startsWith("BID:")) {
        String[] parts = line.substring(4).split(":");
        if (parts.length >= 4) {
          String bidderId = parts[0];
          double amount = Double.parseDouble(parts[1]);
          LocalDateTime time = LocalDateTime.parse(parts[2]);
          boolean isAutoBid = Boolean.parseBoolean(parts[3]);
          BidTransaction bid = new BidTransaction("", bidderId, amount, time, isAutoBid);
          result.add(bid);
        }
      }
    }
    return result;
  }

  /**
   * Parse response đăng nhập từ dạng text (Đã cập nhật nhận Username thật)
   * Format chuẩn: LOGIN_OK:sessionToken:userId:username:role:balance
   */
  public static LoginResponse parseLoginResponse(String response) {
    if (response == null) {
      // CHANGED: "Không có phản hồi từ server" -> "No response from server"
      return new LoginResponse(false, "No response from server");
    }

    // Thử parse JSON (MessageProtocol) trước — server có thể trả JSON envelope
    try {
      if (response.trim().startsWith("{")) {
        Map<String, Object> envelope = protocol().decodeToMap(response);
        Object payload = envelope.get("payload");
        if (payload instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> p = (Map<String, Object>) payload;
          String sessionToken = p.get("sessionToken") != null ? p.get("sessionToken").toString() : (p.get("token") != null ? p.get("token").toString() : null);
          String userId = p.get("userId") != null ? p.get("userId").toString() : null;
          String username = p.get("username") != null ? p.get("username").toString() : null;
          String role = p.get("role") != null ? p.get("role").toString() : null;
          double balance = 0.0;
          if (p.get("balance") != null) {
            try { balance = Double.parseDouble(p.get("balance").toString()); } catch (Exception ex) { /* ignore */ }
          }
          if (sessionToken != null && userId != null) {
            return new LoginResponse(true, "Login successful", userId, username, role, sessionToken, balance);
          } else {
            // CHANGED: "Định dạng phản hồi JSON không hợp lệ" -> "Invalid JSON response format"
            return new LoginResponse(false, "Invalid JSON response format");
          }
        }
      }
    } catch (Exception e) {
      LOGGER.fine("parseLoginResponse: JSON parse failed: " + e.getMessage());
      // fallback to text parsing below
    }

    // Parse text protocol
    if (response.startsWith("LOGIN_OK:")) {
      String[] parts = response.split(":");
      if (parts.length >= 6) { // Nâng cấp từ 5 lên 6 phần tử
        String sessionToken = parts[1];
        String userId = parts[2];
        String username = parts[3]; // Lấy chính xác username từ server trả về
        String role = parts[4];
        double balance = Double.parseDouble(parts[5]);
        return new LoginResponse(true, "Login successful", userId, username, role, sessionToken, balance);
      }
      // CHANGED: "Định dạng phản hồi không hợp lệ" -> "Invalid response format"
      return new LoginResponse(false, "Invalid response format");
    } else if (response.startsWith("LOGIN_FAIL:")) {
      return new LoginResponse(false, response.substring(11));
    }
    // CHANGED: "Phản hồi không xác định: " -> "Unknown response: "
    return new LoginResponse(false, "Unknown response: " + response);
  }

  /**
   * Kiểm tra response có thành công không (dạng text)
   */
  public static boolean isSuccessText(String response) {
    return response != null && !response.startsWith("ERROR:") && !response.startsWith("LOGIN_FAIL");
  }

  /**
   * Lấy thông báo lỗi từ response text
   */
  public static String getErrorMessage(String response) {
    if (response == null) return "Unknown error";
    if (response.startsWith("ERROR:")) return response.substring(6);
    if (response.startsWith("LOGIN_FAIL:")) return response.substring(11);
    return response;
  }

  // ==================== PHẦN 3: TIỆN ÍCH ====================

  private static MessageProtocol protocol() {
    return ServerConnection.getInstance().getProtocol();
  }
}