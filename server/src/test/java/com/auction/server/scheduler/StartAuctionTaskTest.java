package com.auction.server.scheduler;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.service.AuctionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartAuctionTaskTest {

    @Mock private AuctionDAO auctionDAO;
    @Mock private AuctionService auctionService;
    private StartAuctionTask task;
    private Auction auction;

    @BeforeEach
    void setUp() {
        auction = new Auction("item1", "seller1",
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now().plusHours(1),
                100.0);
        auction.setId("auc1");
        auction.setStatus(AuctionStatus.OPEN);
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        task = new StartAuctionTask(auctionDAO, auctionService, "auc1");
    }

    @Test
    void run_ShouldChangeStatusToRunningAndNotify() {
        task.run();
        verify(auctionDAO).saveAuction(auction);
        verify(auctionService).notifyAuctionStarted(auction);
        assertEquals(AuctionStatus.RUNNING, auction.getStatus());
    }

    @Test
    void run_WithDraftStatus_ShouldAlsoStart() {
        auction.setStatus(AuctionStatus.DRAFT);
        task.run();
        assertEquals(AuctionStatus.RUNNING, auction.getStatus());
    }
}