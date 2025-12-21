package com.arms.util;

public class Constants {
    
    // Application Constants
    public static final String APP_NAME = "Academic Resource Management System";
    public static final String APP_VERSION = "1.0.0";
    public static final String APP_AUTHOR = "ARMS Development Team";
    public static final String APP_COPYRIGHT = "© 2026 ARMS. All rights reserved.";
    
    // File Paths
    public static final String DATA_DIRECTORY = "data/";
    public static final String CONFIG_FILE = "config/application.properties";
    public static final String LOG_FILE = "logs/arms.log";
    public static final String BACKUP_DIRECTORY = "data/backups/";
    
    // Database Constants
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final int SESSION_TIMEOUT_MINUTES = 30;
    public static final int MAX_FILE_SIZE_MB = 10;
    public static final int MAX_USERS = 1000;
    public static final int MAX_COURSES = 200;
    
    // Academic Constants
    public static final int MIN_CREDITS_PER_SEMESTER = 12;
    public static final int MAX_CREDITS_PER_SEMESTER = 21;
    public static final int MAX_COURSES_PER_STUDENT = 6;
    public static final int MAX_STUDENTS_PER_COURSE = 60;
    
    // Grade Constants
    public static final double A_GRADE_MIN = 90.0;
    public static final double B_GRADE_MIN = 80.0;
    public static final double C_GRADE_MIN = 70.0;
    public static final double D_GRADE_MIN = 60.0;
    public static final double F_GRADE_MAX = 59.9;
    
    // Date Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DISPLAY_DATE_FORMAT = "MMM dd, yyyy";
    public static final String DISPLAY_DATETIME_FORMAT = "MMM dd, yyyy HH:mm";
    
    // Validation Constants
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 50;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_PHONE_LENGTH = 20;
    
    // UI Constants
    public static final int WINDOW_MIN_WIDTH = 1200;
    public static final int WINDOW_MIN_HEIGHT = 800;
    public static final int DIALOG_MIN_WIDTH = 400;
    public static final int DIALOG_MIN_HEIGHT = 300;
    public static final int TABLE_MIN_HEIGHT = 300;
    
    // Error Messages
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid username or password.";
    public static final String ERROR_USER_INACTIVE = "Your account is inactive. Please contact administrator.";
    public static final String ERROR_SESSION_EXPIRED = "Your session has expired. Please login again.";
    public static final String ERROR_NO_PERMISSION = "You don't have permission to perform this action.";
    public static final String ERROR_DATA_NOT_FOUND = "Requested data not found.";
    public static final String ERROR_VALIDATION_FAILED = "Validation failed. Please check your input.";
    public static final String ERROR_SYSTEM_ERROR = "A system error occurred. Please try again.";
    
    // Success Messages
    public static final String SUCCESS_LOGIN = "Login successful!";
    public static final String SUCCESS_LOGOUT = "Logout successful!";
    public static final String SUCCESS_SAVE = "Data saved successfully!";
    public static final String SUCCESS_DELETE = "Data deleted successfully!";
    public static final String SUCCESS_UPDATE = "Data updated successfully!";
    public static final String SUCCESS_CREATE = "Created successfully!";
    
    // Default Values
    public static final String DEFAULT_PASSWORD = "password123";
    public static final String DEFAULT_DEPARTMENT = "Computer Science";
    public static final int DEFAULT_SEMESTER = 1;
    public static final int DEFAULT_CREDITS = 3;
    public static final int DEFAULT_MAX_STUDENTS = 30;
    
    // Cache Constants
    public static final int CACHE_MAX_SIZE = 1000;
    public static final int CACHE_EXPIRE_MINUTES = 30;
    
    private Constants() {
        // Prevent instantiation
    }
}
