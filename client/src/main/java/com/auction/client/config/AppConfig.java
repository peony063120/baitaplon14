package com.auction.client.config;

public class AppConfig {
    // CHẾ ĐỘ MOCK: true = dữ liệu giả, false = gọi API thật
    // 💡 Khi nộp bài, set false để dùng API thật
    public static final boolean USE_MOCK = Boolean.getBoolean("app.mock");

    // Nếu API thật lỗi, có tự động fallback sang mock không?
    public static final boolean AUTO_FALLBACK = true;
}
