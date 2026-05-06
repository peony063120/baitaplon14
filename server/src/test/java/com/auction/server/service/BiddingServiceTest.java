package com.auction.server.service;

import com.auction.common.dto.BidRequest;
import com.auction.common.entity.*;
import com.auction.common.enums.AuctionStatus;
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

    // ========== THÀNH CÔNG ==========
    @Test
    void placeBid_Valid() {
        // Mock các dependency cần thiết
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        when(userDAO.findUserById("bidder1")).thenReturn(bidder);
        doNothing().when(bidDAO).saveBidTransaction(any(BidTransaction.class));
        doNothing().when(auctionDAO).saveAuction(any(Auction.class));
        doNothing().when(userDAO).saveUser(any(User.class));
        when(antiSnipingService.checkAndExtend(auction)).thenReturn(false);
        doNothing().when(autoBidService).processAutoBids(auction);

        // Thực thi
        assertDoesNotThrow(() -> biddingService.placeBid(validRequest));

        // Kiểm tra kết quả
        assertEquals(150.0, auction.getCurrentPrice());
        assertEquals(350.0, bidder.getBalance());
        verify(bidDAO, times(1)).saveBidTransaction(any(BidTransaction.class));
        verify(auctionDAO, times(1)).saveAuction(auction);
        verify(userDAO, times(1)).saveUser(bidder);
        verify(autoBidService, times(1)).processAutoBids(auction);
    }

    // ========== LỖI: AUCTION KHÔNG TỒN TẠI ==========
    @Test
    void placeBid_AuctionNotFound() {
        when(auctionDAO.getAuction("auc1")).thenReturn(null);
        // Không cần mock các dependency khác vì sẽ throw ngay
        assertThrows(IllegalArgumentException.class, () -> biddingService.placeBid(validRequest));
    }

    // ========== LỖI: AUCTION KHÔNG CHẠY ==========
    @Test
    void placeBid_AuctionNotRunning() {
        auction.setStatus(AuctionStatus.FINISHED);
        when(auctionDAO.getAuction("auc1")).thenReturn(auction); // ✅ already correct
        // The real fix must be in BiddingService — but if you can't change it,
        // verify what method/field the service uses to look up the auction
        assertThrows(IllegalStateException.class, () -> biddingService.placeBid(validRequest));
    }

    // ========== LỖI: SELLER ĐẶT GIÁ ==========
    @Test
    void placeBid_SellerCannotBid() {
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        // Remove any extra stubs — the service throws before needing userDAO
        BidRequest sellerRequest = new BidRequest("auc1", "seller1", 150.0, false);
        assertThrows(IllegalArgumentException.class, () -> biddingService.placeBid(sellerRequest));
    }

    // ========== LỖI: KHÔNG ĐỦ TIỀN ==========
    @Test
    void placeBid_InsufficientBalance() {
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        when(userDAO.findUserById("bidder1")).thenReturn(bidder);
        // Remove extra stubs — only mock what's needed before the throw
        BidRequest highBid = new BidRequest("auc1", "bidder1", 600.0, false);
        assertThrows(IllegalArgumentException.class, () -> biddingService.placeBid(highBid));
    }

    // ========== LỖI: GIÁ KHÔNG HỢP LỆ ==========
    @Test
    void placeBid_InvalidAmount() {
        when(auctionDAO.getAuction("auc1")).thenReturn(auction);
        when(userDAO.findUserById("bidder1")).thenReturn(bidder);
        BidRequest lowBid = new BidRequest("auc1", "bidder1", 80.0, false);
        assertThrows(IllegalArgumentException.class, () -> biddingService.placeBid(lowBid));
    }
}