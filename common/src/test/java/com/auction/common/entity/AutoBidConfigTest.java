package com.auction.common.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AutoBidConfig Tests")
class AutoBidConfigTest {

    private AutoBidConfig config;

    @BeforeEach
    void setUp() {
        // maxBid = 2000000, increment = 100000
        config = new AutoBidConfig("auction-1", "bidder-1", 2000000, 100000);
    }

    // ======== getNextBid ========

    @Test
    @DisplayName("getNextBid: luon tra ve currentPrice + increment")
    void testGetNextBid_normal() {
        assertEquals(1100000, config.getNextBid(1000000));
    }

    @Test
    @DisplayName("getNextBid: khong check maxBid, chi cong increment")
    void testGetNextBid_doesNotCheckMax() {
        // getNextBid chi cong increment, khong check maxBid
        assertEquals(2100000, config.getNextBid(2000000));
    }

    @Test
    @DisplayName("getNextBid: currentPrice = 0")
    void testGetNextBid_zeroCurrentPrice() {
        assertEquals(100000, config.getNextBid(0));
    }

    // ======== canBid ========

    @Test
    @DisplayName("canBid: currentPrice + increment <= maxBid -> true")
    void testCanBid_true() {
        // 1000000 + 100000 = 1100000 <= 2000000
        assertTrue(config.canBid(1000000));
    }

    @Test
    @DisplayName("canBid: currentPrice + increment == maxBid -> true (bang nhau van duoc)")
    void testCanBid_equalsMax() {
        // 1900000 + 100000 = 2000000 <= 2000000
        assertTrue(config.canBid(1900000));
    }

    @Test
    @DisplayName("canBid: currentPrice + increment > maxBid -> false")
    void testCanBid_exceedsMax() {
        // 1950000 + 100000 = 2050000 > 2000000
        assertFalse(config.canBid(1950000));
    }

    @Test
    @DisplayName("canBid: inactive -> false")
    void testCanBid_inactive() {
        config.setActive(false);
        assertFalse(config.canBid(1000000));
    }

    // ======== constructor & defaults ========

    @Test
    @DisplayName("Constructor: active = true, createdAt duoc set")
    void testConstructor_defaults() {
        assertTrue(config.isActive());
        assertNotNull(config.getCreatedAt());
        assertNotNull(config.getId());
        assertEquals("auction-1", config.getAuctionId());
        assertEquals("bidder-1", config.getBidderId());
    }
}