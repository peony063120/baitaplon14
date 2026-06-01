package com.auction.client.network;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.LoginResponse;
import com.auction.common.dto.UserDTO;
import com.auction.common.entity.Auction;
import com.auction.common.entity.BidTransaction;
import com.auction.common.enums.AuctionStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ResponseHandler - Handles server responses
 * Supports both JSON (via MessageProtocol) and plain TEXT formats
 */
public final class ResponseHandler {

  private static final Logger LOGGER = Logger.getLogger(ResponseHandler.class.getName());

  private ResponseHandler() {}

  // ==================== PART 1: JSON PARSING (via MessageProtocol) ====================

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
        return msg != null ? msg.toString() : "Unknown error";
      }
      return "Unknown error";
    } catch (Exception e) {
      return "Error reading response: " + e.getMessage();
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

  // ==================== PART 2: PLAIN TEXT PARSING (for server text format) ====================

  /**
   * Parse danh sách AuctionDTO từ response dạng text
   * Format: AUCTIONS_COUNT:2||AUCTION:id:name:price:status:category:time[:startingPrice:totalBids:imageRef[:startMillis:endMillis]]
  */
  public static List<AuctionDTO> parseAuctionListFromText(String response) {
    List<AuctionDTO> result = new ArrayList<>();
    if (response == null || response.isEmpty()) return result;

    String[] lines = response.split("\\|\\|");
    AuctionDTO current = null;
    for (String line : lines) {
      if (line.startsWith("AUCTION:")) {
        current = parseAuctionListCore(line.substring(8));
        if (current != null) {
          result.add(current);
        }
      } else if (line.startsWith("IMAGE:") && current != null) {
        String image = line.substring(6);
        if (!image.isEmpty()) {
          current.setImagePath(image);
        }
      }
    }
    return result;
  }

  private static AuctionDTO parseAuctionListCore(String body) {
    String[] parts = body.split(":");
    if (parts.length < 4) {
      return null;
    }
    try {
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
        dto.setCategory(parts[4]);
      }
      if (parts.length > 5) {
        dto.setRemainingTimeMillis(Long.parseLong(parts[5]));
      }
      if (parts.length > 6) {
        dto.setStartingPrice(Double.parseDouble(parts[6]));
      }
      if (parts.length > 7) {
        dto.setTotalBids(Integer.parseInt(parts[7]));
      }
      if (parts.length > 8) {
        dto.setStartTime(epochToLocalDateTime(parts[8]));
      }
      if (parts.length > 9) {
        dto.setEndTime(epochToLocalDateTime(parts[9]));
      }
      return dto;
    } catch (Exception ex) {
      LOGGER.warning("parseAuctionListCore failed: " + ex.getMessage());
      return null;
    }
  }

  /**
   * Parse realtime AUCTION_UPDATE từ server text format.
   * Format: AUCTION_UPDATE:ID:uuid:PRICE:123:WINNER:userId:STATUS:RUNNING
   */
  public static AuctionDTO parseAuctionUpdateFromText(String response) {
    if (response == null || !response.startsWith("AUCTION_UPDATE:")) {
      return null;
    }

    AuctionDTO dto = new AuctionDTO();
    String body = response.substring("AUCTION_UPDATE:".length());
    String[] tokens = body.split(":");
    for (int i = 0; i + 1 < tokens.length; i += 2) {
      switch (tokens[i]) {
        case "ID" -> dto.setId(tokens[i + 1]);
        case "PRICE" -> dto.setCurrentPrice(Double.parseDouble(tokens[i + 1]));
        case "WINNER" -> {
          if (!tokens[i + 1].isEmpty() && !"null".equalsIgnoreCase(tokens[i + 1])) {
            dto.setCurrentWinnerId(tokens[i + 1]);
          }
        }
        case "WINNER_NAME" -> dto.setCurrentWinnerName(tokens[i + 1]);
        case "STATUS" -> {
          try {
            dto.setStatus(AuctionStatus.valueOf(tokens[i + 1]));
          } catch (IllegalArgumentException e) {
            dto.setStatus(AuctionStatus.DRAFT);
          }
        }
        default -> { /* ignore unknown keys */ }
      }
    }
    if (dto.getId() == null) {
      return null;
    }
    applyWinnerNameFallback(dto);
    return dto;
  }

  private static void applyWinnerNameFallback(AuctionDTO dto) {
    if (dto.getCurrentWinnerName() == null || dto.getCurrentWinnerName().isBlank()) {
      if (dto.getCurrentWinnerId() != null && !dto.getCurrentWinnerId().isBlank()) {
        dto.setCurrentWinnerName(dto.getCurrentWinnerId());
      }
    }
  }

  /**
   * Parse chi tiết một AuctionDTO từ response dạng text
   * Format: AUCTION:id:name:price:status:winnerId:totalBids:remainingTime[:startingPrice:minIncrement[:imageRef]]
   */
  public static AuctionDTO parseAuctionDetailFromText(String response) {
    if (response == null || !response.startsWith("AUCTION:")) return null;

    String imageRef = "";
    String meta = "";
    String core = response;
    if (response.contains("||IMAGE:")) {
      String[] imageSplit = response.split("\\|\\|IMAGE:", 2);
      core = imageSplit[0];
      String tail = imageSplit[1];
      if (tail.contains("||META:")) {
        String[] metaSplit = tail.split("\\|\\|META:", 2);
        imageRef = metaSplit[0];
        meta = metaSplit.length > 1 ? metaSplit[1] : "";
      } else {
        imageRef = tail;
      }
    } else if (response.contains("||META:")) {
      String[] metaSplit = response.split("\\|\\|META:", 2);
      core = metaSplit[0];
      meta = metaSplit.length > 1 ? metaSplit[1] : "";
    }

    String[] parts = core.substring(8).split(":");
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
      if (!parts[4].isEmpty() && !"null".equalsIgnoreCase(parts[4])) {
        dto.setCurrentWinnerId(parts[4]);
      }
      dto.setTotalBids(Integer.parseInt(parts[5]));
      dto.setRemainingTimeMillis(Long.parseLong(parts[6]));
      if (parts.length >= 9) {
        dto.setStartingPrice(Double.parseDouble(parts[7]));
        dto.setMinIncrement(Double.parseDouble(parts[8]));
      }
      applyWinnerAndScheduleFields(dto, parts);
      if (!imageRef.isEmpty()) {
        dto.setImagePath(imageRef);
      }
      applyDetailMeta(dto, meta);
      applyWinnerNameFallback(dto);
      return dto;
    }
    return null;
  }

  private static void applyWinnerAndScheduleFields(AuctionDTO dto, String[] parts) {
    if (parts.length <= 9) {
      return;
    }
    int idx = 9;
    if (parts[idx].isEmpty() && parts.length > idx + 1 && !looksLikeEpoch(parts[idx + 1])) {
      dto.setCurrentWinnerName(parts[idx + 1]);
      idx += 2;
    } else if (!parts[idx].isEmpty() && !looksLikeEpoch(parts[idx])) {
      dto.setCurrentWinnerName(parts[idx]);
      idx++;
    }
    if (parts.length > idx && looksLikeEpoch(parts[idx])) {
      dto.setStartTime(epochToLocalDateTime(parts[idx]));
      if (parts.length > idx + 1 && looksLikeEpoch(parts[idx + 1])) {
        dto.setEndTime(epochToLocalDateTime(parts[idx + 1]));
      }
    }
  }

  private static boolean looksLikeEpoch(String value) {
    if (value == null || value.isBlank()) {
      return false;
    }
    try {
      return Long.parseLong(value) > 1_000_000_000_000L;
    } catch (NumberFormatException ex) {
      return false;
    }
  }

  private static void applyDetailMeta(AuctionDTO dto, String meta) {
    if (meta == null || meta.isBlank()) {
      return;
    }
    String[] metaParts = meta.split("\\|", 2);
    if (metaParts.length >= 1 && !metaParts[0].isBlank()) {
      dto.setCategory(metaParts[0]);
    }
    if (metaParts.length >= 2 && !metaParts[1].isBlank()) {
      dto.setItemDescription(metaParts[1].replace("\\n", "\n"));
    }
  }

  /**
   * Parse lịch sử đặt giá từ response dạng text
   * Format: BID_HISTORY_COUNT:n||BID:auctionId:bidderId:amount:epochMillis:isAutoBid
   */
  public static List<BidTransaction> parseBidHistoryFromText(String response) {
    List<BidTransaction> result = new ArrayList<>();
    if (response == null || response.isEmpty()) return result;

    String[] lines = response.split("\\|\\|");
    for (String line : lines) {
      BidTransaction bid = parseBidLine(line);
      if (bid != null) {
        result.add(bid);
      }
    }
    return result;
  }

  private static LocalDateTime epochToLocalDateTime(String raw) {
    if (raw == null || raw.isBlank() || "0".equals(raw)) {
      return null;
    }
    if (raw.chars().allMatch(Character::isDigit)) {
      return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(raw)), ZoneId.systemDefault());
    }
    return null;
  }

  private static BidTransaction parseBidLine(String line) {
    if (line == null || !line.startsWith("BID:")) {
      return null;
    }
    String body = line.substring(4);
    String[] parts = body.split(":");
    if (parts.length >= 6) {
      try {
        String auctionId = parts[0];
        String bidderId = parts[1];
        String bidderName = parts[2];
        double amount = Double.parseDouble(parts[3]);
        LocalDateTime time = parseBidTime(parts[4]);
        boolean isAutoBid = Boolean.parseBoolean(parts[5]);
        BidTransaction bid = new BidTransaction(auctionId, bidderId, amount, time, isAutoBid);
        bid.setBidderName(bidderName);
        return bid;
      } catch (Exception ex) {
        LOGGER.warning("parseBidLine failed: " + ex.getMessage());
        return null;
      }
    }
    String[] legacyParts = body.split(":", 5);
    if (legacyParts.length >= 5) {
      try {
        String auctionId = legacyParts[0];
        String bidderId = legacyParts[1];
        double amount = Double.parseDouble(legacyParts[2]);
        LocalDateTime time = parseBidTime(legacyParts[3]);
        boolean isAutoBid = Boolean.parseBoolean(legacyParts[4]);
        BidTransaction bid = new BidTransaction(auctionId, bidderId, amount, time, isAutoBid);
        bid.setBidderName(bidderId);
        return bid;
      } catch (Exception ex) {
        LOGGER.warning("parseBidLine failed: " + ex.getMessage());
        return null;
      }
    }
    if (legacyParts.length >= 4) {
      try {
        String bidderId = legacyParts[0];
        double amount = Double.parseDouble(legacyParts[1]);
        LocalDateTime time = parseBidTime(legacyParts[2]);
        boolean isAutoBid = Boolean.parseBoolean(legacyParts[3]);
        BidTransaction bid = new BidTransaction("", bidderId, amount, time, isAutoBid);
        bid.setBidderName(bidderId);
        return bid;
      } catch (Exception ex) {
        LOGGER.warning("parseBidLine legacy failed: " + ex.getMessage());
        return null;
      }
    }
    return null;
  }

  private static LocalDateTime parseBidTime(String raw) {
    if (raw == null || raw.isBlank()) {
      return LocalDateTime.now();
    }
    if (raw.chars().allMatch(Character::isDigit)) {
      return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(raw)), ZoneId.systemDefault());
    }
    return LocalDateTime.parse(raw);
  }

  /**
   * Parse response đăng nhập từ dạng text (Đã cập nhật nhận Username thật)
   * Format chuẩn: LOGIN_OK:sessionToken:userId:username:role:balance
   */
  public static LoginResponse parseLoginResponse(String response) {
    if (response == null) {
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
        LoginResponse loginResponse = new LoginResponse(true, "Login successful", userId, username, role, sessionToken, balance);
        if (parts.length >= 7 && !parts[6].isEmpty()) {
          loginResponse.setEmail(parts[6]);
        }
        if (parts.length >= 8 && !parts[7].isEmpty()) {
          loginResponse.setFullName(parts[7]);
        }
        return loginResponse;
      }
      return new LoginResponse(false, "Invalid response format");
    } else if (response.startsWith("LOGIN_FAIL:")) {
      return new LoginResponse(false, response.substring(11));
    }
    return new LoginResponse(false, "Unknown response: " + response);
  }

  /**
   * Parse profile response: PROFILE:username:email:fullName:role:balance
   */
  public static UserDTO parseProfileFromText(String response) {
    if (response == null || !response.startsWith("PROFILE:")) {
      return null;
    }
    String[] parts = response.substring(8).split(":", 5);
    if (parts.length < 4) {
      return null;
    }
    UserDTO dto = new UserDTO();
    dto.setUsername(parts[0]);
    dto.setEmail(parts[1]);
    dto.setFullName(parts[2]);
    dto.setRole(parts[3]);
    if (parts.length >= 5 && !parts[4].isEmpty()) {
      try {
        dto.setBalance(Double.parseDouble(parts[4]));
      } catch (NumberFormatException ignored) {
        dto.setBalance(0);
      }
    }
    return dto;
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

  // ==================== PART 3: UTILITIES ====================

  private static MessageProtocol protocol() {
    return ServerConnection.getInstance().getProtocol();
  }
}