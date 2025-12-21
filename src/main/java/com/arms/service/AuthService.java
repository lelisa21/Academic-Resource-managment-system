package com.arms.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

import com.arms.domain.User;
import com.arms.domain.enums.UserRole;
import com.arms.persistence.DataManager;
import com.arms.persistence.IDGenerator;
import com.arms.util.Logger;
import com.arms.util.ValidationHelper;

public class AuthService {
    private static AuthService instance;
    private final DataManager dataManager;
    private User currentUser;
    
    private AuthService() {
        this.dataManager = DataManager.getInstance();
    }
    
    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = dataManager.findUserByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                if (!user.isActive()) {
                    Logger.warn("Login attempt for inactive user: " + username);
                    return Optional.empty();
                }
                
                user.updateLastLogin();
                dataManager.saveUser(user);
                currentUser = user;
                
                Logger.info("User logged in: " + username + " [" + user.getRole() + "]");
                return Optional.of(user);
            }
        }
        
        Logger.warn("Failed login attempt for username: " + username);
        return Optional.empty();
    }
    
    public boolean registerUser(User user, String password) {
        if (!ValidationHelper.isValidEmail(user.getEmail())) {
            return false;
        }
        
        if (dataManager.findUserByUsername(user.getUsername()).isPresent()) {
            return false;
        }
        
        user.setId(IDGenerator.generateUserId());
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(12)));
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        
        dataManager.saveUser(user);
        Logger.info("New user registered: " + user.getUsername());
        return true;
    }
    
    public void logout() {
        if (currentUser != null) {
            Logger.info("User logged out: " + currentUser.getUsername());
            currentUser = null;
        }
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isAuthenticated() {
        return currentUser != null;
    }
    
    public boolean hasRole(UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }
    
    public boolean changePassword(String oldPassword, String newPassword) {
        if (currentUser == null) {
            return false;
        }
        
        if (!BCrypt.checkpw(oldPassword, currentUser.getPasswordHash())) {
            return false;
        }
        
        currentUser.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)));
        dataManager.saveUser(currentUser);
        Logger.info("Password changed for user: " + currentUser.getUsername());
        return true;
    }
    
    public Optional<User> getUserById(String userId) {
        return Optional.ofNullable(dataManager.getUsers().get(userId));
    }
}
