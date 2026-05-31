package com.auction.server.config;

import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * ServerConfig
 * Đọc cấu hình từ file (ví dụ: server.properties).
 */
public class ServerConfig {
    private static ServerConfig instance;
    private final Properties props;

    private ServerConfig() {
        props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("server.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                // Giá trị mặc định
                props.setProperty("server.port", "8080");
                props.setProperty("server.host", "localhost");
                // Cấu hình đấu giá
                props.setProperty("auction.default.duration.hours", "24");  // Default: 24 giờ
                props.setProperty("auction.min.bid.interval.seconds", "5");    // Minimum 5 seconds between bids
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized ServerConfig getInstance() {
        if (instance == null) {
            instance = new ServerConfig();
        }
        return instance;
    }

    public int getPort() {
        return Integer.parseInt(props.getProperty("server.port", "8080").trim());
    }

    public String getHost() {
        return props.getProperty("server.host", "localhost");
    }

    // ==================== AUCTION CONFIGURATION ====================
    
    /**
     * Get default auction duration in hours.
     * Default: 24 hours (can be configured in server.properties)
     */
    public long getDefaultAuctionDurationHours() {
        return Long.parseLong(props.getProperty("auction.default.duration.hours", "24").trim());
    }

    /**
     * Get default auction duration as Java Duration.
     */
    public Duration getDefaultAuctionDuration() {
        return Duration.ofHours(getDefaultAuctionDurationHours());
    }

    /**
     * Get minimum time interval (in seconds) between bids from the same bidder.
     * Default: 5 seconds (prevents spam bidding)
     */
    public long getMinBidIntervalSeconds() {
        return Long.parseLong(props.getProperty("auction.min.bid.interval.seconds", "5").trim());
    }

    /**
     * Get minimum time interval as Java Duration.
     */
    public Duration getMinBidInterval() {
        return Duration.ofSeconds(getMinBidIntervalSeconds());
    }
}