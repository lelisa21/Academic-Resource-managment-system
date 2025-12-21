package com.arms.gui.controllers;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.Notifications;

import com.arms.domain.Assignment;
import com.arms.domain.Course;
import com.arms.domain.Grade;
import com.arms.domain.Student;
import com.arms.gui.components.AssignmentCard;
import com.arms.gui.components.CourseCard;
import com.arms.gui.components.GradeCard;
import com.arms.gui.dialogs.AssignmentSubmissionDialog;
import com.arms.gui.dialogs.CourseEnrollmentDialog;
import com.arms.gui.util.AlertHelper;
import com.arms.gui.util.NavigationHelper;
import com.arms.service.AssignmentService;
import com.arms.service.CourseService;
import com.arms.service.GradeService;
import com.arms.service.UserService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class StudentController extends DashboardController {

    @FXML
    private Label studentIdLabel;
    @FXML
    private Label departmentLabel;
    @FXML
    private Label semesterLabel;
    @FXML
    private Label cgpaLabel;
    @FXML
    private Label creditsCompletedLabel;

    @FXML
    private GridView<Course> enrolledCoursesGrid;
    @FXML
    private GridView<Course> coursesGrid;
    @FXML
    private GridView<Assignment> assignmentsGrid;
    @FXML
    private GridView<Grade> gradesGrid;

    @FXML
    private TabPane mainTabPane;
    @FXML
    @SuppressWarnings("unused")
    private Tab dashboardTab;
    @FXML
    private Tab coursesTab;
    @FXML
    private Tab assignmentsTab;
    @FXML
    private Tab gradesTab;

    @FXML
    private VBox statisticsContainer;
    @FXML
    private ProgressIndicator loadingIndicator;

    private final CourseService courseService = CourseService.getInstance();
    private final AssignmentService assignmentService = AssignmentService.getInstance();
    private final GradeService gradeService = GradeService.getInstance();
    private final UserService userService = UserService.getInstance();

    private Student currentStudent;
    private final ObservableList<Course> enrolledCourses = FXCollections.observableArrayList();
    private final ObservableList<Assignment> assignments = FXCollections.observableArrayList();
    private final ObservableList<Grade> grades = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        if (currentUser instanceof Student student) {
            currentStudent = student;
            initializeStudentUI();
            setupEventHandlers();
            loadStudentData();
        } else {
            AlertHelper.showError("Access Denied", "This dashboard is for students only.");
            NavigationHelper.navigateToLogin();
        }
    }

    @Override
    protected void loadDashboardData() {
        loadStatistics();
    }

    private void initializeStudentUI() {
        studentIdLabel.setText("ID: " + currentStudent.getStudentId());
        departmentLabel.setText("Department: " + currentStudent.getDepartment());
        semesterLabel.setText("Semester: " + currentStudent.getSemester());
        cgpaLabel.setText(String.format("CGPA: %.2f", currentStudent.getCgpa()));
        creditsCompletedLabel.setText("Credits Completed: " + currentStudent.getCreditsCompleted());

        // Configure GridViews
        enrolledCoursesGrid.setCellFactory(gridView -> new CourseCard());
        if (coursesGrid != null) {
            coursesGrid.setCellFactory(gridView -> new CourseCard());
            coursesGrid.setItems(enrolledCourses);
        }
        assignmentsGrid.setCellFactory(gridView -> new AssignmentCard());
        gradesGrid.setCellFactory(gridView -> new GradeCard());

        enrolledCoursesGrid.setItems(enrolledCourses);
        assignmentsGrid.setItems(assignments);
        gradesGrid.setItems(grades);
    }

    private void setupEventHandlers() {
        // Course double-click to view details
        enrolledCoursesGrid.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Object target = event.getTarget();
                Node node = (target instanceof Node) ? (Node) target : null;
                while (node != null && !(node instanceof GridCell)) {
                    node = node.getParent();
                }
                if (node instanceof GridCell) {
                    GridCell<?> cell = (GridCell<?>) node;
                    Object item = cell.getItem();
                    if (item instanceof Course course) {
                        viewCourseDetails(course);
                    }
                }
            }
        });

        // Assignment double-click to submit
        assignmentsGrid.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Object target = event.getTarget();
                Node node = (target instanceof Node) ? (Node) target : null;
                while (node != null && !(node instanceof GridCell)) {
                    node = node.getParent();
                }
                if (node instanceof GridCell) {
                    GridCell<?> cell = (GridCell<?>) node;
                    Object item = cell.getItem();
                    if (item instanceof Assignment assignment) {
                        submitAssignment(assignment);
                    }
                }
            }
        });
    }

    private void loadStudentData() {
        showLoading(true);

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                // Load enrolled courses
                List<Course> courses = courseService.getCoursesByStudent(currentStudent.getId());
                Platform.runLater(() -> enrolledCourses.setAll(courses));

                // Load assignments
                List<Assignment> studentAssignments = assignmentService.getAssignmentsForStudent(currentStudent.getId());
                Platform.runLater(() -> assignments.setAll(studentAssignments));

                // Load grades
                List<Grade> studentGrades = gradeService.getGradesByStudent(currentStudent.getId());
                Platform.runLater(() -> grades.setAll(studentGrades));

                return null;
            }
        };

        loadTask.setOnSucceeded(event -> {
            showLoading(false);
            updateStatistics();

            Notifications.create()
                    .title("Data Loaded")
                    .text("Student data loaded successfully")
                    .showInformation();
        });

        loadTask.setOnFailed(event -> {
            showLoading(false);
            AlertHelper.showError("Load Error", "Failed to load student data. Please try again.");
        });

        new Thread(loadTask).start();
    }

    private void loadStatistics() {
        Task<Void> statsTask = new Task<>() {
            @Override
            protected Void call() {
                // Calculate various statistics
                int totalCourses = enrolledCourses.size();
                int pendingAssignments = assignments.stream()
                        .filter(a -> a.canSubmit(currentStudent.getId()))
                        .collect(Collectors.toList())
                        .size();

                Optional<Double> averageGrade = gradeService.calculateStudentAverage(currentStudent.getId());

                Platform.runLater(() -> {
                    statisticsContainer.getChildren().clear();

                    // Add statistic cards
                    addStatisticCard("Total Courses", String.valueOf(totalCourses), "course-icon");
                    addStatisticCard("Pending Assignments", String.valueOf(pendingAssignments), "assignment-icon");
                    addStatisticCard("Average Grade",
                            averageGrade.map(avg -> String.format("%.2f%%", avg))
                                    .orElse("N/A"),
                            "grade-icon");

                    // Add upcoming deadlines
                    addUpcomingDeadlines();
                });

                return null;
            }
        };

        new Thread(statsTask).start();
    }

    private void addStatisticCard(String title, String value, String iconClass) {
        VBox card = new VBox();
        card.getStyleClass().add("stat-card");
        if (iconClass != null && !iconClass.isEmpty()) {
            card.getStyleClass().add(iconClass);
        }
        card.setPadding(new Insets(15));

        HBox header = new HBox(10);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        Text valueText = new Text(value);
        valueText.getStyleClass().add("stat-value");
        header.getChildren().addAll(titleLabel, valueText);
        card.getChildren().add(header);

        statisticsContainer.getChildren().add(card);
    }

    private void addUpcomingDeadlines() {
        VBox deadlinesCard = new VBox(10);
        deadlinesCard.getStyleClass().add("deadlines-card");
        deadlinesCard.setPadding(new Insets(15));

        Label title = new Label("Upcoming Deadlines");
        title.getStyleClass().add("deadlines-title");

        VBox deadlinesList = new VBox(5);

        List<Assignment> upcoming = assignments.stream()
                .filter(a -> a != null && a.getDueDate() != null && !a.isOverdue() && a.canSubmit(currentStudent.getId()))
                .sorted((a1, a2) -> a1.getDueDate().compareTo(a2.getDueDate()))
                .limit(5)
                .collect(Collectors.toList());

        for (Assignment assignment : upcoming) {
            HBox deadlineItem = new HBox(10);
            Label name = new Label(assignment.getTitle() != null ? assignment.getTitle() : "(no title)");
            Label dueDate = new Label(assignment.getDueDate() != null ? assignment.getDueDate().toString() : "--");

            deadlineItem.getChildren().addAll(name, dueDate);
            deadlinesList.getChildren().add(deadlineItem);
        }

        deadlinesCard.getChildren().addAll(title, deadlinesList);
        statisticsContainer.getChildren().add(deadlinesCard);
    }

    private void updateStatistics() {
        // Update CGPA based on new grades
        Optional<Double> average = gradeService.calculateStudentAverage(currentStudent.getId());
        average.ifPresent(avg -> {
            currentStudent.setCgpa(avg / 25.0); // Convert percentage to 4.0 scale
            userService.updateUser(currentStudent);
            cgpaLabel.setText(String.format("CGPA: %.2f", currentStudent.getCgpa()));
        });
    }

    @FXML
    private void handleEnrollInCourse() {
        CourseEnrollmentDialog dialog = new CourseEnrollmentDialog(currentStudent);
        Optional<Course> result = dialog.showAndWait();
        result.ifPresent(course -> {
            boolean success = courseService.enrollStudent(currentStudent.getId(), course.getId());
            if (success) {
                AlertHelper.showSuccess("Enrollment Successful",
                        "Successfully enrolled in " + course.getTitle());
                loadStudentData();
            } else {
                AlertHelper.showError("Enrollment Failed",
                        "Could not enroll in " + course.getTitle());
            }
        });
    }

    @FXML
    private void handleViewAllCourses() {
        mainTabPane.getSelectionModel().select(coursesTab);
    }

    @FXML
    private void handleViewAllAssignments() {
        mainTabPane.getSelectionModel().select(assignmentsTab);
    }

    @FXML
    private void handleViewAllGrades() {
        mainTabPane.getSelectionModel().select(gradesTab);
    }

    @FXML
    private void handleRefreshData() {
        loadStudentData();
    }

    @FXML
    private void handleGenerateTranscript() {
        AlertHelper.showInfo("Transcript", "Transcript generation feature coming soon!");
    }

    @FXML
    private void handleViewSchedule() {
        AlertHelper.showInfo("Schedule", "Schedule view feature coming soon!");
    }

    private void viewCourseDetails(Course course) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Course Details");
        dialog.setHeaderText(course.getTitle());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Course Code:"), 0, 0);
        grid.add(new Label(course.getCourseCode()), 1, 0);

        grid.add(new Label("Description:"), 0, 1);
        grid.add(new Label(course.getDescription()), 1, 1);

        grid.add(new Label("Credits:"), 0, 2);
        grid.add(new Label(String.valueOf(course.getCredits())), 1, 2);

        grid.add(new Label("Schedule:"), 0, 3);
        grid.add(new Label(course.getSchedule()), 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void submitAssignment(Assignment assignment) {
        AssignmentSubmissionDialog dialog = new AssignmentSubmissionDialog(assignment, currentStudent);
        Optional<Double> result = dialog.showAndWait();
        result.ifPresent(score -> {
            boolean success = assignmentService.submitAssignment(
                    assignment.getId(),
                    currentStudent.getId(),
                    score,
                    "Submission content here" // You would get this from the dialog
            );

            if (success) {
                AlertHelper.showSuccess("Submission Successful",
                        "Assignment submitted successfully!");
                loadStudentData();
            } else {
                AlertHelper.showError("Submission Failed",
                        "Could not submit assignment. Please try again.");
            }
        });
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        mainTabPane.setDisable(show);
    }
}
