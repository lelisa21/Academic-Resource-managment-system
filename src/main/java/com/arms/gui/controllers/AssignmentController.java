package com.arms.gui.controllers;

import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

import com.arms.domain.Assignment;
import com.arms.domain.Course;
import com.arms.domain.enums.AssignmentStatus;
import com.arms.domain.enums.AssignmentType;
import com.arms.domain.enums.UserRole;
import com.arms.gui.components.AssignmentCard;
import com.arms.gui.dialogs.AddAssignmentDialog;
import com.arms.gui.dialogs.GradeSubmissionDialog;
import com.arms.gui.util.AlertHelper;
import com.arms.service.AssignmentService;
import com.arms.service.CourseService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class AssignmentController extends DashboardController {

    @FXML
    private GridView<Assignment> assignmentsGrid;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Course> courseFilter;
    @FXML
    private ComboBox<AssignmentType> typeFilter;
    @FXML
    private ComboBox<AssignmentStatus> statusFilter;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private Button addAssignmentButton;
    @FXML
    private Button editAssignmentButton;
    @FXML
    private Button deleteAssignmentButton;
    @FXML
    private Button gradeButton;
    @FXML
    private Button submitButton;
    @FXML
    private VBox studentSection;
    @FXML
    private VBox teacherSection;

    private final AssignmentService assignmentService = AssignmentService.getInstance();
    private final CourseService courseService = CourseService.getInstance();

    private final ObservableList<Assignment> allAssignments = FXCollections.observableArrayList();
    private final ObservableList<Course> availableCourses = FXCollections.observableArrayList();
    private FilteredList<Assignment> filteredAssignments;
    private Assignment lastSelectedAssignment;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        initializeUI();
        setupEventHandlers();
        loadAssignments();
        loadAvailableCourses();

        // Show role-specific sections
        boolean isStudent = currentUser.getRole() == UserRole.STUDENT;
        boolean isTeacher = currentUser.getRole() == UserRole.TEACHER;

        studentSection.setVisible(isStudent);
        studentSection.setManaged(isStudent);
        teacherSection.setVisible(isTeacher);
        teacherSection.setManaged(isTeacher);

        addAssignmentButton.setVisible(isTeacher || currentUser.getRole() == UserRole.ADMIN);
        addAssignmentButton.setManaged(isTeacher || currentUser.getRole() == UserRole.ADMIN);
        gradeButton.setVisible(isTeacher);
        gradeButton.setManaged(isTeacher);
        submitButton.setVisible(isStudent);
        submitButton.setManaged(isStudent);
    }

    @Override
    protected void loadDashboardData() {
        // Not used in this controller
    }

    protected void initializeUI() {
        assignmentsGrid.setCellFactory(gridView -> new AssignmentCard());

        // Initialize filters
        typeFilter.getItems().addAll(AssignmentType.values());
        typeFilter.setValue(null);
        typeFilter.setPromptText("All Types");

        statusFilter.getItems().addAll(AssignmentStatus.values());
        statusFilter.setValue(AssignmentStatus.ACTIVE);

        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now().plusMonths(1));

        // Initialize filtered list
        filteredAssignments = new FilteredList<>(allAssignments, p -> true);
        SortedList<Assignment> sortedAssignments = new SortedList<>(filteredAssignments);
        assignmentsGrid.setItems(sortedAssignments);

        // Set up search filter
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // Set up other filters
        courseFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        typeFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        fromDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        toDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        filteredAssignments.setPredicate(assignment -> {
            if (assignment == null) {
                return false;
            }

            // Search filter (guard nulls)
            String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
            if (!searchText.isEmpty()) {
                String title = assignment.getTitle() != null ? assignment.getTitle().toLowerCase() : "";
                String desc = assignment.getDescription() != null ? assignment.getDescription().toLowerCase() : "";
                boolean matchesSearch = title.contains(searchText) || desc.contains(searchText);
                if (!matchesSearch) {
                    return false;
                }
            }

            // Course filter
            Course selectedCourse = courseFilter.getValue();
            if (selectedCourse != null) {
                String assCourseId = assignment.getCourseId();
                if (assCourseId == null || !assCourseId.equals(selectedCourse.getId())) {
                    return false;
                }
            }

            // Type filter
            AssignmentType selectedType = typeFilter.getValue();
            if (selectedType != null && assignment.getType() != selectedType) {
                return false;
            }

            // Status filter
            AssignmentStatus selectedStatus = statusFilter.getValue();
            if (selectedStatus != null && assignment.getStatus() != selectedStatus) {
                return false;
            }

            // Date range filter
            LocalDate fromDate = fromDatePicker.getValue();
            LocalDate toDate = toDatePicker.getValue();
            if (fromDate != null && toDate != null) {
                if (assignment.getDueDate() == null) {
                    return false;
                }
                LocalDate dueDate = assignment.getDueDate().toLocalDate();
                if (dueDate.isBefore(fromDate) || dueDate.isAfter(toDate)) {
                    return false;
                }
            }

            return true;
        });
    }

    private void setupEventHandlers() {
        assignmentsGrid.setOnMouseClicked(event -> {
            Object target = event.getTarget();
            Node node = (target instanceof Node) ? (Node) target : null;
            while (node != null && !(node instanceof GridCell)) {
                node = node.getParent();
            }
            if (node instanceof GridCell) {
                GridCell<?> cell = (GridCell<?>) node;
                Object item = cell.getItem();
                if (item instanceof Assignment assignment) {
                    lastSelectedAssignment = assignment;
                    if (event.getClickCount() == 2) {
                        viewAssignmentDetails();
                    }
                }
            }
        });

        addAssignmentButton.setOnAction(event -> handleAddAssignment());
        editAssignmentButton.setOnAction(event -> handleEditAssignment());
        deleteAssignmentButton.setOnAction(event -> handleDeleteAssignment());
        gradeButton.setOnAction(event -> handleGradeAssignment());
        submitButton.setOnAction(event -> handleSubmitAssignment());
    }

    private void loadAssignments() {
        allAssignments.clear();

        if (currentUser.getRole() == UserRole.STUDENT) {
            // Load assignments for student
            // This would require student ID
            // allAssignments.addAll(assignmentService.getAssignmentsForStudent(currentUser.getId()));
            AlertHelper.showInfo("Student Assignments",
                    "Student assignment loading would be implemented here.");
        } else if (currentUser.getRole() == UserRole.TEACHER) {
            // Load assignments created by teacher
            allAssignments.addAll(assignmentService.getAssignmentsByTeacher(currentUser.getId()));
        } else if (currentUser.getRole() == UserRole.ADMIN
                || currentUser.getRole() == UserRole.SUPER_ADMIN) {
            // Load all assignments
            // This would require getAllAssignments method
            AlertHelper.showInfo("All Assignments",
                    "Admin assignment loading would be implemented here.");
        }

        applyFilters();
    }

    private void loadAvailableCourses() {
        availableCourses.clear();

        if (currentUser.getRole() == UserRole.TEACHER) {
            // Load courses taught by this teacher
            availableCourses.addAll(courseService.getCoursesByTeacher(currentUser.getId()));
        } else if (currentUser.getRole() == UserRole.ADMIN
                || currentUser.getRole() == UserRole.SUPER_ADMIN) {
            // Load all courses
            availableCourses.addAll(courseService.getAllCourses());
        }

        courseFilter.setItems(availableCourses);
        courseFilter.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? ""
                        : course.getCourseCode() + " - " + course.getTitle());
            }
        });

        courseFilter.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? ""
                        : course.getCourseCode() + " - " + course.getTitle());
            }
        });

        if (!availableCourses.isEmpty()) {
            courseFilter.setValue(availableCourses.get(0));
        }
    }

    @FXML
    private void handleAddAssignment() {
        if (availableCourses.isEmpty()) {
            AlertHelper.showWarning("No Courses",
                    "You need to have courses available to create assignments.");
            return;
        }

        AddAssignmentDialog dialog = new AddAssignmentDialog(availableCourses);
        Optional<Assignment> result = dialog.showAndWait();
        result.ifPresent(assignment -> {
            boolean success = assignmentService.createAssignment(assignment).isPresent();
            if (success) {
                AlertHelper.showSuccess("Success", "Assignment created successfully!");
                loadAssignments();
            } else {
                AlertHelper.showError("Error", "Failed to create assignment.");
            }
        });
    }

    @FXML
    private void handleEditAssignment() {
        Assignment selected = lastSelectedAssignment;
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select an assignment to edit.");
            return;
        }

        AlertHelper.showInfo("Edit Assignment",
                "Edit feature for assignment: " + selected.getTitle() + "\n\n"
                + "In a full implementation, this would open an edit dialog.");
    }

    @FXML
    private void handleDeleteAssignment() {
        Assignment selected = lastSelectedAssignment;
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select an assignment to delete.");
            return;
        }

        if (AlertHelper.showConfirmation("Confirm Delete",
                "Are you sure you want to delete assignment: " + selected.getTitle() + "?\n"
                + "This action cannot be undone.")) {

            boolean success = assignmentService.deleteAssignment(selected.getId());
            if (success) {
                AlertHelper.showSuccess("Deleted", "Assignment deleted successfully!");
                loadAssignments();
            } else {
                AlertHelper.showError("Delete Failed",
                        "Could not delete assignment. It may have submissions.");
            }
        }
    }

    @FXML
    private void handleGradeAssignment() {
        Assignment selected = lastSelectedAssignment;
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select an assignment to grade.");
            return;
        }

        GradeSubmissionDialog dialog = new GradeSubmissionDialog(selected);
        dialog.showAndWait();
        loadAssignments(); // Refresh to show updated grades
    }

    @FXML
    private void handleSubmitAssignment() {
        Assignment selected = lastSelectedAssignment;
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select an assignment to submit.");
            return;
        }

        if (!selected.canSubmit(currentUser.getId())) {
            AlertHelper.showError("Cannot Submit",
                    "You cannot submit this assignment. It may be overdue or already submitted.");
            return;
        }

        // In a full implementation, this would open a submission dialog
        AlertHelper.showInfo("Submit Assignment",
                "Submission dialog for: " + selected.getTitle() + "\n\n"
                + "This would allow uploading files and entering submission text.");
    }

    @FXML
    private void handleRefresh() {
        loadAssignments();
        AlertHelper.showInfo("Refreshed", "Assignment list refreshed.");
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        courseFilter.setValue(null);
        typeFilter.setValue(null);
        statusFilter.setValue(AssignmentStatus.ACTIVE);
        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now().plusMonths(1));
        applyFilters();
    }

    private void viewAssignmentDetails() {
        Assignment selected = lastSelectedAssignment;
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select an assignment to view details.");
            return;
        }

        // Create details dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Assignment Details");
        dialog.setHeaderText(selected.getTitle());

        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);
        detailsArea.setPrefSize(500, 400);

        StringBuilder details = new StringBuilder();
        details.append("Title: ").append(selected.getTitle()).append("\n\n");
        details.append("Description: ").append(selected.getDescription()).append("\n\n");
        details.append("Type: ").append(selected.getType()).append("\n");
        details.append("Status: ").append(selected.getStatus()).append("\n");
        details.append("Max Score: ").append(selected.getMaxScore()).append("\n");
        details.append("Weight: ").append(selected.getWeight()).append("%\n");
        details.append("Due Date: ").append(selected.getDueDate()).append("\n");
        details.append("Created: ").append(selected.getCreatedAt()).append("\n");
        details.append("Submissions: ").append(selected.getSubmissionStudentIds().size()).append("\n");
        details.append("Overdue: ").append(selected.isOverdue() ? "Yes" : "No").append("\n");

        // Get course info
        Optional<Course> courseOpt = courseService.getCourseById(selected.getCourseId());
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            details.append("\nCourse: ").append(course.getCourseCode())
                    .append(" - ").append(course.getTitle()).append("\n");
        }

        detailsArea.setText(details.toString());

        dialog.getDialogPane().setContent(detailsArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
