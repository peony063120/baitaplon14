package com.auction.server.scheduler;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.service.AutoBidService;
import com.auction.server.service.BiddingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoBidProcessorTest {

    @Mock private AuctionDAO auctionDAO;
    @Mock private AutoBidService autoBidService;
    @Mock private BiddingService biddingService;
    private AutoBidProcessor processor;

    @BeforeEach
    void setUp() {
        // Dùng constructor test thay vì getInstance()
        processor = new AutoBidProcessor(auctionDAO);
        processor.setAutoBidService(autoBidService);   // inject qua setter
        processor.setBiddingService(biddingService);   // inject qua setter
    }

    @Test
    void processAllActiveAuctions_ShouldProcessRunningAuctions() {
        Auction runningAuction = new Auction("item1", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 100.0);
        runningAuction.setId("auc1");
        runningAuction.setStatus(AuctionStatus.RUNNING);

        Auction finishedAuction = new Auction("item2", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 200.0);
        finishedAuction.setId("auc2");
        finishedAuction.setStatus(AuctionStatus.FINISHED);

        when(auctionDAO.getAuctionsByStatus(AuctionStatus.RUNNING)).thenReturn(List.of(runningAuction));

        processor.processAllActiveAuctions();

        verify(auctionDAO, times(1)).getAuctionsByStatus(AuctionStatus.RUNNING);
        // processAutoBids nhận 2 tham số: (auction, biddingService)
        verify(autoBidService, times(1)).processAutoBids(runningAuction, biddingService);
        verify(autoBidService, never()).processAutoBids(eq(finishedAuction), any());
    }

    @Test
    void processAllActiveAuctions_NoRunningAuctions() {
        when(auctionDAO.getAuctionsByStatus(AuctionStatus.RUNNING)).thenReturn(List.of());

        processor.processAllActiveAuctions();

        verify(auctionDAO, times(1)).getAuctionsByStatus(AuctionStatus.RUNNING);
        verify(autoBidService, never()).processAutoBids(any(), any());
    }

    @AfterEach
    void tearDown() {
        processor.shutdown();
    }
}