package com.auction.common.entity;

import java.time.LocalDateTime;

public class AutoBidConfig {
    private String id; // có thể thêm id
    private String auctionId;
    private String bidderId; // thay vì userId
    private double maxBid;
    private double increment;
    private LocalDateTime createdAt;
    private boolean active;

    // Constructor
    public AutoBidConfig(String auctionId, String bidderId, double maxBid, double increment) {
        this.id = java.util.UUID.randomUUID().toString();
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.maxBid = maxBid;
        this.increment = increment;
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public double getNextBid(double currentPrice) {
        return currentPrice + increment;
    }

    public boolean canBid(double currentPrice) {
        return (currentPrice + increment <= maxBid);
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getAuctionId() { return auctionId; }
    public void setAuctionId(String auctionId) { this.auctionId = auctionId; }
    public String getBidderId() { return bidderId; }
    public void setBidderId(String bidderId) { this.bidderId = bidderId; }
    public double getMaxBid() { return maxBid; }
    public void setMaxBid(double maxBid) { this.maxBid = maxBid; }
    public double getIncrement() { return increment; }
    public void setIncrement(double increment) { this.increment = increment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}