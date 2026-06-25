package com.campuslink.utils;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[+]?[0-9]{7,15}$");

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone == null || phone.trim().isEmpty() || PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidDeadline(LocalDate deadline) {
        return deadline != null && !deadline.isBefore(LocalDate.now());
    }

    public static boolean isValidYearOfStudy(int year) {
        return year >= 1 && year <= 7;
    }

    public static boolean isPasswordMatch(String password, String confirm) {
        return password != null && password.equals(confirm) && password.length() >= 6;
    }
}
