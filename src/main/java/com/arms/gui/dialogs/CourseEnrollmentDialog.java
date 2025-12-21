package com.arms.gui.dialogs;

import com.arms.domain.Course;
import com.arms.domain.Student;
import com.arms.service.CourseService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

public class CourseEnrollmentDialog extends Dialog<Course> {
    
    private final Student student;
    private final ObservableList<Course> availableCourses;
    private final ListView<Course> courseListView;
    
    public CourseEnrollmentDialog(Student student) {
        this.student = student;
        this.availableCourses = FXCollections.observableArrayList();
        
        setTitle("Enroll in Course");
        setHeaderText("Select a course to enroll in");
        
        // Create layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // Course list
        Label instructionLabel = new Label("Available courses for enrollment:");
        courseListView = new ListView<>(availableCourses);
        courseListView.setPrefSize(400, 300);
        courseListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                if (empty || course == null) {
                    setText(null);
                } else {
                    setText(course.getCourseCode() + " - " + course.getTitle() + 
                           " (" + course.getDepartment() + ", " + 
                           course.getCurrentEnrollment() + "/" + course.getMaxStudents() + ")");
                    
                    // Disable if course is full
                    setDisable(!course.canEnroll());
                    
                    // Show tooltip with more info
                    Tooltip tooltip = new Tooltip();
                    tooltip.setText("Credits: " + course.getCredits() + 
                                   "\nSchedule: " + course.getSchedule() + 
                                   "\nTeacher: " + course.getTeacherId() + 
                                   "\nStatus: " + (course.canEnroll() ? "Available" : "Full"));
                    setTooltip(tooltip);
                }
            }
        });
        
        // Load available courses
        loadAvailableCourses();
        
        grid.add(instructionLabel, 0, 0);
        grid.add(courseListView, 0, 1);
        
        // Buttons
        ButtonType enrollButton = new ButtonType("Enroll", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(enrollButton, ButtonType.CANCEL);
        
        getDialogPane().setContent(grid);
        
        // Convert result
        setResultConverter(dialogButton -> {
            if (dialogButton == enrollButton) {
                return courseListView.getSelectionModel().getSelectedItem();
            }
            return null;
        });
    }
    
    private void loadAvailableCourses() {
        availableCourses.clear();
        availableCourses.addAll(CourseService.getInstance().getAllCourses());
        
        // Filter out courses student is already enrolled in
        availableCourses.removeIf(course -> 
            student.getEnrolledCourseIds().contains(course.getId()));
    }
}
