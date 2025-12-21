package com.arms.gui.dialogs;

import java.util.Optional;

import com.arms.domain.Assignment;
import com.arms.domain.Student;
import com.arms.domain.Teacher;
import com.arms.gui.util.AlertHelper;
import com.arms.service.AssignmentService;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class GradeDialog {

    private static final AssignmentService assignmentService = AssignmentService.getInstance();

    public static void showDialog(Assignment assignment, Student student, Teacher grader) {
        if (assignment == null || student == null || grader == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Grade Assignment: " + assignment.getTitle());
        dialog.setHeaderText("Enter marks and comments for " + student.getFullName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField marksField = new TextField();
        marksField.setPromptText("Marks");

        TextField commentField = new TextField();
        commentField.setPromptText("Comment");

        grid.add(marksField, 0, 0);
        grid.add(commentField, 0, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                double marks = Double.parseDouble(marksField.getText());
                String comment = commentField.getText();

                assignmentService.gradeAssignment(
                        assignment.getId(),
                        student.getId(),
                        marks,
                        comment,
                        grader.getId()
                );

                AlertHelper.showInfo("Graded", "Assignment graded successfully!");
            } catch (NumberFormatException e) {
                AlertHelper.showError("Invalid Input", "Please enter a valid number for marks.");
            } catch (Exception e) {
                AlertHelper.showError("Error", "Failed to grade assignment: " + e.getMessage());
            }
        }
    }
}
