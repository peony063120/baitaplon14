package com.auction.common.strategy;

import com.auction.common.dto.BidRequest;
import com.auction.common.entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

public class NormalBiddingStrategy implements BiddingStrategy {
  private static final Logger logger = LoggerFactory.getLogger(NormalBiddingStrategy.class);

  @Override
  public boolean execute(Auction auction, BidRequest request) {
    ReentrantLock lock = BidLockManager.getLock(auction.getId());
    lock.lock(); // Chặn Lost Update và Hai người cùng thắng
    try {
      // 1.Kiểm tra tư cách thầu (người bán, trạng thái phiên)
      if (!auction.canBid(request.getBidderId())) {
        return false;
      }

      // 2.Kiểm tra giá đặt so với giá hiện tại + bước giá tối thiểu
      double minRequired = auction.getCurrentPrice() + auction.getMinIncrement();
      if (request.getAmount() < minRequired) {
        logger.warn("[CONCURRENT-REJECT] - Giá đặt thấp hơn mức tối thiểu hiện tại.");
        return false;
      }

      // 3.Cập nhật trạng thái người dẫn đầu mới (Thủ công)
      auction.setCurrentPrice(request.getAmount());
      auction.setCurrentWinnerId(request.getBidderId());
      auction.getBidHistory().add(new BidTransaction(
          auction.getId(), request.getBidderId(), request.getAmount(), LocalDateTime.now(), false
      ));

      logger.info("[NORMAL-SUCCESS] - Người dùng {} dẫn đầu với mức giá {}", request.getBidderId(), request.getAmount());

      // 4.Kích hoạt AutoBiddingStrategy (Yêu cầu 3.2.1)
      // Sau khi có bid thủ công, các máy Auto-bid phải ngay lập tức so kè với nhau
      new AutoBiddingStrategy().execute(auction, request);

      return true;
    } finally {
      lock.unlock(); // Giải phóng để các yêu cầu khác đi vào
    }
  }
}