package com.auction.common.entity;

public abstract class User extends Entity {   // thêm public
    private String username;
    private String password;
    private String email;
    private String fullName;
    private boolean active;

    public User(String username, String password, String email, String fullName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.active = true;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean authenticate(String password) {
        if (!this.active) return false;
        return this.password.equals(password);
    }

    public abstract String getRole();
}