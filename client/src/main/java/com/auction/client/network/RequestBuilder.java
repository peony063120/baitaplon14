package com.auction.client.network;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * RequestBuilder — Lớp tiện ích tĩnh chịu trách nhiệm xây dựng
 * các gói tin yêu cầu (Request Message) chuẩn hóa cho từng nghiệp vụ cụ thể.
 */
public final class RequestBuilder {

  private RequestBuilder() {}

  /**
   * Xây dựng gói tin yêu cầu đăng nhập.
   *
   * @param username Tên đăng nhập
   * @param password Mật khẩu
   * @return Gói tin chuẩn {@code Map<String, Object>} sẵn sàng gửi đi
   */
  public static Map<String, Object> login(String username, String password) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("username", username);
    payload.put("password", password);
    return protocol().buildRequestMessage(MessageProtocol.TYPE_LOGIN, payload);
  }

  /**
   * Xây dựng gói tin yêu cầu đăng ký tài khoản mới.
   *
   * @param username Tên đăng nhập mong muốn
   * @param password Mật khẩu
   * @param email    Địa chỉ email
   * @param fullName Họ và tên đầy đủ
   * @return Gói tin chuẩn sẵn sàng gửi đi
   */
  public static Map<String, Object> register(String username, String password,
                                             String email, String fullName) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("username", username);
    payload.put("password", password);
    payload.put("email",    email);
    payload.put("fullName", fullName);
    return protocol().buildRequestMessage(MessageProtocol.TYPE_REGISTER, payload);
  }

  /**
   * Xây dựng gói tin yêu cầu lấy danh sách toàn bộ phiên đấu giá.
   * Hỗ trợ lọc theo trạng thái và phân trang.
   *
   * @param status   Trạng thái lọc (ACTIVE / ENDED / ALL) — null nghĩa là lấy tất cả
   * @param page     Số trang (bắt đầu từ 0)
   * @param pageSize Số phần tử mỗi trang
   * @return Gói tin chuẩn sẵn sàng gửi đi
   */
  public static Map<String, Object> getAuctions(String status, int page, int pageSize) {
    Map<String, Object> payload = new HashMap<>();
    if (status != null) payload.put("status", status);
    payload.put("page",     page);
    payload.put("pageSize", pageSize);
    return protocol().buildRequestMessage(MessageProtocol.TYPE_GET_AUCTIONS, payload);
  }

  /**
   * Xây dựng gói tin yêu cầu lấy chi tiết một phiên đấu giá theo ID.
   *
   * @param auctionId Mã định danh phiên đấu giá
   * @return Gói tin chuẩn sẵn sàng gửi đi
   */
  public static Map<String, Object> getAuction(Long auctionId) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("auctionId", auctionId);
    return protocol().buildRequestMessage(MessageProtocol.TYPE_GET_AUCTION, payload);
  }

  /**
   * Xây dựng gói tin yêu cầu tạo phiên đấu giá mới.
   *
   * @param title        Tiêu đề phiên đấu giá
   * @param description  Mô tả chi tiết sản phẩm
   * @param startingPrice Giá khởi điểm
   * @param endTimeIso   Thời điểm kết thúc phiên (chuỗi ISO-8601)
   * @param sellerId     Mã định danh người bán
   * @return Gói tin chuẩn sẵn sàng gửi đi
   */
  public static Map<String, Object> createAuction(String title, String description,
                                                  BigDecimal startingPrice,
                                                  String endTimeIso, Long sellerId) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("title",        title);
    payload.put("description",  description);
    payload.put("startingPrice", startingPrice);
    payload.put("endTime",      endTimeIso);
    payload.put("sellerId",     sellerId);
    return protocol().buildRequestMessage(MessageProtocol.TYPE_CREATE_AUCTION, payload);
  }

  /**
   * Xây dựng gói tin yêu cầu đặt một lượt đấu giá thủ công.
   *
   * @param auctionId Mã định danh phiên đấu giá
   * @param bidderId  Mã định danh người đặt giá
   * @param amount    Số tiền đặt giá
   * @return Gói tin chuẩn sẵn sàng gửi đi
   */
  public static Map<String, Object> placeBid(Long auctionId, Long bidderId, BigDecimal amount) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("auctionId", auctionId);
    payload.put("bidderId",  bidderId);
    payload.put("amount",    amount);
    return protocol().buildRequestMessage(MessageProtocol.TYPE_PLACE_BID, payload);
  }

  /**
   * Xây dựng gói tin cấu hình hoặc cập nhật chế độ đặt giá tự động (Auto-Bid).
   *
   * @param auctionId  Mã định danh phiên đấu giá
   * @param bidderId   Mã định danh người dùng kích hoạt auto-bid
   * @param maxAmount  Ngưỡng giá tối đa người dùng chấp nhận
   * @param bidStep    Bước tăng giá tự động mỗi lần đấu
   * @param enabled    {@code true} để bật, {@code false} để tắt auto-bid
   * @return Gói tin chuẩn sẵn sàng gửi đi
   */
  public static Map<String, Object> configAutoBid(Long auctionId, Long bidderId,
                                                  BigDecimal maxAmount, BigDecimal bidStep,
                                                  boolean enabled) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("auctionId", auctionId);
    payload.put("bidderId",  bidderId);
    payload.put("maxAmount", maxAmount);
    payload.put("bidStep",   bidStep);
    payload.put("enabled",   enabled);
    return protocol().buildRequestMessage(MessageProtocol.TYPE_AUTO_BID_CONFIG, payload);
  }

  /**
   * Xây dựng gói tin yêu cầu lấy lịch sử đặt giá của một phiên đấu giá.
   *
   * @param auctionId Mã định danh phiên đấu giá cần lấy lịch sử
   * @return Gói tin chuẩn sẵn sàng gửi đi
   */
  public static Map<String, Object> getBidHistory(Long auctionId) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("auctionId", auctionId);
    return protocol().buildRequestMessage(MessageProtocol.TYPE_BID_HISTORY, payload);
  }

  private static MessageProtocol protocol() {
    return ServerConnection.getInstance().getProtocol();
  }
}