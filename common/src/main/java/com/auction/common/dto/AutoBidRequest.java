package com.auction.common.dto;

import com.auction.common.entity.Auction;

import java.io.Serializable;

public class AutoBidRequest implements Serializable {
    private String userId;
    private String auctionId;
    private double maxBid;
    private double increment;
    private boolean enable;

    public AutoBidRequest() {}
    public AutoBidRequest(String userId, String auctionId, double maxBid, double increment, boolean enable) {
        this.userId = userId;
        this.auctionId = auctionId;
        this.maxBid = maxBid;
        this.increment = increment;
        this.enable = enable;
    }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAuctionId() { return auctionId; }
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }

    public double getMaxBid() { return maxBid; }
    public void setMaxBid(double maxBid) { this.maxBid = maxBid; }

    public double getIncrement() { return increment; }
    public void setIncrement(double increment) { this.increment = increment; }

    public boolean isEnable() { return enable; }
    public void setEnable(boolean enable) { this.enable = enable; }

}
