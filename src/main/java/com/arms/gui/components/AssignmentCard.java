package com.arms.gui.components;

import java.time.format.DateTimeFormatter;

import org.controlsfx.control.GridCell;

import com.arms.domain.Assignment;
import com.arms.domain.enums.AssignmentStatus;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class AssignmentCard extends GridCell<Assignment> {

    private final VBox container;
    private final Label titleLabel;
    private final Label typeLabel;
    private final Label dueDateLabel;
    private final Label statusLabel;
    private final Label scoreLabel;

    private static final DateTimeFormatter DATE_FORMATTER
            = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public AssignmentCard() {
        container = new VBox(8);
        container.setPadding(new Insets(12));
        container.setAlignment(Pos.TOP_LEFT);
        container.getStyleClass().add("assignment-card");

        titleLabel = new Label();
        titleLabel.setFont(Font.font(14));
        titleLabel.setWrapText(true);
        titleLabel.getStyleClass().add("assignment-title");

        typeLabel = new Label();
        typeLabel.setFont(Font.font(11));
        typeLabel.getStyleClass().add("assignment-type");

        dueDateLabel = new Label();
        dueDateLabel.setFont(Font.font(11));
        dueDateLabel.getStyleClass().add("assignment-due");

        statusLabel = new Label();
        statusLabel.setFont(Font.font(11));
        statusLabel.getStyleClass().add("assignment-status");

        scoreLabel = new Label();
        scoreLabel.setFont(Font.font(12));
        scoreLabel.getStyleClass().add("assignment-score");

        container.getChildren().addAll(titleLabel, typeLabel, dueDateLabel, statusLabel, scoreLabel);
    }

    @Override
    protected void updateItem(Assignment assignment, boolean empty) {
        super.updateItem(assignment, empty);

        if (empty || assignment == null) {
            setGraphic(null);
        } else {
            titleLabel.setText(assignment.getTitle() != null ? assignment.getTitle() : "(no title)");
            typeLabel.setText("Type: " + (assignment.getType() != null ? assignment.getType().name() : "N/A"));
            dueDateLabel.setText("Due: " + (assignment.getDueDate() != null ? assignment.getDueDate().format(DATE_FORMATTER) : "--"));
            statusLabel.setText("Status: " + (assignment.getStatus() != null ? assignment.getStatus().name() : "UNKNOWN"));
            scoreLabel.setText("Max Score: " + assignment.getMaxScore());

            // Update colors based on status and due date
            if (assignment.isOverdue()) {
                container.setBackground(new Background(new BackgroundFill(
                        Color.LIGHTCORAL, new CornerRadii(6), Insets.EMPTY)));
            } else if (AssignmentStatus.ACTIVE.equals(assignment.getStatus())) {
                container.setBackground(new Background(new BackgroundFill(
                        Color.LIGHTGREEN, new CornerRadii(6), Insets.EMPTY)));
            } else {
                container.setBackground(new Background(new BackgroundFill(
                        Color.LIGHTGRAY, new CornerRadii(6), Insets.EMPTY)));
            }

            setGraphic(container);
        }
    }
}
