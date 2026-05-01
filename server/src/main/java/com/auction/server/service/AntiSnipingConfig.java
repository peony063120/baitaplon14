package com.auction.server.service;

public class AntiSnipingConfig {
    private int thresholdSeconds = 30; // Ngưỡng kích hoạt
    private int extendSeconds = 60;    // Thời gian gia hạn thêm

    // Getters
    public int getThresholdSeconds() {
        return thresholdSeconds;
    }

    public int getExtendSeconds() {
        return extendSeconds;
    }
}