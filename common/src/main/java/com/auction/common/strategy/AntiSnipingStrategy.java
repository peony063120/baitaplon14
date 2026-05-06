package com.auction.common.strategy;

import com.auction.common.entity.Auction;
import com.auction.common.dto.BidRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

public class AntiSnipingStrategy implements BiddingStrategy {
  private static final Logger logger = LoggerFactory.getLogger(AntiSnipingStrategy.class);

  @Override
  public boolean execute(Auction auction, BidRequest request) {
    // 1. Dùng chung khóa tập trung để đảm bảo tính nhất quán (3.2.2)
    ReentrantLock lock = BidLockManager.getLock(auction.getId());
    lock.lock();
    try {
      // 2. Kiểm tra điều kiện canBid cơ bản
      if (!auction.canBid(request.getBidderId())) {
        return false;
      }

      // (thêm) Thực hiện đặt giá TRƯỚC
      // cần biết việc đặt giá này có thành công (hợp lệ về giá) hay không
      NormalBiddingStrategy normal = new NormalBiddingStrategy();
      boolean bidSuccess = normal.execute(auction, request);

      // 3. Nếu đặt giá THÀNH CÔNG, mới xử lý thuật toán Anti-sniping (3.2.3)
      if (bidSuccess && auction.isAntiSnipingEnabled()) {
        LocalDateTime now = LocalDateTime.now();
        // Ngưỡng X giây cuối (30 giây trước khi kết thúc)
        LocalDateTime threshold = auction.getEndTime().minusSeconds(30);

        if (now.isAfter(threshold) && now.isBefore(auction.getEndTime())) {
          // Tự động gia hạn thêm Y giây
          auction.extendEndTime(auction.getAntiSnipingExtensionSeconds());
          logger.info("[3.2.3-ANTI-SNIPING] - Phát hiện bid phút chót. Gia hạn phiên {} thêm {} giây.",
              auction.getId(), auction.getAntiSnipingExtensionSeconds());
        }
      }

      // 4.Chuyển tiếp sang Normal Bidding
      // Sau khi xử lý gia hạn, ta gọi NormalBiddingStrategy để thực hiện
      // việc đặt giá và kích hoạt chuỗi phản ứng Auto-bid bên trong đó.
      return new NormalBiddingStrategy().execute(auction, request);

    } catch (Exception e) {
      logger.error("[ANTI-SNIPING-ERROR] - Lỗi hệ thống: {}", e.getMessage());
      return false;
    } finally {
      lock.unlock();
    }
  }
}