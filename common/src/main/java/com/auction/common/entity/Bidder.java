package com.auction.common.entity;

import java.util.HashMap;
import java.util.Map;

class Bidder extends User {
    private double balance;
    private Map<String, AutoBidConfig> autoBidConfigs = new HashMap<>();
    public Bidder(String username, String password, String email, String fullName, double balance) {
        super(username, password, email, fullName);
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }
    public void addBalance(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }
    public boolean deductBalance(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }
    public void setAutoBidConfig(String auctionId, AutoBidConfig config) {
        this.autoBidConfigs.put(auctionId, config);
    }

    @Override
    public String getRole() {
        return "BIDDER";
    }
}

