package com.auction.common.exception;

public class AuctionNotFoundException extends AuctionException{
    public AuctionNotFoundException(String auctionId){
        super("Auction not found with id: " + auctionId);
    }
    public AuctionNotFoundException(String auctionId, String message){
        super(message + " (auction id: " + auctionId + ")");
    }
}
