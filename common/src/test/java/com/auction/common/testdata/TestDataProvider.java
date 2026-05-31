package com.auction.common.testdata;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.dto.BidHistoryDTO;
import com.auction.common.dto.BidRequest;
import com.auction.common.dto.UserDTO;
import com.auction.common.entity.*;
import com.auction.common.enums.AuctionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestDataProvider {

    // ==================== USER IDS ====================
    public static final String USER_ADMIN = "user-001";
    public static final String USER_ADMIN2 = "user-011";
    public static final String USER_BIDDER1 = "user-002";
    public static final String USER_BIDDER2 = "user-003";
    public static final String USER_BIDDER3 = "user-004";
    public static final String USER_BIDDER4 = "user-007";
    public static final String USER_BIDDER5 = "user-008";
    public static final String USER_SELLER1 = "user-005";
    public static final String USER_SELLER2 = "user-006";
    public static final String USER_SELLER3 = "user-009";
    public static final String USER_SELLER4 = "user-010";

    // ==================== USERNAMES ====================
    public static final String USERNAME_ADMIN = "admin";
    public static final String USERNAME_ADMIN2 = "admin2";
    public static final String USERNAME_BIDDER1 = "bidder1";
    public static final String USERNAME_BIDDER2 = "bidder2";
    public static final String USERNAME_BIDDER3 = "bidder3";
    public static final String USERNAME_BIDDER4 = "bidder4";
    public static final String USERNAME_BIDDER5 = "bidder5";
    public static final String USERNAME_SELLER1 = "seller1";
    public static final String USERNAME_SELLER2 = "seller2";
    public static final String USERNAME_SELLER3 = "seller3";
    public static final String USERNAME_SELLER4 = "seller4";

    // ==================== ITEM IDS ====================
    public static final String ITEM_1 = "item-001";
    public static final String ITEM_2 = "item-002";
    public static final String ITEM_3 = "item-003";
    public static final String ITEM_4 = "item-004";
    public static final String ITEM_5 = "item-005";
    public static final String ITEM_6 = "item-006";
    public static final String ITEM_7 = "item-007";
    public static final String ITEM_8 = "item-008";
    public static final String ITEM_9 = "item-009";
    public static final String ITEM_10 = "item-010";
    public static final String ITEM_11 = "item-011";
    public static final String ITEM_12 = "item-012";

    // ==================== AUCTION IDS ====================
    public static final String AUCTION_1 = "auc-001";
    public static final String AUCTION_2 = "auc-002";
    public static final String AUCTION_3 = "auc-003";
    public static final String AUCTION_4 = "auc-004";
    public static final String AUCTION_5 = "auc-005";
    public static final String AUCTION_6 = "auc-006";
    public static final String AUCTION_7 = "auc-007";
    public static final String AUCTION_8 = "auc-008";
    public static final String AUCTION_9 = "auc-009";
    public static final String AUCTION_10 = "auc-010";
    public static final String AUCTION_11 = "auc-011";
    public static final String AUCTION_12 = "auc-012";
    public static final String AUCTION_13 = "auc-013";

    // ==================== DEFAULT PASSWORDS ====================
    public static final String PASSWORD_DEFAULT = "pass123";

    // ==================== USER FACTORY ====================

    public static Bidder bidder(String username, String password, String email, String fullName, double balance) {
        Bidder b = new Bidder(username, password, email, fullName, balance);
        b.setId(USER_BIDDER1);
        return b;
    }

    public static Bidder bidder(String id, String username, String password, String email, String fullName, double balance) {
        Bidder b = new Bidder(username, password, email, fullName, balance);
        b.setId(id);
        return b;
    }

    public static Bidder bidder1() {
        return bidder(USER_BIDDER1, USERNAME_BIDDER1, PASSWORD_DEFAULT,
                "bidder1@auction.com", "Alice Nguyen", 1000.0);
    }

    public static Bidder bidder1(String id, double balance) {
        return bidder(id, USERNAME_BIDDER1, PASSWORD_DEFAULT,
                "bidder1@auction.com", "Alice Nguyen", balance);
    }

    public static Bidder bidder2() {
        return bidder(USER_BIDDER2, USERNAME_BIDDER2, PASSWORD_DEFAULT,
                "bidder2@auction.com", "Bob Tran", 500.0);
    }

    public static Bidder bidder2(String id, double balance) {
        return bidder(id, USERNAME_BIDDER2, PASSWORD_DEFAULT,
                "bidder2@auction.com", "Bob Tran", balance);
    }

    public static Bidder bidder3() {
        return bidder(USER_BIDDER3, USERNAME_BIDDER3, PASSWORD_DEFAULT,
                "bidder3@auction.com", "Charlie Pham", 2000.0);
    }

    public static Bidder bidder4() {
        return bidder(USER_BIDDER4, USERNAME_BIDDER4, PASSWORD_DEFAULT,
                "bidder4@auction.com", "Fiona Le", 3000.0);
    }

    public static Bidder bidder5() {
        return bidder(USER_BIDDER5, USERNAME_BIDDER5, PASSWORD_DEFAULT,
                "bidder5@auction.com", "George Vo", 10000.0);
    }

    public static Seller seller(String id, String username, String password,
                                 String email, String fullName) {
        Seller s = new Seller(username, password, email, fullName);
        s.setId(id);
        return s;
    }

    public static Seller seller1() {
        return seller(USER_SELLER1, USERNAME_SELLER1, PASSWORD_DEFAULT,
                "seller1@auction.com", "David Le");
    }

    public static Seller seller2() {
        return seller(USER_SELLER2, USERNAME_SELLER2, PASSWORD_DEFAULT,
                "seller2@auction.com", "Eva Hoang");
    }

    public static Seller seller3() {
        return seller(USER_SELLER3, USERNAME_SELLER3, PASSWORD_DEFAULT,
                "seller3@auction.com", "Helen Dang");
    }

    public static Seller seller4() {
        return seller(USER_SELLER4, USERNAME_SELLER4, PASSWORD_DEFAULT,
                "seller4@auction.com", "Ivan Tran");
    }

    public static Admin admin() {
        Admin a = new Admin(USERNAME_ADMIN, PASSWORD_DEFAULT,
                "admin@auction.com", "Head Administrator", "FULL");
        a.setId(USER_ADMIN);
        return a;
    }

    public static Admin admin2() {
        Admin a = new Admin(USERNAME_ADMIN2, PASSWORD_DEFAULT,
                "admin2@auction.com", "Deputy Administrator", "MODERATE");
        a.setId(USER_ADMIN2);
        return a;
    }

    // ==================== AUCTION FACTORY ====================

    public static Auction auction(String itemId, String sellerId,
                                   LocalDateTime startTime, LocalDateTime endTime,
                                   double startingPrice) {
        return new Auction(itemId, sellerId, startTime, endTime, startingPrice);
    }

    public static Auction auction(String id, String itemId, String sellerId,
                                   LocalDateTime startTime, LocalDateTime endTime,
                                   double currentPrice, String currentWinnerId,
                                   AuctionStatus status) {
        Auction a = new Auction(id, itemId, sellerId, startTime, endTime,
                currentPrice, currentWinnerId, status);
        a.setMinIncrement(1.0);
        return a;
    }

    public static Auction auction(String id, String itemId, String sellerId,
                                   LocalDateTime startTime, LocalDateTime endTime,
                                   double currentPrice, String currentWinnerId,
                                   AuctionStatus status, double minIncrement) {
        Auction a = new Auction(id, itemId, sellerId, startTime, endTime,
                currentPrice, currentWinnerId, status);
        a.setMinIncrement(minIncrement);
        return a;
    }

    public static Auction draftAuction(String id, String itemId, String sellerId,
                                        double startingPrice) {
        return auction(id, itemId, sellerId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(8),
                startingPrice, null, AuctionStatus.DRAFT);
    }

    public static Auction runningAuction(String id, String itemId, String sellerId,
                                          double currentPrice, String currentWinnerId,
                                          double minIncrement) {
        return auction(id, itemId, sellerId,
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(1),
                currentPrice, currentWinnerId, AuctionStatus.RUNNING, minIncrement);
    }

    public static Auction finishedAuction(String id, String itemId, String sellerId,
                                           double currentPrice, String currentWinnerId,
                                           double minIncrement) {
        return auction(id, itemId, sellerId,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusHours(1),
                currentPrice, currentWinnerId, AuctionStatus.FINISHED, minIncrement);
    }

    public static Auction pendingAuction(String id, String itemId, String sellerId,
                                          double startingPrice) {
        return auction(id, itemId, sellerId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(8),
                startingPrice, null, AuctionStatus.PENDING);
    }

    // ==================== SAMPLE AUCTIONS ====================

    public static Auction sampleDraftAuction() {
        return draftAuction(AUCTION_1, ITEM_1, USER_SELLER1, 30000.0);
    }

    public static Auction sampleRunningAuction() {
        return runningAuction(AUCTION_4, ITEM_4, USER_SELLER1, 385.0, USER_BIDDER1, 5.0);
    }

    public static Auction sampleFinishedAuction() {
        return finishedAuction(AUCTION_9, ITEM_1, USER_SELLER1, 32500.0, USER_BIDDER1, 500.0);
    }

    // ==================== BID FACTORY ====================

    public static BidTransaction bid(String transactionId, String auctionId,
                                      String bidderId, double amount,
                                      LocalDateTime bidTime, boolean autoBid) {
        return new BidTransaction(transactionId, auctionId, bidderId, amount, bidTime, autoBid);
    }

    public static BidTransaction bid(String auctionId, String bidderId,
                                      double amount, LocalDateTime bidTime, boolean autoBid) {
        return new BidTransaction(auctionId, bidderId, amount, bidTime, autoBid);
    }

    public static AutoBidConfig autoBidConfig(String auctionId, String bidderId,
                                               double maxBid, double increment) {
        return new AutoBidConfig(auctionId, bidderId, maxBid, increment);
    }

    public static BidRequest bidRequest(String auctionId, String bidderId,
                                         double amount, boolean autoBid) {
        return new BidRequest(auctionId, bidderId, amount, autoBid);
    }

    // ==================== DTO FACTORY ====================

    public static AuctionDTO auctionDTO(String id, String itemId, String itemName,
                                         String sellerId, String sellerName,
                                         double startingPrice, double currentPrice,
                                         AuctionStatus status,
                                         LocalDateTime startTime, LocalDateTime endTime,
                                         double minIncrement) {
        return new AuctionDTO(id, itemId, itemName, null,
                sellerId, sellerName, startingPrice, currentPrice,
                status, startTime, endTime, minIncrement, false, 0);
    }

    public static AuctionDTO auctionDTORunning() {
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        return auctionDTO(AUCTION_4, ITEM_4, "[ELECTRONICS] iPhone 16 Pro Max",
                USER_SELLER1, USERNAME_SELLER1,
                350.0, 385.0, AuctionStatus.RUNNING, start, end, 5.0);
    }

    public static UserDTO userDTO(String id, String username, String password,
                                   String email, String fullName, String role,
                                   double balance) {
        return new UserDTO(id, username, password, email, fullName, role, balance);
    }

    public static UserDTO userDTOBidder1() {
        return userDTO(USER_BIDDER1, USERNAME_BIDDER1, PASSWORD_DEFAULT,
                "bidder1@auction.com", "Alice Nguyen", "BIDDER", 1000.0);
    }

    public static UserDTO userDTOSeller1() {
        return userDTO(USER_SELLER1, USERNAME_SELLER1, PASSWORD_DEFAULT,
                "seller1@auction.com", "David Le", "SELLER", 0.0);
    }

    public static UserDTO userDTOAdmin() {
        return userDTO(USER_ADMIN, USERNAME_ADMIN, PASSWORD_DEFAULT,
                "admin@auction.com", "Head Administrator", "ADMIN", 0.0);
    }

    public static BidHistoryDTO bidHistoryDTO(String bidderName, double amount,
                                               LocalDateTime timestamp, boolean autoBid) {
        return new BidHistoryDTO(bidderName, amount, timestamp, autoBid);
    }

    // ==================== COMPLETE SCENARIOS ====================

    public static List<User> allUsers() {
        return List.of(
                admin(), admin2(),
                bidder1(), bidder2(), bidder3(), bidder4(), bidder5(),
                seller1(), seller2(), seller3(), seller4()
        );
    }

    public static List<Auction> sampleAuctions() {
        return List.of(
                sampleDraftAuction(),
                sampleRunningAuction(),
                sampleFinishedAuction()
        );
    }

    /**
     * Creates standard test data: 2 sellers, 3 bidders, 1 admin,
     * 1 running auction, 1 draft auction, 1 finished auction.
     */
    public static TestScenario createStandardScenario() {
        return new TestScenario();
    }

    public static class TestScenario {
        public final Admin admin = admin();
        public final Admin admin2 = admin2();
        public final Bidder bidder1 = bidder1();
        public final Bidder bidder2 = bidder2();
        public final Bidder bidder3 = bidder3();
        public final Seller seller1 = seller1();
        public final Seller seller2 = seller2();

        public final Auction draftAuction = sampleDraftAuction();
        public final Auction runningAuction = sampleRunningAuction();
        public final Auction finishedAuction = sampleFinishedAuction();
        public final Auction pendingAuction = pendingAuction(AUCTION_12, ITEM_11, USER_SELLER3, 650.0);

        public final List<Auction> allAuctions = List.of(
                draftAuction, runningAuction, finishedAuction, pendingAuction
        );

        public final List<User> allUsers = List.of(
                admin, admin2, bidder1, bidder2, bidder3, seller1, seller2
        );

        public List<BidTransaction> sampleBidsFor(Auction auction) {
            LocalDateTime now = LocalDateTime.now();
            if (auction.getId().equals(AUCTION_4)) {
                return List.of(
                        bid("tx-001", AUCTION_4, USER_BIDDER1, 355.0, now.minusMinutes(100), false),
                        bid("tx-002", AUCTION_4, USER_BIDDER2, 370.0, now.minusMinutes(80), false),
                        bid("tx-003", AUCTION_4, USER_BIDDER1, 385.0, now.minusMinutes(50), false)
                );
            }
            return new ArrayList<>();
        }
    }
}
