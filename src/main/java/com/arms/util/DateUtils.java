package com.arms.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    
    private static final DateTimeFormatter DISPLAY_FORMATTER = 
        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_ONLY_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FILE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    public static String formatDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DISPLAY_FORMATTER);
    }
    
    public static String formatDateOnly(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DATE_ONLY_FORMATTER);
    }
    
    public static String formatTimeOnly(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(TIME_ONLY_FORMATTER);
    }
    
    public static String formatForFilename(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(FILE_FORMATTER);
    }
    
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(dateTimeStr, DISPLAY_FORMATTER);
        } catch (DateTimeParseException e) {
            // Try other formats
            try {
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e2) {
                Logger.error("Failed to parse date time: " + dateTimeStr, e2);
                return null;
            }
        }
    }
    
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(dateStr, DATE_ONLY_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e2) {
                Logger.error("Failed to parse date: " + dateStr, e2);
                return null;
            }
        }
    }
    
    public static boolean isBetween(LocalDateTime date, LocalDateTime start, LocalDateTime end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        return !date.isBefore(start) && !date.isAfter(end);
    }
    
    public static boolean isWithinDays(LocalDateTime date, int days) {
        if (date == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return date.isAfter(now) && date.isBefore(now.plusDays(days));
    }
    
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }
    
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }
    
    public static boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.getDayOfWeek().getValue() >= 6; // Saturday = 6, Sunday = 7
    }
    
    public static LocalDateTime getStartOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }
    
    public static LocalDateTime getEndOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(23, 59, 59);
    }
    
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Unknown";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (minutes < 1440) {
            long hours = minutes / 60;
            return hours + " hours ago";
        } else {
            long days = minutes / 1440;
            return days + " days ago";
        }
    }
}
