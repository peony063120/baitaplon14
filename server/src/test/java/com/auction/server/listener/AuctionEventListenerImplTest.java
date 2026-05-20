package com.auction.server.listener;

import com.auction.common.entity.Auction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuctionEventListenerImplTest {

    private AuctionEventListenerImpl listener;
    private Auction testAuction;

    @BeforeEach
    void setUp() {
        listener = new AuctionEventListenerImpl();
        testAuction = new Auction("item1", "seller1",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                100.0);
        testAuction.setId("auc1");
    }

    @Test
    void onAuctionStart_ShouldNotThrowException() {
        assertDoesNotThrow(() -> listener.onAuctionStart(testAuction));
    }

    @Test
    void onAuctionEnd_ShouldNotThrowException() {
        assertDoesNotThrow(() -> listener.onAuctionEnd(testAuction));
    }
}