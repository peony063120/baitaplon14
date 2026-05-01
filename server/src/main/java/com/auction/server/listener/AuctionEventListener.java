package com.auction.server.listener;

import com.auction.common.entity.Auction;

/**
 * AuctionEventListener
 * Lắng nghe các sự kiện liên quan đến phiên đấu giá (bắt đầu, kết thúc).
 */
public interface AuctionEventListener {

    /**
     * Được gọi khi một phiên đấu giá bắt đầu.
     * @param auction phiên đấu giá vừa bắt đầu
     */
    void onAuctionStart(Auction auction);

    /**
     * Được gọi khi một phiên đấu giá kết thúc.
     * @param auction phiên đấu giá vừa kết thúc
     */
    void onAuctionEnd(Auction auction);
}