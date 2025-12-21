package com.arms.config;

import javafx.scene.paint.Color;

import java.util.prefs.Preferences;

public class ThemeConfig {
    
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeConfig.class);
    private static final String THEME_KEY = "application.theme";
    private static final String PRIMARY_COLOR_KEY = "theme.primary.color";
    private static final String FONT_SIZE_KEY = "theme.font.size";
    
    public enum Theme {
        LIGHT("Light"),
        DARK("Dark"),
        BLUE("Blue"),
        GREEN("Green");
        
        private final String displayName;
        
        Theme(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public static Theme getCurrentTheme() {
        String themeName = prefs.get(THEME_KEY, Theme.LIGHT.name());
        try {
            return Theme.valueOf(themeName);
        } catch (IllegalArgumentException e) {
            return Theme.LIGHT;
        }
    }
    
    public static void setTheme(Theme theme) {
        prefs.put(THEME_KEY, theme.name());
    }
    
    public static Color getPrimaryColor() {
        String colorHex = prefs.get(PRIMARY_COLOR_KEY, "#3498db");
        return Color.web(colorHex);
    }
    
    public static void setPrimaryColor(Color color) {
        prefs.put(PRIMARY_COLOR_KEY, color.toString());
    }
    
    public static int getFontSize() {
        return prefs.getInt(FONT_SIZE_KEY, 14);
    }
    
    public static void setFontSize(int size) {
        prefs.putInt(FONT_SIZE_KEY, size);
    }
    
    public static String getThemeCSS(Theme theme) {
        switch (theme) {
            case DARK:
                return """
                    .root {
                        -fx-base: #2c3e50;
                        -fx-background: #34495e;
                        -fx-control-inner-background: #2c3e50;
                        -fx-text-base-color: #ecf0f1;
                        -fx-accent: #3498db;
                    }
                    """;
            case BLUE:
                return """
                    .root {
                        -fx-base: #2980b9;
                        -fx-background: #3498db;
                        -fx-control-inner-background: #2980b9;
                        -fx-text-base-color: white;
                        -fx-accent: #e74c3c;
                    }
                    """;
            case GREEN:
                return """
                    .root {
                        -fx-base: #27ae60;
                        -fx-background: #2ecc71;
                        -fx-control-inner-background: #27ae60;
                        -fx-text-base-color: white;
                        -fx-accent: #e67e22;
                    }
                    """;
            case LIGHT:
            default:
                return """
                    .root {
                        -fx-base: #ecf0f1;
                        -fx-background: white;
                        -fx-control-inner-background: white;
                        -fx-text-base-color: #2c3e50;
                        -fx-accent: #3498db;
                    }
                    """;
        }
    }
    
    public static void resetToDefaults() {
        prefs.remove(THEME_KEY);
        prefs.remove(PRIMARY_COLOR_KEY);
        prefs.remove(FONT_SIZE_KEY);
    }
}
