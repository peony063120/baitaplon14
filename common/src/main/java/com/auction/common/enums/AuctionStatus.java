package com.auction.common.enums;

public enum AuctionStatus {
    PENDING("Pending Approval"),
    DRAFT("Draft"),
    OPEN("Open"),
    RUNNING("Running"),
    FINISHED("Finished"),
    PAID("Paid"),
    CANCELLED("Cancelled");

    private final String displayName;
    AuctionStatus(String displayName){
        this.displayName = displayName ;
    }
    public String getDisplayName(){
        return displayName;
    }
}
