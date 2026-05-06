package com.auction.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PriceUtils Tests")
class PriceUtilsTest {

    // ======== calculateNextBid ========

    @Test
    @DisplayName("Next bid = currentPrice + increment")
    void testCalculateNextBid() {
        assertEquals(1100000, PriceUtils.calculateNextBid(1000000, 100000));
        assertEquals(150.0, PriceUtils.calculateNextBid(100.0, 50.0));
    }

    // ======== calculateAutoBid ========

    @Test
    @DisplayName("Auto bid: next < maxBid -> tra ve next")
    void testCalculateAutoBid_nextBelowMax() {
        double result = PriceUtils.calculateAutoBid(1000, 100, 2000);
        assertEquals(1100, result);
    }

    @Test
    @DisplayName("Auto bid: next > maxBid -> tra ve maxBid")
    void testCalculateAutoBid_nextExceedsMax() {
        double result = PriceUtils.calculateAutoBid(1900, 200, 2000);
        assertEquals(2000, result);
    }

    @Test
    @DisplayName("Auto bid: next = maxBid -> tra ve maxBid")
    void testCalculateAutoBid_nextEqualsMax() {
        double result = PriceUtils.calculateAutoBid(1800, 200, 2000);
        assertEquals(2000, result);
    }

    // ======== canAutoBid ========

    @Test
    @DisplayName("canAutoBid: next <= maxBid -> true")
    void testCanAutoBid_true() {
        assertTrue(PriceUtils.canAutoBid(1000, 100, 2000));
        assertTrue(PriceUtils.canAutoBid(1900, 100, 2000)); // next == maxBid
    }

    @Test
    @DisplayName("canAutoBid: next > maxBid -> false")
    void testCanAutoBid_false() {
        assertFalse(PriceUtils.canAutoBid(1950, 100, 2000));
        assertFalse(PriceUtils.canAutoBid(2000, 100, 2000));
    }

    // ======== round ========

    @Test
    @DisplayName("Round 2 decimal places")
    void testRound() {
        assertEquals(1.23, PriceUtils.round(1.234));
        assertEquals(1.24, PriceUtils.round(1.235));
        assertEquals(100.0, PriceUtils.round(100.0));
    }

    // ======== formatPlain ========

    @Test
    @DisplayName("formatPlain tra ve 2 chu so thap phan")
    void testFormatPlain() {
        assertEquals("1000000.00", PriceUtils.formatPlain(1000000));
        assertEquals("99.99", PriceUtils.formatPlain(99.99));
    }
}