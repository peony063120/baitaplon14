package com.auction.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BidHistoryDTO implements Serializable {
    private String bidderName;
    private double amount;
    private LocalDateTime timestamp;
    private boolean isAutoBid;

    public BidHistoryDTO() {}
    public BidHistoryDTO(String bidderName, double amount, LocalDateTime timestamp, boolean isAutoBid) {
        this.bidderName = bidderName;
        this.amount = amount;
        this.timestamp = timestamp;
        this.isAutoBid = isAutoBid;
    }
    public String getBidderName() { return bidderName; }
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isAutoBid() { return isAutoBid; }
    public void setAutoBid(boolean autoBid) { isAutoBid = autoBid; }

    @Override
    public String toString() {
        return String.format("%s - $%.2f by %s%s", timestamp, amount, bidderName, isAutoBid ? " (auto)" : "");
    }
}
