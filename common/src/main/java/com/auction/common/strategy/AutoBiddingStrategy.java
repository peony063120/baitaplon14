package com.auction.common.strategy;

import com.auction.common.entity.*;
import com.auction.common.dto.BidRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class AutoBiddingStrategy implements BiddingStrategy {
  private static final Logger logger = LoggerFactory.getLogger(AutoBiddingStrategy.class);

  @Override
  public boolean execute(Auction auction, BidRequest request) {
    var lock = BidLockManager.getLock(auction.getId());
    lock.lock();
    try {
      List<AutoBidConfig> configs = auction.getAutoBidConfigs();
      if (configs == null || configs.isEmpty()) return false;

      // ưu tiên theo thời điểm đăng ký
      configs.sort(Comparator.comparing(AutoBidConfig::getCreatedAt));

      boolean hasChange;
      do {
        hasChange = false;
        for (AutoBidConfig config : configs) {
          if (config.getUserId().equals(auction.getCurrentWinnerId())) continue;

          if (config.canBid(auction.getCurrentPrice())) {
            double nextBid = config.getNextBid(auction.getCurrentPrice());

            // Cập nhật trạng thái an toàn trong Lock (Chống rollback / Lost update)
            auction.setCurrentPrice(nextBid);
            auction.setCurrentWinnerId(config.getUserId());
            auction.getBidHistory().add(new BidTransaction(
                auction.getId(), config.getUserId(), nextBid, LocalDateTime.now(), true
            ));

            logger.info("[AUTO-REACTION] - Nâng giá lên {} cho người dùng {}", nextBid, config.getUserId());
            hasChange = true;
            break; // Quay lại đầu danh sách để đảm bảo tính ưu tiên
          }
        }
      } while (hasChange);

      return true;
    } finally {
      lock.unlock();
    }
  }
}