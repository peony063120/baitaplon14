package com.auction.common.entity;

import com.auction.common.enums.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Auction Tests")
class AuctionTest {

    private Auction auction;

    @BeforeEach
    void setUp() {
        // sellerId = "seller-1", RUNNING, thoi gian hop le
        auction = new Auction(
                "item-1", "seller-1",
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusHours(1),
                1000000
        );
        auction.setStatus(AuctionStatus.RUNNING);
        auction.setMinIncrement(100000);
    }

    // ======== isActive ========

    @Test
    @DisplayName("isActive: RUNNING + trong thoi gian -> true")
    void testIsActive_running() {
        assertTrue(auction.isActive());
    }

    @Test
    @DisplayName("isActive: DRAFT -> false")
    void testIsActive_draft() {
        auction.setStatus(AuctionStatus.DRAFT);
        assertFalse(auction.isActive());
    }

    @Test
    @DisplayName("isActive: FINISHED -> false")
    void testIsActive_finished() {
        auction.setStatus(AuctionStatus.FINISHED);
        assertFalse(auction.isActive());
    }

    @Test
    @DisplayName("isActive: RUNNING nhung da het gio -> false")
    void testIsActive_expired() {
        Auction expired = new Auction(
                "item-2", "seller-1",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusMinutes(1), // da het
                1000000
        );
        expired.setStatus(AuctionStatus.RUNNING);
        assertFalse(expired.isActive());
    }

    // ======== canBid ========

    @Test
    @DisplayName("canBid: phien active + user khac seller -> true")
    void testCanBid_valid() {
        assertTrue(auction.canBid("bidder-1"));
    }

    @Test
    @DisplayName("canBid: user la seller -> false")
    void testCanBid_seller() {
        assertFalse(auction.canBid("seller-1"));
    }

    @Test
    @DisplayName("canBid: phien khong active -> false")
    void testCanBid_notActive() {
        auction.setStatus(AuctionStatus.FINISHED);
        assertFalse(auction.canBid("bidder-1"));
    }

    // ======== addBid ========

    @Test
    @DisplayName("addBid: gia hop le -> tra true, cap nhat currentPrice va currentWinnerId")
    void testAddBid_valid() {
        BidTransaction bid = new BidTransaction(
                auction.getId(), "bidder-1", 1200000,
                LocalDateTime.now(), false
        );
        boolean result = auction.addBid(bid);

        assertTrue(result);
        assertEquals(1200000, auction.getCurrentPrice());
        assertEquals("bidder-1", auction.getCurrentWinnerId());
    }

    @Test
    @DisplayName("addBid: gia thap hon currentPrice -> tra false, khong cap nhat")
    void testAddBid_priceTooLow() {
        BidTransaction bid = new BidTransaction(
                auction.getId(), "bidder-1", 900000,
                LocalDateTime.now(), false
        );
        boolean result = auction.addBid(bid);

        assertFalse(result);
        assertEquals(1000000, auction.getCurrentPrice()); // khong doi
    }

    @Test
    @DisplayName("addBid: gia bang currentPrice -> tra false")
    void testAddBid_priceEqual() {
        BidTransaction bid = new BidTransaction(
                auction.getId(), "bidder-1", 1000000,
                LocalDateTime.now(), false
        );
        assertFalse(auction.addBid(bid));
    }

    @Test
    @DisplayName("addBid: them vao bidHistory khi hop le")
    void testAddBid_addsToBidHistory() {
        BidTransaction bid = new BidTransaction(
                auction.getId(), "bidder-1", 1200000,
                LocalDateTime.now(), false
        );
        auction.addBid(bid);
        assertEquals(1, auction.getBidHistory().size());
    }

    @Test
    @DisplayName("addBid: bidder la seller -> tra false")
    void testAddBid_sellerCannotBid() {
        BidTransaction bid = new BidTransaction(
                auction.getId(), "seller-1", 1200000,
                LocalDateTime.now(), false
        );
        assertFalse(auction.addBid(bid));
    }

    // ======== extendEndTime ========

    @Test
    @DisplayName("extendEndTime: gia han dung so giay")
    void testExtendEndTime() {
        LocalDateTime before = auction.getEndTime();
        auction.extendEndTime(60);
        assertEquals(before.plusSeconds(60), auction.getEndTime());
    }

    // ======== enableAntiSniping ========

    @Test
    @DisplayName("enableAntiSniping: bat anti-sniping va set extension seconds")
    void testEnableAntiSniping() {
        auction.enableAntiSniping(60);
        assertTrue(auction.isAntiSnipingEnabled());
        assertEquals(60, auction.getAntiSnipingExtensionSeconds());
    }

    @Test
    @DisplayName("addBid voi anti-sniping: bid gan het gio -> tu dong gia han")
    void testAddBid_antiSniping_extendsTime() {
        // Phien ket thuc sau 20 giay
        Auction nearEndAuction = new Auction(
                "item-3", "seller-1",
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusSeconds(20),
                1000000
        );
        nearEndAuction.setStatus(AuctionStatus.RUNNING);
        nearEndAuction.enableAntiSniping(60); // gia han 60 giay

        LocalDateTime endBefore = nearEndAuction.getEndTime();

        BidTransaction bid = new BidTransaction(
                nearEndAuction.getId(), "bidder-1", 1200000,
                LocalDateTime.now(), false
        );
        nearEndAuction.addBid(bid);

        assertTrue(nearEndAuction.getEndTime().isAfter(endBefore));
    }
}