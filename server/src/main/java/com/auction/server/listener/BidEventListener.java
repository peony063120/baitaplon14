package com.auction.server.listener;

import com.auction.common.entity.AutoBidConfig;
import com.auction.common.entity.BidTransaction;

/**
 * BidEventListener
 * Lắng nghe các sự kiện liên quan đến đấu giá (bid thường, auto-bid).
 */
public interface BidEventListener {

    /**
     * Được gọi khi một bid hợp lệ được đặt (thủ công hoặc auto).
     * @param bid giao dịch bid vừa được thực hiện
     */
    void onBidPlaced(BidTransaction bid);

    /**
     * Được gọi khi một auto-bid được kích hoạt.
     * @param config cấu hình auto-bid vừa được trigger
     */
    void onAutoBidTriggered(AutoBidConfig config);
}