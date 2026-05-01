package com.auction.server.service;

import com.auction.common.entity.Auction;
import com.auction.common.entity.AutoBidConfig;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AutoBidService {
    private PriorityQueue<AutoBidConfig> autoBidQueue;
    private ScheduledExecutorService scheduler;

    public AutoBidService() {
        this.autoBidQueue = new PriorityQueue<>((a, b) -> Double.compare(b.getMaxBid(), a.getMaxBid()));
        this.scheduler = Executors.newScheduledThreadPool(5);
    }

    public void registerAutoBid(AutoBidConfig config) {
        autoBidQueue.add(config);
    }

    public void processAutoBids(Auction auction) {
        // Duyệt qua hàng đợi và thực hiện đặt giá tự động nếu đủ điều kiện
    }

    public void cancelAutoBid(String auctionId, String userId) {
        autoBidQueue.removeIf(config ->
                config.getAuctionId().equals(auctionId) && config.getAuctionId().equals(userId)
        );
    }
}