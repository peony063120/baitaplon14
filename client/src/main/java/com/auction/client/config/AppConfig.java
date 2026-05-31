package com.auction.client.config;

public class AppConfig {
    private static boolean useMock = Boolean.parseBoolean(
            System.getProperty("app.useMock", System.getenv().getOrDefault("APP_USE_MOCK", "false")));

    private static boolean autoFallback = Boolean.parseBoolean(
            System.getProperty("app.autoFallback", System.getenv().getOrDefault("APP_AUTO_FALLBACK", "false")));

    public static boolean isUseMock() { return useMock; }

    public static void setUseMock(boolean value) { useMock = value; }

    public static boolean isAutoFallback() { return autoFallback; }
}