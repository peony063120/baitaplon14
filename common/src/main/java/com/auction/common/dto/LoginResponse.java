package com.auction.common.dto;

import java.io.Serializable;


public class LoginResponse implements Serializable {
    private boolean success;
    private String message;
    private String userId;
    private String username;
    private String role;
    private String sessionToken;
    private double balance;
    private String email;
    private String fullName;

    public LoginResponse() {}

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LoginResponse(boolean success, String message, String userId, String username, String role, String sessionToken, double balance) {
        this(success, message, userId, username, role, sessionToken, balance, null, null);
    }

    public LoginResponse(boolean success, String message, String userId, String username, String role,
                         String sessionToken, double balance, String email, String fullName) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.sessionToken = sessionToken;
        this.balance = balance;
        this.email = email;
        this.fullName = fullName;
    }

    // Getters and setters
    public boolean isSuccess() {return success;}
    public void setSuccess(boolean success) {this.success = success;}

    public String getMessage() {return message;}
    public void setMessage(String message) {this.message = message;}

    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}

    public String getSessionToken() {return sessionToken;}
    public void setSessionToken(String sessionToken) {this.sessionToken = sessionToken;}

    public double getBalance() {return balance;}
    public void setBalance(double balance) {this.balance = balance;}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", balance=" + balance +
                '}';
    }
}
