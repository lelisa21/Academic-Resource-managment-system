package com.arms.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final String CONFIG_FILE = "application.properties";
    private static final Properties properties = new Properties();
    
    static {
        loadConfig();
    }
    
    private static void loadConfig() {
        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                // Load defaults
                properties.setProperty("app.name", "Academic Records Management System");
                properties.setProperty("app.version", "1.0.0");
                properties.setProperty("app.data.directory", "data/");
                properties.setProperty("app.max.login.attempts", "3");
                properties.setProperty("app.session.timeout.minutes", "30");
                properties.setProperty("app.backup.enabled", "true");
                properties.setProperty("app.backup.interval.hours", "24");
                properties.setProperty("app.log.level", "INFO");
                properties.setProperty("app.cache.enabled", "true");
                properties.setProperty("app.cache.size", "1000");
            }
        } catch (IOException e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
        }
    }
    
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public static int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}
