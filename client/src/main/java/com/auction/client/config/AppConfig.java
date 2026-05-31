package com.auction.client.config;

public class AppConfig {
    // CHẾ ĐỘ MOCK: true = dữ liệu giả, false = gọi API thật
    // Cách bật nhanh (không sửa code):
    // - Thiết lập biến môi trường APP_USE_MOCK=true
    // - Hoặc truyền system property -Dapp.useMock=true khi chạy JVM
    public static final boolean USE_MOCK = Boolean.parseBoolean(System.getProperty("app.useMock", System.getenv().getOrDefault("APP_USE_MOCK", "false")));

    // Nếu API thật lỗi, có tự động fallback sang mock không?
    public static final boolean AUTO_FALLBACK = Boolean.parseBoolean(System.getProperty("app.autoFallback", System.getenv().getOrDefault("APP_AUTO_FALLBACK", "true")));
}