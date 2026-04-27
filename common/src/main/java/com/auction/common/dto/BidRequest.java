package com.auction.common.dto;

import java.io.Serializable;

public class BidRequest implements Serializable {
    private String auctionId;
    private String bidderId;
    private double amount;
    private boolean isAutoBid;

    public BidRequest() {}
    public BidRequest(String auctionId, String bidderId, double amount, boolean isAutoBid) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.isAutoBid = isAutoBid;
    }
    public String getAuctionId() { return auctionId; }
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }

    public String getBidderId() { return bidderId; }
    public void setBidderId(String bidderId) { this.bidderId = bidderId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public boolean isAutoBid() { return isAutoBid; }
    public void setAutoBid(boolean isAutoBid) { this.isAutoBid = isAutoBid; }

    @Override
    public String toString() {
        return "BidRequest{" +
                "auctionId='" + auctionId + '\'' +
                ", bidderId='" + bidderId + '\'' +
                ", amount=" + amount +
                ", isAutoBid=" + isAutoBid +
                '}';
    }
}
