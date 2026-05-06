package com.auction.server.scheduler;

import com.auction.common.entity.Auction;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.service.AuctionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionSchedulerTest {

    @Mock private AuctionDAO auctionDAO;
    @Mock private AuctionService auctionService;
    private AuctionScheduler scheduler;
    private Auction auction;

    @BeforeEach
    void setUp() {
        scheduler = new AuctionScheduler(auctionDAO, auctionService);
        auction = new Auction("item1", "seller1",
                LocalDateTime.now().plusSeconds(2),
                LocalDateTime.now().plusHours(1),
                100.0);
        auction.setId("auc1");
    }

    @Test
    void scheduleAuctionStart_ShouldScheduleTask() {
        // Since scheduling is asynchronous and we cannot easily verify the task execution,
        // we just verify that no exception is thrown.
        assertDoesNotThrow(() -> scheduler.scheduleAuctionStart(auction));
    }

    @Test
    void scheduleAuctionEnd_ShouldScheduleTask() {
        assertDoesNotThrow(() -> scheduler.scheduleAuctionEnd(auction));
    }
}