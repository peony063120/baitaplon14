package com.auction.common.entity;

import com.auction.common.enums.AuctionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Auction {
    private String id;
    private String itemId;
    private String sellerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double currentPrice;
    private String currentWinnerId;
    private AuctionStatus status;
    private List<BidTransaction> bidHistory;
    private List<String> participants;
    private boolean antiSnipingEnabled;
    private double antiSnipingExtensionSeconds;
    private double minIncrement;
    private List<AutoBidConfig> autoBidConfigs = new ArrayList<>();
    private String category;
    private String itemName;
    private String itemDescription;
    private String imagePath;

    public Auction(String itemId, LocalDateTime startTime, LocalDateTime endTime, double startingPrice) {
        this.id = UUID.randomUUID().toString();
        this.itemId = itemId;
        this.sellerId = null;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentPrice = startingPrice;
        this.status = AuctionStatus.DRAFT;
        this.bidHistory = new ArrayList<>();
        this.participants = new ArrayList<>();
        this.antiSnipingEnabled = false;
        this.antiSnipingExtensionSeconds = 0;
        this.minIncrement = 1.0;
    }
    public Auction(String itemId, String sellerId, LocalDateTime startTime, LocalDateTime endTime, double startingPrice) {
        this(itemId, startTime, endTime, startingPrice);
        this.sellerId = sellerId;
    }
    public Auction(String id, String itemId, String sellerId, LocalDateTime startTime, LocalDateTime endTime, double currentPrice, String currentWinnerId, AuctionStatus status) {
        this.id = id;
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentPrice = currentPrice;
        this.currentWinnerId = currentWinnerId;
        this.status = status;
        this.bidHistory = new ArrayList<>();
        this.participants = new ArrayList<>();
        this.antiSnipingEnabled = false;
        this.antiSnipingExtensionSeconds = 0;
        this.minIncrement = 1.0;
    }

    public boolean isActive(){
        LocalDateTime now = LocalDateTime.now();
        return status == AuctionStatus.RUNNING && now.isAfter(startTime) && now.isBefore(endTime);
    }

    public boolean canBid(String userId){
        return isActive() && !userId.equals(sellerId);
    }

    public boolean addBid(BidTransaction bid){
        if (canBid(bid.getBidderId()) && bid.getAmount() > currentPrice){
            currentPrice = bid.getAmount();
            currentWinnerId = bid.getBidderId();
            bidHistory.add(bid);

            //Anti-sniping: neu gan het gio thi gia han
            if (antiSnipingEnabled && endTime.minusSeconds((long) antiSnipingExtensionSeconds).isBefore(bid.getBidTime())){
                extendEndTime(antiSnipingExtensionSeconds);
            }
            return true;
        }
        return false;
    }
    public void extendEndTime(double seconds){
        this.endTime = this.endTime.plusSeconds((long) seconds);
    }
    //Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public String getCurrentWinnerId() { return currentWinnerId; }
    public void setCurrentWinnerId(String currentWinnerId) { this.currentWinnerId = currentWinnerId; }

    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }

    public List<BidTransaction> getBidHistory() { return bidHistory; }
    public void setBidHistory(List<BidTransaction> bidHistory) { this.bidHistory = bidHistory; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public boolean isAntiSnipingEnabled() { return antiSnipingEnabled; }
    public void setAntiSnipingEnabled(boolean antiSnipingEnabled) { this.antiSnipingEnabled = antiSnipingEnabled; }

    public double getAntiSnipingExtensionSeconds() { return antiSnipingExtensionSeconds; }
    public void setAntiSnipingExtensionSeconds(double antiSnipingExtensionSeconds) {
        this.antiSnipingExtensionSeconds = antiSnipingExtensionSeconds;
    }

    public double getMinIncrement() { return minIncrement; }
    public void setMinIncrement(double minIncrement) { this.minIncrement = minIncrement; }

    public void enableAntiSniping(double extensionSeconds) {
        this.antiSnipingEnabled = true;
        this.antiSnipingExtensionSeconds = extensionSeconds;
    }

    // Thêm Getter để AutoBiddingStrategy có thể truy cập danh sách cấu hình
    public List<AutoBidConfig> getAutoBidConfigs() {
        return autoBidConfigs;
    }

    // Phương thức để thêm cấu hình mới cho phiên đấu giá này
    public void addAutoBidConfig(AutoBidConfig config) {
        this.autoBidConfigs.add(config);
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
