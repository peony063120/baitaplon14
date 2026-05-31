package com.auction.client.service;

import com.auction.common.dto.AuctionDTO;
import com.auction.common.enums.AuctionStatus;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MockBidSimulator {
    private static final MockBidSimulator INSTANCE = new MockBidSimulator();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Random random = new Random();
    private final AtomicInteger botCounter = new AtomicInteger(0);

    private static final String[][] BOT_USERS = {
        {"user-002", "Alice Nguyen"},
        {"user-003", "Bob Tran"},
        {"user-004", "Charlie Pham"},
        {"user-007", "Fiona Le"},
        {"user-008", "George Vo"}
    };

    private MockBidSimulator() {}

    public static MockBidSimulator getInstance() {
        return INSTANCE;
    }

    public void start() {
        if (running.getAndSet(true)) return;
        Thread thread = new Thread(this::runLoop, "mock-bid-simulator");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running.set(false);
    }

    private void runLoop() {
        while (running.get()) {
            try {
                Thread.sleep(3000 + random.nextInt(5000));
            } catch (InterruptedException e) {
                break;
            }
            if (!running.get()) break;

            MockAuctionStore store = MockAuctionStore.getInstance();
            List<AuctionDTO> auctions = store.getAuctions();

            long runningCount = auctions.stream()
                    .filter(a -> a.getStatus() == AuctionStatus.RUNNING)
                    .count();

            if (runningCount == 0) continue;

            AuctionDTO target = auctions.stream()
                    .filter(a -> a.getStatus() == AuctionStatus.RUNNING)
                    .skip(random.nextInt((int) runningCount))
                    .findFirst()
                    .orElse(null);

            if (target == null) continue;

            int botIdx = random.nextInt(BOT_USERS.length);
            String bidderId = BOT_USERS[botIdx][0];
            String bidderName = BOT_USERS[botIdx][1];

            double increment = target.getCurrentPrice() * (0.01 + random.nextDouble() * 0.14);
            if (increment < 1000) increment = 1000 + random.nextDouble() * 9000;
            double newBid = Math.round((target.getCurrentPrice() + increment) / 1000.0) * 1000.0;

            boolean success = store.placeBid(target.getId(), bidderId, bidderName, newBid);
            if (success) {
                botCounter.incrementAndGet();
                System.out.println("[MockBidSimulator] " + bidderName + " placed $" + String.format("%,.2f", newBid)
                        + " on \"" + target.getItemName() + "\"");
            }
        }
    }

    public int getTotalBids() {
        return botCounter.get();
    }
}
