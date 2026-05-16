package com.auction.common.strategy;

import com.auction.common.entity.Auction;
import com.auction.common.entity.AutoBidConfig;
import com.auction.common.entity.BidTransaction;
import com.auction.common.dto.BidRequest;
import com.auction.common.exception.InvalidBidException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Tự động đặt giá thay cho các nhà đấu giá đã đăng ký cấu hình tự động.
 */
public class AutoBiddingStrategy implements BiddingStrategy {

  private static final Logger logger =
      Logger.getLogger(AutoBiddingStrategy.class.getName());

  // Giới hạn độ sâu đệ quy tối đa để tránh lỗi tràn bộ nhớ (StackOverflowError) khi các bot tự động tranh đua
  private static final int MAX_RECURSION_DEPTH = 10;

  // Bản đồ lưu trữ khóa (Lock) cho từng phiên đấu giá riêng biệt để đảm bảo an toàn đa luồng (Thread-safety)
  private static final Map<String, ReentrantLock> LOCK_MAP = new ConcurrentHashMap<>();

  @Override
  public boolean execute(Auction auction, BidRequest request)
      throws InvalidBidException {
    return executeInternal(auction, request, 0);
  }

  /**
   * Giải phóng bộ nhớ loại bỏ khóa khi phiên đấu giá kết thúc.
   */
  public static void removeLock(String auctionId) {
    ReentrantLock lock = LOCK_MAP.remove(auctionId);
    if (lock != null && lock.isLocked()) {
      logger.warning(String.format(
          "Cảnh báo: Khóa của phiên đấu giá '%s' bị xóa khi vẫn đang bị chiếm giữ.", auctionId
      ));
    }
  }

  /**
   * Phương thức điều phối đệ quy nội bộ kiểm soát luồng đồng thời theo từng mã phiên.
   */
  private boolean executeInternal(Auction auction, BidRequest request, int depth)
      throws InvalidBidException {

    validateNotNull(auction, request);

    if (depth >= MAX_RECURSION_DEPTH) {
      throw new InvalidBidException(String.format(
          "Chuỗi đặt giá tự động bị dừng tại độ sâu %d (Tối đa=%d) của phiên '%s' để tránh quá tải.",
          depth, MAX_RECURSION_DEPTH, auction.getId()
      ));
    }

    // Đồng bộ hóa luồng (Thread-safe) độc lập theo từng mã đấu giá bằng Fair Lock
    ReentrantLock lock = LOCK_MAP.computeIfAbsent(
        auction.getId(),
        id -> new ReentrantLock(true)
    );

    lock.lock();
    try {
      return doAutoBid(auction, request, depth);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Thực thi logic của thuật toán Đấu giá tự động (Auto-Bidding).
   */
  private boolean doAutoBid(Auction auction, BidRequest request, int depth)
      throws InvalidBidException {

    if (!auction.isActive()) {
      throw new InvalidBidException(String.format("Phiên đấu giá '%s' đã kết thúc hoặc không ở trạng thái mở.", auction.getId()));
    }

    // Tạo hàng đợi ưu tiên gồm các cấu hình tự động hợp lệ (Người có giá tối đa cao nhất xếp trước)
    PriorityQueue<AutoBidConfig> candidates = buildCandidateQueue(
        auction, request.getBidderId(), request.isAutoBid()
    );

    if (candidates.isEmpty()) {
      logger.fine(String.format(
          "Đấu giá tự động [Độ sâu=%d]: Không tìm thấy cấu hình tự động nào khả dụng cho phiên '%s'.", depth, auction.getId()
      ));
      return false;
    }

    final double currentPrice = auction.getCurrentPrice();
    AutoBidConfig best = candidates.peek();

    // Kiểm tra xem cấu hình tối ưu nhất có thể nâng giá được nữa không (Giá hiện tại + bước giá tự động <= giá tối đa)
    if (best == null || !best.canBid(currentPrice)) {
      if (best != null) {
        best.setActive(false); // Hủy kích hoạt cấu hình nếu đã chạm trần giới hạn ngân sách người dùng
      }
      throw new InvalidBidException(String.format(
          "Không có cấu hình tự động nào đủ điều kiện vượt qua mức giá hiện tại %.2f của phiên '%s'.",
          currentPrice, auction.getId()
      ));
    }

    // Tính toán mức giá tự động mới: Tăng theo bước giá thiết lập sẵn nhưng không vượt ngưỡng trần tối đa (maxBid)
    double autoBidAmount = Math.min(
        best.getNextBid(currentPrice),
        best.getMaxBid()
    );

    // Kiểm tra tính hợp lệ về mặt bước giá tối thiểu của hệ thống
    if (autoBidAmount < currentPrice + auction.getMinIncrement()) {
      best.setActive(false);
      throw new InvalidBidException(
          auction.getId(),
          auction.getCurrentPrice(),
          autoBidAmount,
          auction.getMinIncrement()
      );
    }

    // Khởi tạo bản ghi giao dịch hệ thống đấu giá tự động
    BidTransaction autoBidTx = new BidTransaction(
        auction.getId(),
        best.getBidderId(),
        autoBidAmount,
        LocalDateTime.now(),
        true // autoBid = true
    );

    boolean accepted = auction.addBid(autoBidTx);

    if (!accepted) {
      throw new InvalidBidException(String.format(
          "Hệ thống từ chối lệnh đặt giá tự động mức %.2f của người dùng '%s' tại phiên '%s'.",
          autoBidAmount, best.getBidderId(), auction.getId()
      ));
    }

    logger.info(String.format(
        "Đấu giá tự động [Độ sâu=%d] thành công: người đặt='%s', mức đặt=%.2f (Trần cấu hình=%.2f), mã phiên='%s'.",
        depth, best.getBidderId(), autoBidAmount, best.getMaxBid(), auction.getId()
    ));

    // Kích hoạt chuỗi phản ứng (Chain Reaction) gửi yêu cầu mới cho các cấu hình tự động của người dùng khác đối đầu tiếp
    BidRequest followUp = new BidRequest(
        auction.getId(),
        best.getBidderId(),
        autoBidAmount,
        true // isAutoBid = true
    );

    try {
      executeInternal(auction, followUp, depth + 1);
    } catch (InvalidBidException e) {
      logger.fine(String.format(
          "Chuỗi đặt giá tự động kết thúc ở độ sâu=%d cho phiên '%s': %s",
          depth + 1, auction.getId(), e.getMessage()
      ));
    }

    return true;
  }

  /**
   * Lọc và sắp xếp các cấu hình tự động hợp lệ tham gia đua giá.
   */
  private PriorityQueue<AutoBidConfig> buildCandidateQueue(
      Auction auction,
      String triggeringBidderId,
      boolean isFollowUpAutoBid) {

    // Ưu tiên: Ngân sách lớn nhất xếp trước (maxBid giảm dần) -> Thời gian đăng ký sớm xếp trước (createdAt tăng dần)
    PriorityQueue<AutoBidConfig> queue = new PriorityQueue<>(
        Comparator.comparingDouble(AutoBidConfig::getMaxBid)
            .reversed()
            .thenComparing(AutoBidConfig::getCreatedAt)
    );

    List<AutoBidConfig> configs = auction.getAutoBidConfigs();
    if (configs == null) return queue;

    String currentWinnerId = auction.getCurrentWinnerId();

    for (AutoBidConfig config : configs) {
      if (!config.isActive()) continue; // Bỏ qua cấu hình đã tắt
      if (config.getBidderId().equals(currentWinnerId)) continue; // Không tự trả giá đè lên chính mình
      if (isFollowUpAutoBid && config.getBidderId().equals(triggeringBidderId)) continue; // Tránh vòng lặp vô hạn của cùng một bot
      queue.offer(config);
    }

    return queue;
  }
}