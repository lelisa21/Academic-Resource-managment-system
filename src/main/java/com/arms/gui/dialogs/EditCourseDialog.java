package com.arms.gui.dialogs;

import com.arms.domain.Course;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class EditCourseDialog extends Dialog<Course> {
    
    public EditCourseDialog(Course course) {
        setTitle("Edit Course");
        setHeaderText("Edit Course Details");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField codeField = new TextField(course.getCourseCode());
        TextField titleField = new TextField(course.getTitle());
        TextField deptField = new TextField(course.getDepartment());
        TextField semesterField = new TextField(course.getSemester());
        Spinner<Integer> creditsSpinner = new Spinner<>(1, 10, course.getCredits());
        Spinner<Integer> capacitySpinner = new Spinner<>(10, 200, course.getMaxStudents());
        TextArea descArea = new TextArea(course.getDescription());
        descArea.setPrefRowCount(4);
        
        CheckBox activeCheck = new CheckBox("Active");
        activeCheck.setSelected(course.isActive());
        
        grid.add(new Label("Course Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Department:"), 0, 2);
        grid.add(deptField, 1, 2);
        grid.add(new Label("Semester:"), 0, 3);
        grid.add(semesterField, 1, 3);
        grid.add(new Label("Credits:"), 0, 4);
        grid.add(creditsSpinner, 1, 4);
        grid.add(new Label("Max Capacity:"), 0, 5);
        grid.add(capacitySpinner, 1, 5);
        grid.add(new Label("Description:"), 0, 6);
        grid.add(descArea, 1, 6);
        grid.add(activeCheck, 1, 7);
        
        getDialogPane().setContent(grid);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                course.setCourseCode(codeField.getText());
                course.setTitle(titleField.getText());
                course.setDepartment(deptField.getText());
                course.setSemester(semesterField.getText());
                course.setCredits(creditsSpinner.getValue());
                course.setMaxStudents(course.getMaxStudents());
                course.setDescription(descArea.getText());
                course.setActive(activeCheck.isSelected());
                return course;
            }
            return null;
        });
    }
}
