package com.auction.server.controller;

import com.auction.common.dto.AutoBidRequest;
import com.auction.common.dto.BidRequest;
import com.auction.common.entity.BidTransaction;
import com.auction.common.exception.InvalidBidException;
import com.auction.server.service.BiddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidControllerTest {

    @Mock private BiddingService biddingService;
    @InjectMocks private BidController bidController;

    private BidRequest bidRequest;
    private AutoBidRequest autoBidRequest;
    private BidTransaction bidTransaction;

    @BeforeEach
    void setUp() {
        bidRequest = new BidRequest("auc1", "bidder1", 150.0, false);
        autoBidRequest = new AutoBidRequest("auc1", "bidder1", 200.0, 10.0, true);
        bidTransaction = new BidTransaction("tx1", "auc1", "bidder1", 150.0, LocalDateTime.now(), false);
    }

    @Test
    void placeBid_ShouldCallService() throws InvalidBidException {
        doNothing().when(biddingService).placeBid(bidRequest);
        bidController.placeBid(bidRequest);
        verify(biddingService, times(1)).placeBid(bidRequest);
    }

    @Test
    void getBidHistory_ShouldReturnList() {
        when(biddingService.getBidHistory("auc1")).thenReturn(List.of(bidTransaction));
        List<BidTransaction> result = bidController.getBidHistory("auc1");
        assertEquals(1, result.size());
        assertEquals("auc1", result.get(0).getAuctionId());
    }

    @Test
    void configureAutoBid_ShouldCallService() {
        doNothing().when(biddingService).configureAutoBid(autoBidRequest);
        bidController.configureAutoBid(autoBidRequest);
        verify(biddingService, times(1)).configureAutoBid(autoBidRequest);
    }

    @Test
    void cancelAutoBid_ShouldCallService() {
        doNothing().when(biddingService).cancelAutoBid("auc1", "bidder1");
        bidController.cancelAutoBid("auc1", "bidder1");
        verify(biddingService, times(1)).cancelAutoBid("auc1", "bidder1");
    }

    @Test
    void placeBid_ShouldThrowException_WhenBidIsInvalid() throws InvalidBidException {
        doThrow(new InvalidBidException("Mức giá không hợp lệ"))
            .when(biddingService).placeBid(bidRequest);

        assertThrows(InvalidBidException.class, () -> {
            bidController.placeBid(bidRequest);
        });
    }
}