package com.auction.server.service;

import com.auction.common.dto.AutoBidRequest;
import com.auction.common.dto.BidRequest;
import com.auction.common.entity.*;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.exception.InvalidBidException;
import com.auction.common.observer.AuctionSubject;
import com.auction.common.strategy.BiddingStrategy;
import com.auction.common.strategy.NormalBiddingStrategy;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.UserDAO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BiddingService - Xử lý logic đặt giá, auto-bid, chống sniping.
 * Sử dụng synchronized trên từng auction để tránh race condition.
 */
public class BiddingService {
    private final AuctionDAO auctionDAO;
    private final BidTransactionDAO bidDAO;
    private final UserDAO userDAO;
    private BiddingStrategy strategy;
    private final AuctionSubject subject;
    private final AntiSnipingService antiSnipingService;
    private final AutoBidService autoBidService;

    public BiddingService() {
        this.auctionDAO = AuctionDAO.getInstance();
        this.bidDAO = new BidTransactionDAO();
        this.userDAO = UserDAO.getInstance();
        this.subject = new AuctionSubject();
        this.antiSnipingService = new AntiSnipingService();
        this.autoBidService = new AutoBidService();
        this.strategy = new NormalBiddingStrategy();
    }

    /**
     * Đặt giá – đồng bộ trên đối tượng auction để tránh xung đột
     */
    public void placeBid(BidRequest request) throws InvalidBidException {
        Auction auction = auctionDAO.getAuction(request.getAuctionId());
        if (auction == null) {
            throw new IllegalArgumentException("Auction not found");
        }

        // Khóa chính trên auction để đảm bảo không có 2 bid xử lý đồng thời trên cùng phiên
        synchronized (auction) {
            // Kiểm tra trạng thái
            if (auction.getStatus() != AuctionStatus.RUNNING) {
                throw new IllegalArgumentException("Auction is not running");
            }

            // Seller không được đấu giá sản phẩm của mình
            if (auction.getSellerId().equals(request.getBidderId())) {
                throw new IllegalArgumentException("Seller cannot bid on own auction");
            }

            // Lấy bidder – giả sử userDAO có findUserById (nếu chưa có thì thêm vào UserDAO)
            Bidder bidder = (Bidder) userDAO.findUserById(request.getBidderId());
            if (bidder == null) {
                throw new IllegalArgumentException("Bidder not found");
            }

            // Kiểm tra số dư
            if (bidder.getBalance() < request.getAmount()) {
                throw new IllegalArgumentException("Insufficient balance");
            }

            // Kiểm tra giá hợp lệ qua strategy
            if (!strategy.execute(auction, request)) {
                throw new IllegalArgumentException("Invalid bid amount");
            }

            // Tạo giao dịch – dùng constructor rút gọn (tự sinh transactionId)
            BidTransaction bid = new BidTransaction(
                    auction.getId(),
                    bidder.getId(),
                    request.getAmount(),
                    LocalDateTime.now(),
                    request.isAutoBid()
            );

            // Lưu giao dịch
            bidDAO.saveBidTransaction(bid);

            // Cập nhật auction
            auction.addBid(bid);
            auctionDAO.saveAuction(auction);

            // Trừ tiền bidder
            bidder.deductBalance(request.getAmount());
            userDAO.saveUser(bidder);

            // Thông báo realtime cho client
            subject.notifyObservers(auction);

            // Anti-sniping: nếu được bật, gia hạn nếu cần
            if (auction.isAntiSnipingEnabled()) {
                boolean extended = antiSnipingService.checkAndExtend(auction);
                if (extended) {
                    auctionDAO.saveAuction(auction);   // Lưu lại nếu endTime thay đổi
                }
            }

            // Xử lý auto-bid cho các bidder khác
            autoBidService.processAutoBids(auction);
        }
    }

    public List<BidTransaction> getBidHistory(String auctionId) {
        return bidDAO.getBidHistory(auctionId);
    }

    public void configureAutoBid(AutoBidRequest request) {
        Bidder bidder = (Bidder) userDAO.findUserById(request.getUserId());
        if (bidder == null) {
            throw new IllegalArgumentException("User not found");
        }

        Auction auction = auctionDAO.getAuction(request.getAuctionId());
        if (auction == null) {
            throw new IllegalArgumentException("Auction not found");
        }

        if (!request.isEnable()) {
            cancelAutoBid(request.getAuctionId(), request.getUserId());
            return;
        }

        AutoBidConfig config = new AutoBidConfig(
                request.getAuctionId(),
                request.getUserId(),
                request.getMaxBid(),
                request.getIncrement()
        );
        config.setActive(true);

        bidder.setAutoBidConfig(request.getAuctionId(), config);
        userDAO.saveUser(bidder);
        autoBidService.registerAutoBid(config);
    }

    public void cancelAutoBid(String auctionId, String userId) {
        Bidder bidder = (Bidder) userDAO.findUserById(userId);
        if (bidder != null) {
            bidder.removeAutoBidConfig(auctionId);
            userDAO.saveUser(bidder);
        }
        autoBidService.cancelAutoBid(auctionId, userId);
    }

    public void setStrategy(BiddingStrategy strategy) {
        this.strategy = strategy;
    }

    public BidTransactionDAO getBidDAO() {
        return bidDAO;
    }

    public AuctionSubject getSubject() {
        return subject;
    }
}