package com.auction.server.service;

import com.auction.common.entity.Auction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AntiSnipingServiceTest {

    private AntiSnipingService service;
    private Auction auction;

    @BeforeEach
    void setUp() {
        service = new AntiSnipingService();
        auction = new Auction("item1", "seller1",
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusSeconds(5),
                100.0);
        auction.setId("auc1");
        auction.setAntiSnipingEnabled(true);
        auction.setAntiSnipingExtensionSeconds(10);
    }

    @Test
    void checkAndExtend_ShouldExtendWhenNearEnd() {
        boolean extended = service.checkAndExtend(auction);
        assertTrue(extended);
    }

    @Test
    void checkAndExtend_NotEnable() {
        auction.setAntiSnipingEnabled(false);
        boolean extended = service.checkAndExtend(auction);
        assertFalse(extended);
    }

    @Test
    void getRemainingExtensionSeconds() {
        long remaining = service.getRemainingExtensionSeconds(auction);
        assertTrue(remaining >= 0);
    }
}