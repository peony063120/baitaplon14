package com.auction.server.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentBidManager {
    private ConcurrentMap<String, ReentrantLock> bidLocks;
    private ExecutorService executor;

    public ConcurrentBidManager() {
        this.bidLocks = new ConcurrentHashMap<>();
        // Khởi tạo một Thread Pool (ví dụ 10 threads) để xử lý các task đặt thầu
        this.executor = Executors.newFixedThreadPool(10);
    }

    /**
     * executeBid(auctionId: String, task: Runnable): void
     * Thực thi một tác vụ đặt thầu đảm bảo an toàn luồng cho từng Auction.
     */
    public void executeBid(String auctionId, Runnable task) {
        // Lấy hoặc tạo lock cho auctionId
        ReentrantLock lock = bidLocks.computeIfAbsent(auctionId, k -> new ReentrantLock());

        // Gửi task vào executor để xử lý bất đồng bộ
        executor.execute(() -> {
            lock.lock();
            try {
                task.run(); // Chạy logic đặt thầu (ví dụ: trừ tiền, cập nhật giá)
            } finally {
                lock.unlock();
            }
        });
    }

    /**
     * shutdown(): void
     * Dừng toàn bộ các task đang chạy khi hệ thống đóng.
     */
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    // Getter & Setter
    public ConcurrentMap<String, ReentrantLock> getBidLocks() {
        return bidLocks;
    }

    public void setBidLocks(ConcurrentMap<String, ReentrantLock> bidLocks) {
        this.bidLocks = bidLocks;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }
}