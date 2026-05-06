package com.auction.server.service;

import com.auction.common.dto.BidRequest;
import com.auction.common.entity.*;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.exception.InvalidBidException;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BiddingServiceTest {

    @Mock private AuctionDAO auctionDAO;
    @Mock private BidTransactionDAO bidDAO;
    @Mock private UserDAO userDAO;
    @Mock private AntiSnipingService antiSnipingService;
    @Mock private AutoBidService autoBidService;
    @InjectMocks private BiddingService biddingService;

    private Auction auction;
    private Bidder bidder;
    private BidRequest validRequest;

    @BeforeEach
    void setUp() {
        auction = new Auction("item1", "seller1",
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().plusMinutes(30),
                100.0);
        auction.setId("auc1");
        auction.setStatus(AuctionStatus.RUNNING);

        bidder = new Bidder("bidder1", "pass", "b@e.com", "Bidder");
        bidder.addBalance(500.0);

        validRequest = new BidRequest("auc1", "bidder1", 150.0, false);
    }

    @Test
    void placeBid_Valid() {
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        when(userDAO.findUserById("bidder1")).thenReturn(bidder);
        doAnswer(i -> null).when(bidDAO).saveBidTransaction(any());
        doAnswer(i -> null).when(auctionDAO).saveAuction(any());
        doAnswer(i -> null).when(userDAO).saveUser(any());
        when(antiSnipingService.checkAndExtend(auction)).thenReturn(false);

        assertDoesNotThrow(() -> biddingService.placeBid(validRequest));
        assertEquals(150.0, auction.getCurrentPrice());
        assertEquals(350.0, bidder.getBalance());
        verify(bidDAO, times(1)).saveBidTransaction(any());
        verify(auctionDAO, times(1)).saveAuction(auction);
        verify(userDAO, times(1)).saveUser(bidder);
        verify(autoBidService, times(1)).processAutoBids(auction);
    }

    @Test
    void placeBid_AuctionNotRunning() {
        auction.setStatus(AuctionStatus.FINISHED);
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        assertThrows(IllegalStateException.class, () -> biddingService.placeBid(validRequest));
    }

    @Test
    void placeBid_SellerCannotBid() {
        validRequest = new BidRequest("auc1", "seller1", 150.0, false);
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        assertThrows(IllegalArgumentException.class, () -> biddingService.placeBid(validRequest));
    }

    @Test
    void placeBid_InsufficientBalance() {
        bidder.deductBalance(500.0);
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        when(userDAO.findUserById("bidder1")).thenReturn(bidder);
        assertThrows(IllegalArgumentException.class, () -> biddingService.placeBid(validRequest));
    }

    @Test
    void placeBid_InvalidAmount() {
        validRequest = new BidRequest("auc1", "bidder1", 80.0, false);
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        when(userDAO.findUserById("bidder1")).thenReturn(bidder);
        assertThrows(IllegalArgumentException.class, () -> biddingService.placeBid(validRequest));
    }
}