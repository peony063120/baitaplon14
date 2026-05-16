package com.auction.common.strategy;

import com.auction.common.entity.Auction;
import com.auction.common.dto.BidRequest;
import com.auction.common.exception.InvalidBidException;

import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Lớp bọc mẫu thiết kế Decorator thực hiện gia hạn thời gian kết thúc phiên đấu giá
 * khi xuất hiện lượt đặt giá sát giờ đóng phiên (Anti-sniping).
 */
public class AntiSnipingStrategy implements BiddingStrategy {

  private static final Logger logger =
      Logger.getLogger(AntiSnipingStrategy.class.getName());

  // Cấu hình mặc định: Vùng thời gian kích hoạt là 30 giây cuối và mỗi lần gia hạn thêm 60 giây
  private static final double DEFAULT_TRIGGER_WINDOW_SECONDS = 30.0;
  private static final double DEFAULT_EXTENSION_SECONDS = 60.0;

  private final BiddingStrategy wrapped; // Chiến lược gốc được bao bọc (Có thể là Normal hoặc Auto)
  private final double triggerWindowSeconds;
  private final double extensionSeconds;

  public AntiSnipingStrategy(BiddingStrategy wrapped,
                             double triggerWindowSeconds,
                             double extensionSeconds) {
    if (wrapped == null) {
      throw new IllegalArgumentException("Chiến lược gốc được bao bọc không được để trống (null).");
    }
    if (triggerWindowSeconds <= 0 || extensionSeconds <= 0) {
      throw new IllegalArgumentException("Khoảng thời gian kích hoạt và thời gian gia hạn phải lớn hơn 0.");
    }
    this.wrapped = wrapped;
    this.triggerWindowSeconds = triggerWindowSeconds;
    this.extensionSeconds = extensionSeconds;
  }

  public AntiSnipingStrategy(BiddingStrategy wrapped) {
    this(wrapped, DEFAULT_TRIGGER_WINDOW_SECONDS, DEFAULT_EXTENSION_SECONDS);
  }

  @Override
  public boolean execute(Auction auction, BidRequest request)
      throws InvalidBidException {

    validateNotNull(auction, request);

    // Lưu giữ thời gian kết thúc trước khi đặt giá để so khớp chính xác
    LocalDateTime endTimeBeforeBid = auction.getEndTime();

    // Thực thi chiến lược cốt lõi (Đặt giá thủ công thông thường hoặc tự động đua giá)
    boolean accepted = wrapped.execute(auction, request);

    LocalDateTime bidTime = LocalDateTime.now();

    // Nếu cơ chế gia hạn phiên đấu giá của thực thể Auction chưa chạy, Decorator này sẽ đảm nhiệm xử lý mở rộng
    if (!auction.isAntiSnipingEnabled()) {
      applyExtensionIfNeeded(auction, endTimeBeforeBid, bidTime, request.getBidderId());
    }

    return accepted;
  }

  /**
   * Kiểm tra điều kiện thời gian hiện tại và thực hiện gia hạn thời gian đóng phiên đấu giá nếu thỏa mãn.
   */
  private void applyExtensionIfNeeded(Auction auction,
                                      LocalDateTime endTimeBeforeBid,
                                      LocalDateTime bidTime,
                                      String bidderId) {

    // Xác định mốc thời gian bắt đầu rơi vào vùng nhạy cảm sát giờ kết thúc
    LocalDateTime triggerStart = endTimeBeforeBid.minusSeconds((long) triggerWindowSeconds);

    // Lượt đặt giá hợp lệ nằm trong vùng nhạy cảm: triggerStart <= bidTime <= endTimeBeforeBid
    boolean insideWindow = !bidTime.isBefore(triggerStart) && !bidTime.isAfter(endTimeBeforeBid);

    if (insideWindow) {
      auction.extendEndTime(extensionSeconds);

      logger.info(String.format(
          "Kích hoạt cơ chế Anti-sniping: Người dùng '%s' đặt giá sát giờ, thời gian kết thúc được gia hạn thêm %.0f giây → Thời gian đóng phiên mới: %s.",
          bidderId, extensionSeconds, auction.getEndTime()
      ));
    } else {
      logger.fine(String.format(
          "Không kích hoạt Anti-sniping: Thời gian đặt=%s, Khoảng thời gian nhạy cảm=[%s, %s].",
          bidTime, triggerStart, endTimeBeforeBid
      ));
    }
  }

  public BiddingStrategy getWrapped() { return wrapped; }
  public double getTriggerWindowSeconds() { return triggerWindowSeconds; }
  public double getExtensionSeconds() { return extensionSeconds; }
}