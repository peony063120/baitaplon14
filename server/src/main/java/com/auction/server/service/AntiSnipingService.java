package com.auction.server.service;

import com.auction.common.entity.Auction;

import java.time.LocalDateTime;
import java.time.Duration;

public class AntiSnipingService {

    // Ngưỡng kích hoạt gia hạn (giây trước khi kết thúc)
    private static final int EXTEND_THRESHOLD_SECONDS = 30;

    /**
     * Kiểm tra và gia hạn phiên đấu giá nếu còn ít thời gian.
     * @return true nếu đã gia hạn, false nếu không
     */
    public boolean checkAndExtend(Auction auction) {
        if (auction == null || !auction.isAntiSnipingEnabled()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = auction.getEndTime();
        if (endTime == null || now.isAfter(endTime)) {
            return false;
        }

        long secondsLeft = Duration.between(now, endTime).getSeconds();
        if (secondsLeft <= EXTEND_THRESHOLD_SECONDS && secondsLeft > 0) {
            double extension = auction.getAntiSnipingExtensionSeconds();
            auction.extendEndTime(extension);
            return true;
        }
        return false;
    }

    /**
     * Lấy số giây còn lại trước khi kết thúc phiên (sau khi đã gia hạn nếu có).
     */
    public long getRemainingExtensionSeconds(Auction auction) {
        if (auction == null || auction.getEndTime() == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        return Math.max(0, Duration.between(now, auction.getEndTime()).getSeconds());
    }
}