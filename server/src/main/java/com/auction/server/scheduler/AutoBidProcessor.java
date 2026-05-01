package com.auction.server.scheduler;

import com.auction.common.entity.Auction;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.service.AutoBidService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AutoBidProcessor
 * Chạy định kỳ để xử lý auto-bid cho tất cả auction đang chạy.
 */
public class AutoBidProcessor {
    private static AutoBidProcessor instance;
    private final ScheduledExecutorService scheduler;
    private final AutoBidService autoBidService;

    private AutoBidProcessor() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.autoBidService = new AutoBidService();
    }

    public static synchronized AutoBidProcessor getInstance() {
        if (instance == null) {
            instance = new AutoBidProcessor();
        }
        return instance;
    }

    /**
     * Khởi động processor, chạy mỗi 3 giây.
     */
    public void start() {
        scheduler.scheduleAtFixedRate(this::processAllActiveAuctions, 0, 3, TimeUnit.SECONDS);
    }

    /**
     * Xử lý auto-bid cho tất cả auction đang chạy.
     */
    private void processAllActiveAuctions() {
        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        List<Auction> runningAuctions = auctionDAO.getAuctionsByStatus(com.auction.common.enums.AuctionStatus.RUNNING);
        for (Auction auction : runningAuctions) {
            autoBidService.processAutoBids(auction);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}