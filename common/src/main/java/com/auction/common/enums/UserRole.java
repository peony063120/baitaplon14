package com.auction.common.enums;

public enum UserRole {
    BIDDER("Bidder"),
    SELLER("Seller"),
    ADMIN("Admin");

    private final String description;
    UserRole(String description){
        this.description = description;
    }
    public String getDescription(){
        return description;
    }
}
