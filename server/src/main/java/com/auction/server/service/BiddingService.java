package com.auction.server.service;

import com.auction.common.dto.BidRequest;
import com.auction.common.entity.Auction;
import com.auction.common.strategy.BiddingStrategy;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.common.observer.AuctionSubject;

public class BiddingService {
    private final AuctionDAO auctionDAO;
    private final BidTransactionDAO bidDAO;
    private BiddingStrategy strategy;
    private AuctionSubject subject;
    private AntiSnipingService antiSnipingService;

    public BiddingService() {
        this.auctionDAO = AuctionDAO.getInstance();
        this.bidDAO = BidTransactionDAO.getInstance();

        // Khởi tạo các thành phần liên quan (Giả định)
        this.subject = new AuctionSubject();
        this.antiSnipingService = new AntiSnipingService();
    }

    public synchronized void placeBid(BidRequest request) {
        Auction auction = auctionDAO.getAuction(request.getAuctionId());
        if (strategy != null) {
            strategy.execute(auction, request);
        }
    }

    public void setStrategy(BiddingStrategy strategy) {
        this.strategy = strategy;
    }

    public BidTransactionDAO getBidDAO() {
        return bidDAO;
    }
}
/**
 * Đây là nơi quan trọng nhất xử lý việc đặt giá. Việc sử dụng synchronized cho
 *   thấy hệ thống đang xử lý tuần tự các lượt đặt giá để tránh xung đột
 *   dữ liệu (race condition) khi nhiều người cùng đặt giá một lúc.
 */