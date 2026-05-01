package com.auction.server.service;

import com.auction.common.entity.Auction;

/**
 * AntiSnipingService: Tự động gia hạn thời gian đấu giá nếu có lượt đặt thầu phút chót.
 */
public class AntiSnipingService {
    private AntiSnipingConfig config;
    private AuctionScheduler scheduler;

    public AntiSnipingService() {
        this.config = new AntiSnipingConfig();
        this.scheduler = new AuctionScheduler();
    }

    /**
     * checkAndExtend(auction: Auction): boolean
     * Kiểm tra và gia hạn nếu cần thiết. Trả về true nếu cuộc đấu giá được gia hạn.
     */
    public boolean checkAndExtend(Auction auction) {
        // Logic: Nếu thời gian còn lại < ngưỡng trong config, gọi scheduler để gia hạn
        // Ví dụ: return scheduler.extend(auction, config.getExtraSeconds());
        return false;
    }

    /**
     * getRemainingExtensionSeconds(auction: Auction): long
     * Lấy thời gian gia hạn còn lại của phiên đấu giá.
     */
    public long getRemainingExtensionSeconds(Auction auction) {
        return 0L;
    }

    // Getter & Setter
    public AntiSnipingConfig getConfig() {
        return config;
    }

    public void setConfig(AntiSnipingConfig config) {
        this.config = config;
    }

    public AuctionScheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(AuctionScheduler scheduler) {
        this.scheduler = scheduler;
    }
}