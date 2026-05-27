package com.auction.common.dto;

import com.auction.common.enums.AuctionStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Duration;

public class AuctionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String itemId;
    private String itemName;
    private String itemDescription;
    private String sellerId;
    private String sellerName;
    private AuctionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double startingPrice;
    private double currentPrice;
    private String currentWinnerId;
    private String currentWinnerName;
    private double minIncrement;
    private int totalBids;
    private boolean antiSnipingEnabled;
    private int antiSnipingExtensionSeconds;
    private String category;          // ← THÊM category
    private String categoryName;      // ← THÊM categoryName
    private long remainingTimeMillis;

    // Constructors
    public AuctionDTO() {}

    public AuctionDTO(String id, String itemId, String itemName, String itemDescription,
                      String sellerId, String sellerName, double startingPrice, double currentPrice,
                      AuctionStatus status, LocalDateTime startTime, LocalDateTime endTime,
                      double minIncrement, boolean antiSnipingEnabled, int antiSnipingExtensionSeconds) {
        this.id = id;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minIncrement = minIncrement;
        this.antiSnipingEnabled = antiSnipingEnabled;
        this.antiSnipingExtensionSeconds = antiSnipingExtensionSeconds;
    }

    // ========== GETTERS & SETTERS ==========
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

    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }

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

    public boolean isAntiSnipingEnabled() { return antiSnipingEnabled; }
    public void setAntiSnipingEnabled(boolean antiSnipingEnabled) { this.antiSnipingEnabled = antiSnipingEnabled; }

    public int getAntiSnipingExtensionSeconds() { return antiSnipingExtensionSeconds; }
    public void setAntiSnipingExtensionSeconds(int antiSnipingExtensionSeconds) { this.antiSnipingExtensionSeconds = antiSnipingExtensionSeconds; }

    // ========== THÊM CÁC GETTER/SETTER MỚI ==========
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    // ========== PHƯƠNG THỨC TIỆN ÍCH ==========
    // Alias cho startingPrice (để tương thích với AuctionCard)
    public double getStartPrice() {
        return startingPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startingPrice = startPrice;
    }

    // Business logic methods
    public boolean isActive() {
        if (endTime == null) return false;
        return (status == AuctionStatus.OPEN || status == AuctionStatus.RUNNING)
                && LocalDateTime.now().isBefore(endTime);
    }

    public boolean canBid(String userId) {
        if (userId == null) return false;
        if (!isActive()) return false;
        if (sellerId != null && sellerId.equals(userId)) return false;
        if (currentWinnerId != null && currentWinnerId.equals(userId)) return false;
        return true;
    }

    public long getRemainingTimeMillis() {
        if (endTime == null) return 0;
        return Math.max(0, Duration.between(LocalDateTime.now(), endTime).toMillis());
    }

    public void setRemainingTimeMillis(long remainingTimeMillis) {
        this.remainingTimeMillis = remainingTimeMillis;
    }
}