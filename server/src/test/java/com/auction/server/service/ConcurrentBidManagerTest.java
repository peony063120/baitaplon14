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
        ExecutorService testExecutor = Executors.newFixedThreadPool(10);
        AtomicInteger successCount = new AtomicInteger(0);

        String auctionId = "auc1";

        // Gửi 100 yêu cầu đấu giá đồng thời cho cùng một auction
        for (int i = 0; i < 100; i++) {
            testExecutor.submit(() -> {
                // Mỗi yêu cầu sẽ thực hiện tăng successCount
                manager.executeBid(auctionId, successCount::incrementAndGet);
            });
        }

        // Đợi tất cả yêu cầu gửi đi hoàn tất
        testExecutor.shutdown();
        testExecutor.awaitTermination(5, TimeUnit.SECONDS);

        // Đợi thêm một chút để manager xử lý hết các tác vụ trong hàng đợi nội bộ
        Thread.sleep(500);

        // Chỉ có một tác vụ được thực thi thành công (do lock chỉ cho phép một luồng chạy tại một thời điểm)
        assertEquals(1, successCount.get());

        manager.shutdown();
    }
}