package com.auction.server.service;

import com.auction.common.entity.Auction;
import com.auction.common.observer.AuctionSubject;
import com.auction.common.observer.ClientObserver;

import java.util.HashMap;
import java.util.Map;

public class NotificationService {
    private static NotificationService instance;
    private AuctionSubject subject;
    private Map<String, ClientObserver> clientObservers;

    private NotificationService() {
        this(new AuctionSubject());
    }

    NotificationService(AuctionSubject subject) {
        this.subject = subject;
        this.clientObservers = new HashMap<>();
    }

    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    // Dùng cho test để inject mock subject
    public static NotificationService getInstance(AuctionSubject subject) {
        return new NotificationService(subject);
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