package com.arms.gui.components;

import java.time.format.DateTimeFormatter;

import org.controlsfx.control.GridCell;

import com.arms.domain.Grade;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class GradeCard extends GridCell<Grade> {

    private final VBox container;
    private final Label assignmentLabel;
    private final Label scoreLabel;
    private final Label percentageLabel;
    private final Label gradeLabel;
    private final Label dateLabel;
    private final Label feedbackLabel;

    private static final DateTimeFormatter DATE_FORMATTER
            = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public GradeCard() {
        container = new VBox(6);
        container.setPadding(new Insets(10));
        container.setAlignment(Pos.TOP_LEFT);
        container.getStyleClass().add("grade-card");

        assignmentLabel = new Label();
        assignmentLabel.setFont(Font.font(13));
        assignmentLabel.setWrapText(true);
        assignmentLabel.getStyleClass().add("grade-assignment");

        HBox scoreBox = new HBox(10);
        scoreBox.setAlignment(Pos.CENTER_LEFT);

        scoreLabel = new Label();
        scoreLabel.setFont(Font.font(12));
        scoreLabel.getStyleClass().add("grade-score");

        percentageLabel = new Label();
        percentageLabel.setFont(Font.font(11));
        percentageLabel.getStyleClass().add("grade-percentage");

        gradeLabel = new Label();
        gradeLabel.setFont(Font.font(14));
        gradeLabel.getStyleClass().add("grade-letter");

        dateLabel = new Label();
        dateLabel.setFont(Font.font(10));
        dateLabel.getStyleClass().add("grade-date");

        feedbackLabel = new Label();
        feedbackLabel.setFont(Font.font(11));
        feedbackLabel.setWrapText(true);
        feedbackLabel.getStyleClass().add("grade-feedback");

        scoreBox.getChildren().addAll(scoreLabel, percentageLabel, gradeLabel);
        container.getChildren().addAll(assignmentLabel, scoreBox, dateLabel, feedbackLabel);
    }

    @Override
    protected void updateItem(Grade grade, boolean empty) {
        super.updateItem(grade, empty);

        if (empty || grade == null) {
            setGraphic(null);
        } else {
            assignmentLabel.setText(grade.getAssignmentId() != null ? grade.getAssignmentId() : "Assignment");
            scoreLabel.setText(String.format("Score: %.1f/%.1f", grade.getScore(), grade.getMaxScore()));
            percentageLabel.setText(String.format("(%.1f%%)", grade.getPercentage()));
            String letter = grade.getLetterGrade() != null ? grade.getLetterGrade() : "-";
            gradeLabel.setText(letter);
            dateLabel.setText("Graded: " + (grade.getGradedAt() != null
                    ? grade.getGradedAt().format(DATE_FORMATTER) : "Not graded"));
            feedbackLabel.setText(grade.getFeedback() != null
                    ? "Feedback: " + grade.getFeedback() : "No feedback provided");

            // Update background based on grade
            Color backgroundColor;
            switch (letter) {
                case "A":
                    backgroundColor = Color.LIGHTGREEN;
                    break;
                case "B":
                    backgroundColor = Color.LIGHTBLUE;
                    break;
                case "C":
                    backgroundColor = Color.LIGHTYELLOW;
                    break;
                case "D":
                    backgroundColor = Color.LIGHTSALMON;
                    break;
                case "F":
                    backgroundColor = Color.LIGHTCORAL;
                    break;
                default:
                    backgroundColor = Color.LIGHTGRAY;
            }

            container.setBackground(new Background(new BackgroundFill(
                    backgroundColor, new CornerRadii(6), Insets.EMPTY)));

            if (!grade.isPublished()) {
                container.setOpacity(0.6);
            } else {
                container.setOpacity(1.0);
            }

            setGraphic(container);
        }
    }
}
