package com.auction.common.strategy;

import com.auction.common.dto.BidRequest;
import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BiddingStrategy Tests")
class BiddingStrategyTest {

    private Auction runningAuction;

    @BeforeEach
    void setUp() {
        runningAuction = new Auction(
                "item-1", "seller-1",
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusHours(1),
                1000000
        );
        runningAuction.setStatus(AuctionStatus.RUNNING);
        runningAuction.setMinIncrement(100000);
    }

    // ======== NormalBiddingStrategy ========

    @Test
    @DisplayName("Normal: gia >= currentPrice + minIncrement -> true")
    void testNormal_validBid() {
        NormalBiddingStrategy strategy = new NormalBiddingStrategy();
        // 1000000 + 100000 = 1100000 -> can dat it nhat 1100000
        BidRequest req = new BidRequest("auction-1", "bidder-1", 1100000, false);

        assertTrue(strategy.execute(runningAuction, req));
    }

    @Test
    @DisplayName("Normal: gia < currentPrice + minIncrement -> false")
    void testNormal_belowMinIncrement() {
        NormalBiddingStrategy strategy = new NormalBiddingStrategy();
        BidRequest req = new BidRequest("auction-1", "bidder-1", 1050000, false);

        assertFalse(strategy.execute(runningAuction, req));
    }

    @Test
    @DisplayName("Normal: gia bang currentPrice -> false")
    void testNormal_equalCurrentPrice() {
        NormalBiddingStrategy strategy = new NormalBiddingStrategy();
        BidRequest req = new BidRequest("auction-1", "bidder-1", 1000000, false);

        assertFalse(strategy.execute(runningAuction, req));
    }

    @Test
    @DisplayName("Normal: phien FINISHED -> false")
    void testNormal_auctionNotRunning() {
        runningAuction.setStatus(AuctionStatus.FINISHED);
        NormalBiddingStrategy strategy = new NormalBiddingStrategy();
        BidRequest req = new BidRequest("auction-1", "bidder-1", 1200000, false);

        assertFalse(strategy.execute(runningAuction, req));
    }

    @Test
    @DisplayName("Normal: bidder la seller -> false")
    void testNormal_sellerCannotBid() {
        NormalBiddingStrategy strategy = new NormalBiddingStrategy();
        BidRequest req = new BidRequest("auction-1", "seller-1", 1200000, false);

        assertFalse(strategy.execute(runningAuction, req));
    }

    @Test
    @DisplayName("Normal: sau khi execute, currentPrice va currentWinnerId duoc cap nhat")
    void testNormal_updatesAuctionState() {
        NormalBiddingStrategy strategy = new NormalBiddingStrategy();
        BidRequest req = new BidRequest("auction-1", "bidder-1", 1200000, false);

        strategy.execute(runningAuction, req);

        assertEquals(1200000, runningAuction.getCurrentPrice());
        assertEquals("bidder-1", runningAuction.getCurrentWinnerId());
    }

    // ======== AntiSnipingStrategy ========

    @Test
    @DisplayName("AntiSniping: bid trong 30 giay cuoi -> gia han phien")
    void testAntiSniping_extendsTime() {
        Auction nearEnd = new Auction(
                "item-2", "seller-1",
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusSeconds(20), // con 20 giay < nguong 30s
                1000000
        );
        nearEnd.setStatus(AuctionStatus.RUNNING);
        nearEnd.setMinIncrement(100000);
        nearEnd.enableAntiSniping(60); // gia han 60 giay

        LocalDateTime endBefore = nearEnd.getEndTime();
        AntiSnipingStrategy strategy = new AntiSnipingStrategy();
        BidRequest req = new BidRequest("auction-2", "bidder-1", 1200000, false);

        strategy.execute(nearEnd, req);

        assertTrue(nearEnd.getEndTime().isAfter(endBefore));
    }

    @Test
    @DisplayName("AntiSniping: bid ngoai nguong 30 giay -> khong gia han")
    void testAntiSniping_noExtension() {
        // runningAuction con 1 tieng -> ngoai nguong 30 giay
        runningAuction.enableAntiSniping(60);

        LocalDateTime endBefore = runningAuction.getEndTime();
        AntiSnipingStrategy strategy = new AntiSnipingStrategy();
        BidRequest req = new BidRequest("auction-1", "bidder-1", 1200000, false);

        strategy.execute(runningAuction, req);

        assertEquals(endBefore, runningAuction.getEndTime()); // khong doi
    }

    @Test
    @DisplayName("AntiSniping: bid khong hop le -> tra false, khong gia han")
    void testAntiSniping_invalidBid_noExtension() {
        Auction nearEnd = new Auction(
                "item-3", "seller-1",
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusSeconds(20),
                1000000
        );
        nearEnd.setStatus(AuctionStatus.RUNNING);
        nearEnd.setMinIncrement(100000);
        nearEnd.enableAntiSniping(60);

        LocalDateTime endBefore = nearEnd.getEndTime();
        AntiSnipingStrategy strategy = new AntiSnipingStrategy();
        // Gia thap hon muc toi thieu
        BidRequest req = new BidRequest("auction-3", "bidder-1", 500000, false);

        boolean result = strategy.execute(nearEnd, req);

        assertFalse(result);
        assertEquals(endBefore, nearEnd.getEndTime()); // khong gia han
    }

    @Test
    @DisplayName("AntiSniping: anti-sniping disabled -> khong gia han dù bid phut chot")
    void testAntiSniping_disabled_noExtension() {
        Auction nearEnd = new Auction(
                "item-4", "seller-1",
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusSeconds(20),
                1000000
        );
        nearEnd.setStatus(AuctionStatus.RUNNING);
        nearEnd.setMinIncrement(100000);
        // KHONG goi enableAntiSniping

        LocalDateTime endBefore = nearEnd.getEndTime();
        AntiSnipingStrategy strategy = new AntiSnipingStrategy();
        BidRequest req = new BidRequest("auction-4", "bidder-1", 1200000, false);

        strategy.execute(nearEnd, req);

        assertEquals(endBefore, nearEnd.getEndTime());
    }
}