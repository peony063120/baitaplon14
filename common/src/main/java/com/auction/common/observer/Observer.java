package com.auction.common.observer;

import com.auction.common.entity.Auction;

/**
 * Observer interface — Design Pattern: Observer
 */
public interface Observer {
    void update(Auction auction);
}