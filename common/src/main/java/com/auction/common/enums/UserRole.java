package com.auction.common.enums;

public enum UserRole {
    BIDDER("Người đấu giá"),
    SELLER("Người bán"),
    ADMIN("Quản trị viên");

    private final String description;
    UserRole(String description){
        this.description = description;
    }
    public String getDescription(){
        return description;
    }
}
