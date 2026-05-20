package com.auction.server.service;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentBidManagerTest {

    @Test
    void testConcurrentBids_OnlyOneSucceeds() throws InterruptedException {
        ConcurrentBidManager manager = new ConcurrentBidManager();
        ExecutorService testExecutor = Executors.newFixedThreadPool(50); // Tăng lên 50 thread
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger acceptedCount = new AtomicInteger(0); // Thêm để kiểm tra

        String auctionId = "auc1";

        for (int i = 0; i < 500; i++) { // Tăng số lượng lên 500
            testExecutor.submit(() -> {
                boolean accepted = manager.executeBid(auctionId, () -> {
                    // Mô phỏng xử lý lâu
                    try { Thread.sleep(5); } catch (InterruptedException ignored) {}
                    successCount.incrementAndGet();
                });
                if (accepted) acceptedCount.incrementAndGet();
            });
        }

        testExecutor.shutdown();
        testExecutor.awaitTermination(10, TimeUnit.SECONDS);

        manager.shutdown();
        manager.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("Accepted: " + acceptedCount.get() + ", Success: " + successCount.get());

        // Nếu code đúng, cả hai phải bằng 1
        assertEquals(1, successCount.get());
        assertEquals(1, acceptedCount.get());
    }
}