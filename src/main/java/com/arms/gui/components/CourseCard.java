package com.arms.gui.components;

import org.controlsfx.control.GridCell;

import com.arms.domain.Course;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class CourseCard extends GridCell<Course> {

    private final VBox container;
    private final Label codeLabel;
    private final Label titleLabel;
    private final Label detailsLabel;
    private final Label enrollmentLabel;

    public CourseCard() {
        container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setAlignment(Pos.TOP_LEFT);
        container.getStyleClass().add("course-card");

        codeLabel = new Label();
        codeLabel.setFont(Font.font(14));
        codeLabel.getStyleClass().add("course-code");

        titleLabel = new Label();
        titleLabel.setFont(Font.font(16));
        titleLabel.setWrapText(true);
        titleLabel.getStyleClass().add("course-title");

        detailsLabel = new Label();
        detailsLabel.setFont(Font.font(12));
        detailsLabel.setWrapText(true);
        detailsLabel.getStyleClass().add("course-details");

        enrollmentLabel = new Label();
        enrollmentLabel.setFont(Font.font(11));
        enrollmentLabel.getStyleClass().add("course-enrollment");

        container.getChildren().addAll(codeLabel, titleLabel, detailsLabel, enrollmentLabel);

        // Set background based on course status
        container.setBackground(new Background(new BackgroundFill(
                Color.LIGHTBLUE, new CornerRadii(8), Insets.EMPTY)));
    }

    @Override
protected void updateItem(Course course, boolean empty) {
    super.updateItem(course, empty);

    if (empty || course == null) {
        setGraphic(null);
        setOnMouseClicked(null);   // important
        return;
    }

    codeLabel.setText(course.getCourseCode() != null ? course.getCourseCode() : "");
    titleLabel.setText(course.getTitle() != null ? course.getTitle() : "(no title)");
    String dept = course.getDepartment() != null ? course.getDepartment() : "";
    detailsLabel.setText((dept.isEmpty() ? "" : dept + " • ") + course.getCredits() + " credits");
    enrollmentLabel.setText(
            "Enrollment: " + course.getCurrentEnrollment() + "/" + course.getMaxStudents()
    );

    if (!course.canEnroll()) {
        container.setBackground(new Background(
                new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(8), Insets.EMPTY)
        ));
        container.setOpacity(0.7);
    } else {
        container.setBackground(new Background(
                new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(8), Insets.EMPTY)
        ));
        container.setOpacity(1.0);
    }
    setOnMouseClicked(event -> {
        if (event.getClickCount() == 2) {
            showCourseDetails(course);
        }
    });

    setGraphic(container);
}

private void showCourseDetails(Course course) {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Course Details");
    dialog.setHeaderText(course.getTitle());

    StringBuilder details = new StringBuilder();
    details.append("Course Code: ").append(course.getCourseCode()).append("\n");
    details.append("Credits: ").append(course.getCredits()).append("\n");
    details.append("Department: ").append(course.getDepartment()).append("\n");
    details.append("Enrollment: ")
           .append(course.getCurrentEnrollment())
           .append("/")
           .append(course.getMaxStudents())
           .append("\n");

    TextArea area = new TextArea(details.toString());
    area.setEditable(false);
    area.setPrefSize(400, 300);

    dialog.getDialogPane().setContent(area);
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    dialog.showAndWait();
}


}
