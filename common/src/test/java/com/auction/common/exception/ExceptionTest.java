package com.auction.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Custom Exception Tests")
class ExceptionTest {

    // ======== AuctionNotFoundException ========

    @Test
    @DisplayName("AuctionNotFoundException: message chua auction id")
    void testAuctionNotFoundException() {
        AuctionNotFoundException ex = new AuctionNotFoundException("auction-123");
        assertTrue(ex.getMessage().contains("auction-123"));
    }

    @Test
    @DisplayName("AuctionNotFoundException: constructor voi custom message")
    void testAuctionNotFoundException_customMessage() {
        AuctionNotFoundException ex = new AuctionNotFoundException("auction-123", "Custom error");
        assertTrue(ex.getMessage().contains("auction-123"));
        assertTrue(ex.getMessage().contains("Custom error"));
    }

    // ======== AuthenticationException (static factory methods) ========

    @Test
    @DisplayName("AuthenticationException.invalidCredentials: message dung")
    void testInvalidCredentials() {
        AuthenticationException ex = AuthenticationException.invalidCredentials();
        assertTrue(ex.getMessage().contains("Invalid username or password"));
    }

    @Test
    @DisplayName("AuthenticationException.userAlreadyExists: message chua username")
    void testUserAlreadyExists() {
        AuthenticationException ex = AuthenticationException.userAlreadyExists("alice");
        assertTrue(ex.getMessage().contains("alice"));
    }

    @Test
    @DisplayName("AuthenticationException.sessionExpired: message dung")
    void testSessionExpired() {
        AuthenticationException ex = AuthenticationException.sessionExpired();
        assertTrue(ex.getMessage().contains("Session has expired"));
    }

    @Test
    @DisplayName("AuthenticationException.unauthorizedRole: message chua role")
    void testUnauthorizedRole() {
        AuthenticationException ex = AuthenticationException.unauthorizedRole("ADMIN");
        assertTrue(ex.getMessage().contains("ADMIN"));
    }

    // ======== ConcurrentBidException (static factory methods) ========

    @Test
    @DisplayName("ConcurrentBidException.forAuction: message chua auctionId, bidderId, amount")
    void testConcurrentBidException_forAuction() {
        ConcurrentBidException ex = ConcurrentBidException.forAuction("auction-1", "bidder-1", 1200000);
        assertTrue(ex.getMessage().contains("auction-1"));
        assertTrue(ex.getMessage().contains("bidder-1"));
        assertTrue(ex.getMessage().contains("1200000"));
    }

    @Test
    @DisplayName("ConcurrentBidException.lostUpdate: message chua expected va actual price")
    void testConcurrentBidException_lostUpdate() {
        ConcurrentBidException ex = ConcurrentBidException.lostUpdate("auction-1", "bidder-1", 1000000, 1200000);
        assertTrue(ex.getMessage().contains("1000000"));
        assertTrue(ex.getMessage().contains("1200000"));
    }

    // ======== InsufficientBalanceException ========

    @Test
    @DisplayName("InsufficientBalanceException: message chua userId, required, available")
    void testInsufficientBalanceException() {
        InsufficientBalanceException ex = new InsufficientBalanceException("user-1", 2000000, 500000);
        assertTrue(ex.getMessage().contains("user-1"));
        assertTrue(ex.getMessage().contains("2000000"));
        assertTrue(ex.getMessage().contains("500000"));
    }

    // ======== InvalidBidException ========

    @Test
    @DisplayName("InvalidBidException: message chua thong tin bid")
    void testInvalidBidException() {
        InvalidBidException ex = new InvalidBidException("auction-1", 1000000, 900000, 100000);
        assertTrue(ex.getMessage().contains("auction-1"));
        assertTrue(ex.getMessage().contains("1000000"));
        assertTrue(ex.getMessage().contains("900000"));
    }

    // ======== Inheritance check ========

    @Test
    @DisplayName("AuctionNotFoundException la subclass cua AuctionException")
    void testInheritance_auctionNotFound() {
        assertInstanceOf(AuctionException.class, new AuctionNotFoundException("x"));
    }

    @Test
    @DisplayName("InvalidBidException la subclass cua AuctionException")
    void testInheritance_invalidBid() {
        assertInstanceOf(AuctionException.class, new InvalidBidException("msg"));
    }

    @Test
    @DisplayName("ConcurrentBidException la subclass cua AuctionException")
    void testInheritance_concurrentBid() {
        assertInstanceOf(AuctionException.class, new ConcurrentBidException("msg"));
    }

    @Test
    @DisplayName("InsufficientBalanceException la subclass cua AuctionException")
    void testInheritance_insufficientBalance() {
        assertInstanceOf(AuctionException.class, new InsufficientBalanceException("msg"));
    }
}