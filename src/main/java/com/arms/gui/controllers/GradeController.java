package com.arms.gui.controllers;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import com.arms.domain.Assignment;
import com.arms.domain.Course;
import com.arms.domain.Grade;
import com.arms.domain.Student;
import com.arms.domain.Teacher;
import com.arms.domain.enums.UserRole;
import com.arms.gui.util.AlertHelper;
import com.arms.service.AssignmentService;
import com.arms.service.CourseService;
import com.arms.service.GradeService;
import com.arms.service.UserService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class GradeController extends DashboardController {

    // Student View Components
    @FXML
    private Label studentNameLabel;
    @FXML
    private Label studentIdLabel;
    @FXML
    private Label studentCgpaLabel;
    @FXML
    private Label totalCoursesLabel;
    @FXML
    private Label averageGradeLabel;
    @FXML
    private Label completedCreditsLabel;

    @FXML
    private ComboBox<String> courseFilter;
    @FXML
    private ComboBox<String> semesterFilter;
    @FXML
    private CheckBox publishedOnlyCheck;

    @FXML
    private TableView<Grade> gradesTable;

    @FXML
    private PieChart gradeDistributionChart;

    // Teacher Grading Components
    @FXML
    private Tab gradingTab;
    @FXML
    private Tab analyticsTab;

    @FXML
    private ComboBox<Course> gradingCourseFilter;
    @FXML
    private TableView<StudentGradeSummary> studentsTable;

    // Analytics Components
    @FXML
    private BarChart<String, Number> coursePerformanceChart;
    @FXML
    @SuppressWarnings("unused")
    private Label totalStudentsLabel;
    @FXML
    @SuppressWarnings("unused")
    private Label averageCgpaLabel;
    @FXML
    @SuppressWarnings("unused")
    private Label passRateLabel;
    @FXML
    @SuppressWarnings("unused")
    private Label topCourseLabel;

    private final GradeService gradeService = GradeService.getInstance();
    private final CourseService courseService = CourseService.getInstance();
    private final UserService userService = UserService.getInstance();
    private final AssignmentService assignmentService = AssignmentService.getInstance();

    private final ObservableList<Grade> studentGrades = FXCollections.observableArrayList();
    private final ObservableList<Course> teacherCourses = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        initializeUI();
        loadData();

        // Hide teacher tabs for non-teachers
        boolean isTeacher = currentUser.getRole() == UserRole.TEACHER;
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN
                || currentUser.getRole() == UserRole.SUPER_ADMIN;

        gradingTab.setDisable(!isTeacher && !isAdmin);
        analyticsTab.setDisable(!isTeacher && !isAdmin);
    }

    @Override
    protected void loadDashboardData() {
        // Load student-specific data
        if (currentUser instanceof Student student) {
            loadStudentData(student);
        } else if (currentUser instanceof Teacher teacher) {
            loadTeacherData(teacher);
        }
    }

    @Override
    protected void initializeUI() {
        // Initialize student grade table
        initializeGradeTable();

        // Initialize teacher grading table
        initializeStudentTable();

        // Set up filters
        courseFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterGrades());
        semesterFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterGrades());
        publishedOnlyCheck.selectedProperty().addListener((obs, oldVal, newVal) -> filterGrades());

        // Set up grading course filter
        gradingCourseFilter.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? ""
                        : course.getCourseCode() + " - " + course.getTitle());
            }
        });

        gradingCourseFilter.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? ""
                        : course.getCourseCode() + " - " + course.getTitle());
            }
        });
    }

    private void initializeGradeTable() {
        // Define columns
        TableColumn<Grade, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(cellData -> {
            Course course = courseService.getCourseById(cellData.getValue().getCourseId())
                    .orElse(null);
            return new javafx.beans.property.SimpleStringProperty(
                    course != null ? course.getCourseCode() : "Unknown"
            );
        });

        TableColumn<Grade, String> assignmentCol = new TableColumn<>("Assignment");
        assignmentCol.setCellValueFactory(cellData -> {
            Assignment assignment = assignmentService.getAssignmentById(
                    cellData.getValue().getAssignmentId()).orElse(null);
            return new javafx.beans.property.SimpleStringProperty(
                    assignment != null ? assignment.getTitle() : "Unknown"
            );
        });

        TableColumn<Grade, Double> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

        TableColumn<Grade, Double> maxScoreCol = new TableColumn<>("Max Score");
        maxScoreCol.setCellValueFactory(new PropertyValueFactory<>("maxScore"));

        TableColumn<Grade, Double> percentageCol = new TableColumn<>("Percentage");
        percentageCol.setCellValueFactory(new PropertyValueFactory<>("percentage"));
        percentageCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double percentage, boolean empty) {
                super.updateItem(percentage, empty);
                if (empty || percentage == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", percentage));
                }
            }
        });

        TableColumn<Grade, String> letterCol = new TableColumn<>("Letter Grade");
        letterCol.setCellValueFactory(new PropertyValueFactory<>("letterGrade"));

        TableColumn<Grade, String> feedbackCol = new TableColumn<>("Feedback");
        feedbackCol.setCellValueFactory(new PropertyValueFactory<>("feedback"));
        feedbackCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String feedback, boolean empty) {
                super.updateItem(feedback, empty);
                if (empty || feedback == null) {
                    setText(null);
                } else {
                    setText(feedback.length() > 50
                            ? feedback.substring(0, 50) + "..." : feedback);
                }
            }
        });

        TableColumn<Grade, String> dateCol = new TableColumn<>("Graded Date");
        dateCol.setCellValueFactory(cellData
                -> new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getGradedAt() != null
                        ? cellData.getValue().getGradedAt().toString() : "Not graded"
                ));

        TableColumn<Grade, Boolean> publishedCol = new TableColumn<>("Published");
        publishedCol.setCellValueFactory(new PropertyValueFactory<>("published"));
        publishedCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean published, boolean empty) {
                super.updateItem(published, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(published ? "Yes" : "No");
                }
            }
        });

        gradesTable.getColumns().addAll(courseCol, assignmentCol, scoreCol, maxScoreCol,
                percentageCol, letterCol, feedbackCol, dateCol, publishedCol);
        gradesTable.setItems(studentGrades);
    }

    private void initializeStudentTable() {
        TableColumn<StudentGradeSummary, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("studentId"));

        TableColumn<StudentGradeSummary, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));

        TableColumn<StudentGradeSummary, Double> gradeCol = new TableColumn<>("Current Grade");
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("currentGrade"));
        gradeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double grade, boolean empty) {
                super.updateItem(grade, empty);
                if (empty || grade == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", grade));
                }
            }
        });

        TableColumn<StudentGradeSummary, Integer> submittedCol = new TableColumn<>("Assignments Submitted");
        submittedCol.setCellValueFactory(new PropertyValueFactory<>("assignmentsSubmitted"));

        TableColumn<StudentGradeSummary, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button gradeButton = new Button("Grade");
            private final Button viewButton = new Button("View Details");

            {
                gradeButton.setOnAction(event -> {
                    StudentGradeSummary student = getTableView().getItems().get(getIndex());
                    gradeStudent(student);
                });

                viewButton.setOnAction(event -> {
                    StudentGradeSummary student = getTableView().getItems().get(getIndex());
                    viewStudentDetails(student);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, gradeButton, viewButton);
                    setGraphic(buttons);
                }
            }
        });

        studentsTable.getColumns().addAll(idCol, nameCol, gradeCol, submittedCol, actionsCol);
    }

    private void loadData() {
        if (currentUser instanceof Student student) {
            loadStudentData(student);
        } else if (currentUser instanceof Teacher teacher) {
            loadTeacherData(teacher);
        }
    }

    private void loadStudentData(Student student) {
        // Set student info
        studentNameLabel.setText("Name: " + student.getFullName());
        studentIdLabel.setText("Student ID: " + student.getStudentId());
        studentCgpaLabel.setText(String.format("CGPA: %.2f", student.getCgpa()));

        // Load grades
        List<Grade> grades = gradeService.getGradesByStudent(student.getId());
        studentGrades.setAll(grades);

        // Calculate statistics
        long totalCourses = courseService.getCoursesByStudent(student.getId()).size();
        Optional<Double> averageGrade = gradeService.calculateStudentAverage(student.getId());

        totalCoursesLabel.setText("Total Courses: " + totalCourses);
        averageGradeLabel.setText("Average Grade: "
                + averageGrade.map(avg -> String.format("%.1f%%", avg)).orElse("N/A"));
        completedCreditsLabel.setText("Credits Completed: " + student.getCreditsCompleted());

        // Update filters
        updateFilters();

        // Update grade distribution chart
        updateGradeDistributionChart(grades);
    }

    private void loadTeacherData(Teacher teacher) {
        // Load courses taught by this teacher
        teacherCourses.setAll(courseService.getCoursesByTeacher(teacher.getId()));
        gradingCourseFilter.setItems(teacherCourses);

        if (!teacherCourses.isEmpty()) {
            gradingCourseFilter.setValue(teacherCourses.get(0));
        }
    }

    private void updateFilters() {
        // Get unique courses and semesters from grades
        Set<String> courses = studentGrades.stream()
                .map(Grade::getCourseId)
                .map(courseId -> courseService.getCourseById(courseId)
                .map(Course::getCourseCode)
                .orElse("Unknown"))
                .collect(Collectors.toSet());

        Set<String> semesters = studentGrades.stream()
                .map(Grade::getCourseId)
                .map(courseId -> courseService.getCourseById(courseId)
                .map(Course::getSemester)
                .orElse("Unknown"))
                .collect(Collectors.toSet());

        courseFilter.getItems().clear();
        courseFilter.getItems().add("All Courses");
        courseFilter.getItems().addAll(courses);
        courseFilter.setValue("All Courses");

        semesterFilter.getItems().clear();
        semesterFilter.getItems().add("All Semesters");
        semesterFilter.getItems().addAll(semesters);
        semesterFilter.setValue("All Semesters");
    }

    private void filterGrades() {
        String selectedCourse = courseFilter.getValue();
        String selectedSemester = semesterFilter.getValue();
        boolean publishedOnly = publishedOnlyCheck.isSelected();

        List<Grade> filtered = studentGrades.stream()
                .filter(grade -> {
                    if (publishedOnly && !grade.isPublished()) {
                        return false;
                    }

                    if (!"All Courses".equals(selectedCourse)) {
                        String courseCode = courseService.getCourseById(grade.getCourseId())
                                .map(Course::getCourseCode)
                                .orElse("Unknown");
                        if (!courseCode.equals(selectedCourse)) {
                            return false;
                        }
                    }

                    if (!"All Semesters".equals(selectedSemester)) {
                        String semester = courseService.getCourseById(grade.getCourseId())
                                .map(Course::getSemester)
                                .orElse("Unknown");
                        if (!semester.equals(selectedSemester)) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        gradesTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void updateGradeDistributionChart(List<Grade> grades) {
        gradeDistributionChart.getData().clear();

        Map<String, Long> distribution = grades.stream()
                .collect(Collectors.groupingBy(Grade::getLetterGrade, Collectors.counting()));

        for (Map.Entry<String, Long> entry : distribution.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                    entry.getKey() + " (" + entry.getValue() + ")",
                    entry.getValue()
            );
            gradeDistributionChart.getData().add(slice);
        }
    }

    @FXML
    private void handleRefresh() {
        loadData();
        AlertHelper.showInfo("Refreshed", "Grade data refreshed.");
    }

    @FXML
    private void handleExportTranscript() {
        AlertHelper.showInfo("Export Transcript",
                "This feature would generate a PDF transcript of all grades.");
    }

    @FXML
    private void handleLoadStudents() {
        Course selectedCourse = gradingCourseFilter.getValue();
        if (selectedCourse == null) {
            AlertHelper.showWarning("No Course Selected",
                    "Please select a course to load students.");
            return;
        }

        // Load students enrolled in the course
        List<Student> enrolledStudents = courseService.getEnrolledStudents(selectedCourse.getId());

        List<StudentGradeSummary> summaries = enrolledStudents.stream()
                .map(student -> {
                    // Calculate current grade for this student in this course
                    Optional<Double> average = gradeService.calculateCourseAverageForStudent(
                            student.getId(), selectedCourse.getId());

                    // Count assignments submitted
                    List<Assignment> courseAssignments = assignmentService.getAssignmentsByCourse(
                            selectedCourse.getId());
                    long submittedCount = courseAssignments.stream()
                            .filter(assignment -> assignment.getSubmissionStudentIds()
                            .contains(student.getId()))
                            .count();

                    return new StudentGradeSummary(
                            student.getStudentId(),
                            student.getFullName(),
                            average.orElse(0.0),
                            (int) submittedCount,
                            student.getId()
                    );
                })
                .collect(Collectors.toList());

        studentsTable.setItems(FXCollections.observableArrayList(summaries));
    }

    @FXML
    private void handlePublishAll() {
        Course selectedCourse = gradingCourseFilter.getValue();
        if (selectedCourse == null) {
            AlertHelper.showWarning("No Course Selected",
                    "Please select a course first.");
            return;
        }

        if (AlertHelper.showConfirmation("Publish All Grades",
                "Are you sure you want to publish all grades for "
                + selectedCourse.getTitle() + "?\nThis will make grades visible to students.")) {

            // Get all assignments for this course
            List<Assignment> assignments = assignmentService.getAssignmentsByCourse(
                    selectedCourse.getId());

            // Publish grades for each assignment
            assignments.forEach(assignment
                    -> gradeService.publishGradesForAssignment(assignment.getId()));

            AlertHelper.showSuccess("Success", "All grades published successfully!");
            handleLoadStudents(); // Refresh the table
        }
    }

    @FXML
    private void handleCalculateFinal() {
        Course selectedCourse = gradingCourseFilter.getValue();
        if (selectedCourse == null) {
            AlertHelper.showWarning("No Course Selected",
                    "Please select a course first.");
            return;
        }

        Map<String, Double> finalGrades = gradeService.calculateFinalGradesForCourse(
                selectedCourse.getId());

        if (finalGrades.isEmpty()) {
            AlertHelper.showInfo("No Grades",
                    "No grades available to calculate final grades.");
            return;
        }

        // Show final grades in a dialog
        StringBuilder message = new StringBuilder();
        message.append("Final Grades for ").append(selectedCourse.getTitle()).append(":\n\n");

        finalGrades.forEach((studentId, grade) -> {
            Student student = (Student) userService.getUserById(studentId).orElse(null);
            if (student != null) {
                message.append(student.getFullName())
                        .append(" (").append(student.getStudentId()).append("): ")
                        .append(String.format("%.1f%%", grade))
                        .append("\n");
            }
        });

        AlertHelper.showInfo("Final Grades", message.toString());
    }

    @FXML
    private void handleGenerateReport() {
        Course selectedCourse = gradingCourseFilter.getValue();
        if (selectedCourse == null) {
            AlertHelper.showWarning("No Course Selected",
                    "Please select a course first.");
            return;
        }

        AlertHelper.showInfo("Generate Report",
                "This feature would generate a detailed grade report for "
                + selectedCourse.getTitle() + " in PDF format.");
    }

    private void gradeStudent(StudentGradeSummary student) {
        Course selectedCourse = gradingCourseFilter.getValue();
        if (selectedCourse == null) {
            AlertHelper.showWarning("No Course Selected",
                    "Please select a course first.");
            return;
        }

        AlertHelper.showInfo("Grade Student",
                "Grade student: " + student.getStudentName()
                + "\n\nThis would open a dialog to enter grades for individual assignments.");
    }

    private void viewStudentDetails(StudentGradeSummary student) {
        AlertHelper.showInfo("Student Details",
                "Student: " + student.getStudentName()
                + "\nID: " + student.getStudentId()
                + "\nCurrent Grade: " + String.format("%.1f%%", student.getCurrentGrade())
                + "\nAssignments Submitted: " + student.getAssignmentsSubmitted());
    }

    // Inner class for student grade summaries
    public static class StudentGradeSummary {

        private final String studentId;
        private final String studentName;
        private final double currentGrade;
        private final int assignmentsSubmitted;
        private final String userId;

        public StudentGradeSummary(String studentId, String studentName,
                double currentGrade, int assignmentsSubmitted, String userId) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.currentGrade = currentGrade;
            this.assignmentsSubmitted = assignmentsSubmitted;
            this.userId = userId;
        }

        public String getStudentId() {
            return studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public double getCurrentGrade() {
            return currentGrade;
        }

        public int getAssignmentsSubmitted() {
            return assignmentsSubmitted;
        }

        public String getUserId() {
            return userId;
        }
    }
}
