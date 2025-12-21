package com.arms.gui.util;

/**
 * Thin adapter so GUI code can import from com.arms.gui.util.ValidationHelper
 * while core validation lives in com.arms.util.ValidationHelper.
 */
public final class ValidationHelper {

    private ValidationHelper() {
    }

    public static boolean isValidEmail(String email) {
        return com.arms.util.ValidationHelper.isValidEmail(email);
    }

    public static boolean isValidUsername(String username) {
        return com.arms.util.ValidationHelper.isValidUsername(username);
    }

    public static boolean isValidPassword(String password) {
        return com.arms.util.ValidationHelper.isValidPassword(password);
    }

    public static boolean isValidPhone(String phone) {
        return com.arms.util.ValidationHelper.isValidPhone(phone);
    }

    public static boolean isValidCourseCode(String courseCode) {
        return com.arms.util.ValidationHelper.isValidCourseCode(courseCode);
    }

    public static boolean isNumeric(String str) {
        return com.arms.util.ValidationHelper.isNumeric(str);
    }
}
