package com.auction.common.observer;

import com.auction.common.entity.Auction;
import java.util.ArrayList;
import java.util.List;

/**
 * AuctionSubject — implements Subject.
 * Quản lý danh sách observer, notify tất cả khi có bid mới.
 * Thread-safe: synchronized tránh race condition khi nhiều client cùng register.
 */
public class AuctionSubject implements Subject {

    private final List<Observer> observers = new ArrayList<>();

    @Override
    public synchronized void registerObserver(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public synchronized void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Notify toàn bộ observer — gọi sau mỗi bid hợp lệ.
     * Dùng copy của list để tránh ConcurrentModificationException.
     */
    @Override
    public synchronized void notifyObservers(Auction auction) {
        for (Observer observer : new ArrayList<>(observers)) {
            observer.update(auction);
        }
    }

    public int getObserverCount() {
        return observers.size();
    }
}