package com.arms.persistence;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class IDGenerator {
    private static final SecureRandom random = new SecureRandom();
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final AtomicLong counter = new AtomicLong(0);
    
    private static final String CHARACTERS = 
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
    public static String generateUserId() {
        return "USR-" + generateTimestampId();
    }
    
    public static String generateStudentId() {
        return "STU-" + generateTimestampId();
    }
    
    public static String generateTeacherId() {
        return "TCH-" + generateTimestampId();
    }
    
    public static String generateAdminId() {
        return "ADM-" + generateTimestampId();
    }
    
    public static String generateCourseId() {
        return "CRS-" + generateTimestampId();
    }
    
    public static String generateAssignmentId() {
        return "ASG-" + generateTimestampId();
    }
    
    public static String generateGradeId() {
        return "GRD-" + generateTimestampId();
    }
    
    public static String generateEnrollmentId() {
        return "ENR-" + generateTimestampId();
    }
    
    private static String generateTimestampId() {
        String timestamp = LocalDateTime.now().format(formatter);
        long seq = counter.incrementAndGet() % 1000;
        String randomStr = generateRandomString(4);
        return timestamp + String.format("%03d", seq) + randomStr;
    }
    
    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
    
    public static String generateSecureToken() {
        return generateRandomString(32);
    }
    
    public static String generateVerificationCode() {
        return String.format("%06d", random.nextInt(1000000));
    }
}
