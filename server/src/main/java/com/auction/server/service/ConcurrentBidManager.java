package com.auction.server.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Quản lý truy cập đồng thời để tránh Race Conditions.
 * Sử dụng ConcurrentHashMap để lưu trữ các Lock cho từng phiên đấu giá.
 */
public class ConcurrentBidManager {
    // Map lưu trữ Lock cho mỗi AuctionId để đảm bảo nhiều người không update 1 auction cùng lúc
    private ConcurrentHashMap<String, ReentrantLock> auctionLocks;

    public ConcurrentBidManager() {
        this.auctionLocks = new ConcurrentHashMap<>();
    }

    /**
     * Lấy hoặc tạo mới một Lock cho phiên đấu giá cụ thể.
     *
     * @param auctionId ID của phiên đấu giá
     * @return ReentrantLock đối tượng dùng để khóa
     */
    public ReentrantLock getLock(String auctionId) {
        // Nếu chưa có lock cho auctionId này thì tạo mới, nếu có rồi thì lấy ra
        return auctionLocks.computeIfAbsent(auctionId, k -> new ReentrantLock());
    }

    // Getter và Setter theo yêu cầu sơ đồ
    public ConcurrentHashMap<String, ReentrantLock> getAuctionLocks() {
        return auctionLocks;
    }

    public void setAuctionLocks(ConcurrentHashMap<String, ReentrantLock> locks) {
        this.auctionLocks = locks;
    }
}