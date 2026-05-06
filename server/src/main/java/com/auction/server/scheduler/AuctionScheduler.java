package com.auction.server.scheduler;

import com.auction.common.entity.Auction;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.service.AuctionService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionScheduler {
    private static AuctionScheduler instance;
    private final ScheduledExecutorService scheduler;
    private final AuctionDAO auctionDAO;
    private final AuctionService auctionService;

    // Constructor dùng trong production (singleton)
    private AuctionScheduler() {
        this(AuctionDAO.getInstance(), new AuctionService());
    }

    // Constructor dùng cho test (inject mock)
    public AuctionScheduler(AuctionDAO auctionDAO, AuctionService auctionService) {
        this.scheduler = Executors.newScheduledThreadPool(10);
        this.auctionDAO = auctionDAO;
        this.auctionService = auctionService;
    }

    public static synchronized AuctionScheduler getInstance() {
        if (instance == null) {
            instance = new AuctionScheduler();
        }
        return instance;
    }

    public void scheduleAuctionStart(Auction auction) {
        LocalDateTime startTime = auction.getStartTime();
        if (startTime == null || startTime.isBefore(LocalDateTime.now())) {
            new StartAuctionTask(auctionDAO, auctionService, auction.getId()).run();
            return;
        }
        long delay = Duration.between(LocalDateTime.now(), startTime).toMillis();
        scheduler.schedule(new StartAuctionTask(auctionDAO, auctionService, auction.getId()), delay, TimeUnit.MILLISECONDS);
    }

    public void scheduleAuctionEnd(Auction auction) {
        LocalDateTime endTime = auction.getEndTime();
        if (endTime == null || endTime.isBefore(LocalDateTime.now())) {
            new EndAuctionTask(auctionDAO, auctionService, auction.getId()).run();
            return;
        }
        long delay = Duration.between(LocalDateTime.now(), endTime).toMillis();
        scheduler.schedule(new EndAuctionTask(auctionDAO, auctionService, auction.getId()), delay, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}