package com.arms.util;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class Logger {
    private static final String LOG_FILE = "logs/arms.log";
    private static final DateTimeFormatter formatter = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static LogLevel minLevel = LogLevel.INFO;
    
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }  
    static {
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("logs/"));
        } catch (IOException e) {
            System.err.println("Failed to create logs directory: " + e.getMessage());
        }
    }
    public static void setMinLevel(LogLevel level) {
        minLevel = level;
    }
    
    public static void debug(String message) {
        log(LogLevel.DEBUG, message, null);
    } 
    public static void info(String message) {
        log(LogLevel.INFO, message, null);
    }
    
    public static void warn(String message) {
        log(LogLevel.WARN, message, null);
    }
    
    public static void error(String message) {
        log(LogLevel.ERROR, message, null);
    }
    
    public static void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }
    
    private static void log(LogLevel level, String message, Throwable throwable) {
        if (level.ordinal() < minLevel.ordinal()) {
            return;
        }
        
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[%s] [%s] %s", 
            timestamp, level, message);   
        System.out.println(logMessage);
        if (throwable != null) {
            throwable.printStackTrace();
        }
        try (PrintWriter out = new PrintWriter(
            new BufferedWriter(new FileWriter(LOG_FILE, true)))) {
            out.println(logMessage);
            if (throwable != null) {
                throwable.printStackTrace(out);
            }
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
        if (level == LogLevel.ERROR) {
            sendToMonitoringService(logMessage, throwable);
        }
    }
    
    private static void sendToMonitoringService(String message, Throwable throwable) {
       
    }
}
