package com.auction.common.observer;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Observer Pattern Tests")
class ObserverPatternTest {

    private AuctionSubject subject;
    private Auction auction;

    // Observer gia capture ket qua
    private static class FakeObserver implements Observer {
        List<Auction> received = new ArrayList<>();

        @Override
        public void update(Auction a) {
            received.add(a);
        }
    }

    @BeforeEach
    void setUp() {
        subject = new AuctionSubject();
        auction = new Auction(
                "item-1", "seller-1",
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusHours(1),
                1000000
        );
        auction.setStatus(AuctionStatus.RUNNING);
    }

    // ======== registerObserver / notifyObservers ========

    @Test
    @DisplayName("1 observer: nhan duoc update sau notify")
    void testNotify_singleObserver() {
        FakeObserver obs = new FakeObserver();
        subject.registerObserver(obs);
        subject.notifyObservers(auction);

        assertEquals(1, obs.received.size());
        assertEquals(auction.getId(), obs.received.get(0).getId());
    }

    @Test
    @DisplayName("2 observer: ca 2 deu nhan duoc update")
    void testNotify_multipleObservers() {
        FakeObserver obs1 = new FakeObserver();
        FakeObserver obs2 = new FakeObserver();

        subject.registerObserver(obs1);
        subject.registerObserver(obs2);
        subject.notifyObservers(auction);

        assertEquals(1, obs1.received.size());
        assertEquals(1, obs2.received.size());
    }

    @Test
    @DisplayName("Dang ky 2 lan cung observer: chi notify 1 lan (no duplicate)")
    void testRegister_noDuplicate() {
        FakeObserver obs = new FakeObserver();
        subject.registerObserver(obs);
        subject.registerObserver(obs); // lan 2

        subject.notifyObservers(auction);

        assertEquals(1, obs.received.size());
    }

    @Test
    @DisplayName("registerObserver null: bo qua, khong throw exception")
    void testRegister_null_noException() {
        assertDoesNotThrow(() -> subject.registerObserver(null));
        assertDoesNotThrow(() -> subject.notifyObservers(auction));
    }

    // ======== removeObserver ========

    @Test
    @DisplayName("removeObserver: observer bi xoa khong nhan duoc update")
    void testRemoveObserver() {
        FakeObserver obs = new FakeObserver();
        subject.registerObserver(obs);
        subject.removeObserver(obs);
        subject.notifyObservers(auction);

        assertEquals(0, obs.received.size());
    }

    @Test
    @DisplayName("removeObserver: observer khac van nhan duoc update")
    void testRemoveObserver_othersStillNotified() {
        FakeObserver obs1 = new FakeObserver();
        FakeObserver obs2 = new FakeObserver();

        subject.registerObserver(obs1);
        subject.registerObserver(obs2);
        subject.removeObserver(obs1);
        subject.notifyObservers(auction);

        assertEquals(0, obs1.received.size());
        assertEquals(1, obs2.received.size());
    }

    // ======== notifyObservers ========

    @Test
    @DisplayName("notifyObservers nhieu lan: observer nhan dung so lan")
    void testNotify_multipleTimes() {
        FakeObserver obs = new FakeObserver();
        subject.registerObserver(obs);

        subject.notifyObservers(auction);
        subject.notifyObservers(auction);
        subject.notifyObservers(auction);

        assertEquals(3, obs.received.size());
    }

    @Test
    @DisplayName("notifyObservers khi khong co observer: khong throw exception")
    void testNotify_noObservers_noException() {
        assertDoesNotThrow(() -> subject.notifyObservers(auction));
    }
}