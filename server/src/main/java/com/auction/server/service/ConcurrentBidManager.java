package com.auction.server.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConcurrentBidManager {
    private final ConcurrentMap<String, AtomicBoolean> processingFlags;
    private final ExecutorService executor;

    public ConcurrentBidManager() {
        this.processingFlags = new ConcurrentHashMap<>();
        this.executor = Executors.newFixedThreadPool(10);
    }

    /**
     * Thực thi một tác vụ đặt thầu.
     * Đảm bảo chỉ một tác vụ được chấp nhận cho mỗi auctionId.
     *
     * @return true nếu tác vụ được chấp nhận, false nếu bị từ chối
     */
    public boolean executeBid(String auctionId, Runnable task) {
        // Lấy flag hiện có hoặc tạo mới nếu chưa có
        // Dùng computeIfAbsent vẫn an toàn, nhưng cần đảm bảo compareAndSet chỉ gọi 1 lần
        AtomicBoolean flag = processingFlags.computeIfAbsent(auctionId, k -> new AtomicBoolean(false));

        // 🔥 QUAN TRỌNG: Chỉ thread nào set thành công (false -> true) mới được chấp nhận
        // Các thread khác sẽ thấy flag đã là true và bị từ chối
        if (!flag.compareAndSet(false, true)) {
            return false;
        }

        executor.execute(task);
        return true;
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }
}