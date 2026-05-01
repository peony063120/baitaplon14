package com.auction.server.listener;

import com.auction.common.entity.Auction;

public class AuctionEventListenerImpl implements AuctionEventListener {

    @Override
    public void onAuctionStart(Auction auction) {
        System.out.println("[EVENT] Auction " + auction.getId() + " has started.");
        // Có thể gửi thông báo đến các client đang quan tâm
    }

    @Override
    public void onAuctionEnd(Auction auction) {
        System.out.println("[EVENT] Auction " + auction.getId() + " has ended. Winner: "
                + auction.getCurrentWinnerId());
        // Có thể lưu lịch sử, thông báo kết quả
    }
}