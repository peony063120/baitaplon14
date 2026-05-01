package com.auction.common.observer;

import com.auction.common.entity.Auction;
import java.util.ArrayList;
import java.util.List;

public class AuctionSubject implements Subject {
    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void attach(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Auction auction) {
        for (Observer observer : observers) {
            observer.update(auction);
        }
    }
}