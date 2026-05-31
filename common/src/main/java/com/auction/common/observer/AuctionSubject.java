package com.auction.common.observer;

import com.auction.common.entity.Auction;
import java.util.ArrayList;
import java.util.List;

public class AuctionSubject implements Subject {
    private static volatile AuctionSubject instance;
    private final List<Observer> observers = new ArrayList<>();

    // Package-private constructor for testing
    AuctionSubject() {}

    public static synchronized AuctionSubject getInstance() {
        if (instance == null) {
            instance = new AuctionSubject();
        }
        return instance;
    }

    // For testing: reset the singleton instance
    static void resetInstance() {
        instance = null;
    }

    @Override
    public void registerObserver(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Auction auction) {
        for (Observer observer : observers) {
            observer.update(auction);
        }
    }
}