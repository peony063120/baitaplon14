package com.auction.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtils Tests")
class ValidationUtilsTest {

    // ======== isValidUsername ========

    @Test
    @DisplayName("Username hợp lệ")
    void testIsValidUsername_valid() {
        assertTrue(ValidationUtils.isValidUsername("alice123"));
        assertTrue(ValidationUtils.isValidUsername("user_name"));
        assertTrue(ValidationUtils.isValidUsername("ABC"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "this_username_is_too_long_xyz", "has space", "has@symbol"})
    @DisplayName("Username không hợp lệ")
    void testIsValidUsername_invalid(String username) {
        assertFalse(ValidationUtils.isValidUsername(username));
    }

    @Test
    @DisplayName("Username null -> false")
    void testIsValidUsername_null() {
        assertFalse(ValidationUtils.isValidUsername(null));
    }

    // ======== isValidEmail ========

    @Test
    @DisplayName("Email hợp lệ")
    void testIsValidEmail_valid() {
        assertTrue(ValidationUtils.isValidEmail("test@gmail.com"));
        assertTrue(ValidationUtils.isValidEmail("user.name+tag@example.org"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-an-email", "missing@", "@nodomain", ""})
    @DisplayName("Email không hợp lệ")
    void testIsValidEmail_invalid(String email) {
        assertFalse(ValidationUtils.isValidEmail(email));
    }

    @Test
    @DisplayName("Email null -> false")
    void testIsValidEmail_null() {
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    // ======== isValidPassword ========

    @Test
    @DisplayName("Password >= 6 ky tu -> hop le")
    void testIsValidPassword_valid() {
        assertTrue(ValidationUtils.isValidPassword("123456"));
        assertTrue(ValidationUtils.isValidPassword("securePassword!"));
    }

    @Test
    @DisplayName("Password < 6 ky tu -> khong hop le")
    void testIsValidPassword_tooShort() {
        assertFalse(ValidationUtils.isValidPassword("12345"));
        assertFalse(ValidationUtils.isValidPassword(""));
    }

    @Test
    @DisplayName("Password null -> false")
    void testIsValidPassword_null() {
        assertFalse(ValidationUtils.isValidPassword(null));
    }

    // ======== isValidPrice ========

    @Test
    @DisplayName("Gia > 0 -> hop le")
    void testIsValidPrice_valid() {
        assertTrue(ValidationUtils.isValidPrice(1.0));
        assertTrue(ValidationUtils.isValidPrice(1000000));
    }

    @Test
    @DisplayName("Gia <= 0 -> khong hop le")
    void testIsValidPrice_invalid() {
        assertFalse(ValidationUtils.isValidPrice(0));
        assertFalse(ValidationUtils.isValidPrice(-100));
    }

    // ======== isValidBid ========

    @Test
    @DisplayName("Bid hop le - bidAmount >= currentPrice + minIncrement")
    void testIsValidBid_valid() {
        assertTrue(ValidationUtils.isValidBid(1000000, 1100000, 100000));
        assertTrue(ValidationUtils.isValidBid(500, 600, 100));
    }

    @Test
    @DisplayName("Bid khong hop le - bidAmount < currentPrice + minIncrement")
    void testIsValidBid_tooLow() {
        assertFalse(ValidationUtils.isValidBid(1000000, 1050000, 100000));
        assertFalse(ValidationUtils.isValidBid(500, 500, 100));
    }

    // ======== isValidPhone ========

    @Test
    @DisplayName("SDT 10-11 so -> hop le")
    void testIsValidPhone_valid() {
        assertTrue(ValidationUtils.isValidPhone("0123456789"));
        assertTrue(ValidationUtils.isValidPhone("01234567890"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"012345678", "012345678901", "abcdefghij", ""})
    @DisplayName("SDT khong hop le")
    void testIsValidPhone_invalid(String phone) {
        assertFalse(ValidationUtils.isValidPhone(phone));
    }

    // ======== isNullOrEmpty ========

    @Test
    @DisplayName("Null hoac empty -> true")
    void testIsNullOrEmpty_true() {
        assertTrue(ValidationUtils.isNullOrEmpty(null));
        assertTrue(ValidationUtils.isNullOrEmpty(""));
        assertTrue(ValidationUtils.isNullOrEmpty("   "));
    }

    @Test
    @DisplayName("String co noi dung -> false")
    void testIsNullOrEmpty_false() {
        assertFalse(ValidationUtils.isNullOrEmpty("hello"));
        assertFalse(ValidationUtils.isNullOrEmpty(" x "));
    }
}
