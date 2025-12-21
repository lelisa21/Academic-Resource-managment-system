package com.arms.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.arms.domain.Admin;
import com.arms.domain.Student;
import com.arms.domain.Teacher;
import com.arms.domain.User;
import com.arms.domain.enums.UserRole;
import com.arms.gui.util.AlertHelper;
import com.arms.gui.util.NavigationHelper;
import com.arms.service.UserService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignupController implements Initializable {

    @FXML
    private ComboBox<String> roleCombo;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button createButton;

    private final UserService userService = UserService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleCombo.getItems().addAll("STUDENT", "TEACHER", "ADMIN");
        roleCombo.setValue("STUDENT");
    }

    public void selectRole(String role) {
        if (role != null) {
            if (!roleCombo.getItems().contains(role)) {
                roleCombo.getItems().add(role);
            }
            roleCombo.setValue(role);
        }
    }

    @FXML
    private void handleCreateAccount() {
        String role = roleCombo.getValue();
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            AlertHelper.showWarning("Missing Fields", "Please complete username, email and password.");
            return;
        }

        if (!pass.equals(confirm)) {
            AlertHelper.showError("Password Mismatch", "Password and confirmation do not match.");
            return;
        }

        User user;
        if ("TEACHER".equalsIgnoreCase(role)) {
            Teacher t = new Teacher();
            t.setDepartment("");
            user = t;
            user.setRole(UserRole.TEACHER);
        } else if ("ADMIN".equalsIgnoreCase(role)) {
            Admin a = new Admin();
            user = a;
            user.setRole(UserRole.ADMIN);
        } else {
            Student s = new Student();
            user = s;
            user.setRole(UserRole.STUDENT);
        }

        user.setFirstName(first);
        user.setLastName(last);
        user.setUsername(username);
        user.setEmail(email);

        boolean ok = userService.createUser(user, pass).isPresent();
        if (ok) {
            AlertHelper.showSuccess("Account Created", "Your account has been created. You may now log in.");
            // Close dialog and navigate to login
            NavigationHelper.navigateTo("/com/arms/gui/views/LoginView.fxml");
        } else {
            AlertHelper.showError("Create Failed", "Could not create account. Username or email may already exist.");
        }
    }

    @FXML
    private void handleCancel() {
        // Close dialog and return to login view
        NavigationHelper.navigateTo("/com/arms/gui/views/LoginView.fxml");
    }
}
