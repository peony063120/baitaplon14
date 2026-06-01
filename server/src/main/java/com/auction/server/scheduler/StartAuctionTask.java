package com.auction.server.scheduler;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.service.AuctionService;

import java.time.LocalDateTime;

public class StartAuctionTask implements Runnable {
    private final AuctionDAO auctionDAO;
    private final AuctionService auctionService;
    private final String auctionId;

    // Constructor dùng trong production (lấy singleton)
    public StartAuctionTask(String auctionId) {
        this(AuctionDAO.getInstance(), new AuctionService(), auctionId);
    }

    // Constructor dùng cho test (inject mock)
    public StartAuctionTask(AuctionDAO auctionDAO, AuctionService auctionService, String auctionId) {
        this.auctionDAO = auctionDAO;
        this.auctionService = auctionService;
        this.auctionId = auctionId;
    }

    @Override
    public void run() {
        Auction auction = auctionDAO.getAuction(auctionId);
        if (auction == null) {
            return;
        }
        LocalDateTime now = java.time.LocalDateTime.now();
        boolean readyToStart = auction.getStartTime() == null || !auction.getStartTime().isAfter(now);
        if ((auction.getStatus() == AuctionStatus.OPEN || auction.getStatus() == AuctionStatus.DRAFT)
                && readyToStart) {
            auction.setStatus(AuctionStatus.RUNNING);
            auctionDAO.saveAuction(auction);
            auctionService.notifyAuctionStarted(auction);
            com.auction.common.observer.AuctionSubject.getInstance().notifyObservers(auction);
        }
    }
}