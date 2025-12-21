package com.arms.gui.controllers;

import com.arms.domain.User;
import com.arms.gui.util.AlertHelper;
import com.arms.gui.util.NavigationHelper;
import com.arms.gui.util.ValidationHelper;
import com.arms.service.AuthService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.Notifications;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Hyperlink forgotPasswordLink;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private StackPane progressPane;
    @FXML
    private CheckBox rememberMeCheck;
    @FXML
    private javafx.scene.control.ComboBox<String> roleChoice;

    private final AuthService authService = AuthService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEventHandlers();
        loadSavedCredentials();
        // Initialize role choice for quick signup/continue
        roleChoice.getItems().addAll("STUDENT", "TEACHER", "ADMIN");
        roleChoice.setValue("STUDENT");
    }

    private void setupEventHandlers() {
        // Enter key to login
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        // Real-time validation
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateForm();
        });

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateForm();
        });

        loginButton.setOnAction(event -> handleLogin());

        forgotPasswordLink.setOnAction(event -> handleForgotPassword());
    }

    private void loadSavedCredentials() {
        // Load saved credentials from preferences
        // Implementation depends on your preference storage
    }

    private void validateForm() {
        boolean isValid = !usernameField.getText().trim().isEmpty()
                && !passwordField.getText().trim().isEmpty();
        loginButton.setDisable(!isValid);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (!ValidationHelper.isValidUsername(username)) {
            AlertHelper.showError("Invalid Username",
                    "Username must be 3-20 characters and contain only letters, numbers, and underscores.");
            return;
        }

        if (!ValidationHelper.isValidPassword(password)) {
            AlertHelper.showError("Invalid Password",
                    "Password must be at least 8 characters long.");
            return;
        }

        showProgress(true);

        Task<Optional<User>> loginTask = new Task<>() {
            @Override
            protected Optional<User> call() {
                return authService.login(username, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            showProgress(false);
            Optional<User> userOpt = loginTask.getValue();

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                saveCredentialsIfNeeded();
                navigateToDashboard(user);
            } else {
                AlertHelper.showError("Login Failed",
                        "Invalid username or password. Please try again.");
                passwordField.clear();
                passwordField.requestFocus();

                // Shake animation for failed login
                animateFailedLogin();
            }
        });

        loginTask.setOnFailed(event -> {
            showProgress(false);
            AlertHelper.showError("Login Error",
                    "An error occurred during login. Please try again.");
        });

        new Thread(loginTask).start();
    }

    private void navigateToDashboard(User user) {
        switch (user.getRole()) {
            case STUDENT:
                NavigationHelper.navigateTo("/com/arms/gui/views/StudentDashboard.fxml");
                break;
            case TEACHER:
                NavigationHelper.navigateTo("/com/arms/gui/views/TeacherDashboard.fxml");
                break;
            case ADMIN:
            case SUPER_ADMIN:
                NavigationHelper.navigateTo("/com/arms/gui/views/AdminDashboard.fxml");
                break;
        }

        // Show welcome notification
        Platform.runLater(() -> {
            Notifications.create()
                    .title("Welcome")
                    .text("Welcome back, " + user.getFirstName() + "!")
                    .showInformation();
        });
    }

    private void handleForgotPassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Password Recovery");
        dialog.setHeaderText("Enter your email address");
        dialog.setContentText("Email:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            if (ValidationHelper.isValidEmail(email)) {
                AlertHelper.showInfo("Recovery Email Sent",
                        "If an account exists with this email, you will receive recovery instructions.");
            } else {
                AlertHelper.showError("Invalid Email",
                        "Please enter a valid email address.");
            }
        });
    }

    @FXML
    private void handleShowSignup() {
        String role = roleChoice.getValue();
        // Open signup view with role preselected
        NavigationHelper.showDialog("/com/arms/gui/views/SignupView.fxml", "Create Account");
        Object ctrl = NavigationHelper.getController("/com/arms/gui/views/SignupView.fxml");
        if (ctrl instanceof com.arms.gui.controllers.SignupController sc && role != null) {
            sc.selectRole(role);
        }
    }

    private void saveCredentialsIfNeeded() {
        if (rememberMeCheck.isSelected()) {
            // Save credentials securely
        }
    }

    private void showProgress(boolean show) {
        progressPane.setVisible(show);
        loginButton.setDisable(show);
        usernameField.setDisable(show);
        passwordField.setDisable(show);
    }

    private void animateFailedLogin() {
        // Add shake animation to login form
        // Implementation depends on your animation preferences
    }
}
