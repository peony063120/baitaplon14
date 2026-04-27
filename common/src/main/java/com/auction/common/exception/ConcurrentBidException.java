package com.auction.common.exception;

public class ConcurrentBidException extends AuctionException {
    public ConcurrentBidException(String message) {
        super(message);
    }
    public ConcurrentBidException(String message, Throwable cause) {
        super(message, cause);
    }
    public static ConcurrentBidException forAuction(String auctionId, String bidderId, double attemptedAmount) {
        return new ConcurrentBidException(
                String.format("Concurrent bid conflict on auction %s: bidder %s attempted %.2f but price changed",
                        auctionId, bidderId, attemptedAmount)
        );
    }
    public static ConcurrentBidException lostUpdate(String auctionId, String bidderId, double expectedPrice, double actualPrice) {
        return new ConcurrentBidException(
                String.format("Lost update on auction %s: expected price %.2f but actual price %.2f",
                        auctionId, expectedPrice, actualPrice)
        );
    }
}
