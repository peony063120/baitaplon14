package com.auction.server.scheduler;

import com.auction.common.entity.Auction;
import com.auction.common.enums.AuctionStatus;
import com.auction.server.config.AntiSnipingConfig;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.model.AuctionManager;
import com.auction.server.service.AntiSnipingService;

import java.time.LocalDateTime;

/**
 * EndAuctionTask
 * Runnable được gọi khi đến thời gian kết thúc auction.
 * Kiểm tra anti-sniping trước khi kết thúc.
 */
public class EndAuctionTask implements Runnable {
    private final String auctionId;

    public EndAuctionTask(String auctionId) {
        this.auctionId = auctionId;
    }

    @Override
    public void run() {
        AuctionDAO auctionDAO = AuctionDAO.getInstance();
        Auction auction = auctionDAO.getAuction(auctionId);
        if (auction == null) return;

        // Kiểm tra anti-sniping
        if (auction.isAntiSnipingEnabled()) {
            AntiSnipingService antiSnipingService = new AntiSnipingService();
            boolean extended = antiSnipingService.checkAndExtend(auction);
            if (extended) {
                // Nếu được gia hạn, schedule lại task kết thúc mới
                AuctionScheduler.getInstance().scheduleAuctionEnd(auction);
                System.out.println("Auction " + auctionId + " extended due to sniping.");
                return;
            }
        }

        // Kết thúc auction
        if (auction.getStatus() == AuctionStatus.RUNNING) {
            auction.setStatus(AuctionStatus.FINISHED);
            auctionDAO.saveAuction(auction);
            AuctionManager.getInstance().endAuction(auctionId);
            System.out.println("Auction " + auctionId + " has ended. Winner: " + auction.getCurrentWinner());
        }
    }
}