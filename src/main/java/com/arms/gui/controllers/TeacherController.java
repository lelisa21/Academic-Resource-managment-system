package com.arms.gui.controllers;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.controlsfx.control.GridView;
import org.controlsfx.control.Notifications;

import com.arms.domain.Assignment;
import com.arms.domain.Course;
import com.arms.domain.Grade;
import com.arms.domain.Student;
import com.arms.domain.Teacher;
import com.arms.domain.enums.AssignmentStatus;
import com.arms.gui.components.AssignmentCard;
import com.arms.gui.components.CourseCard;
import com.arms.gui.dialogs.AddAssignmentDialog;
import com.arms.gui.dialogs.AddCourseDialog;
import com.arms.gui.dialogs.GradeSubmissionDialog;
import com.arms.gui.util.AlertHelper;
import com.arms.gui.util.NavigationHelper;
import com.arms.service.AssignmentService;
import com.arms.service.CourseService;
import com.arms.service.GradeService;

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
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

public class TeacherController extends DashboardController {

    @FXML private Label employeeIdLabel;
    @FXML private Label departmentLabel;
    @FXML private Label qualificationLabel;
    @FXML private Label experienceLabel;

    @FXML private GridView<Course> dashboardCoursesGrid;
    @FXML private GridView<Assignment> dashboardAssignmentsGrid;
    @FXML private ListView<Student> dashboardStudentsList;
    
    @FXML private GridView<Course> coursesTabGrid;
    @FXML private GridView<Assignment> assignmentsTabGrid;
    @FXML private ListView<Student> studentsTabList;

    @FXML private TabPane mainTabPane;
    @FXML private VBox statisticsContainer;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private javafx.scene.layout.StackPane loadingOverlay;

    private final CourseService courseService = CourseService.getInstance();
    private final AssignmentService assignmentService = AssignmentService.getInstance();
    private final GradeService gradeService = GradeService.getInstance();

    private Teacher currentTeacher;
    private Course selectedCourse;

    private final ObservableList<Course> courses = FXCollections.observableArrayList();
    private final ObservableList<Assignment> assignments = FXCollections.observableArrayList();
    private final ObservableList<Student> students = FXCollections.observableArrayList();
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter SHORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        if (currentUser instanceof Teacher teacher) {
            currentTeacher = teacher;
            initializeTeacherUI();
            setupEventHandlers();
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

        // Initialize GridViews with custom cell factories
        if (dashboardCoursesGrid != null) {
            dashboardCoursesGrid.setCellFactory(grid -> {
                CourseCard card = new CourseCard();
                card.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        viewCourseDetails(card.getItem());
                    }
                });
                return card;
            });
            dashboardCoursesGrid.setItems(courses);
        }
        
        if (dashboardAssignmentsGrid != null) {
            dashboardAssignmentsGrid.setCellFactory(grid -> {
                AssignmentCard card = new AssignmentCard();
                card.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        viewAssignmentDetails(card.getItem());
                    }
                });
                return card;
            });
            dashboardAssignmentsGrid.setItems(assignments);
        }
        
        if (dashboardStudentsList != null) {
            dashboardStudentsList.setCellFactory(param -> new StudentListCell());
            dashboardStudentsList.setItems(students);
        }
        
        // Initialize tab-specific grids
        if (coursesTabGrid != null) {
            coursesTabGrid.setCellFactory(grid -> {
                CourseCard card = new CourseCard();
                card.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        viewCourseDetails(card.getItem());
                    }
                });
                return card;
            });
            coursesTabGrid.setItems(courses);
        }
        
        if (assignmentsTabGrid != null) {
            assignmentsTabGrid.setCellFactory(grid -> {
                AssignmentCard card = new AssignmentCard();
                card.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        viewAssignmentDetails(card.getItem());
                    }
                });
                return card;
            });
            assignmentsTabGrid.setItems(assignments);
        }
        
        if (studentsTabList != null) {
            studentsTabList.setCellFactory(param -> new StudentListCell());
            studentsTabList.setItems(students);
        }
        
        // Setup loading overlay
        loadingOverlay.managedProperty().bind(loadingOverlay.visibleProperty());
    }

    private void setupEventHandlers() {
        // Additional event handlers can be added here
    }

    private void loadTeacherData() {
        showLoading(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    List<Course> teacherCourses = courseService.getCoursesByTeacher(currentTeacher.getId());
                    List<Assignment> teacherAssignments = assignmentService.getAssignmentsByTeacher(currentTeacher.getId());
                    
                    // Get unique students from all courses
                    List<Student> allStudents = new ArrayList<>();
                    for (Course course : teacherCourses) {
                        allStudents.addAll(courseService.getEnrolledStudents(course.getId()));
                    }
                    // Remove duplicates
                    List<Student> uniqueStudents = allStudents.stream()
                            .distinct()
                            .collect(Collectors.toList());

                    Platform.runLater(() -> {
                        courses.setAll(teacherCourses);
                        assignments.setAll(teacherAssignments);
                        students.setAll(uniqueStudents);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            showLoading(false);
            loadStatistics();
            Notifications.create()
                    .title("Data Loaded")
                    .text("Teacher data loaded successfully")
                    .showInformation();
        });

        task.setOnFailed(e -> {
            showLoading(false);
            AlertHelper.showError("Load Error", "Failed to load teacher data: " + e.getSource().getException().getMessage());
        });

        new Thread(task).start();
    }

    private void loadStatistics() {
        if (statisticsContainer == null) return;
        
        Task<Void> statsTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    int totalCourses = courses.size();
                    int totalAssignments = assignments.size();
                    int totalStudents = students.size();
                    
                    // Calculate upcoming deadlines
                    long upcomingDeadlines = assignments.stream()
                            .filter(a -> a.getDueDate() != null && 
                                   a.getDueDate().isAfter(LocalDateTime.now()) &&
                                   a.getStatus() == AssignmentStatus.ACTIVE)
                            .count();
                    
                    // Calculate pending submissions
                    final long[] pendingSubmissions = {0};
                    for (Assignment assignment : assignments) {
                        if (assignment.getStatus() == AssignmentStatus.ACTIVE) {
                            Map<String, Integer> stats = assignmentService.getSubmissionStats(assignment.getId());
                            int submitted = stats.get("submitted");
                            int total = stats.get("total");
                            pendingSubmissions[0] += (total - submitted);
                        }
                    }
                    
                    // Calculate average grades for assignments
                    double avgGrade = assignments.stream()
                            .flatMap(a -> gradeService.getGradesByAssignment(a.getId()).stream())
                            .filter(Grade::isPublished)
                            .mapToDouble(Grade::getPercentage)
                            .average()
                            .orElse(0.0);
                    
                    Platform.runLater(() -> {
                        statisticsContainer.getChildren().clear();
                        
                        GridPane statsGrid = new GridPane();
                        statsGrid.setHgap(20);
                        statsGrid.setVgap(15);
                        statsGrid.setPadding(new Insets(15));
                        
                        statsGrid.add(createStatCard("Courses Teaching", 
                                String.valueOf(totalCourses), Color.web("#4CAF50")), 0, 0);
                        statsGrid.add(createStatCard("Assignments", 
                                String.valueOf(totalAssignments), Color.web("#2196F3")), 1, 0);
                        statsGrid.add(createStatCard("Total Students", 
                                String.valueOf(totalStudents), Color.web("#FF9800")), 2, 0);
                        
                        statsGrid.add(createStatCard("Upcoming Deadlines", 
                                String.valueOf(upcomingDeadlines), Color.web("#9C27B0")), 0, 1);
                        statsGrid.add(createStatCard("Pending Submissions", 
                                String.valueOf(pendingSubmissions[0]), Color.web("#F44336")), 1, 1);
                        statsGrid.add(createStatCard("Avg Student Grade", 
                                String.format("%.1f%%", avgGrade), Color.web("#009688")), 2, 1);
                        
                        statisticsContainer.getChildren().add(statsGrid);
                        
                        // Add performance insights
                        addPerformanceInsights(avgGrade);
                        
                        // Add upcoming deadlines list
                        addUpcomingDeadlines();
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

    private void addPerformanceInsights(double avgGrade) {
        VBox insightsCard = new VBox(10);
        insightsCard.getStyleClass().add("section-card");
        insightsCard.setPadding(new Insets(15));
        
        Label title = new Label("Teaching Insights");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.getStyleClass().add("section-title");
        
        VBox insightsList = new VBox(10);
        insightsList.setPadding(new Insets(10, 0, 0, 0));
        
        // Add insights based on average grade
        if (avgGrade >= 80) {
            insightsList.getChildren().add(createInsightItem("Excellent! Students are performing well in your courses.", Color.GREEN));
            insightsList.getChildren().add(createInsightItem("Consider challenging students with advanced topics.", Color.BLUE));
        } else if (avgGrade >= 70) {
            insightsList.getChildren().add(createInsightItem("Good performance overall.", Color.GREEN));
            insightsList.getChildren().add(createInsightItem("Review areas where students are struggling.", Color.ORANGE));
        } else if (avgGrade >= 60) {
            insightsList.getChildren().add(createInsightItem("Satisfactory performance. Consider additional support.", Color.ORANGE));
            insightsList.getChildren().add(createInsightItem("Schedule review sessions for challenging topics.", Color.BLUE));
        } else {
            insightsList.getChildren().add(createInsightItem("Consider revising teaching methods or materials.", Color.RED));
            insightsList.getChildren().add(createInsightItem("Meet with struggling students individually.", Color.BLUE));
        }
        
        // Check for upcoming deadlines
        long upcomingCount = assignments.stream()
                .filter(a -> a != null && a.getDueDate() != null && 
                       a.getDueDate().isAfter(LocalDateTime.now()) &&
                       a.getStatus() == AssignmentStatus.ACTIVE)
                .count();
        
        if (upcomingCount > 0) {
            insightsList.getChildren().add(createInsightItem(
                    String.format("You have %d upcoming assignment deadline(s).", upcomingCount), 
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

    private void addUpcomingDeadlines() {
        List<Assignment> upcoming = assignments.stream()
                .filter(a -> a != null && a.getDueDate() != null && 
                       a.getDueDate().isAfter(LocalDateTime.now()) &&
                       a.getStatus() == AssignmentStatus.ACTIVE)
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .limit(5)
                .collect(Collectors.toList());
        
        if (upcoming.isEmpty()) return;
        
        VBox deadlinesCard = new VBox(10);
        deadlinesCard.getStyleClass().add("section-card");
        deadlinesCard.setPadding(new Insets(15));
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Upcoming Assignment Deadlines");
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

    @FXML
    public void handleRefreshData() {
        loadTeacherData();
    }

    @FXML
    public void handleCreateAssignment() {
        if (courses.isEmpty()) {
            AlertHelper.showWarning("No Courses", "You must have courses assigned to create assignments.");
            return;
        }

        AddAssignmentDialog dialog = new AddAssignmentDialog(courses);
        Optional<Assignment> result = dialog.showAndWait();

        result.ifPresent(assignment -> {
            showLoading(true);
            Task<Boolean> createTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    try {
                        Optional<Assignment> created = assignmentService.createAssignment(assignment);
                        return created.isPresent();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            };
            
            createTask.setOnSucceeded(e -> {
                showLoading(false);
                boolean success = createTask.getValue();
                if (success) {
                    AlertHelper.showSuccess("Assignment Created", 
                            "Assignment created successfully!");
                    loadTeacherData();
                } else {
                    AlertHelper.showError("Creation Failed", 
                            "Failed to create assignment. Please try again.");
                }
            });
            
            createTask.setOnFailed(e -> {
                showLoading(false);
                AlertHelper.showError("Error", 
                        "An error occurred while creating assignment.");
            });
            
            new Thread(createTask).start();
        });
    }

    @FXML
    public void handleAddCourse() {
        AddCourseDialog dialog = new AddCourseDialog();
        Optional<Course> result = dialog.showAndWait();
        
        result.ifPresent(course -> {
            showLoading(true);
            Task<Boolean> createTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    try {
                        course.setTeacherId(currentTeacher.getId());
                        Optional<Course> created = courseService.createCourse(course);
                        return created.isPresent();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            };
            
            createTask.setOnSucceeded(e -> {
                showLoading(false);
                boolean success = createTask.getValue();
                if (success) {
                    AlertHelper.showSuccess("Course Added", 
                            "Course added successfully!");
                    loadTeacherData();
                } else {
                    AlertHelper.showError("Creation Failed", 
                            "Failed to add course. Please check course details and try again.");
                }
            });
            
            createTask.setOnFailed(e -> {
                showLoading(false);
                AlertHelper.showError("Error", 
                        "An error occurred while adding course.");
            });
            
            new Thread(createTask).start();
        });
    }

    @FXML
    public void handleGradeSubmissions() {
        if (assignments.isEmpty()) {
            AlertHelper.showWarning("No Assignments", "You have no assignments to grade.");
            return;
        }
        
        // Create dialog to select assignment
        Dialog<Assignment> dialog = new Dialog<>();
        dialog.setTitle("Grade Submissions");
        dialog.setHeaderText("Select an assignment to grade");
        
        ButtonType gradeButton = new ButtonType("Grade", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(gradeButton, ButtonType.CANCEL);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label instruction = new Label("Select an assignment to view and grade submissions:");
        ListView<Assignment> assignmentList = new ListView<>();
        assignmentList.setItems(FXCollections.observableArrayList(assignments));
        assignmentList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Assignment item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle() + " - " + getCourseName(item.getCourseId()));
                }
            }
        });
        
        content.getChildren().addAll(instruction, assignmentList);
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == gradeButton) {
                return assignmentList.getSelectionModel().getSelectedItem();
            }
            return null;
        });
        
        Optional<Assignment> result = dialog.showAndWait();
        result.ifPresent(this::gradeAssignmentSubmissions);
    }

   private void gradeAssignmentSubmissions(Assignment assignment) {
    GradeSubmissionDialog dialog = new GradeSubmissionDialog(assignment);
    dialog.showAndWait();
    AlertHelper.showSuccess("Grading Complete", 
            "Student submissions have been graded.");
    // Refresh data
    loadTeacherData();
}

    @FXML
    public void handleViewCourseStudents() {
        if (courses.isEmpty()) {
            AlertHelper.showWarning("No Courses", "You are not assigned to any courses.");
            return;
        }
        
        // Create dialog to select course
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Course Students");
        dialog.setHeaderText("Select a course to view students");
        
        ButtonType viewButton = new ButtonType("View Students", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(viewButton, ButtonType.CANCEL);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label instruction = new Label("Select a course to view enrolled students:");
        ListView<Course> courseList = new ListView<>();
        courseList.setItems(FXCollections.observableArrayList(courses));
        courseList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Course item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCourseCode() + " - " + item.getTitle());
                }
            }
        });
        
        content.getChildren().addAll(instruction, courseList);
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == viewButton) {
                return courseList.getSelectionModel().getSelectedItem();
            }
            return null;
        });
        
        Optional<Course> result = dialog.showAndWait();
        result.ifPresent(this::viewCourseStudents);
    }

    private void viewCourseStudents(Course course) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Course Students");
        dialog.setHeaderText("Students enrolled in " + course.getTitle());
        
        List<Student> enrolledStudents = courseService.getEnrolledStudents(course.getId());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label countLabel = new Label("Total Students: " + enrolledStudents.size());
        countLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        VBox studentsList = new VBox(5);
        for (Student student : enrolledStudents) {
            HBox studentItem = new HBox(10);
            studentItem.setAlignment(Pos.CENTER_LEFT);
            
            VBox studentInfo = new VBox(2);
            Label name = new Label(student.getFullName());
            name.setFont(Font.font("System", FontWeight.BOLD, 12));
            
            Label idLabel = new Label("ID: " + student.getStudentId());
            idLabel.setFont(Font.font("System", 10));
            idLabel.setTextFill(Color.GRAY);
            
            studentInfo.getChildren().addAll(name, idLabel);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            // Get student's grade in this course
            List<Grade> studentGrades = gradeService.getGradesByStudent(student.getId()).stream()
                    .filter(g -> g.getCourseId().equals(course.getId()))
                    .filter(Grade::isPublished)
                    .collect(Collectors.toList());
            
            double averageGrade = studentGrades.stream()
                    .mapToDouble(Grade::getPercentage)
                    .average()
                    .orElse(0.0);
            
            Label gradeLabel = new Label(String.format("%.1f%%", averageGrade));
            gradeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            
            // Color code based on performance
            if (averageGrade >= 80) {
                gradeLabel.setTextFill(Color.GREEN);
            } else if (averageGrade >= 60) {
                gradeLabel.setTextFill(Color.ORANGE);
            } else {
                gradeLabel.setTextFill(Color.RED);
            }
            
            studentItem.getChildren().addAll(studentInfo, spacer, gradeLabel);
            studentsList.getChildren().add(studentItem);
        }
        
        content.getChildren().addAll(countLabel, studentsList);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    @FXML
    public void handleGenerateReport() {
        if (courses.isEmpty()) {
            AlertHelper.showWarning("No Data", "No courses available for report generation.");
            return;
        }
        
        showLoading(true);
        Task<Void> reportTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Thread.sleep(1000); // Simulate report generation
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        
        reportTask.setOnSucceeded(e -> {
            showLoading(false);
            
            // Create report dialog
            Dialog<Void> reportDialog = new Dialog<>();
            reportDialog.setTitle("Teaching Report");
            reportDialog.setHeaderText("Teaching Performance Report");
            
            VBox content = new VBox(15);
            content.setPadding(new Insets(20));
            
            // Summary statistics
            Label summaryLabel = new Label("Summary Statistics:");
            summaryLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            
            GridPane statsGrid = new GridPane();
            statsGrid.setHgap(15);
            statsGrid.setVgap(10);
            
            statsGrid.add(new Label("Total Courses:"), 0, 0);
            statsGrid.add(new Label(String.valueOf(courses.size())), 1, 0);
            
            statsGrid.add(new Label("Total Assignments:"), 0, 1);
            statsGrid.add(new Label(String.valueOf(assignments.size())), 1, 1);
            
            statsGrid.add(new Label("Total Students:"), 0, 2);
            statsGrid.add(new Label(String.valueOf(students.size())), 1, 2);
            
            // Calculate overall average grade
            double overallAvgGrade = assignments.stream()
                    .flatMap(a -> gradeService.getGradesByAssignment(a.getId()).stream())
                    .filter(Grade::isPublished)
                    .mapToDouble(Grade::getPercentage)
                    .average()
                    .orElse(0.0);
            
            statsGrid.add(new Label("Overall Average Grade:"), 0, 3);
            statsGrid.add(new Label(String.format("%.1f%%", overallAvgGrade)), 1, 3);
            
            content.getChildren().addAll(summaryLabel, statsGrid);
            
            // Course-wise breakdown
            if (!courses.isEmpty()) {
                Separator sep = new Separator();
                content.getChildren().add(sep);
                
                Label courseBreakdownLabel = new Label("Course-wise Breakdown:");
                courseBreakdownLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                content.getChildren().add(courseBreakdownLabel);
                
                for (Course course : courses) {
                    HBox courseBox = new HBox(10);
                    courseBox.setAlignment(Pos.CENTER_LEFT);
                    
                    VBox courseInfo = new VBox(2);
                    Label courseTitle = new Label(course.getCourseCode() + " - " + course.getTitle());
                    courseTitle.setFont(Font.font("System", FontWeight.BOLD, 12));
                    
                    List<Student> courseStudents = courseService.getEnrolledStudents(course.getId());
                    Label studentCount = new Label("Students: " + courseStudents.size());
                    
                    // Calculate average grade for this course
                    double courseAvgGrade = assignments.stream()
                            .filter(a -> a.getCourseId().equals(course.getId()))
                            .flatMap(a -> gradeService.getGradesByAssignment(a.getId()).stream())
                            .filter(Grade::isPublished)
                            .mapToDouble(Grade::getPercentage)
                            .average()
                            .orElse(0.0);
                    
                    Label avgGradeLabel = new Label("Average Grade: " + String.format("%.1f%%", courseAvgGrade));
                    
                    courseInfo.getChildren().addAll(courseTitle, studentCount, avgGradeLabel);
                    courseBox.getChildren().add(courseInfo);
                    content.getChildren().add(courseBox);
                }
            }
            
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            reportDialog.getDialogPane().setContent(scrollPane);
            reportDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
            
            // Add print button
            ButtonType printButton = new ButtonType("Print/Save", ButtonBar.ButtonData.OTHER);
            reportDialog.getDialogPane().getButtonTypes().add(printButton);
            
            reportDialog.showAndWait();
            
            AlertHelper.showSuccess("Report Generated", 
                    "Teaching report has been generated successfully!");
        });
        
        reportTask.setOnFailed(e -> {
            showLoading(false);
            AlertHelper.showError("Report Failed", 
                    "Failed to generate report. Please try again.");
        });
        
        new Thread(reportTask).start();
    }

    @FXML
    public void handleViewAllCourses() {
        mainTabPane.getSelectionModel().select(1); // Assuming courses tab is at index 1
    }

    @FXML
    public void handleViewAllAssignments() {
        mainTabPane.getSelectionModel().select(2); // Assuming assignments tab is at index 2
    }

    @FXML
    public void handleViewAllStudents() {
        mainTabPane.getSelectionModel().select(3); // Assuming students tab is at index 3
    }

    private void viewCourseDetails(Course course) {
        selectedCourse = course;
        
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
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        
        // Add action buttons
        ButtonType studentsButton = new ButtonType("View Students", ButtonBar.ButtonData.OTHER);
        ButtonType assignmentsButton = new ButtonType("View Assignments", ButtonBar.ButtonData.OTHER);
        ButtonType editButton = new ButtonType("Edit Course", ButtonBar.ButtonData.OTHER);
        
        dialog.getDialogPane().getButtonTypes().addAll(studentsButton, assignmentsButton, editButton);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == studentsButton) {
                viewCourseStudents(course);
            } else if (buttonType == assignmentsButton) {
                viewCourseAssignments(course);
            } else if (buttonType == editButton) {
                editCourse(course);
            }
            return null;
        });
        
        dialog.showAndWait();
    }

    private void viewCourseAssignments(Course course) {
        List<Assignment> courseAssignments = assignmentService.getAssignmentsByCourse(course.getId());
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Course Assignments");
        dialog.setHeaderText("Assignments for " + course.getTitle());
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        if (courseAssignments.isEmpty()) {
            content.getChildren().add(new Label("No assignments found for this course."));
        } else {
            for (Assignment assignment : courseAssignments) {
                HBox assignmentItem = new HBox(10);
                assignmentItem.setAlignment(Pos.CENTER_LEFT);
                
                VBox assignmentInfo = new VBox(2);
                Label title = new Label(assignment.getTitle());
                title.setFont(Font.font("System", FontWeight.BOLD, 12));
                
                Label dueDate = new Label("Due: " + 
                        (assignment.getDueDate() != null ? 
                         assignment.getDueDate().format(DATE_FORMATTER) : "No deadline"));
                dueDate.setFont(Font.font("System", 10));
                
                // Get submission stats
                Map<String, Integer> stats = assignmentService.getSubmissionStats(assignment.getId());
                Label submissions = new Label("Submissions: " + stats.get("submitted") + "/" + stats.get("total"));
                submissions.setFont(Font.font("System", 10));
                
                assignmentInfo.getChildren().addAll(title, dueDate, submissions);
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Button viewButton = new Button("View");
                viewButton.setOnAction(e -> viewAssignmentDetails(assignment));
                
                Button gradeButton = new Button("Grade");
                gradeButton.setOnAction(e -> gradeAssignmentSubmissions(assignment));
                
                assignmentItem.getChildren().addAll(assignmentInfo, spacer, viewButton, gradeButton);
                content.getChildren().add(assignmentItem);
                content.getChildren().add(new Separator());
            }
        }
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void editCourse(Course course) {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle("Edit Course");
        dialog.setHeaderText("Edit Course Details");
        
        ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField titleField = new TextField(course.getTitle());
        TextField codeField = new TextField(course.getCourseCode());
        TextField creditsField = new TextField(String.valueOf(course.getCredits()));
        TextField departmentField = new TextField(course.getDepartment());
        TextField semesterField = new TextField(course.getSemester());
        TextField scheduleField = new TextField(course.getSchedule());
        TextField classroomField = new TextField(course.getClassroom());
        TextField maxStudentsField = new TextField(String.valueOf(course.getMaxStudents()));
        TextArea descriptionArea = new TextArea(course.getDescription());
        descriptionArea.setPrefRowCount(5);
        
        grid.add(new Label("Course Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Credits:"), 0, 2);
        grid.add(creditsField, 1, 2);
        grid.add(new Label("Department:"), 0, 3);
        grid.add(departmentField, 1, 3);
        grid.add(new Label("Semester:"), 0, 4);
        grid.add(semesterField, 1, 4);
        grid.add(new Label("Schedule:"), 0, 5);
        grid.add(scheduleField, 1, 5);
        grid.add(new Label("Classroom:"), 0, 6);
        grid.add(classroomField, 1, 6);
        grid.add(new Label("Max Students:"), 0, 7);
        grid.add(maxStudentsField, 1, 7);
        grid.add(new Label("Description:"), 0, 8);
        grid.add(descriptionArea, 1, 8);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButton) {
                try {
                    course.setTitle(titleField.getText());
                    course.setCourseCode(codeField.getText());
                    course.setCredits(Integer.parseInt(creditsField.getText()));
                    course.setDepartment(departmentField.getText());
                    course.setSemester(semesterField.getText());
                    course.setSchedule(scheduleField.getText());
                    course.setClassroom(classroomField.getText());
                    course.setMaxStudents(Integer.parseInt(maxStudentsField.getText()));
                    course.setDescription(descriptionArea.getText());
                    return course;
                } catch (NumberFormatException e) {
                    AlertHelper.showError("Invalid Input", "Please enter valid numbers for credits and max students.");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Course> result = dialog.showAndWait();
        result.ifPresent(updatedCourse -> {
            showLoading(true);
            Task<Boolean> updateTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    try {
                        return courseService.updateCourse(updatedCourse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            };
            
            updateTask.setOnSucceeded(e -> {
                showLoading(false);
                boolean success = updateTask.getValue();
                if (success) {
                    AlertHelper.showSuccess("Course Updated", 
                            "Course details have been updated successfully!");
                    loadTeacherData();
                } else {
                    AlertHelper.showError("Update Failed", 
                            "Failed to update course. Please try again.");
                }
            });
            
            updateTask.setOnFailed(e -> {
                showLoading(false);
                AlertHelper.showError("Error", 
                        "An error occurred while updating course.");
            });
            
            new Thread(updateTask).start();
        });
    }

    private void viewAssignmentDetails(Assignment assignment) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Assignment Details");
        dialog.setHeaderText(assignment.getTitle());
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(new Label(assignment.getTitle()), 1, 0);
        
        grid.add(new Label("Course:"), 0, 1);
        grid.add(new Label(getCourseName(assignment.getCourseId())), 1, 1);
        
        grid.add(new Label("Description:"), 0, 2);
        grid.add(new Label(assignment.getDescription()), 1, 2);
        
        grid.add(new Label("Max Score:"), 0, 3);
        grid.add(new Label(String.valueOf(assignment.getMaxScore())), 1, 3);
        
        grid.add(new Label("Weight:"), 0, 4);
        grid.add(new Label(String.valueOf(assignment.getWeight())), 1, 4);
        
        grid.add(new Label("Due Date:"), 0, 5);
        grid.add(new Label(assignment.getDueDate() != null ? 
                assignment.getDueDate().format(DATE_FORMATTER) : "No deadline"), 1, 5);
        
        grid.add(new Label("Status:"), 0, 6);
        grid.add(new Label(assignment.getStatus().toString()), 1, 6);
        
        // Get submission stats
        Map<String, Integer> stats = assignmentService.getSubmissionStats(assignment.getId());
        grid.add(new Label("Submissions:"), 0, 7);
        grid.add(new Label(stats.get("submitted") + "/" + stats.get("total") + 
                " (" + stats.get("percentage") + "%)"), 1, 7);
        
        content.getChildren().add(grid);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        
        // Add action buttons
        ButtonType gradeButton = new ButtonType("Grade Submissions", ButtonBar.ButtonData.OTHER);
        ButtonType closeButton = new ButtonType("Close Assignment", ButtonBar.ButtonData.OTHER);
        ButtonType publishButton = new ButtonType("Publish Grades", ButtonBar.ButtonData.OTHER);
        
        dialog.getDialogPane().getButtonTypes().addAll(gradeButton, closeButton, publishButton);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == gradeButton) {
                gradeAssignmentSubmissions(assignment);
            } else if (buttonType == closeButton) {
                closeAssignment(assignment);
            } else if (buttonType == publishButton) {
                publishAssignmentGrades(assignment);
            }
            return null;
        });
        
        dialog.showAndWait();
    }

    private void closeAssignment(Assignment assignment) {
        boolean confirm = AlertHelper.showConfirmation("Close Assignment", 
                "Are you sure you want to close this assignment? Students will no longer be able to submit.");
        
        if (confirm) {
            showLoading(true);
            Task<Boolean> closeTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    try {
                        return assignmentService.closeAssignment(assignment.getId());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            };
            
            closeTask.setOnSucceeded(e -> {
                showLoading(false);
                boolean success = closeTask.getValue();
                if (success) {
                    AlertHelper.showSuccess("Assignment Closed", 
                            "Assignment has been closed successfully.");
                    loadTeacherData();
                } else {
                    AlertHelper.showError("Close Failed", 
                            "Failed to close assignment. Please try again.");
                }
            });
            
            closeTask.setOnFailed(e -> {
                showLoading(false);
                AlertHelper.showError("Error", 
                        "An error occurred while closing assignment.");
            });
            
            new Thread(closeTask).start();
        }
    }

    private void publishAssignmentGrades(Assignment assignment) {
        boolean confirm = AlertHelper.showConfirmation("Publish Grades", 
                "Are you sure you want to publish grades for this assignment? Students will be able to see their grades.");
        
        if (confirm) {
            showLoading(true);
            Task<Void> publishTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        gradeService.publishGradesForAssignment(assignment.getId());
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                    return null;
                }
            };
            
            publishTask.setOnSucceeded(e -> {
                showLoading(false);
                AlertHelper.showSuccess("Grades Published", 
                        "Grades have been published successfully!");
                loadTeacherData();
            });
            
            publishTask.setOnFailed(e -> {
                showLoading(false);
                AlertHelper.showError("Publish Failed", 
                        "Failed to publish grades. Please try again.");
            });
            
            new Thread(publishTask).start();
        }
    }

    @FXML
    @Override
    protected void handleProfile() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Teacher Profile");
        dialog.setHeaderText("Profile Information");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        grid.add(new Label("Employee ID:"), 0, 0);
        grid.add(new Label(currentTeacher.getEmployeeId()), 1, 0);
        
        grid.add(new Label("Name:"), 0, 1);
        grid.add(new Label(currentTeacher.getFullName()), 1, 1);
        
        grid.add(new Label("Email:"), 0, 2);
        grid.add(new Label(currentTeacher.getEmail()), 1, 2);
        
        grid.add(new Label("Department:"), 0, 3);
        grid.add(new Label(currentTeacher.getDepartment()), 1, 3);
        
        grid.add(new Label("Qualification:"), 0, 4);
        grid.add(new Label(currentTeacher.getQualification()), 1, 4);
        
        grid.add(new Label("Experience:"), 0, 5);
        grid.add(new Label(currentTeacher.getYearsOfExperience() + " years"), 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    @FXML
    @Override
    protected void handleSettings() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Teacher Settings");
        dialog.setHeaderText("Account Settings");
        
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
        AlertHelper.showInfo("Teacher Dashboard Help", 
                """
                Teacher Dashboard Features:
                
                1. Dashboard: View statistics and teaching insights
                2. Courses: Manage your assigned courses
                3. Assignments: Create and manage assignments
                4. Students: View enrolled students
                
                Key Features:
                • Double-click courses to view details and manage
                • Double-click assignments to view details and grade
                • Create new assignments for your courses
                • Grade student submissions
                • View student performance analytics
                • Generate teaching reports
                • Edit course and assignment details
                
                Tips:
                • Use the 'Refresh Data' button to update information
                • Close assignments when submission period ends
                • Publish grades to make them visible to students
                • Use the 'Grade Submissions' feature for bulk grading""");
    }

    private String getCourseName(String courseId) {
        if (courseId == null) return "Unknown Course";
        return courses.stream()
                .filter(c -> courseId.equals(c.getId()))
                .map(Course::getTitle)
                .findFirst()
                .orElse("Unknown Course");
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisible(show);
        loadingIndicator.setVisible(show);
        mainTabPane.setDisable(show);
    }

    // Custom ListCell for displaying students
    private class StudentListCell extends ListCell<Student> {
        @Override
        protected void updateItem(Student student, boolean empty) {
            super.updateItem(student, empty);
            
            if (empty || student == null) {
                setGraphic(null);
                setText(null);
            } else {
                // Create a simple student display
                HBox hbox = new HBox(10);
                hbox.setAlignment(Pos.CENTER_LEFT);
                
                VBox infoBox = new VBox(2);
                Label nameLabel = new Label(student.getFullName());
                nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                
                Label idLabel = new Label("ID: " + student.getStudentId());
                idLabel.setFont(Font.font("System", 10));
                idLabel.setTextFill(Color.GRAY);
                
                infoBox.getChildren().addAll(nameLabel, idLabel);
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                Label gradeLabel = new Label(String.format("CGPA: %.2f", student.getCgpa()));
                gradeLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
                
                hbox.getChildren().addAll(infoBox, spacer, gradeLabel);
                setGraphic(hbox);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                
                // Add double-click handler
                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !isEmpty()) {
                        viewStudentDetails(student);
                    }
                });
            }
        }
    }

    private void viewStudentDetails(Student student) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Student Details");
        dialog.setHeaderText(student.getFullName() + "'s Profile");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        grid.add(new Label("Student ID:"), 0, 0);
        grid.add(new Label(student.getStudentId()), 1, 0);
        
        grid.add(new Label("Name:"), 0, 1);
        grid.add(new Label(student.getFullName()), 1, 1);
        
        grid.add(new Label("Email:"), 0, 2);
        grid.add(new Label(student.getEmail()), 1, 2);
        
        grid.add(new Label("Department:"), 0, 3);
        grid.add(new Label(student.getDepartment()), 1, 3);
        
        grid.add(new Label("Semester:"), 0, 4);
        grid.add(new Label(Integer.toString(student.getSemester())), 1, 4);
        
        grid.add(new Label("CGPA:"), 0, 5);
        grid.add(new Label(String.format("%.2f", student.getCgpa())), 1, 5);
        
        grid.add(new Label("Credits Completed:"), 0, 6);
        grid.add(new Label(String.valueOf(student.getCreditsCompleted())), 1, 6);
        
        content.getChildren().add(grid);
        
        // Get courses this student is enrolled in with the teacher
        List<Course> teacherCourses = courses.stream()
                .filter(c -> courseService.getEnrolledStudents(c.getId()).contains(student))
                .collect(Collectors.toList());
        
        if (!teacherCourses.isEmpty()) {
            Separator sep = new Separator();
            content.getChildren().add(sep);
            
            Label coursesLabel = new Label("Enrolled in Your Courses:");
            coursesLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            content.getChildren().add(coursesLabel);
            
            VBox coursesList = new VBox(5);
            for (Course course : teacherCourses) {
                HBox courseItem = new HBox(10);
                courseItem.setAlignment(Pos.CENTER_LEFT);
                
                Label courseName = new Label(course.getCourseCode() + " - " + course.getTitle());
                courseName.setFont(Font.font("System", FontWeight.BOLD, 12));
                
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                // Get student's grade in this course
                List<Grade> studentGrades = gradeService.getGradesByStudent(student.getId()).stream()
                        .filter(g -> g.getCourseId().equals(course.getId()))
                        .filter(Grade::isPublished)
                        .collect(Collectors.toList());
                
                double averageGrade = studentGrades.stream()
                        .mapToDouble(Grade::getPercentage)
                        .average()
                        .orElse(0.0);
                
                Label gradeLabel = new Label(String.format("%.1f%%", averageGrade));
                gradeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                
                if (averageGrade >= 80) {
                    gradeLabel.setTextFill(Color.GREEN);
                } else if (averageGrade >= 60) {
                    gradeLabel.setTextFill(Color.ORANGE);
                } else {
                    gradeLabel.setTextFill(Color.RED);
                }
                
                courseItem.getChildren().addAll(courseName, spacer, gradeLabel);
                coursesList.getChildren().add(courseItem);
            }
            content.getChildren().add(coursesList);
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
