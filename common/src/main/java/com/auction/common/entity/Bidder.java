package com.auction.common.entity;

import java.util.HashMap;
import java.util.Map;

public class Bidder extends User {
    private double balance;
    private Map<String, AutoBidConfig> autoBidConfigs = new HashMap<>();

    // Constructor đầy đủ
    public Bidder(String username, String password, String email, String fullName, double balance) {
        super(username, password, email, fullName);
        this.balance = balance;
    }

    // Constructor chỉ với 4 tham số (mặc định balance = 0)
    public Bidder(String username, String password, String email, String fullName) {
        this(username, password, email, fullName, 0.0);
    }

    public double getBalance() {
        return balance;
    }

    public void addBalance(double amount) {
        if (amount > 0) this.balance += amount;
    }

    public boolean deductBalance(double amount) {
        if (amount > 0 && this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setAutoBidConfig(String auctionId, AutoBidConfig config) {
        autoBidConfigs.put(auctionId, config);
    }

    public AutoBidConfig getAutoBidConfig(String auctionId) {
        return autoBidConfigs.get(auctionId);
    }

    public void removeAutoBidConfig(String auctionId) {
        autoBidConfigs.remove(auctionId);
    }

    public Map<String, AutoBidConfig> getAutoBidConfigs() {
        return autoBidConfigs;
    }

    @Override
    public String getRole() {
        return "BIDDER";
    }
}