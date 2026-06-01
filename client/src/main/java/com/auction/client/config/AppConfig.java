package com.auction.client.config;

public class AppConfig {
    private static boolean useMock = Boolean.parseBoolean(
            System.getProperty("app.useMock", System.getenv().getOrDefault("APP_USE_MOCK", "false")));

    private static boolean autoFallback = Boolean.parseBoolean(
            System.getProperty("app.autoFallback", System.getenv().getOrDefault("APP_AUTO_FALLBACK", "false")));

    private static final String serverHost = System.getProperty(
            "server.host",
            System.getenv().getOrDefault("SERVER_HOST", "localhost")
    ).trim();

    private static final int serverPort = parsePort(
            System.getProperty("server.port", System.getenv().getOrDefault("SERVER_PORT", "5050"))
    );

    public static boolean isUseMock() { return useMock; }

    public static void setUseMock(boolean value) { useMock = value; }

    public static boolean isAutoFallback() { return autoFallback; }

    public static String getServerHost() { return serverHost; }

    public static int getServerPort() { return serverPort; }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value.trim());
            return (port > 0 && port <= 65535) ? port : 5050;
        } catch (Exception ignored) {
            return 5050;
        }
    }
}