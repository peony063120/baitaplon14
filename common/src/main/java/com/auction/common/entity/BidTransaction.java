package com.auction.common.entity;

import java.time.LocalDateTime;

public class BidTransaction {
    private String auctionId;
    private String bidderId;
    private double amount;
    private LocalDateTime bidTime;
    private boolean autoBid;

    // Constructor
    public BidTransaction(String auctionId, String bidderId, double amount, LocalDateTime bidTime, boolean autoBid) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.bidTime = LocalDateTime.now();
        this.autoBid = autoBid;
    }

    // Getter
    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public boolean isAutoBid() {
        return autoBid;
    }

    // Setter
    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setAutoBid(boolean autoBid) {
        this.autoBid = autoBid;
    }

    @Override
    public String toString() {
        return "BidTransaction{" +
                "auctionId='" + auctionId + '\'' +
                ", bidderId='" + bidderId + '\'' +
                ", amount=" + amount +
                ", bidTime=" + bidTime +
                ", autoBid=" + autoBid +
                '}';
    }
