package com.auction.server.service;

import com.auction.common.dto.AutoBidRequest;

import java.util.PriorityQueue;

/**
 *Quản lý tính năng tự động đặt giá. Việc sử dụng PriorityQueue (Hàng đợi ưu tiên) gợi ý rằng các lệnh tự động đặt giá được sắp xếp
 *    theo mức giá hoặc thời gian để xử lý tối ưu.
 */
public class AutoBidService {
    private PriorityQueue<AutoBidRequest> autoBidQueue;

    public AutoBidService() {
        this.autoBidQueue = new PriorityQueue<>((a, b) -> Double.compare(b.getMaxBid(), a.getMaxBid()));
    }

    // Getters & Setters
    public PriorityQueue<AutoBidRequest> getAutoBidQueue() {
        return autoBidQueue;
    }

    public void setAutoBidQueue(PriorityQueue<AutoBidRequest> autoBidQueue) {
        this.autoBidQueue = autoBidQueue;
    }
}