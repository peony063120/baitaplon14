package com.auction.common.entity;

import java.time.LocalDateTime;

public class AutoBidConfig {
    private String userId;
    private double maxBid;//luu lai gioi han cao nhat ma nguoi dung san sang tra
    private double increment;//khoang gia cong vao them moi khi co nguoi tra gia cao hon
    private LocalDateTime createdAt;

    //Constructor
    public AutoBidConfig(String userId,double maxBid, double increment) {
        this.userId = userId;
        this.maxBid = maxBid;
        this.increment = increment;
        this.createdAt = LocalDateTime.now();
    }

    //Tinh toan muc gia thau tiep theo dua tren gia hien tai
    //Nhap vao gia hien tai va tra ve gia moi.
    public double getNextBid(double currentPrice) {
        return currentPrice + increment;
    }

    //Kiem tra xem co the tiep tuc dat thau hay khong
    //Gia tiep theo khong duoc vuot qua maxBid
    //Tra ve true neu viec nang gia van nam trong gioi han cho phep, nguoc lai tra ve false
    public boolean canBid(double currentPrice) {
        return (currentPrice + increment <= maxBid);
    }

    //Getter & Setter
    public double getMaxBid() {
        return maxBid;
    }

    public void setMaxBid(double maxBid) {
        this.maxBid = maxBid;
    }

    public double getIncrement() {
        return increment;
    }

    public void setIncrement(double increment) {
        this.increment = increment;
    }

    public String getUserId() { return userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
