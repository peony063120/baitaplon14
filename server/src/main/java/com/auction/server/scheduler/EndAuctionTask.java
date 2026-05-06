package com.auction.server.scheduler;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.service.AuctionService;

public class EndAuctionTask implements Runnable {
    private final AuctionDAO auctionDAO;
    private final AuctionService auctionService;
    private final String auctionId;

    // Constructor dùng trong production (lấy singleton)
    public EndAuctionTask(String auctionId) {
        this(AuctionDAO.getInstance(), new AuctionService(), auctionId);
    }

    // Constructor dùng cho test (inject mock)
    public EndAuctionTask(AuctionDAO auctionDAO, AuctionService auctionService, String auctionId) {
        this.auctionDAO = auctionDAO;
        this.auctionService = auctionService;
        this.auctionId = auctionId;
    }

    @Override
    public void run() {
        Auction auction = auctionDAO.getAuction(auctionId);
        if (auction != null && auction.getStatus() == AuctionStatus.RUNNING) {
            auction.setStatus(AuctionStatus.FINISHED);
            auctionDAO.saveAuction(auction);
            auctionService.notifyAuctionEnded(auction);
        }
    }
}