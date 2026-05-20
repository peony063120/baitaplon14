package com.auction.server.scheduler;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.service.AutoBidService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoBidProcessor {
    private static AutoBidProcessor instance;
    private final ScheduledExecutorService scheduler;
    private final AutoBidService autoBidService;
    private final AuctionDAO auctionDAO;

    // Constructor dùng trong production (singleton)
    private AutoBidProcessor() {
        this(AuctionDAO.getInstance(), new AutoBidService());
    }

    // Constructor dùng cho test (inject mock)
    public AutoBidProcessor(AuctionDAO auctionDAO, AutoBidService autoBidService) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.auctionDAO = auctionDAO;
        this.autoBidService = autoBidService;
    }

    public static synchronized AutoBidProcessor getInstance() {
        if (instance == null) {
            instance = new AutoBidProcessor();
        }
        return instance;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::processAllActiveAuctions, 0, 3, TimeUnit.SECONDS);
    }

    // Đổi từ private sang package-private để test có thể gọi
    void processAllActiveAuctions() {
        List<Auction> runningAuctions = auctionDAO.getAuctionsByStatus(AuctionStatus.RUNNING);
        for (Auction auction : runningAuctions) {
            autoBidService.processAutoBids(auction);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}