package com.auction.common.utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$"
    );
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,20}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[0-9]{10,11}$"
    );

    // Validate username (3-20 chars, only letters, numbers, underscore)
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    // Validate email format
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    // Validate password (at least 6 characters)
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Validate positive price
    public static boolean isValidPrice(double price) {
        return price > 0;
    }

    // Validate bid amount
    public static boolean isValidBid(double currentPrice, double bidAmount, double minIncrement) {
        return bidAmount >= currentPrice + minIncrement;
    }

    // Validate phone number (10 or 11 digits)
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    // Check if string is null or empty
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}