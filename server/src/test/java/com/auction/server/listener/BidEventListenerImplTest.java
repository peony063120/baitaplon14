package com.auction.server.listener;

import com.auction.common.entity.AutoBidConfig;
import com.auction.common.entity.BidTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BidEventListenerImplTest {

    private BidEventListenerImpl listener;
    private BidTransaction testBid;

    @BeforeEach
    void setUp() {
        listener = new BidEventListenerImpl();
        testBid = new BidTransaction("auc1", "bidder1", 150.0, LocalDateTime.now(), false);
    }

    @Test
    void onBidPlaced_ShouldNotThrowException() {
        assertDoesNotThrow(() -> listener.onBidPlaced(testBid));
    }

    @Test
    void onAutoBidTriggered_ShouldNotThrowException() {
        AutoBidConfig config = new AutoBidConfig("auc1", "bidder1", 500.0, 10.0);
        assertDoesNotThrow(() -> listener.onAutoBidTriggered(config));
    }
}