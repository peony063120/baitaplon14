package com.auction.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    // Format LocalDateTime to string
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DEFAULT_FORMATTER) : "N/A";
    }
    // Format with custom pattern
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern(pattern)) : "N/A";
    }
    // Parse string to LocalDateTime
    public static LocalDateTime parseDateTime(String datetimeStr, String pattern) {
        return LocalDateTime.parse(datetimeStr, DateTimeFormatter.ofPattern(pattern));
    }
    // Get remaining time in milliseconds
    public static long getRemainingMillis(LocalDateTime endTime) {
        if (endTime == null) return 0;
        return Math.max(0, ChronoUnit.MILLIS.between(LocalDateTime.now(), endTime));
    }

    // Get remaining time as human readable string (e.g., "02d 05h 10m 30s")
    public static String getRemainingTimeString(LocalDateTime endTime) {
        long millis = getRemainingMillis(endTime);
        if (millis <= 0) return "Ended";

        long seconds = millis / 1000;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        if (days > 0) return String.format("%dd %02dh %02dm %02ds", days, hours, minutes, seconds);
        if (hours > 0) return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
        if (minutes > 0) return String.format("%02dm %02ds", minutes, seconds);
        return String.format("%02ds", seconds);
    }

    // Check if auction is ending soon (within X seconds)
    public static boolean isEndingSoon(LocalDateTime endTime, long thresholdSeconds) {
        return getRemainingMillis(endTime) <= thresholdSeconds * 1000;
    }
}
