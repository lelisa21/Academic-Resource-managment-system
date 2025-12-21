package com.arms.gui.dialogs;

import java.util.List;
import java.util.Optional;

import com.arms.domain.Assignment;
import com.arms.domain.Grade;
import com.arms.domain.Student;
import com.arms.service.AssignmentService;
import com.arms.service.GradeService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class GradeSubmissionDialog extends Dialog<Void> {

    private final Assignment assignment;
    private final TableView<Student> submissionsTable;
    private final ObservableList<Student> submissions;

    public GradeSubmissionDialog(Assignment assignment) {
        this.assignment = assignment;

        setTitle("Grade Submissions");
        setHeaderText("Grade submissions for: " + assignment.getTitle());

        // Load submissions
        submissions = FXCollections.observableArrayList(
                AssignmentService.getInstance().getStudentsWhoSubmitted(assignment.getId())
        );

        // Create table
        submissionsTable = new TableView<>(submissions);

        TableColumn<Student, String> nameColumn = new TableColumn<>("Student");
        nameColumn.setCellValueFactory(cellData
                -> new SimpleStringProperty(
                        cellData.getValue().getFullName()
                ));

        TableColumn<Student, String> idColumn = new TableColumn<>("Student ID");
        idColumn.setCellValueFactory(cellData
                -> new SimpleStringProperty(
                        cellData.getValue().getStudentId()
                ));

        TableColumn<Student, String> gradeColumn = new TableColumn<>("Grade");
        gradeColumn.setCellFactory(col -> new TableCell<>() {
            private final TextField gradeField = new TextField();
            private final Button gradeButton = new Button("Grade");

            {
                gradeButton.setOnAction(event -> {
                    Student student = getTableView().getItems().get(getIndex());
                    gradeStudent(student, gradeField.getText());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().isEmpty()) {
                    setGraphic(null);
                } else {
                    Student student = getTableView().getItems().get(getIndex());
                    // Check if already graded
                    Optional<Grade> existingGrade = GradeService.getInstance()
                            .getGradesByAssignment(assignment.getId()).stream()
                            .filter(g -> g.getStudentId().equals(student.getId()))
                            .findFirst();

                    if (existingGrade.isPresent()) {
                        gradeField.setText(String.valueOf(existingGrade.get().getScore()));
                        gradeField.setDisable(true);
                        gradeButton.setText("Update");
                    } else {
                        gradeField.clear();
                        gradeField.setDisable(false);
                        gradeButton.setText("Grade");
                    }

                    setGraphic(new VBox(5, gradeField, gradeButton));
                }
            }
        });

        submissionsTable.getColumns().addAll(nameColumn, idColumn, gradeColumn);
        submissionsTable.setPrefHeight(400);

        // Bulk grading controls
        GridPane bulkControls = new GridPane();
        bulkControls.setHgap(10);
        bulkControls.setVgap(10);
        bulkControls.setPadding(new Insets(10));

        TextField bulkGradeField = new TextField();
        bulkGradeField.setPromptText("Enter grade for all");

        Button bulkGradeButton = new Button("Grade All");
        bulkGradeButton.setOnAction(event -> {
            try {
                double grade = Double.parseDouble(bulkGradeField.getText());
                if (grade < 0 || grade > assignment.getMaxScore()) {
                    showAlert("Invalid Grade",
                            "Grade must be between 0 and " + assignment.getMaxScore());
                    return;
                }

                for (Student student : submissions) {
                    gradeStudent(student, String.valueOf(grade));
                }

                showAlert("Success", "All submissions graded with score: " + grade);

            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number for grade.");
            }
        });

        bulkControls.add(new Label("Bulk Grade:"), 0, 0);
        bulkControls.add(bulkGradeField, 1, 0);
        bulkControls.add(bulkGradeButton, 2, 0);

        // Main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));
        mainLayout.getChildren().addAll(submissionsTable, bulkControls);

        getDialogPane().setContent(mainLayout);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Update table when dialog is shown
        setOnShown(event -> refreshSubmissions());
    }

    private void gradeStudent(Student student, String gradeStr) {
        try {
            double grade = Double.parseDouble(gradeStr);
            if (grade < 0 || grade > assignment.getMaxScore()) {
                showAlert("Invalid Grade",
                        "Grade must be between 0 and " + assignment.getMaxScore());
                return;
            }

            // Create or update grade
            Grade newGrade = new Grade(
                    null,
                    student.getId(),
                    assignment.getCourseId(),
                    assignment.getId(),
                    grade,
                    assignment.getMaxScore(),
                    0.0,
                    null,
                    "",
                    "Teacher",
                    java.time.LocalDateTime.now(),
                    false
            );

            boolean success = ((Optional<Grade>) GradeService.getInstance()
                    .createOrUpdateGrade(newGrade))
                    .isPresent();

            if (success) {
                showAlert("Success",
                        "Graded " + student.getFullName() + " with score: " + grade);
                refreshSubmissions();
            } else {
                showAlert("Error", "Failed to grade student.");
            }

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for grade.");
        }
    }

    private void refreshSubmissions() {
        List<Student> updatedSubmissions = AssignmentService.getInstance()
                .getStudentsWhoSubmitted(assignment.getId());
        submissions.setAll(updatedSubmissions);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
