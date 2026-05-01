package com.auction.common.dto;

import com.auction.common.entity.Bidder;
import com.auction.common.entity.User;

import java.io.Serializable;

public class UserDTO implements Serializable {
    private String id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String role;
    private double balance;
    private boolean active;
    private int totalBids;
    private int wonAuctions;
    private double totalRevenue;

    public UserDTO() {}
    public UserDTO(String id, String username, String password, String email, String fullName, String role, double balance) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.balance = balance;
        this.active = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getTotalBids() { return totalBids; }
    public void setTotalBids(int totalBids) { this.totalBids = totalBids; }

    public int getWonAuctions() { return wonAuctions; }
    public void setWonAuctions(int wonAuctions) {  this.wonAuctions = wonAuctions; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
}
