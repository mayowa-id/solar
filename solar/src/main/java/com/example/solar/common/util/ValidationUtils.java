package com.example.solar.common.util;


import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[1-9]\\d{1,14}$");


    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // Remove spaces, hyphens, and parentheses
        String cleanPhone = phone.replaceAll("[\\s\\-()]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }


    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }


    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.trim();
    }
}