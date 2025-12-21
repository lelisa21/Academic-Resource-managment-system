package com.arms.gui.dialogs;

import java.time.format.DateTimeFormatter;

import com.arms.domain.Assignment;
import com.arms.domain.Student;
import com.arms.gui.util.AlertHelper;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class AssignmentSubmissionDialog extends Dialog<Double> {

    private final Assignment assignment;
    private final Student student;
    private final TextArea submissionText;
    private final TextField scoreField;

    private static final DateTimeFormatter DATE_FORMATTER
            = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public AssignmentSubmissionDialog(Assignment assignment, Student student) {
        this.assignment = assignment;
        this.student = student;

        setTitle("Submit Assignment");
        setHeaderText("Submit: " + assignment.getTitle());

        // Create layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Assignment info
        Label infoLabel = new Label();
        infoLabel.setText(
                "Assignment: " + assignment.getTitle() + "\n"
                + "Due Date: " + assignment.getDueDate().format(DATE_FORMATTER) + "\n"
                + "Max Score: " + assignment.getMaxScore() + "\n"
                + "Status: " + (assignment.isOverdue() ? "OVERDUE" : "Active")
        );

        // Submission text area
        Label submissionLabel = new Label("Your Submission:");
        submissionText = new TextArea();
        submissionText.setPromptText("Enter your submission here...");
        submissionText.setWrapText(true);
        submissionText.setPrefRowCount(10);

        // For self-scoring or practice assignments
        Label scoreLabel = new Label("Self-Assessed Score (optional):");
        scoreField = new TextField();
        scoreField.setPromptText("0-" + assignment.getMaxScore());

        // File upload section (simplified)
        Label fileLabel = new Label("Attach Files:");
        Button uploadButton = new Button("Choose File");
        Label fileNameLabel = new Label("No file chosen");

        uploadButton.setOnAction(e -> {
            // In a real implementation, this would open a file chooser
            fileNameLabel.setText("file_uploaded.pdf");
        });

        // Add components to grid
        int row = 0;
        grid.add(infoLabel, 0, row++, 2, 1);

        grid.add(submissionLabel, 0, row);
        grid.add(submissionText, 0, row + 1, 2, 1);
        row += 2;

        grid.add(scoreLabel, 0, row);
        grid.add(scoreField, 1, row++);

        grid.add(fileLabel, 0, row);
        HBox fileBox = new HBox(10, uploadButton, fileNameLabel);
        grid.add(fileBox, 1, row++);

        // Buttons
        ButtonType submitButton = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);

        getDialogPane().setContent(grid);

        // Convert result
        setResultConverter(dialogButton -> {
            if (dialogButton == submitButton) {
                return validateAndGetScore();
            }
            return null;
        });
    }

    private Double validateAndGetScore() {
        // Validate submission text
        if (submissionText.getText().trim().isEmpty()) {
            AlertHelper.showError("Invalid Submission",
                    "Please enter your submission text.");
            return null;
        }

        // Validate score if provided
        String scoreText = scoreField.getText().trim();
        if (!scoreText.isEmpty()) {
            try {
                double score = Double.parseDouble(scoreText);
                if (score < 0 || score > assignment.getMaxScore()) {
                    AlertHelper.showError("Invalid Score",
                            "Score must be between 0 and " + assignment.getMaxScore());
                    return null;
                }
                return score;
            } catch (NumberFormatException e) {
                AlertHelper.showError("Invalid Score",
                        "Please enter a valid number for score.");
                return null;
            }
        }

        return 0.0; // Default score if not provided
    }
}
