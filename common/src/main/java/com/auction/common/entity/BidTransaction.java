package com.auction.common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BidTransaction implements Serializable {
    private String transactionId;
    private String auctionId;
    private String bidderId;
    private double amount;
    private LocalDateTime bidTime;
    private String bidderName;
    private boolean autoBid;

    // Constructor đầy đủ với transactionId
    public BidTransaction(String transactionId, String auctionId, String bidderId,
                          double amount, LocalDateTime bidTime, boolean autoBid) {
        this.transactionId = transactionId;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.bidTime = bidTime;      // ← sửa: dùng tham số thay vì LocalDateTime.now()
        this.autoBid = autoBid;
    }

    // Constructor rút gọn (nếu không cần transactionId, tự sinh UUID)
    public BidTransaction(String auctionId, String bidderId, double amount, LocalDateTime bidTime, boolean autoBid) {
        this(java.util.UUID.randomUUID().toString(), auctionId, bidderId, amount, bidTime, autoBid);
    }

    // Getters
    public String getTransactionId() { return transactionId; }
    public String getAuctionId() { return auctionId; }
    public String getBidderId() { return bidderId; }
    public String getBidderName() { return bidderName; }
    public double getAmount() { return amount; }
    public LocalDateTime getBidTime() { return bidTime; }
    public boolean isAutoBid() { return autoBid; }

    // Setters
    public void setAmount(double amount) { this.amount = amount; }
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }
    public void setAutoBid(boolean autoBid) { this.autoBid = autoBid; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    // Thêm method này
    public long getTimestamp() {
        return bidTime != null ? bidTime.toEpochSecond(java.time.ZoneOffset.UTC) : 0;
    }

    @Override
    public String toString() {
        return "BidTransaction{" +
                "transactionId='" + transactionId + '\'' +
                ", auctionId='" + auctionId + '\'' +
                ", bidderId='" + bidderId + '\'' +
                ", amount=" + amount +
                ", bidTime=" + bidTime +
                ", autoBid=" + autoBid +
                '}';
    }
}