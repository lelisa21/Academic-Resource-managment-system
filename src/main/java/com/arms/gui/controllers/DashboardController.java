package com.arms.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.arms.domain.User;
import com.arms.gui.util.AlertHelper;
import com.arms.gui.util.NavigationHelper;
import com.arms.service.AuthService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public abstract class DashboardController implements Initializable {
    
    @FXML protected Label welcomeLabel;
    @FXML protected Label roleLabel;
    @FXML protected ImageView profileImage;
    @FXML protected VBox sidebar;
    
    protected User currentUser;
    protected final AuthService authService = AuthService.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            NavigationHelper.navigateToLogin();
            return;
        }
        
        initializeUI();
        loadDashboardData();
    }
    
    protected void initializeUI() {
    if (welcomeLabel != null) {
        welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + "!");
    }
    
    if (roleLabel != null) {
        roleLabel.setText(currentUser.getRole().toString());
    }
    
    // Load profile image if exists
    if (profileImage != null && currentUser.getProfileImagePath() != null) {
        try {
            Image image = new Image("file:" + currentUser.getProfileImagePath());
            profileImage.setImage(image);
        } catch (Exception e) {
            // Use default image
            try {
                profileImage.setImage(new Image(getClass().getResourceAsStream(
                    "/com/arms/gui/images/default-avatar.png")));
            } catch (Exception ex) {
                // Silently fail if default image not found
            }
        }
    }
}
    
    protected abstract void loadDashboardData();
    
    @FXML
    protected void handleLogout() {
        if (AlertHelper.showConfirmation("Confirm Logout", 
            "Are you sure you want to logout?")) {
            authService.logout();
            NavigationHelper.navigateToLogin();
        }
    }
    
    @FXML
    protected void handleProfile() {
        NavigationHelper.navigateTo("/com/arms/gui/views/ProfileView.fxml");
    }
    
    @FXML
    protected void handleSettings() {
        NavigationHelper.navigateTo("/com/arms/gui/views/SettingsView.fxml");
    }
    
    @FXML
    protected void handleHelp() {
        NavigationHelper.showHelpDialog();
    }
}
