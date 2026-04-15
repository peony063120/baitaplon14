package com.auction.common.entity;

import java.util.ArrayList;
import java.util.List;

class Seller extends User {
    private List<String> auctionIds;
    public Seller(String username, String password, String email, String fullName) {
        super(username, password, email, fullName);
        this.auctionIds = new ArrayList<>();
    }
    public void addAuctionId(String auctionId) {
        if (!auctionIds.contains(auctionId)) {
            auctionIds.add(auctionId);
        }
    }
    public void removeAuctionId(String auctionId) {
        auctionIds.remove(auctionId);
    }

    public List<String> getAuctionIds() {
        return new ArrayList<>(auctionIds);
    }

    @Override
    public String getRole() {
        return "SELLER";
    }
}

