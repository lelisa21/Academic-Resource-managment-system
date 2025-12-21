package com.arms.gui.util;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.arms.App;
import com.arms.util.Logger;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class NavigationHelper {

    private static final Map<String, Object> controllers = new HashMap<>();

    public static void navigateTo(String fxmlPath) {
        try {
            URL fxmlUrl = NavigationHelper.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Store controller if needed
            Object controller = loader.getController();
            if (controller != null) {
                controllers.put(fxmlPath, controller);
            }

            Scene scene = App.getPrimaryStage().getScene();
            if (scene == null) {
                scene = new Scene(root);
                App.getPrimaryStage().setScene(scene);
            } else {
                scene.setRoot(root);
            }

            App.getPrimaryStage().sizeToScene();
            App.getPrimaryStage().centerOnScreen();

        } catch (IOException e) {
            Logger.error("Failed to navigate to: " + fxmlPath, e);
            AlertHelper.showError("Navigation Error",
                    "Failed to load view: " + fxmlPath + "\n" + e.getMessage());
        }
    }

    public static void navigateToLogin() {
        navigateTo("/com/arms/gui/views/LoginView.fxml");
    }

    public static Scene createScene(String fxmlPath) throws IOException {
        URL fxmlUrl = NavigationHelper.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            throw new IOException("FXML file not found: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        // Store controller
        Object controller = loader.getController();
        if (controller != null) {
            controllers.put(fxmlPath, controller);
        }

        return new Scene(root);
    }

    public static void showDialog(String fxmlPath, String title) {
        try {
            URL fxmlUrl = NavigationHelper.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("FXML file not found: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(App.getPrimaryStage());
            dialogStage.initStyle(StageStyle.UTILITY);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (IOException e) {
            Logger.error("Failed to show dialog: " + fxmlPath, e);
            AlertHelper.showError("Dialog Error",
                    "Failed to load dialog: " + fxmlPath);
        }
    }

    public static void showModalDialog(String fxmlPath, String title) {
        showDialog(fxmlPath, title);
    }

    public static Object getController(String fxmlPath) {
        return controllers.get(fxmlPath);
    }

    public static void clearControllers() {
        controllers.clear();
    }

    public static boolean showConfirmation(String title, String message) {
        return AlertHelper.showConfirmation(title, message);
    }

    public static void showHelpDialog() {
        AlertHelper.showInfo("Help",
                "Academic Records Management System\n\n"
                + "Version: 1.0.0\n"
                + "For assistance, please contact system administrator.\n\n"
                + "Keyboard Shortcuts:\n"
                + "• Ctrl+S: Save\n"
                + "• Ctrl+Q: Logout\n"
                + "• F1: Help\n"
                + "• F5: Refresh");
    }
}
