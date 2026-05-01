package com.auction.server.service;

public class AntiSnipingService {
    private int thresholdSeconds;

    public void extendIfNeed(String auctionId) {
    }

    // Getters & Setters
    public int getThresholdSeconds() {
        return thresholdSeconds;
    }

    public void setThresholdSeconds(int thresholdSeconds) {
        this.thresholdSeconds = thresholdSeconds;
    }
}
/**
 * Chống "sniping" (việc đặt giá sát giây cuối để người khác không kịp phản ứng).
 *   Nếu có lượt đặt giá mới ở những giây cuối cùng, dịch vụ này sẽ tự động gia hạn thêm thời gian kết thúc.
 */