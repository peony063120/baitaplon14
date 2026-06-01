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
    private AuctionSubject subject;
    private final AntiSnipingService antiSnipingService;
    private AutoBidService autoBidService;

    // Constructor mặc định (dùng trong production)
    public BiddingService() {
        this.auctionDAO = AuctionDAO.getInstance();
        this.bidDAO = BidTransactionDAO.getInstance();
        this.userDAO = UserDAO.getInstance();
        this.subject = AuctionSubject.getInstance();
        this.antiSnipingService = new AntiSnipingService();
        this.autoBidService = new AutoBidService();
        this.strategy = new NormalBiddingStrategy();
    }

    // Constructor để test inject mock
    public BiddingService(AuctionDAO auctionDAO,
                          BidTransactionDAO bidDAO,
                          UserDAO userDAO,
                          AuctionSubject subject,
                          AntiSnipingService antiSnipingService,
                          AutoBidService autoBidService) {
        this.auctionDAO = auctionDAO;
        this.bidDAO = bidDAO;
        this.userDAO = userDAO;
        this.subject = subject;
        this.antiSnipingService = antiSnipingService;
        this.autoBidService = autoBidService;
        this.strategy = new NormalBiddingStrategy();
    }

    // Setter để inject AuctionSubject (nếu cần)
    public void setAuctionSubject(AuctionSubject subject) {
        this.subject = subject;
    }

    /**
     * Đặt giá – đồng bộ trên đối tượng auction để tránh xung đột
     * Method chính gọi từ client, sẽ trigger auto-bid cho người khác
     */
    public void placeBid(BidRequest request) throws InvalidBidException {
        placeBid(request, true);
    }

    /**
     * Internal method with flag to control auto-bid triggering.
     * When triggerAutoBids is false, prevents infinite recursion for auto-bids.
     */
    public void placeBid(BidRequest request, boolean triggerAutoBids) throws InvalidBidException {
        Auction auction = auctionDAO.getAuction(request.getAuctionId());
        if (auction == null) {
            throw new InvalidBidException("Auction not found");
        }

        // Khóa chính trên auction để đảm bảo không có 2 bid xử lý đồng thời trên cùng phiên
        synchronized (auction) {
            // Kiểm tra trạng thái và thời gian
            if (!auction.isActive()) {
                throw new InvalidBidException("Auction is not active (not running or outside time window)");
            }

            // Seller không được đấu giá sản phẩm của mình
            if (auction.getSellerId().equals(request.getBidderId())) {
                throw new InvalidBidException("Seller cannot bid on own auction");
            }

            // Lấy bidder – giả sử userDAO có findUserById (nếu chưa có thì thêm vào UserDAO)
            User user = userDAO.findUserById(request.getBidderId());
            if (user == null) {
                throw new InvalidBidException("Bidder not found");
            }
            if (!(user instanceof Bidder)) {
                throw new InvalidBidException("User is not a bidder");
            }
            Bidder bidder = (Bidder) user;

            // Kiểm tra số dư - phải có đủ tiền cho toàn bộ giá thầu
            if (bidder.getBalance() < request.getAmount()) {
                throw new InvalidBidException("Insufficient balance");
            }

            // Lưu giá và người thắng trước khi strategy cập nhật auction
            double previousPrice = auction.getCurrentPrice();
            String previousWinnerId = auction.getCurrentWinnerId();

            // Validate và thực thi bid (NormalBiddingStrategy vừa kiểm tra vừa apply)
            strategy.execute(auction, request);

            // Lấy bid vừa được thêm vào bidHistory từ strategy
            // (Strategy đã tạo BidTransaction với autoBid=false, cập nhật thành request.isAutoBid())
            BidTransaction bid = auction.getBidHistory().get(auction.getBidHistory().size() - 1);
            bid.setAutoBid(request.isAutoBid());

            // Lưu giao dịch vào database
            bidDAO.saveBidTransaction(bid);
            auctionDAO.saveAuction(auction);

            // HOÀN TIỀN CHO NGƯỜI THẮNG TRƯỚC (nếu có)
            if (previousWinnerId != null && !previousWinnerId.equals(request.getBidderId())) {
                User previousWinner = userDAO.findUserById(previousWinnerId);
                if (previousWinner instanceof Bidder) {
                    Bidder previousBidder = (Bidder) previousWinner;
                    previousBidder.addBalance(previousPrice);
                    userDAO.saveUser(previousBidder);
                }
            }

            // CHỈ TRỪ SỐ TIỀN CHÊNH LỆCH (giá mới - giá cũ)
            double amountToDeduct = request.getAmount() - previousPrice;
            if (amountToDeduct > 0) {
                if (!bidder.deductBalance(amountToDeduct)) {
                    throw new InvalidBidException("Insufficient balance for bid increment");
                }
                userDAO.saveUser(bidder);
            }

            // Thông báo realtime cho client
            subject.notifyObservers(auction);

            // Anti-sniping: bid trong phút cuối → gia hạn 3 phút và lên lịch kết thúc lại
            if (auction.isAntiSnipingEnabled()) {
                boolean extended = antiSnipingService.checkAndExtend(auction);
                if (extended) {
                    auctionDAO.saveAuction(auction);
                    com.auction.server.scheduler.AuctionScheduler.getInstance()
                            .rescheduleAuctionEnd(auction);
                    subject.notifyObservers(auction);
                }
            }

            // Xử lý auto-bid cho các bidder khác (chỉ nếu triggerAutoBids = true)
            if (triggerAutoBids) {
                autoBidService.processAutoBids(auction);
            }
        }
    }

    /**
     * Internal method to place a bid without triggering auto-bids for others.
     * Used by AutoBidService to prevent infinite recursion.
     */
    public void placeBidInternal(BidRequest request) throws InvalidBidException {
        placeBid(request, false);
    }

    public List<BidTransaction> getBidHistory(String auctionId) {
        return bidDAO.getBidHistory(auctionId);
    }

    public List<BidTransaction> getBidsByUser(String userId) {
        return bidDAO.getBidsByUser(userId);
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

    /**
     * Hủy đấu giá - hoàn tiền cho người đấu và cập nhật auction
     */
    public void cancelBid(String auctionId, String bidderId) {
        Auction auction = auctionDAO.getAuction(auctionId);
        if (auction == null) {
            throw new IllegalArgumentException("Auction not found");
        }

        synchronized (auction) {
            // Kiểm tra nếu auction đang chạy
            if (auction.getStatus() != AuctionStatus.RUNNING) {
                throw new IllegalArgumentException("Auction is not running");
            }

            // Kiểm tra nếu bidder là người thắng hiện tại
            String currentWinnerId = auction.getCurrentWinnerId();
            if (!bidderId.equals(currentWinnerId)) {
                throw new IllegalArgumentException("Only the current highest bidder can cancel their bid");
            }

            // Lấy bidder
            Bidder bidder = (Bidder) userDAO.findUserById(bidderId);
            if (bidder == null) {
                throw new IllegalArgumentException("Bidder not found");
            }

            // Lấy giá hiện tại (sẽ được hoàn lại)
            double currentPrice = auction.getCurrentPrice();
            
            // Cập nhật auction: đặt trở lại người thắng trước đó
            // Tìm bid trước đó trong lịch sử
            List<BidTransaction> history = bidDAO.getBidHistory(auctionId);
            BidTransaction previousBid = null;
            
            // Tìm bid cuối cùng không phải của bidder này
            for (int i = history.size() - 1; i >= 0; i--) {
                BidTransaction bid = history.get(i);
                if (!bid.getBidderId().equals(bidderId)) {
                    previousBid = bid;
                    break;
                }
            }

            if (previousBid != null) {
                // Cập nhật auction với giá trước
                auction.setCurrentPrice(previousBid.getAmount());
                auction.setCurrentWinnerId(previousBid.getBidderId());
                auctionDAO.saveAuction(auction);

                // Hoàn tiền cho bidder đang hủy
                bidder.addBalance(currentPrice);
                userDAO.saveUser(bidder);
            } else {
                // Không có bid trước, đặt về giá khởi điểm
                auction.setCurrentPrice(auction.getStartingPrice());
                auction.setCurrentWinnerId(null);
                auctionDAO.saveAuction(auction);

                // Hoàn tiền cho bidder
                bidder.addBalance(currentPrice);
                userDAO.saveUser(bidder);
            }

            // Thông báo realtime
            subject.notifyObservers(auction);
        }
    }

    public void setStrategy(BiddingStrategy strategy) {
        this.strategy = strategy;
    }

    public void setAutoBidService(AutoBidService autoBidService) {
        this.autoBidService = autoBidService;
    }

    public BidTransactionDAO getBidDAO() {
        return bidDAO;
    }

    public AuctionSubject getSubject() {
        return subject;
    }
}