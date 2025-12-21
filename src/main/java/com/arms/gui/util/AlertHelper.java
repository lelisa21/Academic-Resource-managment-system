package com.arms.gui.util;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

public class AlertHelper {

    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, null, message);
    }

    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, null, message);
    }

    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, null, message);
    }

    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Customize for success
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add("success-alert");

        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Customize buttons
        Button okBtn = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        Button cancelBtn = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (okBtn != null) {
            okBtn.setText("Yes");
        }
        if (cancelBtn != null) {
            cancelBtn.setText("No");
        }

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static Optional<ButtonType> showCustomConfirmation(String title, String message,
            ButtonType... buttonTypes) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, buttonTypes);
        alert.setTitle(title);
        alert.setHeaderText(null);
        return alert.showAndWait();
    }

    private static void showAlert(Alert.AlertType type, String title,
            String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initStyle(StageStyle.UTILITY);

        // Apply custom styling
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStyleClass().add(type.name().toLowerCase() + "-alert");

        alert.showAndWait();
    }

    public static void showException(String title, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("An error occurred");
        alert.setContentText(exception.getMessage());

        // Create expandable Exception
        String exceptionText = getExceptionText(exception);

        alert.getDialogPane().setExpandableContent(new javafx.scene.control.TextArea(exceptionText));
        alert.getDialogPane().setExpanded(false);

        alert.showAndWait();
    }

    private static String getExceptionText(Exception exception) {
        StringBuilder sb = new StringBuilder();
        sb.append(exception.toString()).append("\n");

        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append("    at ").append(element).append("\n");
        }

        Throwable cause = exception.getCause();
        if (cause != null) {
            sb.append("\nCaused by: ").append(cause.toString()).append("\n");
            for (StackTraceElement element : cause.getStackTrace()) {
                sb.append("    at ").append(element).append("\n");
            }
        }

        return sb.toString();
    }
}
