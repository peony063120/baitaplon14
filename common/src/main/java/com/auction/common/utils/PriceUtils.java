package com.auction.common.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class PriceUtils {

    private static final NumberFormat VND_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final NumberFormat USD_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US);

    static {
        VND_FORMATTER.setMaximumFractionDigits(0);
        USD_FORMATTER.setMaximumFractionDigits(2);
    }

    // Format as VND currency (e.g., "1,000,000 ₫")
    public static String formatVND(double amount) {
        return VND_FORMATTER.format(amount);
    }

    // Format as USD currency (e.g., "$100.00")
    public static String formatUSD(double amount) {
        return USD_FORMATTER.format(amount);
    }

    // Simple format with 2 decimal places (no currency symbol)
    public static String formatPlain(double amount) {
        return String.format("%.2f", amount);
    }

    // Calculate next bid amount based on current price and increment
    public static double calculateNextBid(double currentPrice, double increment) {
        return currentPrice + increment;
    }

    // Calculate max bid for auto-bidder (cannot exceed maxBid)
    public static double calculateAutoBid(double currentPrice, double increment, double maxBid) {
        double next = currentPrice + increment;
        return Math.min(next, maxBid);
    }

    // Check if bid is valid for auto-bidder
    public static boolean canAutoBid(double currentPrice, double increment, double maxBid) {
        return (currentPrice + increment) <= maxBid;
    }

    // Round to 2 decimal places
    public static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}