package com.arms.gui.controllers;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.controlsfx.control.GridView;

import com.arms.domain.Admin;
import com.arms.domain.Course;
import com.arms.domain.User;
import com.arms.domain.enums.UserRole;
import com.arms.gui.components.UserCard;
import com.arms.gui.dialogs.AddCourseDialog;
import com.arms.gui.dialogs.AddUserDialog;
import com.arms.gui.util.AlertHelper;
import com.arms.gui.util.NavigationHelper;
import com.arms.persistence.DataManager;
import com.arms.persistence.FileHandler;
import com.arms.service.CourseService;
import com.arms.service.UserService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminController extends DashboardController {

    @FXML private Label adminIdLabel;
    @FXML private Label accessLevelLabel;

    // Updated to match AdminDashboard.fxml
    @FXML private GridView<User> usersGrid;
    @FXML private GridView<Course> coursesGrid;
    @FXML private ListView<String> logsListView;
    @FXML private HBox statisticsContainer;
    @FXML private PieChart userDistributionChart;

    @FXML private TabPane mainTabPane;
    @FXML private ProgressIndicator loadingIndicator;

    private final UserService userService = UserService.getInstance();
    private final CourseService courseService = CourseService.getInstance();

    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final ObservableList<Course> courses = FXCollections.observableArrayList();
    private final ObservableList<String> logs = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        if (currentUser instanceof Admin admin) {
            initializeAdminUI(admin);
            loadAdminData();
        } else {
            AlertHelper.showError("Access Denied", "Admins only.");
            NavigationHelper.navigateToLogin();
        }
    }

    @Override
    protected void loadDashboardData() {
        loadAdminData();
    }

    private void initializeAdminUI(Admin admin) {
        adminIdLabel.setText("Admin ID: " + admin.getAdminId());
        accessLevelLabel.setText("Access Level: " + admin.getAccessLevel());

        if (usersGrid != null) {
            usersGrid.setCellFactory(grid -> new UserCard());
            usersGrid.setItems(users);
        }

        if (coursesGrid != null) {
            coursesGrid.setCellFactory(grid -> new com.arms.gui.components.CourseCard());
            coursesGrid.setItems(courses);
        }

        if (logsListView != null) {
            logsListView.setItems(logs);
        }
    }

    private void loadAdminData() {
        showLoading(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<User> allUsers = userService.getAllUsers();
                List<Course> allCourses = DataManager.getInstance()
                        .getCourses()
                        .values()
                        .stream()
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    users.setAll(allUsers);
                    courses.setAll(allCourses);
                    logs.setAll(
                            "Admin logged in",
                            "Users loaded: " + allUsers.size(),
                            "Courses loaded: " + allCourses.size()
                    );
                });
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            showLoading(false);
            loadStatistics();
            updateUserDistributionChart();
        });

        task.setOnFailed(e -> {
            showLoading(false);
            AlertHelper.showError("Error", "Failed to load admin data.");
        });

        new Thread(task).start();
    }

    private void loadStatistics() {
        if (statisticsContainer == null) return;
        
        statisticsContainer.getChildren().clear();

        addStat("Total Users", String.valueOf(users.size()));
        addStat("Active Users",
                String.valueOf(users.stream().filter(User::isActive).count()));
        addStat("Total Courses", String.valueOf(courses.size()));
    }

    private void addStat(String title, String value) {
        VBox box = new VBox(5);
        box.getStyleClass().add("stat-card");
        box.getChildren().addAll(new Label(title), new Label(value));
        statisticsContainer.getChildren().add(box);
    }

    private void updateUserDistributionChart() {
        if (userDistributionChart == null) return;
        
        userDistributionChart.getData().clear();
        Map<UserRole, Long> stats = userService.getUserStatistics();

        for (var entry : stats.entrySet()) {
            userDistributionChart.getData().add(
                    new PieChart.Data(
                            entry.getKey().name(),
                            entry.getValue()
                    )
            );
        }
    }

    @FXML
    private void handleAddUser() {
        Optional<User> result = new AddUserDialog().showAndWait();
        result.ifPresent(user -> {
            userService.createUser(user, "default123");
            loadAdminData();
        });
    }

    @FXML
    private void handleAddCourse() {
        Optional<Course> result = new AddCourseDialog().showAndWait();
        result.ifPresent(course -> {
            courseService.createCourse(course);
            loadAdminData();
        });
    }

    @FXML
    private void handleEditUser() {
        AlertHelper.showInfo(
                "Edit User",
                "Double-click a user card to view or edit."
        );
    }

    @FXML
    private void handleDeleteUser() {
        AlertHelper.showInfo(
                "Delete User",
                "Delete user from the user card."
        );
    }

    @FXML
    private void handleActivateUser() {
        AlertHelper.showInfo(
                "Activate User",
                "Activate user from the user card."
        );
    }

    @FXML
    private void handleDeactivateUser() {
        AlertHelper.showInfo(
                "Deactivate User",
                "Deactivate user from the user card."
        );
    }

    @FXML
    private void handleResetPassword() {
        AlertHelper.showInfo(
                "Reset Password",
                "Reset password from the user card."
        );
    }

    @FXML
    private void handleRefreshData() {
        loadAdminData();
    }

    @FXML
    private void handleBackupData() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                FileHandler.getInstance().backupData();
                return null;
            }
        };

        task.setOnSucceeded(e ->
                AlertHelper.showSuccess("Backup", "Backup completed successfully.")
        );

        task.setOnFailed(e ->
                AlertHelper.showError("Backup Failed", "Could not create backup.")
        );

        new Thread(task).start();
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
        if (mainTabPane != null) {
            mainTabPane.setDisable(show);
        }
    }
}
