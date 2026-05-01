package com.auction.common.strategy;

import com.auction.common.entity.*;
import com.auction.common.dto.BidRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;


public class AutoBiddingStrategy implements BiddingStrategy {
  private static final Logger logger = LoggerFactory.getLogger(AutoBiddingStrategy.class);

  @Override
  public boolean execute(Auction auction, BidRequest request) {
    var lock = BidLockManager.getLock(auction.getId());
    lock.lock();
    try {
      List<AutoBidConfig> configs = auction.getAutoBidConfigs();
      if (configs == null || configs.isEmpty()) return false;

      // sắp xếp theo thời gian tạo (ưu tiên đăng ký sớm hơn)
      configs.sort(Comparator.comparing(AutoBidConfig::getCreatedAt));

      boolean hasChange;
      do {
        hasChange = false;
        for (AutoBidConfig config : configs) {
          // SỬA: dùng getBidderId() thay vì getUserId()
          if (config.getBidderId().equals(auction.getCurrentWinnerId())) continue;

          if (config.canBid(auction.getCurrentPrice())) {
            double nextBid = config.getNextBid(auction.getCurrentPrice());

            // Cập nhật auction
            auction.setCurrentPrice(nextBid);
            auction.setCurrentWinnerId(config.getBidderId());
            auction.getBidHistory().add(new BidTransaction(
                    auction.getId(),
                    config.getBidderId(),
                    nextBid,
                    LocalDateTime.now(),
                    true
            ));

            logger.info("[AUTO-REACTION] Ra giá {} cho người dùng {}", nextBid, config.getBidderId());
            hasChange = true;
            break; // quay lại vòng lặp để đảm bảo ưu tiên
          }
        }
      } while (hasChange);

      return true;
    } finally {
      lock.unlock();
    }
  }
}