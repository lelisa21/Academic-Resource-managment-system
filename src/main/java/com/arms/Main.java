package com.arms;

import com.arms.config.AppConfig;
import com.arms.gui.util.AlertHelper;
import com.arms.util.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main {
    public static void main(String[] args) {
        // Initialize configuration
        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");
        
        // Set up global exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Logger.error("Uncaught exception in thread: " + thread.getName(), throwable);
            Platform.runLater(() -> {
                AlertHelper.showError("Application Error", 
                    "An unexpected error occurred. The application may become unstable.");
            });
        });
        
        // Launch JavaFX application
        Application.launch(App.class, args);
    }
}
