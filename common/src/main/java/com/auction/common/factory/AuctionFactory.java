package com.auction.common.factory;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;

import java.time.LocalDateTime;

public class AuctionFactory {
    public static Auction createStandardAuction(String itemId, LocalDateTime startTime, LocalDateTime endTime, double startingPrice) {
        Auction auction = new Auction(itemId, startTime, endTime, startingPrice);
        auction.setMinIncrement(1.0);
        return auction;
    }
    public static Auction creatAuctionWithIncrement(String itemId, LocalDateTime startTime, LocalDateTime endTime, double startingPrice, double minIncrement) {
        Auction auction = new Auction(itemId, startTime, endTime, startingPrice);
        auction.setMinIncrement(minIncrement);
        return auction;
    }
    public static Auction createRunningAuction(String itemId, double currentPrice, String currentWinnerId, LocalDateTime endTime) {
        Auction auction = new Auction(itemId, LocalDateTime.now().minusMinutes(5), endTime, currentPrice);
        auction.setStatus(AuctionStatus.RUNNING);
        auction.setCurrentWinnerId(currentWinnerId);
        auction.setCurrentPrice(currentPrice);
        return auction;
    }
}
