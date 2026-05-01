package com.auction.server.scheduler;

import com.auction.common.entity.Auction;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AuctionScheduler
 * Sử dụng ScheduledExecutorService để lên lịch bắt đầu và kết thúc phiên đấu giá.
 */
public class AuctionScheduler {
    private static AuctionScheduler instance;
    private final ScheduledExecutorService scheduler;

    private AuctionScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(10);
    }

    public static synchronized AuctionScheduler getInstance() {
        if (instance == null) {
            instance = new AuctionScheduler();
        }
        return instance;
    }

    /**
     * Lên lịch bắt đầu một phiên đấu giá.
     */
    public void scheduleAuctionStart(Auction auction) {
        LocalDateTime startTime = auction.getStartTime();
        if (startTime == null || startTime.isBefore(LocalDateTime.now())) {
            // Nếu đã quá giờ bắt đầu thì chạy ngay lập tức
            new StartAuctionTask(auction.getId()).run();
            return;
        }
        long delay = Duration.between(LocalDateTime.now(), startTime).toMillis();
        scheduler.schedule(new StartAuctionTask(auction.getId()), delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Lên lịch kết thúc một phiên đấu giá.
     */
    public void scheduleAuctionEnd(Auction auction) {
        LocalDateTime endTime = auction.getEndTime();
        if (endTime == null || endTime.isBefore(LocalDateTime.now())) {
            new EndAuctionTask(auction.getId()).run();
            return;
        }
        long delay = Duration.between(LocalDateTime.now(), endTime).toMillis();
        scheduler.schedule(new EndAuctionTask(auction.getId()), delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Đóng scheduler khi shutdown server.
     */
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