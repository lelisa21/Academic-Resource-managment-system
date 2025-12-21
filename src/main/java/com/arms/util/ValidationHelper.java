package com.arms.util;

import java.util.regex.Pattern;

public class ValidationHelper {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^.{8,}$");
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final Pattern COURSE_CODE_PATTERN = 
        Pattern.compile("^[A-Z]{3,4}-[0-9]{3,4}$");
    
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }
    
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
    
    public static boolean isValidCourseCode(String courseCode) {
        return courseCode != null && COURSE_CODE_PATTERN.matcher(courseCode).matches();
    }
    
    public static boolean isValidGrade(double grade, double maxGrade) {
        return grade >= 0 && grade <= maxGrade;
    }
    
    public static boolean isValidCreditHours(int credits) {
        return credits > 0 && credits <= 6;
    }
    
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim()
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#x27;");
    }
    
    public static boolean isNumeric(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }
}
