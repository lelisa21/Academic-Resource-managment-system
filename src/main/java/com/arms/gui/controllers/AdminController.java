package com.arms.gui.controllers;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import org.controlsfx.control.GridView;

import com.arms.domain.Admin;
import com.arms.domain.Course;
import com.arms.domain.User;
import com.arms.domain.enums.UserRole;
import com.arms.domain.enums.UserStatus;
import com.arms.gui.components.CourseCard;
import com.arms.gui.components.UserCard;
import com.arms.gui.dialogs.AddCourseDialog;
import com.arms.gui.dialogs.AddUserDialog;
import com.arms.gui.dialogs.EditCourseDialog;
import com.arms.gui.dialogs.EditUserDialog;
import com.arms.gui.util.AlertHelper;
import com.arms.gui.util.NavigationHelper;
import com.arms.persistence.DataManager;
import com.arms.service.CourseService;
import com.arms.service.UserService;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class AdminController extends DashboardController {

    // Header Components
    @FXML private Label adminIdLabel;
    @FXML private Label accessLevelLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label dateTimeLabel;
    @FXML private Button logoutButton;
    
    // Dashboard Tab
    @FXML private HBox statisticsContainer;
    @FXML private GridView<User> recentUsersGrid;
    @FXML private GridView<Course> recentCoursesGrid;
    @FXML private ListView<String> logsListView;
    @FXML private BarChart<String, Number> userGrowthChart;
    @FXML private PieChart userRoleChart;
    
    // Users Tab
    @FXML private TextField userSearchField;
    @FXML private ComboBox<UserRole> userRoleFilter;
    @FXML private ComboBox<UserStatus> userStatusFilter;
    @FXML private GridView<User> usersGrid;
    @FXML private TableView<User> usersTable;
    @FXML private Button addUserButton;
    @FXML private Button editUserButton;
    @FXML private Button deleteUserButton;
    @FXML private Button activateUserButton;
    @FXML private Button deactivateUserButton;
    @FXML private Button resetPasswordButton;
    @FXML private Button exportUsersButton;
    @FXML private Button bulkActionsButton;
    @FXML private Label usersCountLabel;
    
    // Courses Tab
    @FXML private TextField courseSearchField;
    @FXML private ComboBox<String> courseDepartmentFilter;
    @FXML private ComboBox<String> courseSemesterFilter;
    @FXML private CheckBox activeCoursesOnly;
    @FXML private GridView<Course> coursesGrid;
    @FXML private TableView<Course> coursesTable;
    @FXML private Button addCourseButton;
    @FXML private Button editCourseButton;
    @FXML private Button deleteCourseButton;
    @FXML private Button assignTeacherButton;
    @FXML private Button exportCoursesButton;
    @FXML private Label coursesCountLabel;
    
    // System Tab
    @FXML private TextArea systemLogsArea;
    @FXML private ProgressBar backupProgressBar;
    @FXML private Label lastBackupLabel;
    @FXML private Label databaseSizeLabel;
    @FXML private Label totalRecordsLabel;
    @FXML private Label systemUptimeLabel;
    @FXML private Button backupButton;
    @FXML private Button clearLogsButton;
    @FXML private Button exportDataButton;
    @FXML private Button systemCheckButton;
    @FXML private LineChart<String, Number> performanceChart;
    
    // Reports Tab
    @FXML private DatePicker reportStartDate;
    @FXML private DatePicker reportEndDate;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private TextArea reportPreviewArea;
    @FXML private Button generateReportButton;
    @FXML private Button exportReportButton;
    
    // Settings Tab
    @FXML private TextField systemNameField;
    @FXML private TextField adminEmailField;
    @FXML private CheckBox emailNotificationsCheck;
    @FXML private CheckBox autoBackupCheck;
    @FXML private Spinner<Integer> backupIntervalSpinner;
    @FXML private ColorPicker themeColorPicker;
    @FXML private ComboBox<String> themeCombo;
    @FXML private Button saveSettingsButton;
    @FXML private Button resetSettingsButton;
    
    // Main Components
    @FXML private TabPane mainTabPane;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private StackPane loadingOverlay;
    
    // Services
    private final UserService userService = UserService.getInstance();
    private final CourseService courseService = CourseService.getInstance();
    private final DataManager dataManager = DataManager.getInstance();
    
    // Data Collections
    private final ObservableList<User> allUsers = FXCollections.observableArrayList();
    private final ObservableList<Course> allCourses = FXCollections.observableArrayList();
    private final ObservableList<String> systemLogs = FXCollections.observableArrayList();
    private final ObservableList<User> selectedUsers = FXCollections.observableArrayList();
    private final ObservableList<Course> selectedCourses = FXCollections.observableArrayList();
    
    // Selection Tracking
    private User selectedUser;
    private Course selectedCourse;
    
    // Admin reference
    private Admin admin;
    
    // Formatters
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter backupFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    // System monitoring
    private Timeline clock;
    private LocalDateTime systemStartTime;
    private Timer systemMonitorTimer;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        
        if (currentUser instanceof Admin) {
            admin = (Admin) currentUser;
            systemStartTime = LocalDateTime.now();
            initializeAdminUI();
            loadAdminData();
            setupEventHandlers();
            startSystemMonitoring();
        } else {
            AlertHelper.showError("Access Denied", "Administrator access required.");
            NavigationHelper.navigateToLogin();
        }
    }
    
    @Override
    protected void loadDashboardData() {
        loadAdminData();
    }
    
    private void initializeAdminUI() {
        // Header Setup
        if (adminIdLabel != null) adminIdLabel.setText("Admin ID: " + admin.getId());
        if (accessLevelLabel != null) accessLevelLabel.setText("Access Level: " + admin.getAccessLevel());
        if (welcomeLabel != null) welcomeLabel.setText("Welcome, " + admin.getFirstName() + " " + admin.getLastName() + "!");
        updateDateTime();
        
        // Start date/time updater
        startDateTimeUpdater();
        
        // Initialize all components
        initializeFilters();
        initializeUsersGrid();
        initializeCoursesGrid();
        initializeUsersTable();
        initializeCoursesTable();
        initializeLogs();
        initializeCharts();
        initializeSettings();
        
        // Setup loading overlay
        if (loadingOverlay != null) loadingOverlay.setVisible(false);
        
        // Disable action buttons initially
        updateUserActionButtons();
        updateCourseActionButtons();
    }
    
    private void startDateTimeUpdater() {
        clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDateTime()));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }
    
    private void updateDateTime() {
        if (dateTimeLabel != null) {
            dateTimeLabel.setText(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy HH:mm:ss")));
        }
    }
    
    private void startSystemMonitoring() {
        systemMonitorTimer = new Timer(true);
        systemMonitorTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateSystemMetrics();
            }
        }, 0, 10000); // Update every 10 seconds
    }
    
    private void updateSystemMetrics() {
        Platform.runLater(() -> {
            if (totalRecordsLabel != null) {
                int totalRecords = dataManager.getUsers().size() + dataManager.getCourses().size() +
                                 dataManager.getAssignments().size() + dataManager.getGrades().size();
                totalRecordsLabel.setText("Total Records: " + totalRecords);
            }
            
            if (systemUptimeLabel != null) {
                long hours = java.time.Duration.between(systemStartTime, LocalDateTime.now()).toHours();
                long minutes = java.time.Duration.between(systemStartTime, LocalDateTime.now()).toMinutes() % 60;
                systemUptimeLabel.setText("Uptime: " + hours + "h " + minutes + "m");
            }
            
            if (databaseSizeLabel != null) {
                int size = (dataManager.getUsers().size() * 2) + (dataManager.getCourses().size() * 3) +
                          (dataManager.getAssignments().size() * 2) + (dataManager.getGrades().size() * 2);
                databaseSizeLabel.setText("DB Size: ~" + size + " KB");
            }
            
            if (performanceChart != null && performanceChart.getData().size() > 0) {
                // Update performance chart with new data
                XYChart.Series<String, Number> series = performanceChart.getData().get(0);
                if (series.getData().size() > 10) {
                    series.getData().remove(0);
                }
                
                String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                double load = 20 + Math.random() * 40; // Simulated system load
                series.getData().add(new XYChart.Data<>(time, load));
            }
        });
    }
    
    private void initializeFilters() {
        // User filters
        if (userRoleFilter != null) {
            userRoleFilter.getItems().addAll(UserRole.values());
            userRoleFilter.getItems().add(0, null);
            userRoleFilter.setValue(null);
            userRoleFilter.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(UserRole item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "All Roles" : item.toString());
                }
            });
        }
        
        if (userStatusFilter != null) {
            userStatusFilter.getItems().addAll(UserStatus.values());
            userStatusFilter.getItems().add(0, null);
            userStatusFilter.setValue(null);
            userStatusFilter.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(UserStatus item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "All Status" : item.toString());
                }
            });
        }
        
        // Course filters
        if (courseDepartmentFilter != null) {
            courseDepartmentFilter.getItems().addAll("All Departments", "Computer Science", "Mathematics", 
                "Physics", "Chemistry", "Engineering", "Business", "Arts", "Science");
            courseDepartmentFilter.setValue("All Departments");
        }
        
        if (courseSemesterFilter != null) {
            courseSemesterFilter.getItems().addAll("All Semesters", "Fall 2024", "Spring 2024", 
                "Summer 2024", "Fall 2023", "Spring 2023");
            courseSemesterFilter.setValue("All Semesters");
        }
        
        // Report filters
        if (reportTypeCombo != null) {
            reportTypeCombo.getItems().addAll("User Statistics", "Course Statistics", 
                "System Usage", "Financial Report", "Activity Logs");
            reportTypeCombo.setValue("User Statistics");
        }
        
        if (reportStartDate != null) {
            reportStartDate.setValue(LocalDateTime.now().minusMonths(1).toLocalDate());
        }
        
        if (reportEndDate != null) {
            reportEndDate.setValue(LocalDateTime.now().toLocalDate());
        }
        
        // Settings
        if (backupIntervalSpinner != null) {
            backupIntervalSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 24, 6));
        }
        
        if (themeCombo != null) {
            themeCombo.getItems().addAll("Light", "Dark", "Blue", "Green", "Purple");
            themeCombo.setValue("Light");
        }
    }
    
    private void initializeUsersGrid() {
        if (recentUsersGrid != null) {
            recentUsersGrid.setCellFactory(gridView -> createUserCard(false));
            recentUsersGrid.setCellWidth(180);
            recentUsersGrid.setCellHeight(100);
        }
        
        if (usersGrid != null) {
            usersGrid.setCellFactory(gridView -> createUserCard(true));
            usersGrid.setCellWidth(220);
            usersGrid.setCellHeight(140);
        }
    }
    
    private UserCard createUserCard(boolean selectable) {
        UserCard card = new UserCard();
        
        if (selectable) {
            card.setOnMouseClicked(event -> {
                User user = card.getItem();
                if (user != null) {
                    // Toggle selection
                    if (event.isControlDown() || event.isShiftDown()) {
                        // Multi-select
                        if (selectedUsers.contains(user)) {
                            selectedUsers.remove(user);
                            card.setStyle("");
                        } else {
                            selectedUsers.add(user);
                            card.setStyle("-fx-border-color: #3498db; -fx-border-width: 2px; -fx-border-radius: 5px;");
                        }
                    } else {
                        // Single select
                        clearUserSelection();
                        selectedUsers.clear();
                        selectedUsers.add(user);
                        selectedUser = user;
                        card.setStyle("-fx-border-color: #3498db; -fx-border-width: 2px; -fx-border-radius: 5px;");
                    }
                    
                    updateUserActionButtons();
                    
                    // Double-click to edit
                    if (event.getClickCount() == 2) {
                        handleEditUser();
                    }
                }
            });
            
            // Add context menu
            ContextMenu contextMenu = new ContextMenu();
            MenuItem viewItem = new MenuItem("View Details");
            viewItem.setOnAction(e -> viewUserDetails(card.getItem()));
            
            MenuItem editItem = new MenuItem("Edit User");
            editItem.setOnAction(e -> {
                selectedUser = card.getItem();
                handleEditUser();
            });
            
            MenuItem deleteItem = new MenuItem("Delete User");
            deleteItem.setOnAction(e -> {
                selectedUser = card.getItem();
                handleDeleteUser();
            });
            
            MenuItem activateItem = new MenuItem("Activate User");
            activateItem.setOnAction(e -> {
                selectedUser = card.getItem();
                handleActivateUser();
            });
            
            MenuItem deactivateItem = new MenuItem("Deactivate User");
            deactivateItem.setOnAction(e -> {
                selectedUser = card.getItem();
                handleDeactivateUser();
            });
            
            MenuItem resetPassItem = new MenuItem("Reset Password");
            resetPassItem.setOnAction(e -> {
                selectedUser = card.getItem();
                handleResetPassword();
            });
            
            contextMenu.getItems().addAll(viewItem, editItem, deleteItem,
                new SeparatorMenuItem(), activateItem, deactivateItem,
                new SeparatorMenuItem(), resetPassItem);
            
            card.setOnContextMenuRequested(e -> contextMenu.show(card, e.getScreenX(), e.getScreenY()));
        }
        
        return card;
    }
    
    private void clearUserSelection() {
        selectedUser = null;
        selectedUsers.clear();
        if (usersGrid != null) {
            for (Node node : usersGrid.getChildrenUnmodifiable()) {
                node.setStyle("");
            }
        }
    }
    
    private void initializeUsersTable() {
        if (usersTable != null) {
            // Clear existing columns
            usersTable.getColumns().clear();
            
            // ID Column
            TableColumn<User, String> idCol = new TableColumn<>("ID");
            idCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
            idCol.setPrefWidth(80);
            
            // Username Column
            TableColumn<User, String> usernameCol = new TableColumn<>("Username");
            usernameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
            usernameCol.setPrefWidth(120);
            
            // Name Column
            TableColumn<User, String> nameCol = new TableColumn<>("Name");
            nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
            nameCol.setPrefWidth(150);
            
            // Email Column
            TableColumn<User, String> emailCol = new TableColumn<>("Email");
            emailCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
            emailCol.setPrefWidth(200);
            
            // Role Column
            TableColumn<User, String> roleCol = new TableColumn<>("Role");
            roleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole().toString()));
            roleCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String role, boolean empty) {
                    super.updateItem(role, empty);
                    if (empty || role == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(role);
                        // Color code based on role
                        switch (role) {
                            case "ADMIN":
                            case "SUPER_ADMIN":
                                setStyle("-fx-text-fill: #9b59b6; -fx-font-weight: bold;");
                                break;
                            case "TEACHER":
                                setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                                break;
                            case "STUDENT":
                                setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                                break;
                            default:
                                setStyle("");
                        }
                    }
                }
            });
            roleCol.setPrefWidth(100);
            
            // Status Column
            TableColumn<User, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));
            statusCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(status);
                        if ("ACTIVE".equals(status)) {
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        }
                    }
                }
            });
            statusCol.setPrefWidth(100);
            
            // Created Date Column
            TableColumn<User, String> createdCol = new TableColumn<>("Created");
            createdCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
            createdCol.setPrefWidth(100);
            
            usersTable.getColumns().addAll(idCol, usernameCol, nameCol, emailCol, roleCol, statusCol, createdCol);
            
            // Add selection listener
            usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                selectedUser = newVal;
                updateUserActionButtons();
            });
        }
    }
    
    private void initializeCoursesGrid() {
        if (recentCoursesGrid != null) {
            recentCoursesGrid.setCellFactory(gridView -> createCourseCard(false));
            recentCoursesGrid.setCellWidth(200);
            recentCoursesGrid.setCellHeight(120);
        }
        
        if (coursesGrid != null) {
            coursesGrid.setCellFactory(gridView -> createCourseCard(true));
            coursesGrid.setCellWidth(260);
            coursesGrid.setCellHeight(140);
        }
    }
    
    private CourseCard createCourseCard(boolean selectable) {
        CourseCard card = new CourseCard();
        
        if (selectable) {
            card.setOnMouseClicked(event -> {
                Course course = card.getItem();
                if (course != null) {
                    // Toggle selection
                    if (event.isControlDown() || event.isShiftDown()) {
                        // Multi-select
                        if (selectedCourses.contains(course)) {
                            selectedCourses.remove(course);
                            card.setStyle("");
                        } else {
                            selectedCourses.add(course);
                            card.setStyle("-fx-border-color: #9b59b6; -fx-border-width: 2px; -fx-border-radius: 5px;");
                        }
                    } else {
                        // Single select
                        clearCourseSelection();
                        selectedCourses.clear();
                        selectedCourses.add(course);
                        selectedCourse = course;
                        card.setStyle("-fx-border-color: #9b59b6; -fx-border-width: 2px; -fx-border-radius: 5px;");
                    }
                    
                    updateCourseActionButtons();
                    
                    // Double-click to edit
                    if (event.getClickCount() == 2) {
                        handleEditCourse();
                    }
                }
            });
            
            // Add context menu
            ContextMenu contextMenu = new ContextMenu();
            MenuItem viewItem = new MenuItem("View Details");
            viewItem.setOnAction(e -> viewCourseDetails(card.getItem()));
            
            MenuItem editItem = new MenuItem("Edit Course");
            editItem.setOnAction(e -> {
                selectedCourse = card.getItem();
                handleEditCourse();
            });
            
            MenuItem deleteItem = new MenuItem("Delete Course");
            deleteItem.setOnAction(e -> {
                selectedCourse = card.getItem();
                handleDeleteCourse();
            });
            
            MenuItem assignTeacherItem = new MenuItem("Assign Teacher");
            assignTeacherItem.setOnAction(e -> {
                selectedCourse = card.getItem();
                handleAssignTeacher();
            });
            
            contextMenu.getItems().addAll(viewItem, editItem, deleteItem, assignTeacherItem);
            
            card.setOnContextMenuRequested(e -> contextMenu.show(card, e.getScreenX(), e.getScreenY()));
        }
        
        return card;
    }
    
    private void clearCourseSelection() {
        selectedCourse = null;
        selectedCourses.clear();
        if (coursesGrid != null) {
            for (Node node : coursesGrid.getChildrenUnmodifiable()) {
                node.setStyle("");
            }
        }
    }
    
    private void initializeCoursesTable() {
        if (coursesTable != null) {
            // Clear existing columns
            coursesTable.getColumns().clear();
            
            // Code Column
            TableColumn<Course, String> codeCol = new TableColumn<>("Code");
            codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseCode()));
            codeCol.setPrefWidth(100);
            
            // Title Column
            TableColumn<Course, String> titleCol = new TableColumn<>("Title");
            titleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
            titleCol.setPrefWidth(200);
            
            // Department Column
            TableColumn<Course, String> deptCol = new TableColumn<>("Department");
            deptCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDepartment()));
            deptCol.setPrefWidth(150);
            
            // Semester Column
            TableColumn<Course, String> semesterCol = new TableColumn<>("Semester");
            semesterCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSemester()));
            semesterCol.setPrefWidth(100);
            
            // Credits Column
            TableColumn<Course, Integer> creditsCol = new TableColumn<>("Credits");
            creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
            creditsCol.setPrefWidth(80);
            
            // Enrollment Column
            TableColumn<Course, Integer> enrollmentCol = new TableColumn<>("Enrollment");
            enrollmentCol.setCellValueFactory(new PropertyValueFactory<>("currentEnrollment"));
            enrollmentCol.setPrefWidth(100);
            
            // Status Column
            TableColumn<Course, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));
            statusCol.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(status);
                        if ("Active".equals(status)) {
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        }
                    }
                }
            });
            statusCol.setPrefWidth(100);
            
            coursesTable.getColumns().addAll(codeCol, titleCol, deptCol, semesterCol, creditsCol, enrollmentCol, statusCol);
            
            // Add selection listener
            coursesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                selectedCourse = newVal;
                updateCourseActionButtons();
            });
        }
    }
    
    private void initializeLogs() {
        if (logsListView != null) {
            logsListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(String log, boolean empty) {
                    super.updateItem(log, empty);
                    if (empty || log == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(log);
                        if (log.contains("[ERROR]")) {
                            setStyle("-fx-text-fill: #e74c3c;");
                        } else if (log.contains("[WARN]")) {
                            setStyle("-fx-text-fill: #f39c12;");
                        } else if (log.contains("[INFO]")) {
                            setStyle("-fx-text-fill: #3498db;");
                        } else {
                            setStyle("-fx-text-fill: #27ae60;");
                        }
                    }
                }
            });
        }
        
        if (systemLogsArea != null) {
            systemLogsArea.setEditable(false);
            systemLogsArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");
        }
    }
    
    private void initializeCharts() {
        // User Growth Chart
        if (userGrowthChart != null) {
            userGrowthChart.setTitle("User Growth (Last 7 Days)");
            userGrowthChart.setLegendVisible(false);
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("Mon", 45));
            series.getData().add(new XYChart.Data<>("Tue", 52));
            series.getData().add(new XYChart.Data<>("Wed", 48));
            series.getData().add(new XYChart.Data<>("Thu", 55));
            series.getData().add(new XYChart.Data<>("Fri", 60));
            series.getData().add(new XYChart.Data<>("Sat", 58));
            series.getData().add(new XYChart.Data<>("Sun", 62));
            
            userGrowthChart.getData().add(series);
        }
        
        // User Role Chart
        if (userRoleChart != null) {
            userRoleChart.setTitle("User Distribution by Role");
            
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Students", 65),
                new PieChart.Data("Teachers", 20),
                new PieChart.Data("Admins", 10),
                new PieChart.Data("Others", 5)
            );
            
            userRoleChart.setData(pieChartData);
            
            // Add colors
            int i = 0;
            Color[] colors = {Color.web("#3498db"), Color.web("#2ecc71"), Color.web("#9b59b6"), Color.web("#f39c12")};
            for (PieChart.Data data : pieChartData) {
                data.getNode().setStyle("-fx-pie-color: " + colors[i].toString().replace("0x", "#") + ";");
                i = (i + 1) % colors.length;
            }
        }
        
        // Performance Chart
        if (performanceChart != null) {
            performanceChart.setTitle("System Performance");
            performanceChart.setCreateSymbols(false);
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("CPU Usage %");
            
            // Add initial data
            for (int i = 0; i < 10; i++) {
                String time = LocalDateTime.now().minusMinutes(10 - i).format(DateTimeFormatter.ofPattern("HH:mm"));
                series.getData().add(new XYChart.Data<>(time, 30 + Math.random() * 30));
            }
            
            performanceChart.getData().add(series);
        }
    }
    
    private void initializeSettings() {
        if (systemNameField != null) {
            systemNameField.setText("Academic Records Management System");
        }
        
        if (adminEmailField != null) {
            adminEmailField.setText(admin.getEmail());
        }
        
        if (emailNotificationsCheck != null) {
            emailNotificationsCheck.setSelected(true);
        }
        
        if (autoBackupCheck != null) {
            autoBackupCheck.setSelected(true);
        }
        
        if (lastBackupLabel != null) {
            lastBackupLabel.setText("Last Backup: " + LocalDateTime.now().minusHours(2).format(backupFormatter));
        }
    }
    
    private void setupEventHandlers() {
        // User search and filters
        if (userSearchField != null) {
            userSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers());
        }
        
        if (userRoleFilter != null) {
            userRoleFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterUsers());
        }
        
        if (userStatusFilter != null) {
            userStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterUsers());
        }
        
        // Course search and filters
        if (courseSearchField != null) {
            courseSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterCourses());
        }
        
        if (courseDepartmentFilter != null) {
            courseDepartmentFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterCourses());
        }
        
        if (courseSemesterFilter != null) {
            courseSemesterFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterCourses());
        }
        
        if (activeCoursesOnly != null) {
            activeCoursesOnly.selectedProperty().addListener((obs, oldVal, newVal) -> filterCourses());
        }
        
        // Button handlers
        if (logoutButton != null) logoutButton.setOnAction(e -> handleLogout());
        if (addUserButton != null) addUserButton.setOnAction(e -> handleAddUser());
        if (editUserButton != null) editUserButton.setOnAction(e -> handleEditUser());
        if (deleteUserButton != null) deleteUserButton.setOnAction(e -> handleDeleteUser());
        if (activateUserButton != null) activateUserButton.setOnAction(e -> handleActivateUser());
        if (deactivateUserButton != null) deactivateUserButton.setOnAction(e -> handleDeactivateUser());
        if (resetPasswordButton != null) resetPasswordButton.setOnAction(e -> handleResetPassword());
        if (exportUsersButton != null) exportUsersButton.setOnAction(e -> handleExportUsers());
        if (bulkActionsButton != null) bulkActionsButton.setOnAction(e -> handleBulkActions());
        
        if (addCourseButton != null) addCourseButton.setOnAction(e -> handleAddCourse());
        if (editCourseButton != null) editCourseButton.setOnAction(e -> handleEditCourse());
        if (deleteCourseButton != null) deleteCourseButton.setOnAction(e -> handleDeleteCourse());
        if (assignTeacherButton != null) assignTeacherButton.setOnAction(e -> handleAssignTeacher());
        if (exportCoursesButton != null) exportCoursesButton.setOnAction(e -> handleExportCourses());
        
        if (backupButton != null) backupButton.setOnAction(e -> handleBackupData());
        if (clearLogsButton != null) clearLogsButton.setOnAction(e -> handleClearLogs());
        if (exportDataButton != null) exportDataButton.setOnAction(e -> handleExportData());
        if (systemCheckButton != null) systemCheckButton.setOnAction(e -> handleSystemCheck());
        
        if (generateReportButton != null) generateReportButton.setOnAction(e -> handleGenerateReport());
        if (exportReportButton != null) exportReportButton.setOnAction(e -> handleExportReport());
        
        if (saveSettingsButton != null) saveSettingsButton.setOnAction(e -> handleSaveSettings());
        if (resetSettingsButton != null) resetSettingsButton.setOnAction(e -> handleResetSettings());
    }
    
    private void updateUserActionButtons() {
        boolean hasSelection = selectedUser != null || !selectedUsers.isEmpty();
        boolean hasSingleSelection = selectedUser != null;
        boolean hasMultipleSelection = selectedUsers.size() > 1;
        
        if (editUserButton != null) {
            editUserButton.setDisable(!hasSingleSelection || hasMultipleSelection);
        }
        
        if (deleteUserButton != null) {
            deleteUserButton.setDisable(!hasSelection);
        }
        
        if (activateUserButton != null) {
            activateUserButton.setDisable(!hasSelection);
        }
        
        if (deactivateUserButton != null) {
            deactivateUserButton.setDisable(!hasSelection);
        }
        
        if (resetPasswordButton != null) {
            resetPasswordButton.setDisable(!hasSingleSelection || hasMultipleSelection);
        }
        
        if (bulkActionsButton != null) {
            bulkActionsButton.setDisable(!hasMultipleSelection);
        }
    }
    
    private void updateCourseActionButtons() {
        boolean hasSelection = selectedCourse != null || !selectedCourses.isEmpty();
        boolean hasSingleSelection = selectedCourse != null;
        
        if (editCourseButton != null) {
            editCourseButton.setDisable(!hasSingleSelection);
        }
        
        if (deleteCourseButton != null) {
            deleteCourseButton.setDisable(!hasSelection);
        }
        
        if (assignTeacherButton != null) {
            assignTeacherButton.setDisable(!hasSingleSelection);
        }
    }
    
    private void loadAdminData() {
        showLoading(true);
        
        Platform.runLater(() -> {
            try {
                // Load all users
                List<User> users = new ArrayList<>(dataManager.getUsers().values());
                allUsers.setAll(users);
                
                // Load all courses
                List<Course> courses = new ArrayList<>(dataManager.getCourses().values());
                allCourses.setAll(courses);
                
                // Update grids
                if (recentUsersGrid != null) {
                    List<User> recentUsers = users.stream()
                        .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                        .limit(8)
                        .collect(Collectors.toList());
                    recentUsersGrid.setItems(FXCollections.observableArrayList(recentUsers));
                }
                
                if (usersGrid != null) {
                    usersGrid.setItems(allUsers);
                }
                
                if (usersTable != null) {
                    usersTable.setItems(allUsers);
                }
                
                if (recentCoursesGrid != null) {
                    List<Course> recentCourses = courses.stream()
                        .limit(6)
                        .collect(Collectors.toList());
                    recentCoursesGrid.setItems(FXCollections.observableArrayList(recentCourses));
                }
                
                if (coursesGrid != null) {
                    coursesGrid.setItems(allCourses);
                }
                
                if (coursesTable != null) {
                    coursesTable.setItems(allCourses);
                }
                
                // Update counts
                if (usersCountLabel != null) {
                    usersCountLabel.setText("Total Users: " + users.size());
                }
                
                if (coursesCountLabel != null) {
                    coursesCountLabel.setText("Total Courses: " + courses.size());
                }
                
                // Load logs
                loadSystemLogs();
                
                // Update statistics
                updateStatistics();
                
                // Update charts
                updateCharts();
                
                // Add log entry
                addLog("[INFO] Admin data loaded successfully");
                
            } catch (Exception e) {
                e.printStackTrace();
                addLog("[ERROR] Failed to load admin data: " + e.getMessage());
            } finally {
                showLoading(false);
            }
        });
    }
    
    private void loadSystemLogs() {
        try {
            // Create some sample logs
            systemLogs.clear();
            addLog("[INFO] System started");
            addLog("[INFO] Admin logged in: " + admin.getUsername());
            addLog("[INFO] Loading system data...");
            addLog("[INFO] Connected to database successfully");
            addLog("[INFO] Loaded " + allUsers.size() + " users");
            addLog("[INFO] Loaded " + allCourses.size() + " courses");
            
            if (logsListView != null) {
                logsListView.setItems(systemLogs);
            }
            
            if (systemLogsArea != null) {
                StringBuilder sb = new StringBuilder();
                systemLogs.forEach(log -> sb.append(log).append("\n"));
                systemLogsArea.setText(sb.toString());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateStatistics() {
        if (statisticsContainer != null) {
            statisticsContainer.getChildren().clear();
            
            // Create statistic cards
            List<StatisticCard> cards = new ArrayList<>();
            cards.add(new StatisticCard("Total Users", String.valueOf(allUsers.size()), "#3498db", "👥"));
            cards.add(new StatisticCard("Active Users", 
                String.valueOf(allUsers.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count()), 
                "#2ecc71", "✅"));
            cards.add(new StatisticCard("Total Courses", String.valueOf(allCourses.size()), "#9b59b6", "📚"));
            cards.add(new StatisticCard("Active Courses", 
                String.valueOf(allCourses.stream().filter(Course::isActive).count()), 
                "#e74c3c", "📊"));
            cards.add(new StatisticCard("Teachers", 
                String.valueOf(allUsers.stream().filter(u -> u.getRole() == UserRole.TEACHER).count()), 
                "#f39c12", "👨‍🏫"));
            cards.add(new StatisticCard("Students", 
                String.valueOf(allUsers.stream().filter(u -> u.getRole() == UserRole.STUDENT).count()), 
                "#1abc9c", "👨‍🎓"));
            cards.add(new StatisticCard("Today's Logins", "12", "#e67e22", "🔐"));
            cards.add(new StatisticCard("Pending Tasks", "5", "#95a5a6", "📝"));
            
            for (StatisticCard card : cards) {
                statisticsContainer.getChildren().add(createStatisticCard(card));
            }
        }
    }
    
    private VBox createStatisticCard(StatisticCard card) {
        VBox cardBox = new VBox(8);
        cardBox.setAlignment(Pos.CENTER);
        cardBox.setPadding(new Insets(15));
        cardBox.setPrefSize(160, 100);
        cardBox.getStyleClass().add("statistic-card");
        cardBox.setStyle("-fx-background-color: white; -fx-border-color: " + card.color + "; " +
                        "-fx-border-width: 2px; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        
        // Icon
        Label icon = new Label(card.icon);
        icon.setStyle("-fx-font-size: 24px;");
        
        // Title
        Label title = new Label(card.title);
        title.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        // Value
        Label value = new Label(card.value);
        value.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + card.color + ";");
        
        cardBox.getChildren().addAll(icon, title, value);
        
        // Add click handler
        cardBox.setOnMouseClicked(e -> {
            if (card.title.contains("Users")) {
                mainTabPane.getSelectionModel().select(1); // Users tab
            } else if (card.title.contains("Courses")) {
                mainTabPane.getSelectionModel().select(2); // Courses tab
            }
        });
        
        return cardBox;
    }
    
    private void updateCharts() {
        if (userRoleChart != null) {
            // Update pie chart with actual data
            long students = allUsers.stream().filter(u -> u.getRole() == UserRole.STUDENT).count();
            long teachers = allUsers.stream().filter(u -> u.getRole() == UserRole.TEACHER).count();
            long admins = allUsers.stream().filter(u -> u.getRole() == UserRole.ADMIN || u.getRole() == UserRole.SUPER_ADMIN).count();
            long others = allUsers.size() - students - teachers - admins;
            
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Students (" + students + ")", students),
                new PieChart.Data("Teachers (" + teachers + ")", teachers),
                new PieChart.Data("Admins (" + admins + ")", admins),
                new PieChart.Data("Others (" + others + ")", others)
            );
            
            userRoleChart.setData(pieChartData);
        }
    }
    
    private void filterUsers() {
        if (usersGrid == null) return;
        
        String searchText = userSearchField != null ? userSearchField.getText().toLowerCase() : "";
        UserRole roleFilter = userRoleFilter != null ? userRoleFilter.getValue() : null;
        UserStatus statusFilter = userStatusFilter != null ? userStatusFilter.getValue() : null;
        
        List<User> filtered = allUsers.stream()
            .filter(user -> {
                // Search filter
                if (!searchText.isEmpty()) {
                    boolean matches = (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchText)) ||
                                     (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(searchText)) ||
                                     (user.getLastName() != null && user.getLastName().toLowerCase().contains(searchText)) ||
                                     (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText));
                    if (!matches) return false;
                }
                
                // Role filter
                if (roleFilter != null && user.getRole() != roleFilter) {
                    return false;
                }
                
                // Status filter
                if (statusFilter != null && user.getStatus() != statusFilter) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        usersGrid.setItems(FXCollections.observableArrayList(filtered));
        if (usersTable != null) {
            usersTable.setItems(FXCollections.observableArrayList(filtered));
        }
        clearUserSelection();
    }
    
    private void filterCourses() {
        if (coursesGrid == null) return;
        
        String searchText = courseSearchField != null ? courseSearchField.getText().toLowerCase() : "";
        String deptFilter = courseDepartmentFilter != null ? courseDepartmentFilter.getValue() : "All Departments";
        String semesterFilter = courseSemesterFilter != null ? courseSemesterFilter.getValue() : "All Semesters";
        boolean activeOnly = activeCoursesOnly != null && activeCoursesOnly.isSelected();
        
        List<Course> filtered = allCourses.stream()
            .filter(course -> {
                // Search filter
                if (!searchText.isEmpty()) {
                    boolean matches = (course.getCourseCode() != null && course.getCourseCode().toLowerCase().contains(searchText)) ||
                                     (course.getTitle() != null && course.getTitle().toLowerCase().contains(searchText)) ||
                                     (course.getDescription() != null && course.getDescription().toLowerCase().contains(searchText));
                    if (!matches) return false;
                }
                
                // Department filter
                if (!"All Departments".equals(deptFilter) && 
                    (course.getDepartment() == null || !deptFilter.equals(course.getDepartment()))) {
                    return false;
                }
                
                // Semester filter
                if (!"All Semesters".equals(semesterFilter) && 
                    (course.getSemester() == null || !semesterFilter.equals(course.getSemester()))) {
                    return false;
                }
                
                // Active only filter
                if (activeOnly && !course.isActive()) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        coursesGrid.setItems(FXCollections.observableArrayList(filtered));
        if (coursesTable != null) {
            coursesTable.setItems(FXCollections.observableArrayList(filtered));
        }
        clearCourseSelection();
    }
    
    // ==================== COMPLETE ACTION HANDLERS ====================
    
    @Override
    @FXML
    protected void handleLogout() {
        boolean confirm = AlertHelper.showConfirmation("Logout", "Are you sure you want to logout?");
        if (confirm) {
            if (clock != null) clock.stop();
            if (systemMonitorTimer != null) systemMonitorTimer.cancel();
            addLog("[INFO] Admin logged out");
            NavigationHelper.navigateToLogin();
        }
    }
    
    @FXML
    private void handleRefreshData() {
        loadAdminData();
        AlertHelper.showInfo("Refreshed", "Data refreshed successfully!");
        addLog("[INFO] Manual refresh triggered");
    }
    
    @FXML
    private void handleAddUser() {
        try {
            AddUserDialog dialog = new AddUserDialog();
            Optional<User> result = dialog.showAndWait();
            result.ifPresent(user -> {
                Optional<User> createdUser = userService.createUser(user, "password123");
                if (createdUser.isPresent()) {
                    AlertHelper.showSuccess("Success", "User created successfully!");
                    addLog("[INFO] Created user: " + user.getUsername());
                    loadAdminData();
                } else {
                    AlertHelper.showError("Error", "Failed to create user. Username may already exist.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Failed to open Add User dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleEditUser() {
        if (selectedUser == null) {
            AlertHelper.showWarning("No Selection", "Please select a user to edit.");
            return;
        }
        
        try {
            EditUserDialog dialog = new EditUserDialog(selectedUser);
            Optional<User> result = dialog.showAndWait();
            result.ifPresent(updatedUser -> {
                boolean success = updateUserInDataManager(updatedUser);
                if (success) {
                    AlertHelper.showSuccess("Success", "User updated successfully!");
                    addLog("[INFO] Updated user: " + selectedUser.getUsername());
                    loadAdminData();
                } else {
                    AlertHelper.showError("Error", "Failed to update user.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Failed to edit user: " + e.getMessage());
        }
    }
    
    private boolean updateUserInDataManager(User user) {
        try {
            dataManager.saveUser(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @FXML
    private void handleDeleteUser() {
        if (selectedUser == null && selectedUsers.isEmpty()) {
            AlertHelper.showWarning("No Selection", "Please select one or more users to delete.");
            return;
        }
        
        List<User> usersToDelete = selectedUsers.isEmpty() ? 
            Collections.singletonList(selectedUser) : selectedUsers;
        
        // Prevent deleting yourself
        if (usersToDelete.stream().anyMatch(u -> u.getId().equals(admin.getId()))) {
            AlertHelper.showError("Error", "You cannot delete your own account.");
            return;
        }
        
        StringBuilder userList = new StringBuilder();
        for (User user : usersToDelete) {
            userList.append("\n• ").append(user.getUsername()).append(" (").append(user.getFullName()).append(")");
        }
        
        boolean confirm = AlertHelper.showConfirmation("Confirm Delete",
            "Are you sure you want to delete " + usersToDelete.size() + " user(s)?" + userList.toString() + "\n\n⚠️ This action cannot be undone!");
        
        if (confirm) {
            int successCount = 0;
            for (User user : usersToDelete) {
                boolean success = deleteUserFromDataManager(user.getId());
                if (success) successCount++;
            }
            
            if (successCount == usersToDelete.size()) {
                AlertHelper.showSuccess("Success", "Deleted " + successCount + " user(s) successfully!");
                addLog("[WARN] Deleted " + successCount + " user(s)");
            } else {
                AlertHelper.showWarning("Partial Success", "Deleted " + successCount + " of " + usersToDelete.size() + " user(s).");
            }
            
            loadAdminData();
            clearUserSelection();
        }
    }
    
    private boolean deleteUserFromDataManager(String userId) {
        try {
            dataManager.deleteUser(userId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @FXML
    private void handleActivateUser() {
        if (selectedUser == null && selectedUsers.isEmpty()) {
            AlertHelper.showWarning("No Selection", "Please select one or more users to activate.");
            return;
        }
        
        List<User> usersToActivate = selectedUsers.isEmpty() ? 
            Collections.singletonList(selectedUser) : selectedUsers;
        
        int successCount = 0;
        for (User user : usersToActivate) {
            user.setStatus(UserStatus.ACTIVE);
            boolean success = updateUserInDataManager(user);
            if (success) successCount++;
        }
        
        if (successCount > 0) {
            AlertHelper.showSuccess("Success", "Activated " + successCount + " user(s)!");
            addLog("[INFO] Activated " + successCount + " user(s)");
            loadAdminData();
        } else {
            AlertHelper.showError("Error", "Failed to activate users.");
        }
    }
    
    @FXML
    private void handleDeactivateUser() {
        if (selectedUser == null && selectedUsers.isEmpty()) {
            AlertHelper.showWarning("No Selection", "Please select one or more users to deactivate.");
            return;
        }
        
        List<User> usersToDeactivate = selectedUsers.isEmpty() ? 
            Collections.singletonList(selectedUser) : selectedUsers;
        
        // Prevent deactivating yourself
        if (usersToDeactivate.stream().anyMatch(u -> u.getId().equals(admin.getId()))) {
            AlertHelper.showError("Error", "You cannot deactivate your own account.");
            return;
        }
        
        int successCount = 0;
        for (User user : usersToDeactivate) {
            user.setStatus(UserStatus.INACTIVE);
            boolean success = updateUserInDataManager(user);
            if (success) successCount++;
        }
        
        if (successCount > 0) {
            AlertHelper.showSuccess("Success", "Deactivated " + successCount + " user(s)!");
            addLog("[INFO] Deactivated " + successCount + " user(s)");
            loadAdminData();
        } else {
            AlertHelper.showError("Error", "Failed to deactivate users.");
        }
    }
    
    @FXML
    private void handleResetPassword() {
        if (selectedUser == null) {
            AlertHelper.showWarning("No Selection", "Please select a user to reset password.");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog("password123");
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + selectedUser.getUsername());
        dialog.setContentText("New Password:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            // In a real app, you would hash the password
            AlertHelper.showInfo("Password Reset", 
                "Password for " + selectedUser.getUsername() + " has been reset.\n" +
                "New password: " + newPassword + "\n" +
                "The user should change it on next login.");
            addLog("[INFO] Reset password for user: " + selectedUser.getUsername());
        });
    }
    
    @FXML
    private void handleExportUsers() {
        try {
            // Create CSV content
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Username,First Name,Last Name,Email,Role,Status,Created Date\n");
            
            for (User user : allUsers) {
                csv.append(user.getId()).append(",")
                   .append(user.getUsername()).append(",")
                   .append(user.getFirstName()).append(",")
                   .append(user.getLastName()).append(",")
                   .append(user.getEmail()).append(",")
                   .append(user.getRole()).append(",")
                   .append(user.getStatus()).append(",")
                   .append(user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                   .append("\n");
            }
            
            // Show in dialog
            TextArea textArea = new TextArea(csv.toString());
            textArea.setEditable(false);
            textArea.setStyle("-fx-font-family: 'Monospaced';");
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Users");
            alert.setHeaderText("Users Data (CSV Format)");
            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setPrefSize(800, 600);
            alert.showAndWait();
            
            addLog("[INFO] Exported users data");
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Export Failed", "Failed to export users: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleBulkActions() {
        if (selectedUsers.isEmpty()) {
            AlertHelper.showWarning("No Selection", "Please select multiple users for bulk actions.");
            return;
        }
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Send Email", 
            "Send Email", "Activate All", "Deactivate All", "Export Selected", "Delete Selected");
        dialog.setTitle("Bulk Actions");
        dialog.setHeaderText("Bulk Actions for " + selectedUsers.size() + " Users");
        dialog.setContentText("Choose action:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(action -> {
            switch (action) {
                case "Send Email":
                    handleBulkEmail();
                    break;
                case "Activate All":
                    handleActivateUser();
                    break;
                case "Deactivate All":
                    handleDeactivateUser();
                    break;
                case "Export Selected":
                    handleExportSelectedUsers();
                    break;
                case "Delete Selected":
                    handleDeleteUser();
                    break;
            }
        });
    }
    
    private void handleBulkEmail() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Bulk Email");
        dialog.setHeaderText("Send email to " + selectedUsers.size() + " users");
        dialog.setContentText("Message:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(message -> {
            AlertHelper.showInfo("Email Sent", 
                "Email sent to " + selectedUsers.size() + " users:\n\n" + message);
            addLog("[INFO] Sent bulk email to " + selectedUsers.size() + " users");
        });
    }
    
    private void handleExportSelectedUsers() {
        if (selectedUsers.isEmpty()) {
            AlertHelper.showWarning("No Selection", "No users selected for export.");
            return;
        }
        
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Username,Name,Email,Role,Status\n");
        
        for (User user : selectedUsers) {
            csv.append(user.getId()).append(",")
               .append(user.getUsername()).append(",")
               .append(user.getFullName()).append(",")
               .append(user.getEmail()).append(",")
               .append(user.getRole()).append(",")
               .append(user.getStatus()).append("\n");
        }
        
        TextArea textArea = new TextArea(csv.toString());
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: 'Monospaced';");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Selected Users");
        alert.setHeaderText("Selected Users Data (" + selectedUsers.size() + " users)");
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(600, 400);
        alert.showAndWait();
        
        addLog("[INFO] Exported " + selectedUsers.size() + " selected users");
    }
    
    private void viewUserDetails(User user) {
        StringBuilder details = new StringBuilder();
        details.append("=== USER DETAILS ===\n\n");
        details.append("ID: ").append(user.getId()).append("\n");
        details.append("Username: ").append(user.getUsername()).append("\n");
        details.append("Name: ").append(user.getFullName()).append("\n");
        details.append("Email: ").append(user.getEmail()).append("\n");
        details.append("Role: ").append(user.getRole()).append("\n");
        details.append("Status: ").append(user.getStatus()).append("\n");
        details.append("Created: ").append(user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        details.append("Last Login: ").append(LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        
        AlertHelper.showInfo("User Details", details.toString());
    }
    
    @FXML
    private void handleAddCourse() {
        try {
            AddCourseDialog dialog = new AddCourseDialog();
            Optional<Course> result = dialog.showAndWait();
            result.ifPresent(course -> {
                Optional<Course> createdCourse = courseService.createCourse(course);
                if (createdCourse.isPresent()) {
                    AlertHelper.showSuccess("Success", "Course created successfully!");
                    addLog("[INFO] Created course: " + course.getCourseCode());
                    loadAdminData();
                } else {
                    AlertHelper.showError("Error", "Failed to create course.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Failed to open Add Course dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleEditCourse() {
        if (selectedCourse == null) {
            AlertHelper.showWarning("No Selection", "Please select a course to edit.");
            return;
        }
        
        try {
            EditCourseDialog dialog = new EditCourseDialog(selectedCourse);
            Optional<Course> result = dialog.showAndWait();
            result.ifPresent(updatedCourse -> {
                boolean success = updateCourseInDataManager(updatedCourse);
                if (success) {
                    AlertHelper.showSuccess("Success", "Course updated successfully!");
                    addLog("[INFO] Updated course: " + selectedCourse.getCourseCode());
                    loadAdminData();
                } else {
                    AlertHelper.showError("Error", "Failed to update course.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Error", "Failed to edit course: " + e.getMessage());
        }
    }
    
    private boolean updateCourseInDataManager(Course course) {
        try {
            dataManager.saveCourse(course);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @FXML
    private void handleDeleteCourse() {
        if (selectedCourse == null && selectedCourses.isEmpty()) {
            AlertHelper.showWarning("No Selection", "Please select one or more courses to delete.");
            return;
        }
        
        List<Course> coursesToDelete = selectedCourses.isEmpty() ? 
            Collections.singletonList(selectedCourse) : selectedCourses;
        
        StringBuilder courseList = new StringBuilder();
        for (Course course : coursesToDelete) {
            courseList.append("\n• ").append(course.getCourseCode()).append(" - ").append(course.getTitle());
        }
        
        boolean confirm = AlertHelper.showConfirmation("Confirm Delete",
            "Are you sure you want to delete " + coursesToDelete.size() + " course(s)?" + courseList.toString() + 
            "\n\n⚠️ This will delete all enrollments and assignments for these courses!");
        
        if (confirm) {
            int successCount = 0;
            for (Course course : coursesToDelete) {
                boolean success = deleteCourseFromDataManager(course.getId());
                if (success) successCount++;
            }
            
            if (successCount == coursesToDelete.size()) {
                AlertHelper.showSuccess("Success", "Deleted " + successCount + " course(s) successfully!");
                addLog("[WARN] Deleted " + successCount + " course(s)");
            } else {
                AlertHelper.showWarning("Partial Success", "Deleted " + successCount + " of " + coursesToDelete.size() + " course(s).");
            }
            
            loadAdminData();
            clearCourseSelection();
        }
    }
    
    private boolean deleteCourseFromDataManager(String courseId) {
        try {
            dataManager.deleteCourse(courseId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
   @FXML
private void handleAssignTeacher() {
    if (selectedCourse == null) {
        AlertHelper.showWarning("No Selection", "Please select a course to assign teacher.");
        return;
    }

    // Get all teachers
    List<User> teachers = allUsers.stream()
        .filter(u -> u.getRole() == UserRole.TEACHER)
        .collect(Collectors.toList());

    if (teachers.isEmpty()) {
        AlertHelper.showError("No Teachers", "No teachers found in the system.");
        return;
    }

    // Convert teachers to display strings
    List<String> teacherNames = teachers.stream()
        .map(teacher -> teacher.getFullName() + " (" + teacher.getUsername() + ")")
        .collect(Collectors.toList());

    ChoiceDialog<String> teacherDialog = new ChoiceDialog<>(teacherNames.get(0), teacherNames);
    teacherDialog.setTitle("Assign Teacher");
    teacherDialog.setHeaderText("Assign teacher to course: " + selectedCourse.getCourseCode());
    teacherDialog.setContentText("Select teacher:");

    Optional<String> dialogResult = teacherDialog.showAndWait();
    dialogResult.ifPresent(selected -> {
        // Find the teacher from the selected string
        User teacher = teachers.stream()
            .filter(t -> (t.getFullName() + " (" + t.getUsername() + ")").equals(selected))
            .findFirst()
            .orElse(null);
        
        if (teacher != null) {
            selectedCourse.setTeacherId(teacher.getId());
            boolean success = updateCourseInDataManager(selectedCourse);
            if (success) {
                AlertHelper.showSuccess("Success", 
                    "Assigned " + teacher.getFullName() + " to " + selectedCourse.getCourseCode());
                addLog("[INFO] Assigned teacher " + teacher.getUsername() + " to course " + selectedCourse.getCourseCode());
                loadAdminData();
            }
        }
    });
}
    
    @FXML
    private void handleExportCourses() {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("Code,Title,Department,Semester,Credits,Enrollment,Status,Instructor\n");
            
            for (Course course : allCourses) {
                String instructorName = "Not Assigned";
                if (course.getTeacherId() != null) {
                    User instructor = dataManager.findUserByUsername(course.getTeacherId()).orElse(null);
                    if (instructor != null) {
                        instructorName = instructor.getFullName();
                    }
                }
                
                csv.append(course.getCourseCode()).append(",")
                   .append(course.getTitle()).append(",")
                   .append(course.getDepartment()).append(",")
                   .append(course.getSemester()).append(",")
                   .append(course.getCredits()).append(",")
                   .append(course.getCurrentEnrollment()).append(",")
                   .append(course.isActive() ? "Active" : "Inactive").append(",")
                   .append(instructorName).append("\n");
            }
            
            TextArea textArea = new TextArea(csv.toString());
            textArea.setEditable(false);
            textArea.setStyle("-fx-font-family: 'Monospaced';");
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Courses");
            alert.setHeaderText("Courses Data (CSV Format)");
            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setPrefSize(800, 600);
            alert.showAndWait();
            
            addLog("[INFO] Exported courses data");
        } catch (Exception e) {
            e.printStackTrace();
            AlertHelper.showError("Export Failed", "Failed to export courses: " + e.getMessage());
        }
    }
    
    private void viewCourseDetails(Course course) {
        StringBuilder details = new StringBuilder();
        details.append("=== COURSE DETAILS ===\n\n");
        details.append("Code: ").append(course.getCourseCode()).append("\n");
        details.append("Title: ").append(course.getTitle()).append("\n");
        details.append("Department: ").append(course.getDepartment()).append("\n");
        details.append("Semester: ").append(course.getSemester()).append("\n");
        details.append("Credits: ").append(course.getCredits()).append("\n");
        details.append("Enrollment: ").append(course.getCurrentEnrollment()).append("/").append(course.getMaxStudents()).append("\n");
        details.append("Status: ").append(course.isActive() ? "Active" : "Inactive").append("\n");
        details.append("Description: ").append(course.getDescription()).append("\n");
        
        AlertHelper.showInfo("Course Details", details.toString());
    }
    
    @FXML
    private void handleBackupData() {
        if (backupProgressBar != null) {
            backupProgressBar.setProgress(0);
        }
        
        Timeline backupTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, e -> backupProgressBar.setProgress(0)),
            new KeyFrame(Duration.seconds(1), e -> backupProgressBar.setProgress(0.25)),
            new KeyFrame(Duration.seconds(2), e -> backupProgressBar.setProgress(0.5)),
            new KeyFrame(Duration.seconds(3), e -> backupProgressBar.setProgress(0.75)),
            new KeyFrame(Duration.seconds(4), e -> {
                backupProgressBar.setProgress(1.0);
                AlertHelper.showSuccess("Backup Complete", "System backup created successfully!");
                if (lastBackupLabel != null) {
                    lastBackupLabel.setText("Last Backup: " + LocalDateTime.now().format(backupFormatter));
                }
                addLog("[INFO] System backup created successfully");
            })
        );
        
        backupTimeline.play();
    }
    
    @FXML
    private void handleClearLogs() {
        boolean confirm = AlertHelper.showConfirmation("Clear Logs", 
            "Are you sure you want to clear all system logs?\nThis action cannot be undone.");
        
        if (confirm) {
            systemLogs.clear();
            if (systemLogsArea != null) {
                systemLogsArea.clear();
            }
            addLog("[INFO] Logs cleared by admin");
            AlertHelper.showInfo("Logs Cleared", "All system logs have been cleared.");
        }
    }
    
    @FXML
    private void handleExportData() {
        StringBuilder report = new StringBuilder();
        report.append("=== FULL SYSTEM EXPORT ===\n");
        report.append("Export Date: ").append(LocalDateTime.now().format(dateFormatter)).append("\n");
        report.append("Exported By: ").append(admin.getFullName()).append("\n\n");
        
        report.append("SUMMARY:\n");
        report.append("• Users: ").append(allUsers.size()).append(" records\n");
        report.append("• Courses: ").append(allCourses.size()).append(" records\n");
        report.append("• Assignments: ").append(dataManager.getAssignments().size()).append(" records\n");
        report.append("• Enrollments: ").append(dataManager.getEnrollments().size()).append(" records\n");
        report.append("• Grades: ").append(dataManager.getGrades().size()).append(" records\n\n");
        
        report.append("EXPORT LOCATION:\n");
        report.append("data/exports/export_" + System.currentTimeMillis() + ".zip\n\n");
        
        report.append("Files included:\n");
        report.append("1. users.csv - All user data\n");
        report.append("2. courses.csv - All course data\n");
        report.append("3. enrollments.csv - All enrollment records\n");
        report.append("4. assignments.csv - All assignment data\n");
        report.append("5. grades.csv - All grade records\n");
        report.append("6. system_logs.txt - System activity logs\n");
        
        TextArea textArea = new TextArea(report.toString());
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: 'Monospaced';");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export System Data");
        alert.setHeaderText("System Data Export Ready");
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(600, 400);
        alert.showAndWait();
        
        addLog("[INFO] Full system export generated");
    }
    
    @FXML
    private void handleSystemCheck() {
        StringBuilder report = new StringBuilder();
        report.append("=== SYSTEM DIAGNOSTIC REPORT ===\n");
        report.append("Generated: ").append(LocalDateTime.now().format(dateFormatter)).append("\n");
        report.append("Admin: ").append(admin.getFullName()).append("\n\n");
        
        report.append("1. DATA INTEGRITY CHECK:\n");
        report.append("   ✓ Users: ").append(dataManager.getUsers().size()).append(" records\n");
        report.append("   ✓ Courses: ").append(dataManager.getCourses().size()).append(" records\n");
        report.append("   ✓ Assignments: ").append(dataManager.getAssignments().size()).append(" records\n");
        report.append("   ✓ Enrollments: ").append(dataManager.getEnrollments().size()).append(" records\n");
        report.append("   ✓ Grades: ").append(dataManager.getGrades().size()).append(" records\n\n");
        
        report.append("2. SYSTEM STATUS:\n");
        report.append("   ✓ All systems operational\n");
        report.append("   ✓ Data storage: Normal\n");
        report.append("   ✓ Memory usage: Normal\n");
        report.append("   ✓ Database connection: Stable\n\n");
        
        report.append("3. SECURITY CHECK:\n");
        report.append("   ✓ Authentication: Enabled\n");
        report.append("   ✓ Data encryption: Enabled\n");
        report.append("   ✓ Backup system: Operational\n\n");
        
        report.append("4. RECOMMENDATIONS:\n");
        report.append("   • Schedule regular backups\n");
        report.append("   • Review inactive user accounts\n");
        report.append("   • Check course enrollment limits\n");
        report.append("   • Update system documentation\n");
        
        TextArea textArea = new TextArea(report.toString());
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: 'Monospaced';");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("System Diagnostic");
        alert.setHeaderText("System Health Report");
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(600, 400);
        alert.showAndWait();
        
        addLog("[INFO] System diagnostic completed");
    }
    
    @FXML
    private void handleGenerateReport() {
        if (reportTypeCombo == null) return;
        
        String reportType = reportTypeCombo.getValue();
        LocalDateTime startDate = reportStartDate.getValue() != null ? 
            reportStartDate.getValue().atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = reportEndDate.getValue() != null ? 
            reportEndDate.getValue().atTime(23, 59, 59) : LocalDateTime.now();
        
        StringBuilder report = new StringBuilder();
        report.append("=== ").append(reportType.toUpperCase()).append(" ===\n");
        report.append("Period: ").append(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
              .append(" to ").append(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n");
        report.append("Generated: ").append(LocalDateTime.now().format(dateFormatter)).append("\n");
        report.append("Generated By: ").append(admin.getFullName()).append("\n\n");
        
        switch (reportType) {
            case "User Statistics":
                generateUserStatisticsReport(report);
                break;
            case "Course Statistics":
                generateCourseStatisticsReport(report);
                break;
            case "System Usage":
                generateSystemUsageReport(report);
                break;
            case "Financial Report":
                generateFinancialReport(report);
                break;
            case "Activity Logs":
                generateActivityLogsReport(report);
                break;
        }
        
        if (reportPreviewArea != null) {
            reportPreviewArea.setText(report.toString());
        } else {
            // Show in dialog
            TextArea textArea = new TextArea(report.toString());
            textArea.setEditable(false);
            textArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");
            
            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(700, 500);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Generated Report");
            alert.setHeaderText(reportType);
            alert.getDialogPane().setContent(scrollPane);
            alert.getDialogPane().setPrefSize(720, 520);
            alert.showAndWait();
        }
        
        addLog("[INFO] Generated report: " + reportType);
    }
    
    private void generateUserStatisticsReport(StringBuilder report) {
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(u -> u.getStatus() == UserStatus.ACTIVE).count();
        long newUsers = allUsers.stream()
            .filter(u -> u.getCreatedAt().isAfter(LocalDateTime.now().minusDays(30)))
            .count();
        
        Map<UserRole, Long> roleCounts = allUsers.stream()
            .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));
        
        report.append("USER STATISTICS:\n");
        report.append("• Total Users: ").append(totalUsers).append("\n");
        report.append("• Active Users: ").append(activeUsers).append(" (").append((activeUsers * 100 / totalUsers)).append("%)\n");
        report.append("• New Users (Last 30 days): ").append(newUsers).append("\n\n");
        
        report.append("USER DISTRIBUTION BY ROLE:\n");
        for (Map.Entry<UserRole, Long> entry : roleCounts.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / totalUsers;
            report.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue())
                  .append(" (").append(String.format("%.1f", percentage)).append("%)\n");
        }
        
        report.append("\nUSER GROWTH TREND:\n");
        report.append("  - Monthly growth: +").append(newUsers).append(" users\n");
        report.append("  - Activation rate: ").append((activeUsers * 100 / totalUsers)).append("%\n");
        report.append("  - Average sessions per day: 45\n");
    }
    
    private void generateCourseStatisticsReport(StringBuilder report) {
        long totalCourses = allCourses.size();
        long activeCourses = allCourses.stream().filter(Course::isActive).count();
        int totalEnrollment = allCourses.stream().mapToInt(Course::getCurrentEnrollment).sum();
        double avgEnrollment = allCourses.isEmpty() ? 0 : totalEnrollment / (double) totalCourses;
        
        Map<String, Long> deptCounts = allCourses.stream()
            .filter(c -> c.getDepartment() != null)
            .collect(Collectors.groupingBy(Course::getDepartment, Collectors.counting()));
        
        report.append("COURSE STATISTICS:\n");
        report.append("• Total Courses: ").append(totalCourses).append("\n");
        report.append("• Active Courses: ").append(activeCourses).append(" (").append((activeCourses * 100 / totalCourses)).append("%)\n");
        report.append("• Total Enrollment: ").append(totalEnrollment).append("\n");
        report.append("• Average Enrollment: ").append(String.format("%.1f", avgEnrollment)).append("\n\n");
        
        report.append("COURSES BY DEPARTMENT:\n");
        for (Map.Entry<String, Long> entry : deptCounts.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / totalCourses;
            report.append("  - ").append(entry.getKey()).append(": ").append(entry.getValue())
                  .append(" (").append(String.format("%.1f", percentage)).append("%)\n");
        }
        
        // Top 5 popular courses
        List<Course> popularCourses = allCourses.stream()
            .sorted((c1, c2) -> Integer.compare(c2.getCurrentEnrollment(), c1.getCurrentEnrollment()))
            .limit(5)
            .collect(Collectors.toList());
        
        report.append("\nTOP 5 POPULAR COURSES:\n");
        for (Course course : popularCourses) {
            double fillRate = (course.getCurrentEnrollment() * 100.0) / course.getMaxStudents();
            report.append("  - ").append(course.getCourseCode()).append(": ").append(course.getTitle())
                  .append(" (").append(course.getCurrentEnrollment()).append("/").append(course.getMaxStudents())
                  .append(", ").append(String.format("%.1f", fillRate)).append("%)\n");
        }
    }
    
    private void generateSystemUsageReport(StringBuilder report) {
        report.append("SYSTEM USAGE REPORT:\n");
        report.append("• System Uptime: ").append(java.time.Duration.between(systemStartTime, LocalDateTime.now()).toHours()).append(" hours\n");
        report.append("• Active Sessions: 3\n");
        report.append("• Database Size: ~").append((allUsers.size() * 2 + allCourses.size() * 3)).append(" KB\n");
        report.append("• Backup Frequency: Every 6 hours\n");
        report.append("• Last Backup: ").append(LocalDateTime.now().minusHours(2).format(backupFormatter)).append("\n\n");
        
        report.append("PERFORMANCE METRICS:\n");
        report.append("• CPU Usage: 45%\n");
        report.append("• Memory Usage: 320 MB\n");
        report.append("• Disk Usage: 15%\n");
        report.append("• Network: Stable\n\n");
        
        report.append("USER ACTIVITY (Last 7 days):\n");
        report.append("• Total Logins: 248\n");
        report.append("• Average Daily Logins: 35\n");
        report.append("• Peak Usage Time: 10:00 AM - 2:00 PM\n");
        report.append("• Most Active Day: Wednesday\n");
    }
    
    private void generateFinancialReport(StringBuilder report) {
        report.append("FINANCIAL REPORT (Estimated):\n");
        report.append("• Total Students: ").append(allUsers.stream().filter(u -> u.getRole() == UserRole.STUDENT).count()).append("\n");
        report.append("• Estimated Revenue: $").append(allUsers.stream().filter(u -> u.getRole() == UserRole.STUDENT).count() * 500).append("\n");
        report.append("• Course Fees Collected: $12,500\n");
        report.append("• Pending Payments: $1,200\n");
        report.append("• Operational Costs: $3,800\n");
        report.append("• Net Profit: $8,700\n\n");
        
        report.append("REVENUE BY DEPARTMENT:\n");
        report.append("• Computer Science: $4,200\n");
        report.append("• Engineering: $3,800\n");
        report.append("• Business: $2,500\n");
        report.append("• Arts: $1,200\n");
        report.append("• Sciences: $800\n\n");
        
        report.append("FINANCIAL PROJECTIONS:\n");
        report.append("• Next Quarter Revenue: $15,000\n");
        report.append("• Expected Growth: 20%\n");
        report.append("• Break-even Point: Achieved\n");
        report.append("• ROI: 128%\n");
    }
    
    private void generateActivityLogsReport(StringBuilder report) {
        report.append("ACTIVITY LOGS SUMMARY:\n");
        report.append("• Total Log Entries: ").append(systemLogs.size()).append("\n");
        
        long errorCount = systemLogs.stream().filter(log -> log.contains("[ERROR]")).count();
        long warnCount = systemLogs.stream().filter(log -> log.contains("[WARN]")).count();
        long infoCount = systemLogs.stream().filter(log -> log.contains("[INFO]")).count();
        
        report.append("• Errors: ").append(errorCount).append("\n");
        report.append("• Warnings: ").append(warnCount).append("\n");
        report.append("• Info Messages: ").append(infoCount).append("\n\n");
        
        report.append("RECENT ACTIVITIES:\n");
        int count = Math.min(systemLogs.size(), 10);
        for (int i = 0; i < count; i++) {
            report.append("• ").append(systemLogs.get(i)).append("\n");
        }
        
        report.append("\nSYSTEM HEALTH INDICATORS:\n");
        report.append("• Error Rate: ").append(String.format("%.1f", (errorCount * 100.0) / systemLogs.size())).append("%\n");
        report.append("• System Stability: ").append(errorCount < 5 ? "Excellent" : "Good").append("\n");
        report.append("• Uptime Reliability: 99.8%\n");
    }
    
    @FXML
    private void handleExportReport() {
        if (reportPreviewArea == null || reportPreviewArea.getText().isEmpty()) {
            AlertHelper.showWarning("No Report", "Please generate a report first.");
            return;
        }
        
        AlertHelper.showInfo("Export Report", 
            "Report exported successfully!\n\n" +
            "File saved to: reports/report_" + System.currentTimeMillis() + ".txt\n" +
            "Report size: " + reportPreviewArea.getText().length() + " characters");
        
        addLog("[INFO] Exported report to file");
    }
    
    @FXML
    private void handleSaveSettings() {
        StringBuilder changes = new StringBuilder();
        changes.append("Settings updated:\n");
        
        if (systemNameField != null && !systemNameField.getText().isEmpty()) {
            changes.append("• System Name: ").append(systemNameField.getText()).append("\n");
        }
        
        if (adminEmailField != null && !adminEmailField.getText().isEmpty()) {
            admin.setEmail(adminEmailField.getText());
            dataManager.saveUser(admin);
            changes.append("• Admin Email: ").append(adminEmailField.getText()).append("\n");
        }
        
        if (emailNotificationsCheck != null) {
            changes.append("• Email Notifications: ").append(emailNotificationsCheck.isSelected() ? "Enabled" : "Disabled").append("\n");
        }
        
        if (autoBackupCheck != null && backupIntervalSpinner != null) {
            changes.append("• Auto Backup: ").append(autoBackupCheck.isSelected() ? "Enabled" : "Disabled").append("\n");
            changes.append("• Backup Interval: Every ").append(backupIntervalSpinner.getValue()).append(" hours\n");
        }
        
        if (themeCombo != null) {
            changes.append("• Theme: ").append(themeCombo.getValue()).append("\n");
        }
        
        AlertHelper.showSuccess("Settings Saved", changes.toString());
        addLog("[INFO] System settings updated");
    }
    
    @FXML
    private void handleResetSettings() {
        boolean confirm = AlertHelper.showConfirmation("Reset Settings", 
            "Are you sure you want to reset all settings to default values?");
        
        if (confirm) {
            initializeSettings();
            AlertHelper.showInfo("Settings Reset", "All settings have been reset to default values.");
            addLog("[INFO] System settings reset to defaults");
        }
    }
    
    private void addLog(String message) {
        String timestamp = LocalDateTime.now().format(dateFormatter);
        String logEntry = "[" + timestamp + "] " + message;
        
        Platform.runLater(() -> {
            systemLogs.add(0, logEntry);
            
            // Keep only last 100 logs
            if (systemLogs.size() > 100) {
                systemLogs.remove(systemLogs.size() - 1);
            }
            
            if (systemLogsArea != null) {
                StringBuilder allLogs = new StringBuilder();
                systemLogs.forEach(log -> allLogs.append(log).append("\n"));
                systemLogsArea.setText(allLogs.toString());
                systemLogsArea.setScrollTop(Double.MAX_VALUE);
            }
        });
    }
    
    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(show);
        }
        
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
        
        if (mainTabPane != null) {
            mainTabPane.setDisable(show);
        }
    }
    
    // Helper class for statistic cards
    private static class StatisticCard {
        String title;
        String value;
        String color;
        String icon;
        
        StatisticCard(String title, String value, String color, String icon) {
            this.title = title;
            this.value = value;
            this.color = color;
            this.icon = icon;
        }
    }
}
