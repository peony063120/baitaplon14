package com.auction.server.model;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PriceCalculatorTest {

    private PriceCalculator calculator;
    private Auction testAuction;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
        testAuction = new Auction("item1", "seller1",
                LocalDateTime.now().minusMinutes(1),  // Đã bắt đầu
                LocalDateTime.now().plusDays(1),
                100.0);
        testAuction.setId("auc1");
        testAuction.setStatus(AuctionStatus.RUNNING);  // THÊM DÒNG NÀY
        testAuction.setMinIncrement(10.0);
    }

    @Test
    void testCalculateNextBid() {
        double nextBid = PriceCalculator.calculateNextBid(100.0, 10.0);
        // Giá tăng: max(10, 100*0.01=1) = 10 → 100 + 10 = 110
        assertEquals(110.0, nextBid, 0.01);
    }

    @Test
    void testIsBidValid_ValidBid() {
        // currentPrice = 100, currentWinnerId = null → minAllowed = 100
        // bidAmount = 150 >= 100 → true
        boolean valid = PriceCalculator.isBidValid(testAuction, 150.0);
        assertTrue(valid);
    }

    @Test
    void testIsBidValid_InvalidBidTooLow() {
        // currentPrice = 100, currentWinnerId = null → minAllowed = 100
        // bidAmount = 95 < 100 → false
        boolean valid = PriceCalculator.isBidValid(testAuction, 95.0);
        assertFalse(valid);
    }

    @Test
    void testIsBidValid_WhenHasWinner() {
        testAuction.setCurrentWinnerId("bidder1");
        testAuction.setCurrentPrice(100.0);
        testAuction.setMinIncrement(10.0);
        // Có winner → cần bidAmount >= 100 + 10 = 110

        assertTrue(PriceCalculator.isBidValid(testAuction, 110.0));
        assertFalse(PriceCalculator.isBidValid(testAuction, 109.99));
    }

    @Test
    void testIsBidValid_AuctionNotActive() {
        testAuction.setStatus(AuctionStatus.FINISHED);
        boolean valid = PriceCalculator.isBidValid(testAuction, 200.0);
        assertFalse(valid);
    }
}