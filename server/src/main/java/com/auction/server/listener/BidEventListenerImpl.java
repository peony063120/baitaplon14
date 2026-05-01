package com.auction.server.listener;

import com.auction.common.entity.AutoBidConfig;
import com.auction.common.entity.BidTransaction;

public class BidEventListenerImpl implements BidEventListener {

    @Override
    public void onBidPlaced(BidTransaction bid) {
        System.out.println("[EVENT] New bid placed: " + bid.getBidderId()
                + " bid " + bid.getAmount() + " for auction " + bid.getAuctionId());
        // Cập nhật realtime cho client qua Observer
    }

    @Override
    public void onAutoBidTriggered(AutoBidConfig config) {
        System.out.println("[EVENT] Auto-bid triggered for user " + config.getBidderId()
                + " on auction " + config.getAuctionId() + " up to " + config.getMaxBid());
    }
}