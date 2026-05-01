package com.auction.server.config;

/**
 * AppConfig
 * Cấu hình các tham số chung của hệ thống.
 */
public class AppConfig {
    private static AppConfig instance;
    private boolean debugMode;
    private int maxConcurrentAuctions;

    private AppConfig() {
        // Giá trị mặc định
        this.debugMode = false;
        this.maxConcurrentAuctions = 100;
        // Có thể đọc từ file nếu cần
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public int getMaxConcurrentAuctions() {
        return maxConcurrentAuctions;
    }

    public void setMaxConcurrentAuctions(int maxConcurrentAuctions) {
        this.maxConcurrentAuctions = maxConcurrentAuctions;
    }
}