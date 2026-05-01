package com.auction.server.model;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AuctionManager (Singleton)
 * Quản lý các phiên đấu giá đang diễn ra (RUNNING).
 * Hỗ trợ start/end auction, kiểm tra trạng thái.
 */
public class AuctionManager {
    private static AuctionManager instance;
    private final Map<String, Auction> runningAuctions; // auctionId -> Auction

    private AuctionManager() {
        runningAuctions = new ConcurrentHashMap<>();
    }

    public static synchronized AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    /**
     * Bắt đầu một phiên đấu giá.
     * Chuyển status từ OPEN/DRAFT sang RUNNING và thêm vào danh sách đang chạy.
     */
    public void startAuction(Auction auction) {
        if (auction == null) return;
        if (auction.getStatus() == AuctionStatus.OPEN || auction.getStatus() == AuctionStatus.DRAFT) {
            auction.setStatus(AuctionStatus.RUNNING);
            runningAuctions.put(auction.getId(), auction);
        }
    }

    /**
     * Kết thúc phiên đấu giá.
     * Xóa khỏi danh sách đang chạy, cập nhật status FINISHED.
     */
    public void endAuction(String auctionId) {
        Auction auction = runningAuctions.remove(auctionId);
        if (auction != null && auction.getStatus() == AuctionStatus.RUNNING) {
            auction.setStatus(AuctionStatus.FINISHED);
        }
    }

    /**
     * Kiểm tra một auction có đang chạy không.
     */
    public boolean isRunning(String auctionId) {
        return runningAuctions.containsKey(auctionId);
    }

    /**
     * Lấy auction đang chạy theo ID.
     */
    public Auction getRunningAuction(String auctionId) {
        return runningAuctions.get(auctionId);
    }

    /**
     * Lấy tất cả auction đang chạy.
     */
    public Map<String, Auction> getRunningAuctions() {
        return new ConcurrentHashMap<>(runningAuctions);
    }
}