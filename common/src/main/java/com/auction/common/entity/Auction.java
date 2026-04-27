package com.auction.common.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.auction.common.enums.AuctionStatus;

public class Auction {
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

    public Auction(String itemId, String sellerId, LocalDateTime startTime, LocalDateTime endTime, double startingPrice){
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startTime = startTime;
        this. endTime = endTime;
        this. currentPrice = startingPrice;
        this. status = AuctionStatus.DRAFT;
        this.bidHistory = new ArrayList<>();
        this.participants = new ArrayList<>();
        this.antiSnipingEnabled = false;
        this.antiSnipingExtensionSeconds = 0;
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
    public String getItemId(){
        return itemId;
    }
    public String getSellerId(){
        return sellerId;
    }
    public double getCurrentPrice(){
        return currentPrice;
    }
    public String getCurrentWinnerId(){
        return currentWinnerId;
    }

    public AuctionStatus getStatus() {
        return status;
    }
    public List<BidTransaction> getBidHistory(){
        return bidHistory;
    }
    public List<String> getParticipants(){
        return participants;
    }

    public void setStatus(AuctionStatus status){
        this.status = status;
    }
    public void enableAntiSniping(double extensionSeconds){
        this.antiSnipingEnabled = true;
        this. antiSnipingExtensionSeconds = extensionSeconds;
    }
}
