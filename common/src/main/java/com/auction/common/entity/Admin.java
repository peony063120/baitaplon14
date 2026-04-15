package com.auction.common.entity;

class Admin extends User {
    private String permissionLevel;
    public Admin(String username, String password, String email, String fullName, String permissionLevel) {
        super(username, password, email, fullName);
        this.permissionLevel = permissionLevel;
    }
    public String getPermissionLevel() {
        return permissionLevel;
    }
    public void setPermissionLevel(String level) {
        this.permissionLevel = level;
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }
}

