package com.arms;

import com.arms.gui.util.NavigationHelper;
import com.arms.util.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        try {
            // Initialize logger
            Logger.info("Starting Academic Records Management System");

            // Set application icon (optional)
            try {
                java.net.URL iconUrl = getClass().getResource("/com/arms/gui/images/app-icon.png");
                if (iconUrl != null) {
                    Image appIcon = new Image(iconUrl.toExternalForm());
                    stage.getIcons().add(appIcon);
                } else {
                    Logger.info("App icon not found, continuing without icon");
                }
            } catch (Exception ex) {
                Logger.error("Failed to load app icon", ex);
            }

            // Configure stage
            stage.setTitle("Academic Records Management System");
            stage.initStyle(StageStyle.DECORATED);

            // Set up scene
            Scene scene = NavigationHelper.createScene("/com/arms/gui/views/LoginView.fxml");

            // Apply CSS (optional)
            java.net.URL cssUrl = getClass().getResource("/com/arms/gui/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                Logger.info("Global stylesheet not found: /com/arms/gui/css/styles.css");
            }

            // Set up stage
            stage.setScene(scene);
            stage.setMinWidth(1200);
            stage.setMinHeight(800);
            stage.show();

            // Center on screen
            stage.centerOnScreen();

            // Handle close request
            stage.setOnCloseRequest(event -> {
                event.consume();
                handleExit();
            });

            Logger.info("Application started successfully");

        } catch (Exception e) {
            Logger.error("Failed to start application", e);
            Platform.exit();
        }
    }

    private void handleExit() {
        if (NavigationHelper.showConfirmation("Exit Application",
                "Are you sure you want to exit? Any unsaved changes will be lost.")) {

            Logger.info("Application shutting down");

            // Perform cleanup
            cleanup();

            Platform.exit();
            System.exit(0);
        }
    }

    private void cleanup() {
        // Save any pending data
        // Close database connections
        // Clean up temporary files
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
