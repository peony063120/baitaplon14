package com.auction.common.observer;
/**
 * Subject interface — Design Pattern: Observer
 */
public interface Subject {
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers(com.auction.common.entity.Auction auction);
}
