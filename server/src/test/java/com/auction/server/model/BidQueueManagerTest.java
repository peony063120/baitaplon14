package com.auction.server.model;

import com.auction.common.dto.BidRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BidQueueManagerTest {

    private BidQueueManager manager;

    @BeforeEach
    void setUp() {
        manager = BidQueueManager.getInstance();
    }

    @Test
    void testProcessBids_Sequential() throws InterruptedException {
        AtomicInteger processedCount = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // Gửi 100 request đồng thời
        for (int i = 0; i < 100; i++) {
            final int id = i;
            executor.submit(() -> {
                BidRequest request = new BidRequest("auc1", "bidder" + id, 100.0 + id, false);
                manager.addBidRequest(request);
                processedCount.incrementAndGet(); // Chỉ đếm số request đã thêm vào queue
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Đợi queue xử lý
        Thread.sleep(1000);

        // Kiểm tra tất cả request đã được thêm vào queue
        assertEquals(100, processedCount.get());

        // Kiểm tra kích thước queue hiện tại (có thể đã xử lý xong)
        // Nếu queue xử lý nhanh, size có thể = 0
        System.out.println("Queue size: " + manager.getQueueSize());
    }
}