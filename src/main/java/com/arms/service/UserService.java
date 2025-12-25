package com.arms.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mindrot.jbcrypt.BCrypt;

import com.arms.domain.Admin;
import com.arms.domain.Student;
import com.arms.domain.Teacher;
import com.arms.domain.User;
import com.arms.domain.enums.UserRole;
import com.arms.domain.enums.UserStatus;
import com.arms.persistence.DataManager;
import com.arms.persistence.IDGenerator;

public class UserService {

    private static UserService instance;
    private final DataManager dataManager;

    private UserService() {
        this.dataManager = DataManager.getInstance();
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public Optional<User> getUserById(String id) {
        return Optional.ofNullable(dataManager.getUsers().get(id));
    }

    public Optional<User> getUserByUsername(String username) {
        return dataManager.getUsers().values().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public Optional<User> getUserByEmail(String email) {
        return dataManager.getUsers().values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(dataManager.getUsers().values());
    }

    public List<User> getUsersByRole(UserRole role) {
        return dataManager.getUsers().values().stream()
                .filter(user -> user.getRole() == role)
                .collect(Collectors.toList());
    }

    public Optional<User> createUser(User user, String password) {
        // Check if username already exists
        if (getUserByUsername(user.getUsername()).isPresent()) {
            return Optional.empty();
        }

        // Check if email already exists
        if (getUserByEmail(user.getEmail()).isPresent()) {
            return Optional.empty();
        }

        // Set user properties
        user.setId(IDGenerator.generateUserId());
        user.setPasswordHash(hashPassword(password));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        user.setEmailVerified(false);

        // Set role-specific IDs
        if (user instanceof Student student) {
            student.setStudentId(IDGenerator.generateStudentId());
        } else if (user instanceof Teacher teacher) {
            teacher.setEmployeeId(IDGenerator.generateTeacherId());
        } else if (user instanceof Admin admin) {
            admin.setAdminId(IDGenerator.generateAdminId());
        }

        // Save user
        dataManager.saveUser(user);
        return Optional.of(user);
    }

    public boolean updateUser(User user) {
        User existing = dataManager.getUsers().get(user.getId());
        if (existing == null) {
            return false;
        }

        // Preserve password hash if not being changed
        if (user.getPasswordHash() == null) {
            user.setPasswordHash(existing.getPasswordHash());
        }

        // Preserve timestamps
        user.setCreatedAt(existing.getCreatedAt());
        user.setLastLogin(existing.getLastLogin());

        dataManager.saveUser(user);
        return true;
    }

    public boolean deleteUser(String userId) {
        User user = dataManager.getUsers().get(userId);
        if (user == null) {
            return false;
        }

        // Check if user has dependencies
        if (user instanceof Student) {
            // Check enrollments
            long enrollments = dataManager.getEnrollments().values().stream()
                    .filter(e -> e.getStudentId().equals(userId))
                    .count();
            if (enrollments > 0) {
                return false;
            }
        } else if (user instanceof Teacher) {
            // Check courses assigned
            long courses = dataManager.getCourses().values().stream()
                    .filter(c -> c.getTeacherId().equals(userId))
                    .count();
            if (courses > 0) {
                return false;
            }
        }

        dataManager.deleteUser(userId);
        return true;
    }

    public boolean updatePassword(String userId, String newPassword) {
        User user = dataManager.getUsers().get(userId);
        if (user == null) {
            return false;
        }

        user.setPasswordHash(hashPassword(newPassword));
        dataManager.saveUser(user);
        return true;
    }

    public boolean verifyPassword(String username, String password) {
        Optional<User> userOpt = getUserByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return BCrypt.checkpw(password, user.getPasswordHash());
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    public boolean activateUser(String userId) {
        User user = dataManager.getUsers().get(userId);
        if (user == null) {
            return false;
        }

        user.setStatus(UserStatus.ACTIVE);
        dataManager.saveUser(user);
        return true;
    }

    public boolean deactivateUser(String userId) {
        User user = dataManager.getUsers().get(userId);
        if (user == null) {
            return false;
        }

        user.setStatus(UserStatus.INACTIVE);
        dataManager.saveUser(user);
        return true;
    }

    public Map<UserRole, Long> getUserStatistics() {
        return dataManager.getUsers().values().stream()
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));
    }
    public List<User> searchUsers(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return dataManager.getUsers().values().stream()
                .filter(user
                        -> user.getUsername().toLowerCase().contains(lowerKeyword)
                || user.getEmail().toLowerCase().contains(lowerKeyword)
                || user.getFirstName().toLowerCase().contains(lowerKeyword)
                || user.getLastName().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }
}
