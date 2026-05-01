package com.auction.server.config;

/**
 * AntiSnipingConfig
 * Cấu hình cho thuật toán chống sniping.
 */
public class AntiSnipingConfig {
    private static AntiSnipingConfig instance;
    private int thresholdSeconds;   // Nếu có bid trong X giây cuối
    private int extensionSeconds;   // Gia hạn thêm Y giây

    private AntiSnipingConfig() {
        // Giá trị mặc định
        this.thresholdSeconds = 10;
        this.extensionSeconds = 30;
    }

    public static synchronized AntiSnipingConfig getInstance() {
        if (instance == null) {
            instance = new AntiSnipingConfig();
        }
        return instance;
    }

    public int getThresholdSeconds() {
        return thresholdSeconds;
    }

    public void setThresholdSeconds(int thresholdSeconds) {
        this.thresholdSeconds = thresholdSeconds;
    }

    public int getExtensionSeconds() {
        return extensionSeconds;
    }

    public void setExtensionSeconds(int extensionSeconds) {
        this.extensionSeconds = extensionSeconds;
    }

    /**
     * Kiểm tra xem một bid có nên kích hoạt gia hạn không, dựa trên thời gian còn lại của auction.
     */
    public boolean shouldExtend(long remainingMillis) {
        return remainingMillis <= thresholdSeconds * 1000L;
    }
}