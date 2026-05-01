package com.auction.server.service;

import com.auction.common.entity.Auction;

public class AuctionScheduler {
    public boolean extend(Auction auction, int seconds) {
        return true;
    }
}