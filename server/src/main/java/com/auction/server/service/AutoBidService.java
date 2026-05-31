package com.auction.server.service;

import com.auction.common.dto.BidRequest;
import com.auction.common.entity.Auction;
import com.auction.common.entity.AutoBidConfig;
import com.auction.common.enums.AuctionStatus;
import com.auction.server.dao.UserDAO;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AutoBidService {
    private PriorityQueue<AutoBidConfig> autoBidQueue;
    private ScheduledExecutorService scheduler;
    private BiddingService biddingService;
    private UserDAO userDAO;

    public AutoBidService() {
        this.autoBidQueue = new PriorityQueue<>((a, b) -> Double.compare(b.getMaxBid(), a.getMaxBid()));
        this.scheduler = Executors.newScheduledThreadPool(5);
        this.userDAO = UserDAO.getInstance();
        // Don't create BiddingService here to avoid circular dependency
        // It will be set via setter or passed to processAutoBids
    }

    public AutoBidService(BiddingService biddingService) {
        this.autoBidQueue = new PriorityQueue<>((a, b) -> Double.compare(b.getMaxBid(), a.getMaxBid()));
        this.scheduler = Executors.newScheduledThreadPool(5);
        this.biddingService = biddingService;
        this.userDAO = UserDAO.getInstance();
    }

    public void setBiddingService(BiddingService biddingService) {
        this.biddingService = biddingService;
    }

    public void registerAutoBid(AutoBidConfig config) {
        autoBidQueue.add(config);
    }

    public void processAutoBids(Auction auction) {
        // Delegate to the version with BiddingService parameter
        // This allows AutoBidProcessor to inject the BiddingService
        processAutoBids(auction, this.biddingService);
    }

    public void processAutoBids(Auction auction, BiddingService biddingService) {
        if (biddingService == null) {
            System.err.println("[AutoBidService] BiddingService is null, cannot process auto-bids");
            return;
        }
        
        if (auction == null || auction.getStatus() != AuctionStatus.RUNNING) {
            return;
        }

        // Find all auto-bid configs for this auction
        List<AutoBidConfig> configsForAuction = new ArrayList<>();
        Iterator<AutoBidConfig> iterator = autoBidQueue.iterator();
        while (iterator.hasNext()) {
            AutoBidConfig config = iterator.next();
            if (config.getAuctionId().equals(auction.getId()) && config.isActive()) {
                configsForAuction.add(config);
            }
        }

        // Get current price
        double currentPrice = auction.getCurrentPrice();
        String currentWinnerId = auction.getCurrentWinnerId();

        // Find the highest auto-bid that can outbid
        for (AutoBidConfig config : configsForAuction) {
            // Skip if this is the current winner's auto-bid (they already won)
            if (config.getBidderId().equals(currentWinnerId)) {
                continue;
            }

            // Check if this auto-bid can place a bid
            if (config.canBid(currentPrice)) {
                double nextBid = config.getNextBid(currentPrice);
                
                // Check if nextBid exceeds current highest
                if (nextBid > currentPrice) {
                    try {
                        // Place the auto-bid
                        // Note: We pass false for isAutoBid to prevent infinite recursion
                        // (BiddingService.placeBid will call processAutoBids again)
                        BidRequest request = new BidRequest(
                                auction.getId(),
                                config.getBidderId(),
                                nextBid,
                                false  // Don't trigger another auto-bid cycle
                        );
                        biddingService.placeBidInternal(request);
                        
                        // Only one auto-bid should fire per auction cycle
                        // The highest priority one (by maxBid) will execute first
                        break;
                    } catch (Exception e) {
                        System.err.println("[AutoBidService] Failed to place auto-bid: " + e.getMessage());
                    }
                }
            }
        }
    }

    public void cancelAutoBid(String auctionId, String userId) {
        autoBidQueue.removeIf(config ->
                config.getAuctionId().equals(auctionId) && config.getBidderId().equals(userId)
        );
    }

    // For testing
    public PriorityQueue<AutoBidConfig> getAutoBidQueue() {
        return autoBidQueue;
    }
}