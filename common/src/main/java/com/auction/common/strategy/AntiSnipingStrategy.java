package com.auction.common.strategy;

import com.auction.common.entity.Auction;
import com.auction.common.dto.BidRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

public class AntiSnipingStrategy implements BiddingStrategy {
  private static final Logger logger = LoggerFactory.getLogger(AntiSnipingStrategy.class);
  private static final int SNIPING_THRESHOLD_SECONDS = 30; // Ngưỡng 30 giây cuối

  @Override
  public boolean execute(Auction auction, BidRequest request) {
    ReentrantLock lock = BidLockManager.getLock(auction.getId());
    lock.lock();

    try {
      if (!auction.canBid(request.getBidderId())) {
        return false;
      }

      // Kiểm tra trước xem có rơi vào khung giờ Anti-sniping hay không
      boolean isSnipingZone = false;
      if (auction.isAntiSnipingEnabled()) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = auction.getEndTime().minusSeconds(SNIPING_THRESHOLD_SECONDS);

        if (now.isAfter(threshold) && now.isBefore(auction.getEndTime())) {
          isSnipingZone = true;
        }
      }

      // Thực hiện đặt giá
      NormalBiddingStrategy normalBidding = new NormalBiddingStrategy();
      boolean bidSuccess = normalBidding.execute(auction, request);

      // Nếu đặt giá thành công VÀ nằm trong khung giờ sniping -> Tiến hành gia hạn
      if (bidSuccess && isSnipingZone) {
        auction.extendEndTime(auction.getAntiSnipingExtensionSeconds());
        logger.info("[ANTI-SNIPING] - Phát hiện bid phút chót thành công. Gia hạn phiên {} thêm {} giây. Thời gian mới: {}",
            auction.getId(), auction.getAntiSnipingExtensionSeconds(), auction.getEndTime());
      }

      return bidSuccess;

    } catch (Exception e) {
      logger.error("[ANTI-SNIPING-ERROR] - Lỗi hệ thống khi xử lý bid cho phiên {}: {}", auction.getId(), e.getMessage(), e);
      return false;
    } finally {
      lock.unlock();
    }
  }
}