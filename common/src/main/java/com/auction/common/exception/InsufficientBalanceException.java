package com.auction.common.exception;

public class InsufficientBalanceException extends AuctionException {
    public InsufficientBalanceException(String userId, double required, double available) {
        super(String.format("User %s has insufficient balance. Required: %.2f, Available: %.2f", userId, required, available));
    }
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
