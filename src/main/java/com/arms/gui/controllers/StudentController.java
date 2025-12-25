package com.arms.gui.controllers;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.controlsfx.control.GridView;
import org.controlsfx.control.Notifications;

import com.arms.domain.Assignment;
import com.arms.domain.Course;
import com.arms.domain.Enrollment;
import com.arms.domain.Grade;
import com.arms.domain.Student;
import com.arms.domain.enums.AssignmentStatus;
import com.arms.domain.enums.EnrollmentStatus;
import com.arms.gui.components.AssignmentCard;
import com.arms.gui.components.CourseCard;
import com.arms.gui.components.GradeCard;
import com.arms.gui.dialogs.CourseEnrollmentDialog;
import com.arms.gui.util.AlertHelper;
import com.arms.gui.util.NavigationHelper;
import com.arms.service.AssignmentService;
import com.arms.service.CourseService;
import com.arms.service.EnrollmentService;
import com.arms.service.GradeService;
import com.arms.service.UserService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

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
    private Label statusLabel;

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
    @FXML
    private javafx.scene.layout.StackPane loadingOverlay;

    private final CourseService courseService = CourseService.getInstance();
    private final AssignmentService assignmentService = AssignmentService.getInstance();
    private final GradeService gradeService = GradeService.getInstance();
    private final UserService userService = UserService.getInstance();
    private final EnrollmentService enrollmentService = EnrollmentService.getInstance();

    private Student currentStudent;
    private final ObservableList<Course> enrolledCourses = FXCollections.observableArrayList();
    private final ObservableList<Assignment> assignments = FXCollections.observableArrayList();
    private final ObservableList<Grade> grades = FXCollections.observableArrayList();
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");

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
        }
        assignmentsGrid.setCellFactory(gridView -> new AssignmentCard());
        gradesGrid.setCellFactory(gridView -> new GradeCard());

        enrolledCoursesGrid.setItems(enrolledCourses);
        coursesGrid.setItems(enrolledCourses);
        assignmentsGrid.setItems(assignments);
        gradesGrid.setItems(grades);
        
        // Setup loading overlay
        loadingOverlay.managedProperty().bind(loadingOverlay.visibleProperty());
    }

    private void setupEventHandlers() {
        // Add cell factory to handle clicks for courses
        enrolledCoursesGrid.setCellFactory(gridView -> {
            CourseCard card = new CourseCard();
            card.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Course course = card.getItem();
                    if (course != null) {
                        viewCourseDetails(course);
                    }
                }
            });
            return card;
        });
        
        coursesGrid.setCellFactory(gridView -> {
            CourseCard card = new CourseCard();
            card.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Course course = card.getItem();
                    if (course != null) {
                        viewCourseDetails(course);
                    }
                }
            });
            return card;
        });

        // Add cell factory to handle clicks for assignments
        assignmentsGrid.setCellFactory(gridView -> {
            AssignmentCard card = new AssignmentCard();
            card.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Assignment assignment = card.getItem();
                    if (assignment != null) {
                        submitAssignment(assignment);
                    }
                }
            });
            return card;
        });
        
        // Add cell factory to handle clicks for grades
        gradesGrid.setCellFactory(gridView -> {
            GradeCard card = new GradeCard();
            card.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Grade grade = card.getItem();
                    if (grade != null) {
                        viewGradeDetails(grade);
                    }
                }
            });
            return card;
        });
    }

    private void loadStudentData() {
        showLoading(true);
        statusLabel.setText("Loading data...");

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Get enrolled courses using EnrollmentService
                    List<Enrollment> studentEnrollments = enrollmentService.getEnrollmentsByStudent(currentStudent.getId());
                    
                    List<Course> courses = studentEnrollments.stream()
                            .filter(e -> e.getStatus() == EnrollmentStatus.ENROLLED)
                            .map(Enrollment::getCourseId)
                            .map(courseService::getCourseById)
                            .flatMap(Optional::stream)
                            .collect(Collectors.toList());
                    
                    Platform.runLater(() -> {
                        enrolledCourses.setAll(courses);
                        updateStatistics();
                    });

                    // Load assignments from enrolled courses
                    List<Assignment> studentAssignments = new ArrayList<>();
                    for (Course course : courses) {
                        try {
                            List<Assignment> courseAssignments = assignmentService.getAssignmentsByCourse(course.getId());
                            if (courseAssignments != null) {
                                studentAssignments.addAll(courseAssignments);
                            }
                        } catch (Exception e) {
                            System.err.println("Error loading assignments for course " + course.getId() + ": " + e.getMessage());
                        }
                    }
                    
                    Platform.runLater(() -> assignments.setAll(studentAssignments));

                    // Load grades for this student
                    List<Grade> studentGrades = gradeService.getGradesByStudent(currentStudent.getId());
                    
                    Platform.runLater(() -> grades.setAll(studentGrades));

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        AlertHelper.showError("Load Error", "Failed to load student data: " + e.getMessage());
                    });
                    throw e;
                }
                return null;
            }
        };

        loadTask.setOnSucceeded(event -> {
            showLoading(false);
            statusLabel.setText("Data loaded successfully");
            
            Notifications.create()
                    .title("Data Loaded")
                    .text("Student data loaded successfully")
                    .showInformation();
        });

        loadTask.setOnFailed(event -> {
            showLoading(false);
            statusLabel.setText("Load failed");
            AlertHelper.showError("Load Error", "Failed to load student data. Please try again.");
        });

        new Thread(loadTask).start();
    }

    private void loadStatistics() {
        if (statisticsContainer == null) return;
        
        Task<Void> statsTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    // Calculate various statistics
                    int totalCourses = enrolledCourses.size();
                    
                    long pendingAssignments = assignments.stream()
                            .filter(a -> a.canSubmit(currentStudent.getId()))
                            .count();
                    
                    long overdueAssignments = assignments.stream()
                            .filter(a -> a.isOverdue() && a.canSubmit(currentStudent.getId()))
                            .count();
                    
                    // Calculate average grade
                    double averageGrade = grades.stream()
                            .filter(Grade::isPublished)
                            .mapToDouble(Grade::getPercentage)
                            .average()
                            .orElse(0.0);
                    
                    double totalCredits = enrolledCourses.stream()
                            .mapToDouble(Course::getCredits)
                            .sum();
                    
                    // Get upcoming deadlines
                    List<Assignment> upcoming = assignments.stream()
                            .filter(a -> a != null && a.getDueDate() != null && 
                                   !a.isOverdue() && a.canSubmit(currentStudent.getId()))
                            .sorted((a1, a2) -> a1.getDueDate().compareTo(a2.getDueDate()))
                            .limit(5)
                            .collect(Collectors.toList());
                    
                    Platform.runLater(() -> {
                        statisticsContainer.getChildren().clear();
                        
                        // Create statistics grid
                        GridPane statsGrid = new GridPane();
                        statsGrid.setHgap(20);
                        statsGrid.setVgap(15);
                        statsGrid.setPadding(new Insets(15));
                        
                        // Row 1
                        statsGrid.add(createStatCard("Total Courses", 
                                String.valueOf(totalCourses), Color.web("#4CAF50")), 0, 0);
                        statsGrid.add(createStatCard("Total Credits", 
                                String.format("%.0f", totalCredits), Color.web("#2196F3")), 1, 0);
                        statsGrid.add(createStatCard("Average Grade", 
                                String.format("%.1f%%", averageGrade), Color.web("#FF9800")), 2, 0);
                        
                        // Row 2
                        statsGrid.add(createStatCard("Pending Assignments", 
                                String.valueOf(pendingAssignments), Color.web("#9C27B0")), 0, 1);
                        statsGrid.add(createStatCard("Overdue Assignments", 
                                String.valueOf(overdueAssignments), Color.web("#F44336")), 1, 1);
                        statsGrid.add(createStatCard("CGPA", 
                                String.format("%.2f", currentStudent.getCgpa()), Color.web("#009688")), 2, 1);
                        
                        statisticsContainer.getChildren().add(statsGrid);
                        
                        // Add upcoming deadlines section
                        if (!upcoming.isEmpty()) {
                            addUpcomingDeadlines(upcoming);
                        }
                        
                        // Add performance insights
                        addPerformanceInsights(averageGrade);
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        new Thread(statsTask).start();
    }

    private VBox createStatCard(String title, String value, Color color) {
        VBox card = new VBox(10);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        card.setStyle(String.format("-fx-border-color: %s; -fx-border-width: 0 0 3 0;", 
                color.toString().replace("0x", "#")));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLabel.setTextFill(color);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        titleLabel.setTextFill(Color.GRAY);
        
        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    private void addUpcomingDeadlines(List<Assignment> upcoming) {
        VBox deadlinesCard = new VBox(10);
        deadlinesCard.getStyleClass().add("section-card");
        deadlinesCard.setPadding(new Insets(15));
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Upcoming Deadlines");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.getStyleClass().add("section-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer);
        
        VBox deadlinesList = new VBox(8);
        deadlinesList.setPadding(new Insets(10, 0, 0, 0));
        
        for (Assignment assignment : upcoming) {
            HBox deadlineItem = new HBox(10);
            deadlineItem.setAlignment(Pos.CENTER_LEFT);
            deadlineItem.getStyleClass().add("deadline-item");
            
            VBox assignmentInfo = new VBox(2);
            Label name = new Label(assignment.getTitle());
            name.setFont(Font.font("System", FontWeight.BOLD, 12));
            name.setWrapText(true);
            
            Label courseLabel = new Label(getCourseName(assignment.getCourseId()));
            courseLabel.setFont(Font.font("System", 10));
            courseLabel.setTextFill(Color.GRAY);
            
            assignmentInfo.getChildren().addAll(name, courseLabel);
            
            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            
            Label dueDate = new Label(assignment.getDueDate().format(SHORT_DATE_FORMATTER));
            dueDate.setFont(Font.font("System", FontWeight.BOLD, 11));
            
            // Color code based on urgency
            long daysUntilDue = java.time.Duration.between(LocalDateTime.now(), assignment.getDueDate()).toDays();
            if (daysUntilDue <= 1) {
                dueDate.setTextFill(Color.RED);
            } else if (daysUntilDue <= 3) {
                dueDate.setTextFill(Color.ORANGE);
            } else {
                dueDate.setTextFill(Color.GREEN);
            }
            
            deadlineItem.getChildren().addAll(assignmentInfo, spacer2, dueDate);
            deadlinesList.getChildren().add(deadlineItem);
        }
        
        deadlinesCard.getChildren().addAll(header, deadlinesList);
        statisticsContainer.getChildren().add(deadlinesCard);
    }

    private void addPerformanceInsights(double averageGrade) {
        VBox insightsCard = new VBox(10);
        insightsCard.getStyleClass().add("section-card");
        insightsCard.setPadding(new Insets(15));
        
        Label title = new Label("Performance Insights");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.getStyleClass().add("section-title");
        
        VBox insightsList = new VBox(10);
        insightsList.setPadding(new Insets(10, 0, 0, 0));
        
        // Add insights based on performance
        if (averageGrade >= 90) {
            insightsList.getChildren().add(createInsightItem("Excellent work! You're performing in the top tier.", Color.GREEN));
            insightsList.getChildren().add(createInsightItem("Consider taking on leadership roles in group projects.", Color.BLUE));
        } else if (averageGrade >= 80) {
            insightsList.getChildren().add(createInsightItem("Good performance. You're on track for success.", Color.GREEN));
            insightsList.getChildren().add(createInsightItem("Focus on areas where you lost marks to reach 90%+.", Color.ORANGE));
        } else if (averageGrade >= 70) {
            insightsList.getChildren().add(createInsightItem("Satisfactory performance. Room for improvement.", Color.ORANGE));
            insightsList.getChildren().add(createInsightItem("Consider attending office hours for challenging topics.", Color.BLUE));
        } else {
            insightsList.getChildren().add(createInsightItem("Needs improvement. Consider seeking academic support.", Color.RED));
            insightsList.getChildren().add(createInsightItem("Meet with your advisor to discuss study strategies.", Color.BLUE));
        }
        
        // Check for overdue assignments
        long overdueCount = assignments.stream()
                .filter(a -> a.isOverdue() && a.canSubmit(currentStudent.getId()))
                .count();
        
        if (overdueCount > 0) {
            insightsList.getChildren().add(createInsightItem(
                    String.format("You have %d overdue assignment(s). Submit them ASAP!", overdueCount), 
                    Color.RED));
        }
        
        // Check upcoming deadlines
        long upcomingCount = assignments.stream()
                .filter(a -> a != null && a.getDueDate() != null && 
                       !a.isOverdue() && a.canSubmit(currentStudent.getId()))
                .count();
        
        if (upcomingCount > 0) {
            insightsList.getChildren().add(createInsightItem(
                    String.format("You have %d upcoming assignment(s). Plan your time wisely.", upcomingCount), 
                    Color.BLUE));
        }
        
        insightsCard.getChildren().addAll(title, insightsList);
        statisticsContainer.getChildren().add(insightsCard);
    }

    private HBox createInsightItem(String text, Color color) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        
        Circle bullet = new Circle(4);
        bullet.setFill(color);
        
        TextFlow textFlow = new TextFlow();
        Text insightText = new Text(text);
        insightText.setFont(Font.font("System", 12));
        textFlow.getChildren().add(insightText);
        
        item.getChildren().addAll(bullet, textFlow);
        return item;
    }

    private void updateStatistics() {
        try {
            // Update CGPA based on published grades
            double average = grades.stream()
                    .filter(Grade::isPublished)
                    .mapToDouble(Grade::getPercentage)
                    .average()
                    .orElse(0.0);
            
            // Convert percentage to 4.0 scale (assuming 100% = 4.0)
            double newCgpa = (average / 100.0) * 4.0;
            currentStudent.setCgpa(newCgpa);
            cgpaLabel.setText(String.format("CGPA: %.2f", currentStudent.getCgpa()));
            
            // Update credits completed based on completed courses
            int completedCredits = enrolledCourses.stream()
                    .filter(c -> {
                        // Check if student has completed this course
                        List<Grade> courseGrades = grades.stream()
                                .filter(g -> g.getCourseId().equals(c.getId()))
                                .filter(Grade::isPublished)
                                .collect(Collectors.toList());
                        
                        // If student has grades for this course, consider it completed
                        return !courseGrades.isEmpty();
                    })
                    .mapToInt(Course::getCredits)
                    .sum();
            
            currentStudent.setCreditsCompleted(completedCredits);
            creditsCompletedLabel.setText("Credits Completed: " + completedCredits);
            
            // Update student's completed course list
            List<String> completedCourseIds = enrolledCourses.stream()
                    .filter(c -> {
                        double courseGrade = grades.stream()
                                .filter(g -> g.getCourseId().equals(c.getId()) && g.getStudentId().equals(currentStudent.getId()))
                                .filter(Grade::isPublished)
                                .mapToDouble(Grade::getPercentage)
                                .average()
                                .orElse(0.0);
                        return courseGrade >= 60.0; // Pass if average is 60% or higher
                    })
                    .map(Course::getId)
                    .collect(Collectors.toList());
            
            currentStudent.setCompletedCourseIds(completedCourseIds);
            
            // Save updated student data
            userService.updateUser(currentStudent);
            
        } catch (Exception e) {
            // Log error but don't show alert for minor update failures
            System.err.println("Failed to update statistics: " + e.getMessage());
        }
    }

    @FXML
    private void handleEnrollInCourse() {
        try {
            CourseEnrollmentDialog dialog = new CourseEnrollmentDialog(currentStudent);
            Optional<Course> result = dialog.showAndWait();
            result.ifPresent(course -> {
                // Check if student is already enrolled
                boolean alreadyEnrolled = enrolledCourses.stream()
                        .anyMatch(c -> c.getId().equals(course.getId()));
                
                if (alreadyEnrolled) {
                    AlertHelper.showWarning("Already Enrolled", 
                            "You are already enrolled in " + course.getTitle());
                    return;
                }
                
                // Check course prerequisites
                if (!checkPrerequisites(course)) {
                    AlertHelper.showError("Prerequisites Not Met", 
                            "You do not meet the prerequisites for " + course.getTitle());
                    return;
                }
                
                // Check if course has available seats
                if (!course.canEnroll()) {
                    AlertHelper.showError("Course Full", 
                            course.getTitle() + " is already full. Maximum capacity: " + course.getMaxStudents());
                    return;
                }
                
                // Enroll student using EnrollmentService
                boolean success = enrollmentService.enrollStudentInCourse(
                        currentStudent.getId(), 
                        course.getId(), 
                        LocalDateTime.now()
                );
                
                if (success) {
                    AlertHelper.showSuccess("Enrollment Successful",
                            "Successfully enrolled in " + course.getTitle());
                    loadStudentData(); // Reload data to show new enrollment
                } else {
                    AlertHelper.showError("Enrollment Failed",
                            "Could not enroll in " + course.getTitle() + ". Please try again.");
                }
            });
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to open enrollment dialog: " + e.getMessage());
        }
    }

    private boolean checkPrerequisites(Course course) {
        if (course.getPrerequisites() == null || course.getPrerequisites().isEmpty()) {
            return true; // No prerequisites
        }
        
        // Get student's completed courses
        List<String> completedCourseIds = currentStudent.getCompletedCourseIds();
        if (completedCourseIds == null) {
            return false;
        }
        
        // Check if all prerequisites are in completed courses
        return completedCourseIds.containsAll(course.getPrerequisites());
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
        mainTabPane.getSelectionModel().select(dashboardTab);
    }

    @FXML
    private void handleGenerateTranscript() {
        showLoading(true);
        statusLabel.setText("Generating transcript...");
        
        Task<Void> transcriptTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Calculate GPA
                    double totalGradePoints = 0;
                    int totalCredits = 0;
                    
                    for (Course course : enrolledCourses) {
                        double courseGrade = grades.stream()
                                .filter(g -> g.getCourseId().equals(course.getId()) && 
                                           g.getStudentId().equals(currentStudent.getId()))
                                .filter(Grade::isPublished)
                                .mapToDouble(Grade::getPercentage)
                                .average()
                                .orElse(0.0);
                        
                        if (courseGrade > 0) {
                            double gradePoint = convertPercentageToGradePoint(courseGrade);
                            totalGradePoints += gradePoint * course.getCredits();
                            totalCredits += course.getCredits();
                        }
                    }
                    
                    double gpa = totalCredits > 0 ? totalGradePoints / totalCredits : 0.0;
                    
                    final double finalGpa = gpa;
                    final int finalTotalCredits = totalCredits;
                    
                    Platform.runLater(() -> {
                        showLoading(false);
                        
                        // Create transcript dialog
                        Dialog<Void> transcriptDialog = new Dialog<>();
                        transcriptDialog.setTitle("Academic Transcript");
                        transcriptDialog.setHeaderText("Official Transcript for " + currentStudent.getFullName());
                        
                        ScrollPane scrollPane = new ScrollPane();
                        scrollPane.setFitToWidth(true);
                        
                        VBox content = new VBox(15);
                        content.setPadding(new Insets(20));
                        
                        // Student info
                        GridPane infoGrid = new GridPane();
                        infoGrid.setHgap(10);
                        infoGrid.setVgap(5);
                        infoGrid.setPadding(new Insets(0, 0, 15, 0));
                        
                        infoGrid.add(new Label("Student ID:"), 0, 0);
                        infoGrid.add(new Label(currentStudent.getStudentId()), 1, 0);
                        infoGrid.add(new Label("Name:"), 0, 1);
                        infoGrid.add(new Label(currentStudent.getFullName()), 1, 1);
                        infoGrid.add(new Label("Department:"), 0, 2);
                        infoGrid.add(new Label(currentStudent.getDepartment()), 1, 2);
                        infoGrid.add(new Label("CGPA:"), 0, 3);
                        infoGrid.add(new Label(String.format("%.2f", finalGpa)), 1, 3);
                        infoGrid.add(new Label("Total Credits:"), 0, 4);
                        infoGrid.add(new Label(String.valueOf(finalTotalCredits)), 1, 4);
                        infoGrid.add(new Label("Date Generated:"), 0, 5);
                        infoGrid.add(new Label(LocalDate.now().toString()), 1, 5);
                        
                        content.getChildren().add(infoGrid);
                        
                        // Course list
                        if (!enrolledCourses.isEmpty()) {
                            Label coursesLabel = new Label("Course History:");
                            coursesLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                            content.getChildren().add(coursesLabel);
                            
                            GridPane coursesGrid = new GridPane();
                            coursesGrid.setHgap(15);
                            coursesGrid.setVgap(5);
                            coursesGrid.setPadding(new Insets(10));
                            coursesGrid.getStyleClass().add("transcript-grid");
                            
                            // Headers
                            coursesGrid.add(new Label("Course Code"), 0, 0);
                            coursesGrid.add(new Label("Course Title"), 1, 0);
                            coursesGrid.add(new Label("Credits"), 2, 0);
                            coursesGrid.add(new Label("Grade"), 3, 0);
                            coursesGrid.add(new Label("Grade Points"), 4, 0);
                            
                            int row = 1;
                            for (Course course : enrolledCourses) {
                                double courseGrade = grades.stream()
                                        .filter(g -> g.getCourseId().equals(course.getId()) && 
                                                   g.getStudentId().equals(currentStudent.getId()))
                                        .filter(Grade::isPublished)
                                        .mapToDouble(Grade::getPercentage)
                                        .average()
                                        .orElse(0.0);
                                
                                coursesGrid.add(new Label(course.getCourseCode()), 0, row);
                                coursesGrid.add(new Label(course.getTitle()), 1, row);
                                coursesGrid.add(new Label(String.valueOf(course.getCredits())), 2, row);
                                
                                if (courseGrade > 0) {
                                    String letterGrade = convertPercentageToLetterGrade(courseGrade);
                                    double gradePoints = convertPercentageToGradePoint(courseGrade);
                                    
                                    coursesGrid.add(new Label(letterGrade), 3, row);
                                    coursesGrid.add(new Label(String.format("%.2f", gradePoints)), 4, row);
                                } else {
                                    coursesGrid.add(new Label("N/A"), 3, row);
                                    coursesGrid.add(new Label("0.00"), 4, row);
                                }
                                
                                row++;
                            }
                            
                            content.getChildren().add(coursesGrid);
                        }
                        
                        scrollPane.setContent(content);
                        transcriptDialog.getDialogPane().setContent(scrollPane);
                        transcriptDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
                        
                        transcriptDialog.showAndWait();
                        statusLabel.setText("Transcript generated");
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showLoading(false);
                        AlertHelper.showError("Transcript Error", "Failed to generate transcript: " + e.getMessage());
                        statusLabel.setText("Transcript failed");
                    });
                }
                return null;
            }
        };
        
        new Thread(transcriptTask).start();
    }

    private String convertPercentageToLetterGrade(double percentage) {
        if (percentage >= 90) return "A";
        else if (percentage >= 80) return "B";
        else if (percentage >= 70) return "C";
        else if (percentage >= 60) return "D";
        else return "F";
    }
    
    private double convertPercentageToGradePoint(double percentage) {
        if (percentage >= 90) return 4.0;
        else if (percentage >= 80) return 3.0;
        else if (percentage >= 70) return 2.0;
        else if (percentage >= 60) return 1.0;
        else return 0.0;
    }

    @FXML
    private void handleViewSchedule() {
        showLoading(true);
        statusLabel.setText("Loading schedule...");
        
        Task<Void> scheduleTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Get all enrolled courses with schedule information
                    List<Course> scheduledCourses = enrolledCourses.stream()
                            .filter(c -> c.getSchedule() != null && !c.getSchedule().isEmpty())
                            .collect(Collectors.toList());
                    
                    Platform.runLater(() -> {
                        showLoading(false);
                        
                        Dialog<Void> scheduleDialog = new Dialog<>();
                        scheduleDialog.setTitle("Class Schedule");
                        scheduleDialog.setHeaderText("Weekly Schedule for " + currentStudent.getFullName());
                        
                        VBox content = new VBox(15);
                        content.setPadding(new Insets(20));
                        
                        if (scheduledCourses.isEmpty()) {
                            content.getChildren().add(new Label("No scheduled classes found."));
                        } else {
                            // Group courses by day
                            for (Course course : scheduledCourses) {
                                HBox courseSchedule = new HBox(10);
                                courseSchedule.setAlignment(Pos.CENTER_LEFT);
                                
                                VBox courseInfo = new VBox(5);
                                Label courseTitle = new Label(course.getTitle() + " (" + course.getCourseCode() + ")");
                                courseTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
                                
                                Label scheduleInfo = new Label("Schedule: " + course.getSchedule());
                                Label location = new Label("Room: " + (course.getClassroom() != null ? course.getClassroom() : "TBA"));
                                
                                courseInfo.getChildren().addAll(courseTitle, scheduleInfo, location);
                                courseSchedule.getChildren().add(courseInfo);
                                content.getChildren().add(courseSchedule);
                                content.getChildren().add(new Separator());
                            }
                        }
                        
                        scheduleDialog.getDialogPane().setContent(content);
                        scheduleDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
                        scheduleDialog.showAndWait();
                        statusLabel.setText("Schedule loaded");
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showLoading(false);
                        AlertHelper.showError("Schedule Error", "Failed to load schedule: " + e.getMessage());
                        statusLabel.setText("Schedule failed");
                    });
                }
                return null;
            }
        };
        
        new Thread(scheduleTask).start();
    }

    private void viewCourseDetails(Course course) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Course Details");
        dialog.setHeaderText(course.getTitle() + " (" + course.getCourseCode() + ")");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        // Basic information
        grid.add(new Label("Course Code:"), 0, 0);
        grid.add(new Label(course.getCourseCode()), 1, 0);
        
        grid.add(new Label("Title:"), 0, 1);
        grid.add(new Label(course.getTitle()), 1, 1);
        
        grid.add(new Label("Credits:"), 0, 2);
        grid.add(new Label(String.valueOf(course.getCredits())), 1, 2);
        
        grid.add(new Label("Department:"), 0, 3);
        grid.add(new Label(course.getDepartment()), 1, 3);
        
        grid.add(new Label("Semester:"), 0, 4);
        grid.add(new Label(course.getSemester()), 1, 4);
        
        grid.add(new Label("Schedule:"), 0, 5);
        grid.add(new Label(course.getSchedule() != null ? course.getSchedule() : "Not scheduled"), 1, 5);
        
        grid.add(new Label("Classroom:"), 0, 6);
        grid.add(new Label(course.getClassroom() != null ? course.getClassroom() : "TBA"), 1, 6);
        
        grid.add(new Label("Enrollment:"), 0, 7);
        grid.add(new Label(course.getCurrentEnrollment() + "/" + course.getMaxStudents()), 1, 7);
        
        grid.add(new Label("Status:"), 0, 8);
        grid.add(new Label(course.isActive() ? "Active" : "Inactive"), 1, 8);
        
        content.getChildren().add(grid);
        
        // Description
        if (course.getDescription() != null && !course.getDescription().isEmpty()) {
            Separator sep = new Separator();
            content.getChildren().add(sep);
            
            VBox descBox = new VBox(5);
            Label descLabel = new Label("Description:");
            descLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            
            TextArea descText = new TextArea(course.getDescription());
            descText.setEditable(false);
            descText.setWrapText(true);
            descText.setPrefHeight(100);
            
            descBox.getChildren().addAll(descLabel, descText);
            content.getChildren().add(descBox);
        }
        
        // Learning outcomes
        if (course.getLearningOutcomes() != null && !course.getLearningOutcomes().isEmpty()) {
            Separator sep = new Separator();
            content.getChildren().add(sep);
            
            VBox outcomesBox = new VBox(5);
            Label outcomesLabel = new Label("Learning Outcomes:");
            outcomesLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            
            VBox outcomesList = new VBox(3);
            for (String outcome : course.getLearningOutcomes()) {
                HBox outcomeItem = new HBox(5);
                Label bullet = new Label("•");
                Label outcomeText = new Label(outcome);
                outcomeText.setWrapText(true);
                outcomeItem.getChildren().addAll(bullet, outcomeText);
                outcomesList.getChildren().add(outcomeItem);
            }
            
            outcomesBox.getChildren().addAll(outcomesLabel, outcomesList);
            content.getChildren().add(outcomesBox);
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        
        // Add extra buttons for enrolled courses
        if (enrolledCourses.contains(course)) {
            ButtonType dropButton = new ButtonType("Drop Course", ButtonBar.ButtonData.OTHER);
            dialog.getDialogPane().getButtonTypes().add(dropButton);
            
            dialog.setResultConverter(buttonType -> {
                if (buttonType == dropButton) {
                    dropCourse(course);
                }
                return null;
            });
        }
        
        dialog.showAndWait();
    }

    private void dropCourse(Course course) {
        boolean confirm = AlertHelper.showConfirmation("Drop Course", 
                "Are you sure you want to drop " + course.getTitle() + "?");
        
        if (confirm) {
            try {
                // Drop course using EnrollmentService
                boolean success = enrollmentService.dropCourse(currentStudent.getId(), course.getId());
                
                if (success) {
                    AlertHelper.showSuccess("Course Dropped", 
                            "Successfully dropped " + course.getTitle());
                    loadStudentData();
                } else {
                    AlertHelper.showError("Drop Failed", 
                            "Failed to drop course. You may not be enrolled or the course has already ended.");
                }
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to drop course: " + e.getMessage());
            }
        }
    }

    private void submitAssignment(Assignment assignment) {
        // Check if assignment can be submitted
        if (!assignment.canSubmit(currentStudent.getId())) {
            if (assignment.isOverdue()) {
                AlertHelper.showError("Submission Closed", 
                        "This assignment is overdue and can no longer be submitted.");
            } else if (assignment.getStatus() != AssignmentStatus.ACTIVE) {
                AlertHelper.showError("Submission Closed", 
                        "This assignment is not currently accepting submissions.");
            } else {
                AlertHelper.showWarning("Already Submitted", 
                        "You have already submitted this assignment.");
            }
            return;
        }
        
        // Create submission dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Submit Assignment");
        dialog.setHeaderText("Submit: " + assignment.getTitle());
        
        // Set the button types
        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);
        
        // Create the content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Enter your submission content here...");
        contentArea.setPrefRowCount(10);
        contentArea.setPrefColumnCount(50);
        
        grid.add(new Label("Submission:"), 0, 0);
        grid.add(contentArea, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the text area
        Platform.runLater(() -> contentArea.requestFocus());
        
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                return contentArea.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(submissionContent -> {
            showLoading(true);
            statusLabel.setText("Submitting assignment...");
            
            Task<Boolean> submitTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    try {
                        // Submit assignment using AssignmentService
                        boolean success = assignmentService.submitAssignment(
                                assignment.getId(), 
                                currentStudent.getId(),
                                0, // Score will be set when graded
                                submissionContent
                        );
                        return success;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            };
            
            submitTask.setOnSucceeded(event -> {
                showLoading(false);
                boolean success = submitTask.getValue();
                
                if (success) {
                    AlertHelper.showSuccess("Submission Successful",
                            "Assignment submitted successfully! Awaiting grading.");
                    loadStudentData();
                    statusLabel.setText("Assignment submitted");
                } else {
                    AlertHelper.showError("Submission Failed",
                            "Could not submit assignment. Please try again.");
                    statusLabel.setText("Submission failed");
                }
            });
            
            submitTask.setOnFailed(event -> {
                showLoading(false);
                AlertHelper.showError("Submission Error",
                        "An error occurred while submitting. Please try again.");
                statusLabel.setText("Submission error");
            });
            
            new Thread(submitTask).start();
        });
    }

    private void viewGradeDetails(Grade grade) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Grade Details");
        dialog.setHeaderText("Assignment Grade");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // Get course name
        String courseName = getCourseName(grade.getCourseId());
        String assignmentName = getAssignmentName(grade.getAssignmentId());
        
        grid.add(new Label("Course:"), 0, 0);
        grid.add(new Label(courseName), 1, 0);
        
        grid.add(new Label("Assignment:"), 0, 1);
        grid.add(new Label(assignmentName), 1, 1);
        
        grid.add(new Label("Score:"), 0, 2);
        grid.add(new Label(String.format("%.1f / %.1f", grade.getScore(), grade.getMaxScore())), 1, 2);
        
        grid.add(new Label("Percentage:"), 0, 3);
        grid.add(new Label(String.format("%.1f%%", grade.getPercentage())), 1, 3);
        
        grid.add(new Label("Letter Grade:"), 0, 4);
        Label letterGradeLabel = new Label(grade.getLetterGrade());
        letterGradeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        grid.add(letterGradeLabel, 1, 4);
        
        grid.add(new Label("Graded By:"), 0, 5);
        grid.add(new Label(grade.getGradedBy()), 1, 5);
        
        grid.add(new Label("Graded On:"), 0, 6);
        grid.add(new Label(grade.getGradedAt() != null ? 
                grade.getGradedAt().format(DATE_FORMATTER) : "Not graded"), 1, 6);
        
        grid.add(new Label("Status:"), 0, 7);
        grid.add(new Label(grade.isPublished() ? "Published" : "Draft"), 1, 7);
        
        // Feedback
        if (grade.getFeedback() != null && !grade.getFeedback().isEmpty()) {
            grid.add(new Label("Feedback:"), 0, 8);
            
            TextArea feedbackArea = new TextArea(grade.getFeedback());
            feedbackArea.setEditable(false);
            feedbackArea.setWrapText(true);
            feedbackArea.setPrefHeight(80);
            feedbackArea.setPrefWidth(300);
            
            grid.add(feedbackArea, 1, 8);
        }
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private String getCourseName(String courseId) {
        if (courseId == null) return "Unknown Course";
        return enrolledCourses.stream()
                .filter(c -> courseId.equals(c.getId()))
                .map(Course::getTitle)
                .findFirst()
                .orElseGet(() -> {
                    try {
                        Optional<Course> course = courseService.getCourseById(courseId);
                        return course.map(Course::getTitle).orElse("Unknown Course");
                    } catch (Exception e) {
                        return "Unknown Course";
                    }
                });
    }

    private String getAssignmentName(String assignmentId) {
        if (assignmentId == null) return "Unknown Assignment";
        return assignments.stream()
                .filter(a -> assignmentId.equals(a.getId()))
                .map(Assignment::getTitle)
                .findFirst()
                .orElse("Unknown Assignment");
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisible(show);
        loadingIndicator.setVisible(show);
        mainTabPane.setDisable(show);
    }

    // Override parent methods
    @FXML
    @Override
    protected void handleProfile() {
        // Create profile dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Student Profile");
        dialog.setHeaderText("Profile Information");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        grid.add(new Label("Student ID:"), 0, 0);
        grid.add(new Label(currentStudent.getStudentId()), 1, 0);
        
        grid.add(new Label("Name:"), 0, 1);
        grid.add(new Label(currentStudent.getFullName()), 1, 1);
        
        grid.add(new Label("Email:"), 0, 2);
        grid.add(new Label(currentStudent.getEmail()), 1, 2);
        
        grid.add(new Label("Department:"), 0, 3);
        grid.add(new Label(currentStudent.getDepartment()), 1, 3);
        
        grid.add(new Label("Semester:"), 0, 4);
        grid.add(new Label(Integer.toString(currentStudent.getSemester())), 1, 4);
        
        grid.add(new Label("CGPA:"), 0, 5);
        grid.add(new Label(String.format("%.2f", currentStudent.getCgpa())), 1, 5);
        
        grid.add(new Label("Credits Completed:"), 0, 6);
        grid.add(new Label(String.valueOf(currentStudent.getCreditsCompleted())), 1, 6);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    @FXML
    @Override
    protected void handleSettings() {
        // Create settings dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Settings");
        dialog.setHeaderText("Student Settings");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setOnAction(e -> changePassword());
        
        content.getChildren().addAll(
                new Label("Account Settings:"),
                changePasswordBtn
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    private void changePassword() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your new password");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Current password");
        
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New password");
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        
        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(oldPasswordField, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        ButtonType changeButtonType = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                    AlertHelper.showError("Password Mismatch", "New passwords do not match!");
                    return null;
                }
                return oldPasswordField.getText() + "|" + newPasswordField.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(passwords -> {
            String[] parts = passwords.split("\\|");
            if (parts.length == 2) {
                showLoading(true);
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        // Use AuthService to change password
                        com.arms.service.AuthService authService = com.arms.service.AuthService.getInstance();
                        return authService.changePassword(parts[0], parts[1]);
                    }
                };
                
                task.setOnSucceeded(e -> {
                    showLoading(false);
                    if (task.getValue()) {
                        AlertHelper.showSuccess("Password Changed", "Your password has been updated successfully.");
                    } else {
                        AlertHelper.showError("Password Change Failed", "Current password is incorrect.");
                    }
                });
                
                task.setOnFailed(e -> {
                    showLoading(false);
                    AlertHelper.showError("Error", "Failed to change password: " + e.getSource().getException().getMessage());
                });
                
                new Thread(task).start();
            }
        });
    }

    @FXML
    @Override
    protected void handleHelp() {
        AlertHelper.showInfo("Help", 
                "Student Dashboard Help:\n\n" +
                "1. Dashboard: View your statistics and upcoming deadlines\n" +
                "2. My Courses: View and manage your enrolled courses\n" +
                "3. Assignments: View and submit your assignments\n" +
                "4. Grades: View your grades and performance\n" +
                "5. Transcript: Generate your academic transcript\n" +
                "6. Schedule: View your class schedule\n\n" +
                "Double-click on courses to view details\n" +
                "Double-click on assignments to submit them\n" +
                "Double-click on grades to view details\n\n" +
                "Enroll in new courses using the 'Enroll in Course' button\n" +
                "Drop courses from course details dialog\n" +
                "Refresh data using the refresh button or Dashboard menu");
    }
}
