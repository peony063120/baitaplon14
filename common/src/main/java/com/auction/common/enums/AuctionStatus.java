package com.auction.common.enums;

public enum AuctionStatus {
    PENDING("Chờ duyệt"),
    DRAFT("Bản nháp"),
    OPEN("Đang mở"),
    RUNNING("Đang diễn ra"),
    FINISHED("Đã kết thúc"),
    PAID("Đã thanh toán"),
    CANCELLED("Đã hủy");

    private final String displayName;
    AuctionStatus(String displayName){
        this.displayName = displayName ;
    }
    public String getDisplayName(){
        return displayName;
    }
}
