package com.arms.gui.controllers;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

import com.arms.domain.Course;
import com.arms.domain.Teacher;
import com.arms.domain.enums.UserRole;
import com.arms.gui.components.CourseCard;
import com.arms.gui.dialogs.AddCourseDialog;
import com.arms.gui.util.AlertHelper;
import com.arms.service.CourseService;
import com.arms.service.UserService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class CourseController extends DashboardController {

    @FXML
    private GridView<Course> coursesGrid;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> departmentFilter;
    @FXML
    private ComboBox<String> semesterFilter;
    @FXML
    private CheckBox activeOnlyCheck;
    @FXML
    private Button addCourseButton;
    @FXML
    private Button editCourseButton;
    @FXML
    private Button deleteCourseButton;
    @FXML
    private Button viewDetailsButton;
    @FXML
    private VBox teacherSection;
    @FXML
    private Label teacherInfoLabel;

    private final CourseService courseService = CourseService.getInstance();
    private final UserService userService = UserService.getInstance();

    private ObservableList<Course> allCourses = FXCollections.observableArrayList();
    private FilteredList<Course> filteredCourses;
    private Course lastSelectedCourse;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        initializeUI();
        setupEventHandlers();
        loadCourses();

        // Show teacher section only for teachers
        boolean isTeacher = currentUser.getRole() == UserRole.TEACHER;
        teacherSection.setVisible(isTeacher);
        teacherSection.setManaged(isTeacher);

        if (isTeacher) {
            Teacher teacher = (Teacher) currentUser;
            teacherInfoLabel.setText("Teaching " + teacher.getAssignedCourseIds().size() + " courses");
        }
    }

    @Override
    protected void loadDashboardData() {
        // Not used in this controller
    }

    protected void initializeUI() {
        coursesGrid.setCellFactory(gridView -> new CourseCard());

        // Initialize filters
        departmentFilter.getItems().addAll("All Departments", "Computer Science",
                "Mathematics", "Physics", "Chemistry", "Biology", "Engineering", "Business");
        departmentFilter.setValue("All Departments");

        semesterFilter.getItems().addAll("All Semesters", "Fall 2024", "Spring 2024",
                "Summer 2024", "Fall 2023", "Spring 2023");
        semesterFilter.setValue("All Semesters");

        // Initialize filtered list
        filteredCourses = new FilteredList<>(allCourses, p -> true);
        SortedList<Course> sortedCourses = new SortedList<>(filteredCourses);
        coursesGrid.setItems(sortedCourses);

        // Set up search filter
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCourses.setPredicate(course -> {
                if (course == null) {
                    return false;
                }
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                String code = course.getCourseCode() != null ? course.getCourseCode().toLowerCase() : "";
                String title = course.getTitle() != null ? course.getTitle().toLowerCase() : "";
                String desc = course.getDescription() != null ? course.getDescription().toLowerCase() : "";
                String dept = course.getDepartment() != null ? course.getDepartment().toLowerCase() : "";

                return code.contains(lowerCaseFilter) || title.contains(lowerCaseFilter)
                        || desc.contains(lowerCaseFilter) || dept.contains(lowerCaseFilter);
            });
        });

        // Set up department filter
        departmentFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // Set up semester filter
        semesterFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        // Set up active only filter
        activeOnlyCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    private void applyFilters() {
        filteredCourses.setPredicate(course -> {
            // Search filter
            String searchText = searchField.getText().toLowerCase();
            if (!searchText.isEmpty()) {
                boolean matchesSearch = course.getCourseCode().toLowerCase().contains(searchText)
                        || course.getTitle().toLowerCase().contains(searchText)
                        || course.getDescription().toLowerCase().contains(searchText)
                        || course.getDepartment().toLowerCase().contains(searchText);
                if (!matchesSearch) {
                    return false;
                }
            }

            // Department filter
            String selectedDept = departmentFilter.getValue();
            if (!"All Departments".equals(selectedDept)) {
                String dept = course.getDepartment() != null ? course.getDepartment() : "";
                if (!dept.equals(selectedDept)) {
                    return false;
                }
            }

            // Semester filter
            String selectedSemester = semesterFilter.getValue();
            if (!"All Semesters".equals(selectedSemester)) {
                String sem = course.getSemester() != null ? course.getSemester() : "";
                if (!sem.equals(selectedSemester)) {
                    return false;
                }
            }

            // Active only filter
            if (activeOnlyCheck.isSelected() && !course.isActive()) {
                return false;
            }

            return true;
        });
    }

    private void setupEventHandlers() {
        coursesGrid.setOnMouseClicked(event -> {
            Object target = event.getTarget();
            Node node = (target instanceof Node) ? (Node) target : null;
            while (node != null && !(node instanceof GridCell)) {
                node = node.getParent();
            }
            if (node instanceof GridCell) {
                GridCell<?> cell = (GridCell<?>) node;
                Object item = cell.getItem();
                if (item instanceof Course course) {
                    lastSelectedCourse = course;
                    if (event.getClickCount() == 2) {
                        viewCourseDetails();
                    }
                }
            }
        });

        addCourseButton.setOnAction(event -> handleAddCourse());
        editCourseButton.setOnAction(event -> handleEditCourse());
        deleteCourseButton.setOnAction(event -> handleDeleteCourse());
        viewDetailsButton.setOnAction(event -> viewCourseDetails());
    }

    private void loadCourses() {
        allCourses.clear();

        if (currentUser.getRole() == UserRole.TEACHER) {
            // Show only courses taught by this teacher
            Teacher teacher = (Teacher) currentUser;
            teacher.getAssignedCourseIds().forEach(courseId -> {
                Course course = courseService.getCourseById(courseId).orElse(null);
                if (course != null) {
                    allCourses.add(course);
                }
            });
        } else if (currentUser.getRole() == UserRole.STUDENT) {
            // Show courses the student is enrolled in
            // This would require enrollment service
        } else if (currentUser.getRole() == UserRole.ADMIN
                || currentUser.getRole() == UserRole.SUPER_ADMIN) {
            // Show all courses
            allCourses.addAll(courseService.getAllCourses());
        }

        applyFilters();
    }

    @FXML
    private void handleAddCourse() {
        if (!hasPermission("CREATE_COURSE")) {
            AlertHelper.showError("Permission Denied",
                    "You don't have permission to add courses.");
            return;
        }

        AddCourseDialog dialog = new AddCourseDialog();
        Optional<Course> result = dialog.showAndWait();
        result.ifPresent(course -> {
            boolean success = courseService.createCourse(course).isPresent();
            if (success) {
                AlertHelper.showSuccess("Success", "Course added successfully!");
                loadCourses();
            } else {
                AlertHelper.showError("Error", "Failed to add course.");
            }
        });
    }

    @FXML
    private void handleEditCourse() {
        Course selected = lastSelectedCourse;
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a course to edit.");
            return;
        }

        if (!hasPermission("EDIT_COURSE")) {
            AlertHelper.showError("Permission Denied",
                    "You don't have permission to edit courses.");
            return;
        }

        AlertHelper.showInfo("Edit Course",
                "Edit feature for course: " + selected.getTitle() + "\n\n"
                + "In a full implementation, this would open an edit dialog.");
    }

    @FXML
    private void handleDeleteCourse() {
        Course selected = lastSelectedCourse;
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a course to delete.");
            return;
        }

        if (!hasPermission("DELETE_COURSE")) {
            AlertHelper.showError("Permission Denied",
                    "You don't have permission to delete courses.");
            return;
        }

        if (AlertHelper.showConfirmation("Confirm Delete",
                "Are you sure you want to delete course: " + selected.getTitle() + "?\n"
                + "This action cannot be undone.")) {

            boolean success = courseService.deleteCourse(selected.getId());
            if (success) {
                AlertHelper.showSuccess("Deleted", "Course deleted successfully!");
                loadCourses();
            } else {
                AlertHelper.showError("Delete Failed",
                        "Could not delete course. It may have enrolled students.");
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadCourses();
        AlertHelper.showInfo("Refreshed", "Course list refreshed.");
    }

    @FXML
    private void handleExport() {
        AlertHelper.showInfo("Export",
                "Export feature would save course list to CSV/PDF format.");
    }

    private void viewCourseDetails() {
        Course selected = lastSelectedCourse;
        if (selected == null) {
            AlertHelper.showWarning("No Selection", "Please select a course to view details.");
            return;
        }

        // Create details dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Course Details");
        dialog.setHeaderText(selected.getTitle() + " (" + selected.getCourseCode() + ")");

        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);
        detailsArea.setPrefSize(500, 400);

        StringBuilder details = new StringBuilder();
        details.append("Course Code: ").append(selected.getCourseCode()).append("\n\n");
        details.append("Title: ").append(selected.getTitle()).append("\n\n");
        details.append("Description: ").append(selected.getDescription()).append("\n\n");
        details.append("Department: ").append(selected.getDepartment()).append("\n");
        details.append("Semester: ").append(selected.getSemester()).append("\n");
        details.append("Credits: ").append(selected.getCredits()).append("\n");
        details.append("Teacher: ").append(getTeacherName(selected.getTeacherId())).append("\n");
        details.append("Enrollment: ").append(selected.getCurrentEnrollment())
                .append("/").append(selected.getMaxStudents()).append("\n");
        details.append("Schedule: ").append(selected.getSchedule()).append("\n");
        details.append("Classroom: ").append(selected.getClassroom()).append("\n");
        details.append("Start Date: ").append(selected.getStartDate()).append("\n");
        details.append("End Date: ").append(selected.getEndDate()).append("\n");
        details.append("Status: ").append(selected.isActive() ? "Active" : "Inactive").append("\n");

        detailsArea.setText(details.toString());

        dialog.getDialogPane().setContent(detailsArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private String getTeacherName(String teacherId) {
        return userService.getUserById(teacherId)
                .map(user -> user.getFullName())
                .orElse("Unknown Teacher");
    }

    private boolean hasPermission(String permission) {
        if (currentUser.getRole() == UserRole.ADMIN
                || currentUser.getRole() == UserRole.SUPER_ADMIN) {
            return true;
        }

        if (currentUser instanceof com.arms.domain.Admin admin) {
            return admin.hasPermission(permission);
        }

        return false;
    }
}
