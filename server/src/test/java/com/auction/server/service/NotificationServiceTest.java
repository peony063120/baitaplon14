package com.auction.server.service;

import com.auction.common.entity.Auction;
import com.auction.common.observer.AuctionSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private AuctionSubject subject;
    private NotificationService notificationService;
    private Auction auction;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(subject);
        auction = new Auction("item1", LocalDateTime.now(), LocalDateTime.now().plusHours(1), 100.0);
        auction.setId("auc1");
    }

    @Test
    void sendBidUpdate() {
        notificationService.sendBidUpdate(auction);
        verify(subject, times(1)).notifyObservers(auction);
    }

    @Test
    void sendAuctionEndNotification() {
        notificationService.sendAuctionEndNotification(auction);
        verify(subject, times(1)).notifyObservers(auction);
    }
}