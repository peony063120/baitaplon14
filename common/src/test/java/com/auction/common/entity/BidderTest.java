package com.auction.common.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bidder Tests")
class BidderTest {

    private Bidder bidder;

    @BeforeEach
    void setUp() {
        bidder = new Bidder("alice", "pass123", "alice@email.com", "Alice");
    }

    // ======== addBalance ========

    @Test
    @DisplayName("addBalance: amount > 0 -> balance tang")
    void testAddBalance_positive() {
        bidder.addBalance(1000000);
        assertEquals(1000000, bidder.getBalance());
    }

    @Test
    @DisplayName("addBalance: goi nhieu lan -> cong don")
    void testAddBalance_multiple() {
        bidder.addBalance(1000000);
        bidder.addBalance(500000);
        assertEquals(1500000, bidder.getBalance());
    }

    @Test
    @DisplayName("addBalance: amount <= 0 -> bo qua, balance khong doi")
    void testAddBalance_nonPositive() {
        bidder.addBalance(1000000);
        bidder.addBalance(0);
        bidder.addBalance(-500);
        assertEquals(1000000, bidder.getBalance()); // van giu nguyen
    }

    // ======== deductBalance ========

    @Test
    @DisplayName("deductBalance: du tien -> tru thanh cong, tra true")
    void testDeductBalance_success() {
        bidder.addBalance(1000000);
        boolean result = bidder.deductBalance(500000);
        assertTrue(result);
        assertEquals(500000, bidder.getBalance());
    }

    @Test
    @DisplayName("deductBalance: vua dung bang balance -> thanh cong, ve 0")
    void testDeductBalance_exactAmount() {
        bidder.addBalance(500000);
        boolean result = bidder.deductBalance(500000);
        assertTrue(result);
        assertEquals(0, bidder.getBalance());
    }

    @Test
    @DisplayName("deductBalance: khong du tien -> tra false, balance khong doi")
    void testDeductBalance_insufficient() {
        bidder.addBalance(100000);
        boolean result = bidder.deductBalance(500000);
        assertFalse(result);
        assertEquals(100000, bidder.getBalance());
    }

    @Test
    @DisplayName("deductBalance: amount <= 0 -> tra false")
    void testDeductBalance_nonPositive() {
        bidder.addBalance(1000000);
        assertFalse(bidder.deductBalance(0));
        assertFalse(bidder.deductBalance(-100));
        assertEquals(1000000, bidder.getBalance()); // khong doi
    }

    // ======== getRole ========

    @Test
    @DisplayName("getRole tra ve BIDDER")
    void testGetRole() {
        assertEquals("BIDDER", bidder.getRole());
    }

    // ======== autoBidConfig ========

    @Test
    @DisplayName("setAutoBidConfig va getAutoBidConfig: luu va lay dung config")
    void testSetAndGetAutoBidConfig() {
        AutoBidConfig config = new AutoBidConfig("auction-1", "alice", 2000000, 100000);
        bidder.setAutoBidConfig("auction-1", config);

        AutoBidConfig result = bidder.getAutoBidConfig("auction-1");
        assertNotNull(result);
        assertEquals(2000000, result.getMaxBid());
        assertEquals(100000, result.getIncrement());
    }

    @Test
    @DisplayName("getAutoBidConfig: auction chua dang ky -> null")
    void testGetAutoBidConfig_notFound() {
        assertNull(bidder.getAutoBidConfig("unknown-auction"));
    }

    @Test
    @DisplayName("removeAutoBidConfig: xoa xong getAutoBidConfig tra null")
    void testRemoveAutoBidConfig() {
        AutoBidConfig config = new AutoBidConfig("auction-1", "alice", 2000000, 100000);
        bidder.setAutoBidConfig("auction-1", config);
        bidder.removeAutoBidConfig("auction-1");

        assertNull(bidder.getAutoBidConfig("auction-1"));
    }

    @Test
    @DisplayName("Constructor 4 tham so: balance mac dinh = 0")
    void testConstructor_defaultBalance() {
        assertEquals(0.0, bidder.getBalance());
    }

    @Test
    @DisplayName("Constructor 5 tham so: balance duoc set")
    void testConstructor_withBalance() {
        Bidder b = new Bidder("bob", "pass", "bob@email.com", "Bob", 500000);
        assertEquals(500000, b.getBalance());
    }
}