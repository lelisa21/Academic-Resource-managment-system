package com.arms.gui.controllers;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.controlsfx.control.GridView;

import com.arms.domain.Assignment;
import com.arms.domain.Course;
import com.arms.domain.Student;
import com.arms.domain.Teacher;
import com.arms.gui.components.AssignmentCard;
import com.arms.gui.components.CourseCard;
import com.arms.gui.dialogs.AddAssignmentDialog;
import com.arms.gui.util.AlertHelper;
import com.arms.gui.util.NavigationHelper;
import com.arms.service.AssignmentService;
import com.arms.service.CourseService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

public class TeacherController extends DashboardController {

    @FXML private Label employeeIdLabel;
    @FXML private Label departmentLabel;
    @FXML private Label qualificationLabel;
    @FXML private Label experienceLabel;

    // Updated to match TeacherDashboard.fxml
    @FXML private GridView<Course> dashboardCoursesGrid;
    @FXML private GridView<Assignment> dashboardAssignmentsGrid;
    @FXML private ListView<Student> dashboardStudentsList;
    
    // For tab-specific grids
    @FXML private GridView<Course> coursesTabGrid;
    @FXML private GridView<Assignment> assignmentsTabGrid;
    @FXML private ListView<Student> studentsTabList;

    @FXML private TabPane mainTabPane;
    @FXML private VBox statisticsContainer;
    @FXML private ProgressIndicator loadingIndicator;

    private final CourseService courseService = CourseService.getInstance();
    private final AssignmentService assignmentService = AssignmentService.getInstance();

    private Teacher currentTeacher;

    private final ObservableList<Course> courses = FXCollections.observableArrayList();
    private final ObservableList<Assignment> assignments = FXCollections.observableArrayList();
    private final ObservableList<Student> students = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        if (currentUser instanceof Teacher teacher) {
            currentTeacher = teacher;
            initializeTeacherUI();
            loadTeacherData();
        } else {
            AlertHelper.showError("Access Denied", "This dashboard is for teachers only.");
            NavigationHelper.navigateToLogin();
        }
    }

    @Override
    protected void loadDashboardData() {
        loadStatistics();
    }

    private void initializeTeacherUI() {
        employeeIdLabel.setText("Employee ID: " + currentTeacher.getEmployeeId());
        departmentLabel.setText("Department: " + currentTeacher.getDepartment());
        qualificationLabel.setText("Qualification: " + currentTeacher.getQualification());
        experienceLabel.setText("Experience: " + currentTeacher.getYearsOfExperience() + " years");

        // Initialize GridViews if they exist
        if (dashboardCoursesGrid != null) {
            dashboardCoursesGrid.setCellFactory(grid -> new CourseCard());
            dashboardCoursesGrid.setItems(courses);
        }
        
        if (dashboardAssignmentsGrid != null) {
            dashboardAssignmentsGrid.setCellFactory(grid -> new AssignmentCard());
            dashboardAssignmentsGrid.setItems(assignments);
        }
        
        if (dashboardStudentsList != null) {
            dashboardStudentsList.setItems(students);
        }
        
        // Initialize tab-specific grids
        if (coursesTabGrid != null) {
            coursesTabGrid.setCellFactory(grid -> new CourseCard());
            coursesTabGrid.setItems(courses);
        }
        
        if (assignmentsTabGrid != null) {
            assignmentsTabGrid.setCellFactory(grid -> new AssignmentCard());
            assignmentsTabGrid.setItems(assignments);
        }
        
        if (studentsTabList != null) {
            studentsTabList.setItems(students);
        }
    }

    private void loadTeacherData() {
        showLoading(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                List<Course> teacherCourses =
                        courseService.getCoursesByTeacher(currentTeacher.getId());

                List<Assignment> teacherAssignments =
                        assignmentService.getAssignmentsByTeacher(currentTeacher.getId());

                List<Student> allStudents = teacherCourses.stream()
                        .flatMap(c -> courseService.getEnrolledStudents(c.getId()).stream())
                        .distinct()
                        .toList();

                Platform.runLater(() -> {
                    courses.setAll(teacherCourses);
                    assignments.setAll(teacherAssignments);
                    students.setAll(allStudents);
                });

                return null;
            }
        };

        task.setOnSucceeded(e -> {
            showLoading(false);
            loadStatistics();
        });

        task.setOnFailed(e -> {
            showLoading(false);
            AlertHelper.showError("Load Error", "Failed to load teacher data.");
        });

        new Thread(task).start();
    }

    private void loadStatistics() {
        if (statisticsContainer == null) return;
        
        statisticsContainer.getChildren().clear();

        addStatisticCard("Courses Teaching", String.valueOf(courses.size()));
        addStatisticCard("Assignments Created", String.valueOf(assignments.size()));
        addStatisticCard("Total Students", String.valueOf(students.size()));
    }

    private void addStatisticCard(String title, String value) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("stat-card");

        card.getChildren().addAll(
                new Label(title),
                new Label(value)
        );

        statisticsContainer.getChildren().add(card);
    }

    @FXML
    private void handleRefreshData() {
        loadTeacherData();
    }

    @FXML
    private void handleCreateAssignment() {
        if (courses.isEmpty()) {
            AlertHelper.showWarning("No Courses", "No courses assigned.");
            return;
        }

        AddAssignmentDialog dialog = new AddAssignmentDialog(courses);
        Optional<Assignment> result = dialog.showAndWait();

        result.ifPresent(a -> {
            assignmentService.createAssignment(a);
            loadTeacherData();
        });
    }

    @FXML
    private void handleGradeSubmissions() {
        AlertHelper.showInfo(
                "Grade Submissions",
                "This feature would open a grading interface for assignments."
        );
    }

    @FXML
    private void handleViewCourseStudents() {
        AlertHelper.showInfo(
                "View Students",
                "This feature would show students enrolled in your courses."
        );
    }

    @FXML
    private void handleGenerateReport() {
        AlertHelper.showInfo("Coming Soon", "Report generation is under development.");
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
