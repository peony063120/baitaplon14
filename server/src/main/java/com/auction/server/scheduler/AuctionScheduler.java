package com.auction.server.scheduler;

import com.auction.common.entity.Auction;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.service.AuctionService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AuctionScheduler {
    private static AuctionScheduler instance;
    private final ScheduledExecutorService scheduler;
    private final AuctionDAO auctionDAO;
    private final AuctionService auctionService;
    private final Map<String, ScheduledFuture<?>> endTasks = new ConcurrentHashMap<>();

    private AuctionScheduler() {
        this(AuctionDAO.getInstance(), new AuctionService());
    }

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
        rescheduleAuctionEnd(auction);
    }

    public void rescheduleAuctionEnd(Auction auction) {
        if (auction == null || auction.getId() == null) {
            return;
        }
        cancelEndTask(auction.getId());

        LocalDateTime endTime = auction.getEndTime();
        if (endTime == null || !endTime.isAfter(LocalDateTime.now())) {
            new EndAuctionTask(auctionDAO, auctionService, auction.getId()).run();
            return;
        }
        long delay = Duration.between(LocalDateTime.now(), endTime).toMillis();
        ScheduledFuture<?> future = scheduler.schedule(
                new EndAuctionTask(auctionDAO, auctionService, auction.getId()),
                delay,
                TimeUnit.MILLISECONDS);
        endTasks.put(auction.getId(), future);
    }

    private void cancelEndTask(String auctionId) {
        ScheduledFuture<?> existing = endTasks.remove(auctionId);
        if (existing != null) {
            existing.cancel(false);
        }
    }

    public void shutdown() {
        endTasks.values().forEach(f -> f.cancel(false));
        endTasks.clear();
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
