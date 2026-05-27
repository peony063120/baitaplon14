package com.auction.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BidHistoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

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

    // ==================== GETTERS ====================
    public String getBidderName() { return bidderName; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isAutoBid() { return isAutoBid; }

    // ==================== SETTERS ====================
    public void setBidderName(String bidderName) { this.bidderName = bidderName; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setAutoBid(boolean autoBid) { isAutoBid = autoBid; }

    /**
     * Lấy chuỗi thời gian đã định dạng để hiển thị trên TableView.
     * @return Chuỗi thời gian định dạng "dd/MM/yyyy HH:mm:ss"
     */
    public String getTimestampString() {
        if (timestamp == null) return "";
        return timestamp.format(TIME_FORMATTER);
    }

    @Override
    public String toString() {
        return String.format("%s - %,.0f VNĐ by %s%s",
                getTimestampString(),
                amount,
                bidderName,
                isAutoBid ? " (auto)" : "");
    }
}