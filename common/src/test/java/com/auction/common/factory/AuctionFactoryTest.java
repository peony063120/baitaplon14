package com.auction.common.factory;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuctionFactory Tests")
class AuctionFactoryTest {

    private final LocalDateTime start = LocalDateTime.now().plusMinutes(5);
    private final LocalDateTime end   = LocalDateTime.now().plusHours(2);

    @Test
    @DisplayName("createStandardAuction: minIncrement mac dinh = 1.0")
    void testCreateStandardAuction() {
        Auction a = AuctionFactory.createStandardAuction("item-1", start, end, 500000);

        assertEquals("item-1", a.getItemId());
        assertEquals(500000, a.getCurrentPrice());
        assertEquals(1.0, a.getMinIncrement());
    }

    @Test
    @DisplayName("createAuctionWithIncrement: minIncrement dung gia tri truyen vao")
    void testCreateAuctionWithIncrement() {
        Auction a = AuctionFactory.creatAuctionWithIncrement("item-2", start, end, 1000000, 50000);

        assertEquals(50000, a.getMinIncrement());
        assertEquals(1000000, a.getCurrentPrice());
    }

    @Test
    @DisplayName("createRunningAuction: status RUNNING, currentWinner duoc set")
    void testCreateRunningAuction() {
        Auction a = AuctionFactory.createRunningAuction("item-3", 2000000, "winner-1", end);

        assertEquals(AuctionStatus.RUNNING, a.getStatus());
        assertEquals("winner-1", a.getCurrentWinnerId());
        assertEquals(2000000, a.getCurrentPrice());
        assertTrue(a.isActive());
    }
}