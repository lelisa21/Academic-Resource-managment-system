package com.arms.gui.dialogs;

import java.time.LocalDate;

import com.arms.domain.Course;
import com.arms.gui.util.AlertHelper;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class AddCourseDialog extends Dialog<Course> {

    private final TextField courseCodeField;
    private final TextField titleField;
    private final TextArea descriptionArea;
    private final TextField creditsField;
    private final TextField departmentField;
    private final TextField semesterField;
    // private final ComboBox<Teacher> teacherComboBox;
    private final TextField maxStudentsField;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final TextField scheduleField;
    private final TextField classroomField;

    public AddCourseDialog() {
        setTitle("Add New Course");
        setHeaderText("Enter course details");

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Form fields
        courseCodeField = new TextField();
        courseCodeField.setPromptText("e.g., CS-101");

        titleField = new TextField();
        titleField.setPromptText("Course Title");

        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Course description");
        descriptionArea.setPrefRowCount(3);

        creditsField = new TextField();
        creditsField.setPromptText("Credit hours");

        departmentField = new TextField();
        departmentField.setPromptText("Department");

        semesterField = new TextField();
        semesterField.setPromptText("Semester");

        // teacherComboBox = new ComboBox<>();
        // loadTeachers();

        maxStudentsField = new TextField();
        maxStudentsField.setPromptText("Maximum students");

        startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate.now());

        endDatePicker = new DatePicker();
        endDatePicker.setValue(LocalDate.now().plusMonths(4));

        scheduleField = new TextField();
        scheduleField.setPromptText("e.g., Mon/Wed 10:00-11:30");

        classroomField = new TextField();
        classroomField.setPromptText("Room number");

        // Add fields to grid
        int row = 0;
        grid.add(new Label("Course Code:*"), 0, row);
        grid.add(courseCodeField, 1, row++);

        grid.add(new Label("Title:*"), 0, row);
        grid.add(titleField, 1, row++);

        grid.add(new Label("Description:"), 0, row);
        grid.add(descriptionArea, 1, row++);

        grid.add(new Label("Credits:*"), 0, row);
        grid.add(creditsField, 1, row++);

        grid.add(new Label("Department:*"), 0, row);
        grid.add(departmentField, 1, row++);

        grid.add(new Label("Semester:*"), 0, row);
        grid.add(semesterField, 1, row++);

        // grid.add(new Label("Teacher:*"), 0, row);
        // grid.add(teacherComboBox, 1, row++);

        grid.add(new Label("Max Students:*"), 0, row);
        grid.add(maxStudentsField, 1, row++);

        grid.add(new Label("Start Date:*"), 0, row);
        grid.add(startDatePicker, 1, row++);

        grid.add(new Label("End Date:*"), 0, row);
        grid.add(endDatePicker, 1, row++);

        grid.add(new Label("Schedule:*"), 0, row);
        grid.add(scheduleField, 1, row++);

        grid.add(new Label("Classroom:"), 0, row);
        grid.add(classroomField, 1, row++);

        // Set up buttons
        ButtonType addButton = new ButtonType("Add Course", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        getDialogPane().setContent(grid);

        // Convert result to Course object
        setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                return createCourseFromInput();
            }
            return null;
        });
    }

    // private void loadTeachers() {
    //     ObservableList<Teacher> teachers = FXCollections.observableArrayList(
    //             UserService.getInstance().getAllTeachers()
    //     );
    //     teacherComboBox.setItems(teachers);

    //     // Set cell factory to display teacher name
    //     teacherComboBox.setCellFactory(lv -> new ListCell<>() {
    //         @Override
    //         protected void updateItem(Teacher teacher, boolean empty) {
    //             super.updateItem(teacher, empty);
    //             setText(empty || teacher == null ? "" : teacher.getFullName());
    //         }
    //     });

    //     teacherComboBox.setButtonCell(new ListCell<>() {
    //         @Override
    //         protected void updateItem(Teacher teacher, boolean empty) {
    //             super.updateItem(teacher, empty);
    //             setText(empty || teacher == null ? "" : teacher.getFullName());
    //         }
    //     });

    //     if (!teachers.isEmpty()) {
    //         teacherComboBox.setValue(teachers.get(0));
    //     }
    // }

    private Course createCourseFromInput() {
        // Validate required fields
        if (courseCodeField.getText().isEmpty()
                || titleField.getText().isEmpty()
                || creditsField.getText().isEmpty()
                || departmentField.getText().isEmpty()
                || semesterField.getText().isEmpty()
                // || teacherComboBox.getValue() == null
                || maxStudentsField.getText().isEmpty()
                || startDatePicker.getValue() == null
                || endDatePicker.getValue() == null
                || scheduleField.getText().isEmpty()) {

            AlertHelper.showError("Missing Information",
                    "Please fill in all required fields (marked with *).");
            return null;
        }

        try {
            Course course = new Course();
            course.setCourseCode(courseCodeField.getText());
            course.setTitle(titleField.getText());
            course.setDescription(descriptionArea.getText());
            course.setCredits(Integer.parseInt(creditsField.getText()));
            course.setDepartment(departmentField.getText());
            course.setSemester(semesterField.getText());
            // course.setTeacherId(teacherComboBox.getValue().getId());
            course.setMaxStudents(Integer.parseInt(maxStudentsField.getText()));
            course.setCurrentEnrollment(0);
            course.setStartDate(startDatePicker.getValue());
            course.setEndDate(endDatePicker.getValue());
            course.setSchedule(scheduleField.getText());
            course.setClassroom(classroomField.getText());
            course.setActive(true);
            return course;

        } catch (NumberFormatException e) {
            AlertHelper.showError("Invalid Number",
                    "Please enter valid numbers for credits and max students.");
            return null;
        }
    }
}
