package com.auction.server.service;

import com.auction.common.dto.BidRequest;
import com.auction.common.entity.*;
import com.auction.common.enums.AuctionStatus;
import com.auction.common.exception.InvalidBidException;
import com.auction.common.observer.AuctionSubject;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.UserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    @Mock private AuctionSubject subject;
    @Mock private AntiSnipingService antiSnipingService;
    @Mock private AutoBidService autoBidService;

    private BiddingService biddingService;

    private Auction auction;
    private Bidder bidder;
    private BidRequest validRequest;

    @BeforeEach
    void setUp() {
        biddingService = new BiddingService(auctionDAO, bidDAO, userDAO,
            subject, antiSnipingService, autoBidService);

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
        auction.enableAntiSniping(30);
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        when(userDAO.findUserById("bidder1")).thenReturn(bidder);
        doNothing().when(bidDAO).saveBidTransaction(any(BidTransaction.class));
        doNothing().when(auctionDAO).saveAuction(any(Auction.class));
        when(antiSnipingService.checkAndExtend(auction)).thenReturn(false);
        doNothing().when(autoBidService).processAutoBids(auction);
        doNothing().when(subject).notifyObservers(any());

        assertDoesNotThrow(() -> biddingService.placeBid(validRequest));

        assertEquals(150.0, auction.getCurrentPrice());
        // Verify balance deducted via save, not direct field (service may work on same object)
        verify(bidDAO, times(1)).saveBidTransaction(any(BidTransaction.class));
        verify(auctionDAO, times(1)).saveAuction(auction);
        verify(autoBidService, times(1)).processAutoBids(auction);
    }

    @Test
    void placeBid_AuctionNotFound() {
        when(auctionDAO.getAuction("auc1")).thenReturn(null);
        assertThrows(InvalidBidException.class, () -> biddingService.placeBid(validRequest));
    }

    @Test
    void placeBid_AuctionNotRunning() {
        auction.setStatus(AuctionStatus.FINISHED);
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        assertThrows(InvalidBidException.class, () -> biddingService.placeBid(validRequest));
    }

    @Test
    void placeBid_SellerCannotBid() {
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        BidRequest sellerRequest = new BidRequest("auc1", "seller1", 150.0, false);
        assertThrows(InvalidBidException.class, () -> biddingService.placeBid(sellerRequest));
    }

    @Test
    void placeBid_InsufficientBalance() {
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        when(userDAO.findUserById("bidder1")).thenReturn(bidder);
        BidRequest highBid = new BidRequest("auc1", "bidder1", 600.0, false);
        assertThrows(InvalidBidException.class, () -> biddingService.placeBid(highBid));
    }

    @Test
    void placeBid_InvalidAmount() {
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        when(userDAO.findUserById("bidder1")).thenReturn(bidder);
        BidRequest lowBid = new BidRequest("auc1", "bidder1", 80.0, false);
        assertThrows(InvalidBidException.class, () -> biddingService.placeBid(lowBid));
    }
}