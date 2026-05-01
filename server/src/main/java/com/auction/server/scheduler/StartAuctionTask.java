package com.auction.server.scheduler;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.model.AuctionManager;

/**
 * StartAuctionTask
 * Runnable được gọi khi đến thời gian bắt đầu auction.
 */
public class StartAuctionTask implements Runnable {
    private final String auctionId;

    public StartAuctionTask(String auctionId) {
        this.auctionId = auctionId;
    }

    @Override
    public void run() {
        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        Auction auction = auctionDAO.getAuction(auctionId);
        if (auction == null) return;

        // Chỉ bắt đầu nếu đang ở trạng thái OPEN hoặc DRAFT
        if (auction.getStatus() == AuctionStatus.OPEN || auction.getStatus() == AuctionStatus.DRAFT) {
            auction.setStatus(AuctionStatus.RUNNING);
            auctionDAO.saveAuction(auction);
            AuctionManager.getInstance().startAuction(auction);
            System.out.println("Auction " + auctionId + " has started.");
            // Có thể gọi event listener
        }
    }
}