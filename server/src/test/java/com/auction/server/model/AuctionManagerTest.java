package com.auction.server.model;

import com.auction.common.entity.Auction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionManagerTest {

    private AuctionManager manager;
    private Auction testAuction;

    @BeforeEach
    void setUp() {
        manager = AuctionManager.getInstance();
        testAuction = new Auction("item1", "seller1",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                100.0);
        testAuction.setId("auc1");
    }

    @Test
    void testGetInstance_ShouldReturnSameInstance() {
        AuctionManager manager2 = AuctionManager.getInstance();
        assertSame(manager, manager2);
    }

    @Test
    void testStartAuction_ShouldAddToRunningAuctions() {
        manager.startAuction(testAuction);
        Auction running = manager.getRunningAuction("auc1");
        assertNotNull(running);
        assertEquals("auc1", running.getId());
    }

    @Test
    void testEndAuction_ShouldRemoveFromRunningAuctions() {
        manager.startAuction(testAuction);
        manager.endAuction("auc1");
        Auction ended = manager.getRunningAuction("auc1");
        assertNull(ended);
    }
}