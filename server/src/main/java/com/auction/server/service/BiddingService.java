package com.auction.server.service;

import com.auction.common.dto.BidRequest;
import com.auction.common.dto.AutoBidRequest;
import com.auction.common.entity.BidTransaction;
import com.auction.server.dao.BidTransactionDAO;

import java.util.ArrayList;
import java.util.List;

public class BiddingService {
    private BidTransactionDAO bidDAO;

    public BiddingService() {
        this.bidDAO = BidTransactionDAO.getInstance();
    }

    public synchronized void placeBid(BidRequest request) {
        // Logic đồng bộ hóa xử lý đặt giá
    }

    public List<BidTransaction> getBidHistory(String auctionId) {
        return new ArrayList<BidTransaction>();
    }

    public void configureAutoBid(AutoBidRequest request) {
    }

    public void cancelAutoBid(String auctionId, String userId) {
    }

    // Getters & Setters
    public BidTransactionDAO getBidDAO() {
        return bidDAO;
    }

    public void setBidDAO(BidTransactionDAO bidDAO) {
        this.bidDAO = bidDAO;
    }
}
/**
 * Đây là nơi quan trọng nhất xử lý việc đặt giá. Việc sử dụng synchronized cho
 *   thấy hệ thống đang xử lý tuần tự các lượt đặt giá để tránh xung đột
 *   dữ liệu (race condition) khi nhiều người cùng đặt giá một lúc.
 */