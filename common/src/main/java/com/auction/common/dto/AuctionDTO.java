package com.auction.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AuctionDTO implements Serializable {
    private String id;
    private String itemId;
    private String itemName;
    private String itemDescription;
    private String sellerId;
    private String sellerName;
    private AuctionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double currentPrice;
    private String currentWinnerId;
    private String currentWinnerName;
    private double minIncrement;
    private int totalBids;

    public AuctionDTO() {}
    public AuctionDTO(String id, String itemId, String itemName, double currentPrice, AuctionStatus status, LocalDateTime endTime) {
        this.id = id;
        this.itemId = itemId;
        this.itemName = itemName;
        this.currentPrice = currentPrice;
        this.status = status;
        this.endTime = endTime;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public String getCurrentWinnerId() { return currentWinnerId; }
    public void setCurrentWinnerId(String currentWinnerId) { this.currentWinnerId = currentWinnerId; }

    public String getCurrentWinnerName() { return currentWinnerName; }
    public void setCurrentWinnerName(String currentWinnerName) { this.currentWinnerName = currentWinnerName; }

    public double getMinIncrement() { return minIncrement; }
    public void setMinIncrement(double minIncrement) { this.minIncrement = minIncrement; }

    public int getTotalBids() { return totalBids; }
    public void setTotalBids(int totalBids) { this.totalBids = totalBids; }

    public boolean isActive() {
        return (status == AuctionStatus.OPEN || status == AuctionStatus.RUNNING) && LocalDateTime.now().isBefore(endTime);
    }
    public long getRemainingTimeMillis() {
        if (endTime == null) return 0;
        return Math.max(0, java.time.Duration.between(LocalDateTime.now(), endTime).toMillis());
    }
}
