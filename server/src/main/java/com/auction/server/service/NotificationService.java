package com.auction.server.service;

import com.auction.common.entity.Auction;
import com.auction.common.observer.AuctionSubject;
import com.auction.common.observer.ClientObserver;

import java.util.HashMap;
import java.util.Map;

public class NotificationService {
    private AuctionSubject subject;
    private Map<String, ClientObserver> clientObservers;

    // Constructor mặc định (tự tạo AuctionSubject)
    public NotificationService() {
        this(new AuctionSubject());
    }

    // Constructor có tham số (dùng để test, hoặc inject từ bên ngoài)
    public NotificationService(AuctionSubject subject) {
        this.subject = subject;
        this.clientObservers = new HashMap<>();
    }

    public void sendBidUpdate(Auction auction) {
        subject.notifyObservers(auction);
    }

    public void sendAuctionEndNotification(Auction auction) {
        subject.notifyObservers(auction);
    }

    public void registerObserver(String clientId, ClientObserver observer) {
        clientObservers.put(clientId, observer);
        subject.registerObserver(observer);
    }
}