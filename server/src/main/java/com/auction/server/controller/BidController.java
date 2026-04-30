package com.auction.server.controller;

import com.auction.common.dto.AutoBidRequest;
import com.auction.common.dto.BidRequest;
import com.auction.common.entity.BidTransaction;
import com.auction.server.service.BiddingService;
import java.util.List;

/**
 * BidController - nhận request liên quan đến đặt giá từ ClientHandler,
 * ủy thác xử lí cho BiddingService.
 *
 * Attributes (theo diagram):
 *  - biddingService: BiddingService
 *
 * Methods (theo diagram):
 *  + placeBid(request: BidRequest): void
 *  + getBidHistory(auctionId: String): List<BidTransaction>
 *  + configureAutoBid(request: AutoBidRequest): void
 *  + cancelAutoBid(auctionId:String, userId: String): void
 */
public class BidController {

    private final BiddingService biddingService;

    public BidController(BiddingService biddingService) {
        this.biddingService = biddingService;
    }

    // BidRequest: (auctionId, bidderId, amount, isAutoBid)
    public void placeBid(BidRequest request) {
        biddingService.placeBid(request);
    }

    public List<BidTransaction> getBidHistory(String auctionId) {
        return biddingService.getBidHistory(auctionId);
    }

    // AutoBidRequest: (userId, auctionId, maxBid, increment, enable)
    public void configureAutoBid(AutoBidRequest request) {
        biddingService.configureAutoBid(request);
    }

    public void cancelAutoBid(String auctionId, String userId) {
        biddingService.cancelAutoBid(auctionId, userId);
    }
}
