package com.auction.common.exception;

public class InvalidBidException extends AuctionException {
    public InvalidBidException(String message) {
        super(message);
    }

    public InvalidBidException(String auctionId, double currentPrice, double attemptedBid, double minIncrement) {
        super(String.format("Invalid bid for auction %s: current price = %.2f, bid amount = %.2f, min increment = %.2f",
                auctionId, currentPrice, attemptedBid, minIncrement));
    }
}