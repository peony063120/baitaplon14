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
class EndAuctionTaskTest {

    @Mock private AuctionDAO auctionDAO;
    @Mock private AuctionService auctionService;
    private EndAuctionTask task;
    private Auction auction;

    @BeforeEach
    void setUp() {
        auction = new Auction("item1", "seller1",
                java.time.LocalDateTime.now().minusHours(1),
                java.time.LocalDateTime.now().minusSeconds(10),
                100.0);
        auction.setId("auc1");
        auction.setStatus(AuctionStatus.RUNNING);
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        task = new EndAuctionTask(auctionDAO, auctionService, "auc1");
    }

    @Test
    void run_ShouldChangeStatusToFinishedAndNotify() {
        task.run();
        verify(auctionDAO).saveAuction(auction);
        verify(auctionService).notifyAuctionEnded(auction);
        assertEquals(AuctionStatus.FINISHED, auction.getStatus());
    }
}