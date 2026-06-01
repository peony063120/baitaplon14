package com.auction.server.scheduler;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.service.AutoBidService;
import com.auction.server.service.BiddingService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoBidProcessor {
    private static AutoBidProcessor instance;
    private final ScheduledExecutorService scheduler;
    private final AuctionDAO auctionDAO;
    private AutoBidService autoBidService;
    private BiddingService biddingService;

    // Constructor dùng trong production (singleton)
    private AutoBidProcessor() {
        this(AuctionDAO.getInstance());
    }

    // Constructor dùng cho test (inject mock)
    public AutoBidProcessor(AuctionDAO auctionDAO) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.auctionDAO = auctionDAO;
        this.autoBidService = new AutoBidService();
    }

    // Constructor cho test với cả AutoBidService và AuctionDAO
    public AutoBidProcessor(AuctionDAO auctionDAO, AutoBidService autoBidService) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.auctionDAO = auctionDAO;
        this.autoBidService = autoBidService;
    }

    // Setter to inject BiddingService
    public void setBiddingService(BiddingService biddingService) {
        this.biddingService = biddingService;
        // Also set it in AutoBidService
        if (this.autoBidService != null) {
            this.autoBidService.setBiddingService(biddingService);
        }
    }

    // Setter to inject AutoBidService (to use the same instance as BiddingService)
    public void setAutoBidService(AutoBidService autoBidService) {
        this.autoBidService = autoBidService;
        // Also set BiddingService in the new AutoBidService
        if (this.biddingService != null && autoBidService != null) {
            autoBidService.setBiddingService(this.biddingService);
        }
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
        if (auctionDAO == null) return;
        List<Auction> runningAuctions = auctionDAO.getAuctionsByStatus(AuctionStatus.RUNNING);
        if (autoBidService != null) {
            for (Auction auction : runningAuctions) {
                autoBidService.processAutoBids(auction);
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}