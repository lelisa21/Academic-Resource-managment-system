package com.arms.gui.dialogs;

import java.time.LocalDateTime;

import com.arms.domain.Assignment;
import com.arms.domain.Course;
import com.arms.domain.enums.AssignmentType;
import com.arms.gui.util.AlertHelper;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class AddAssignmentDialog extends Dialog<Assignment> {

    private final ComboBox<Course> courseComboBox;
    private final TextField titleField;
    private final TextArea descriptionArea;
    private final ComboBox<AssignmentType> typeComboBox;
    private final TextField maxScoreField;
    private final TextField weightField;
    private final DatePicker dueDatePicker;
    private final Spinner<Integer> dueHourSpinner;
    private final Spinner<Integer> dueMinuteSpinner;

    public AddAssignmentDialog(ObservableList<Course> courses) {
        setTitle("Create Assignment");
        setHeaderText("Enter assignment details");

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Form fields
        courseComboBox = new ComboBox<>(courses);
        courseComboBox.setPromptText("Select Course");

        titleField = new TextField();
        titleField.setPromptText("Assignment Title");

        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Assignment description and instructions");
        descriptionArea.setPrefRowCount(4);

        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(AssignmentType.values());
        typeComboBox.setValue(AssignmentType.HOMEWORK);

        maxScoreField = new TextField();
        maxScoreField.setPromptText("Maximum score");
        maxScoreField.setText("100");

        weightField = new TextField();
        weightField.setPromptText("Weight in percentage");
        weightField.setText("10");

        dueDatePicker = new DatePicker();
        dueDatePicker.setValue(LocalDateTime.now().plusDays(7).toLocalDate());

        dueHourSpinner = new Spinner<>(0, 23, 23);
        dueHourSpinner.setEditable(true);

        dueMinuteSpinner = new Spinner<>(0, 59, 59);
        dueMinuteSpinner.setEditable(true);

        // Add fields to grid
        int row = 0;
        grid.add(new Label("Course:*"), 0, row);
        grid.add(courseComboBox, 1, row++);

        grid.add(new Label("Title:*"), 0, row);
        grid.add(titleField, 1, row++);

        grid.add(new Label("Description:"), 0, row);
        grid.add(descriptionArea, 1, row++);

        grid.add(new Label("Type:*"), 0, row);
        grid.add(typeComboBox, 1, row++);

        grid.add(new Label("Max Score:*"), 0, row);
        grid.add(maxScoreField, 1, row++);

        grid.add(new Label("Weight (%):*"), 0, row);
        grid.add(weightField, 1, row++);

        grid.add(new Label("Due Date:*"), 0, row);
        grid.add(dueDatePicker, 1, row++);

        grid.add(new Label("Due Time:*"), 0, row);

        HBox timeBox = new HBox(5);
        timeBox.getChildren().addAll(
                new Label("Hour:"), dueHourSpinner,
                new Label("Minute:"), dueMinuteSpinner
        );
        grid.add(timeBox, 1, row++);

        // Set up buttons
        ButtonType createButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        getDialogPane().setContent(grid);

        // Convert result to Assignment object
        setResultConverter(dialogButton -> {
            if (dialogButton == createButton) {
                return createAssignmentFromInput();
            }
            return null;
        });

        // Set cell factory for course combo box
        courseComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? ""
                        : course.getCourseCode() + " - " + course.getTitle());
            }
        });

        courseComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                setText(empty || course == null ? ""
                        : course.getCourseCode() + " - " + course.getTitle());
            }
        });
    }

    private Assignment createAssignmentFromInput() {
        // Validate required fields
        if (courseComboBox.getValue() == null
                || titleField.getText().isEmpty()
                || maxScoreField.getText().isEmpty()
                || weightField.getText().isEmpty()
                || dueDatePicker.getValue() == null) {

            AlertHelper.showError("Missing Information",
                    "Please fill in all required fields (marked with *).");
            return null;
        }

        try {
            // Create due date time
            LocalDateTime dueDateTime = LocalDateTime.of(
                    dueDatePicker.getValue(),
                    java.time.LocalTime.of(dueHourSpinner.getValue(), dueMinuteSpinner.getValue())
            );

            Assignment assignment = new Assignment();
            assignment.setCourseId(courseComboBox.getValue().getId());
            assignment.setTitle(titleField.getText());
            assignment.setDescription(descriptionArea.getText());
            assignment.setType(typeComboBox.getValue());
            assignment.setMaxScore(Double.parseDouble(maxScoreField.getText()));
            assignment.setWeight(Double.parseDouble(weightField.getText()));
            assignment.setDueDate(dueDateTime);
            return assignment;

        } catch (NumberFormatException e) {
            AlertHelper.showError("Invalid Number",
                    "Please enter valid numbers for score and weight.");
            return null;
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to create assignment: " + e.getMessage());
            return null;
        }
    }
}
