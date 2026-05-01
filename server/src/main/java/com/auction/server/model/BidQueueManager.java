package com.auction.server.model;

import com.auction.common.dto.BidRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BidQueueManager
 * Quản lý hàng đợi các yêu cầu đấu giá đến đồng thời, xử lý tuần tự để tránh race condition.
 */
public class BidQueueManager {
    private static BidQueueManager instance;
    private final BlockingQueue<BidRequest> queue;
    private final AtomicBoolean processing;

    private BidQueueManager() {
        this.queue = new LinkedBlockingQueue<>();
        this.processing = new AtomicBoolean(false);
    }

    public static synchronized BidQueueManager getInstance() {
        if (instance == null) {
            instance = new BidQueueManager();
        }
        return instance;
    }

    /**
     * Thêm một bid request vào hàng đợi.
     */
    public void addBidRequest(BidRequest request) {
        queue.offer(request);
        startProcessingIfNeeded();
    }

    /**
     * Khởi động luồng xử lý nếu chưa chạy.
     */
    private void startProcessingIfNeeded() {
        if (processing.compareAndSet(false, true)) {
            Thread processor = new Thread(() -> {
                while (true) {
                    try {
                        BidRequest request = queue.take();
                        // Ủy thác xử lý cho BiddingService (sẽ được inject)
                        // Ở đây giả sử có một callback hoặc service tĩnh.
                        // Thực tế nên dùng một Consumer hoặc xử lý trực tiếp.
                        processRequest(request);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                processing.set(false);
            });
            processor.setDaemon(true);
            processor.start();
        }
    }

    private void processRequest(BidRequest request) {
        // TODO: Gọi BiddingService.placeBid(request) một cách đồng bộ.
        // Vì đã xếp hàng nên không cần thêm synchronized ở đây.
        // Ví dụ: BiddingService.getInstance().placeBid(request);
    }

    /**
     * Kiểm tra kích thước hàng đợi hiện tại.
     */
    public int getQueueSize() {
        return queue.size();
    }
}