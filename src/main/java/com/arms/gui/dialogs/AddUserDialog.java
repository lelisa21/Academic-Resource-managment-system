package com.arms.gui.dialogs;

import com.arms.domain.Admin;
import com.arms.domain.Student;
import com.arms.domain.Teacher;
import com.arms.domain.User;
import com.arms.domain.enums.UserRole;
import com.arms.domain.enums.UserStatus;
import com.arms.gui.util.AlertHelper;
import com.arms.gui.util.ValidationHelper;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class AddUserDialog extends Dialog<User> {

    private final TextField firstNameField;
    private final TextField lastNameField;
    private final TextField usernameField;
    private final TextField emailField;
    private final TextField phoneField;
    private final ComboBox<UserRole> roleComboBox;
    private final TextField studentIdField;
    private final TextField employeeIdField;
    private final TextField departmentField;
    private final TextField semesterField;
    private final TextField qualificationField;
    private final TextField experienceField;

    public AddUserDialog() {
        setTitle("Add New User");
        setHeaderText("Enter user details");

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Basic fields
        firstNameField = new TextField();
        firstNameField.setPromptText("First Name");

        lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");

        usernameField = new TextField();
        usernameField.setPromptText("Username");

        emailField = new TextField();
        emailField.setPromptText("Email");

        phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(UserRole.values());
        roleComboBox.setValue(UserRole.STUDENT);

        // Role-specific fields
        studentIdField = new TextField();
        studentIdField.setPromptText("Student ID");
        studentIdField.setVisible(false);

        employeeIdField = new TextField();
        employeeIdField.setPromptText("Employee ID");
        employeeIdField.setVisible(false);

        departmentField = new TextField();
        departmentField.setPromptText("Department");

        semesterField = new TextField();
        semesterField.setPromptText("Semester");
        semesterField.setVisible(false);

        qualificationField = new TextField();
        qualificationField.setPromptText("Qualification");
        qualificationField.setVisible(false);

        experienceField = new TextField();
        experienceField.setPromptText("Years of Experience");
        experienceField.setVisible(false);

        // Add fields to grid
        int row = 0;
        grid.add(new Label("First Name:"), 0, row);
        grid.add(firstNameField, 1, row++);

        grid.add(new Label("Last Name:"), 0, row);
        grid.add(lastNameField, 1, row++);

        grid.add(new Label("Username:"), 0, row);
        grid.add(usernameField, 1, row++);

        grid.add(new Label("Email:"), 0, row);
        grid.add(emailField, 1, row++);

        grid.add(new Label("Phone:"), 0, row);
        grid.add(phoneField, 1, row++);

        grid.add(new Label("Role:"), 0, row);
        grid.add(roleComboBox, 1, row++);

        grid.add(new Label("Department:"), 0, row);
        grid.add(departmentField, 1, row++);

        // Role-specific rows
        grid.add(studentIdField, 1, row);
        studentIdField.setManaged(false);

        grid.add(employeeIdField, 1, row + 1);
        employeeIdField.setManaged(false);

        grid.add(semesterField, 1, row + 2);
        semesterField.setManaged(false);

        grid.add(qualificationField, 1, row + 3);
        qualificationField.setManaged(false);

        grid.add(experienceField, 1, row + 4);
        experienceField.setManaged(false);

        // Role change listener
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateRoleSpecificFields(newVal);
        });

        // Set up buttons
        ButtonType addButton = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        getDialogPane().setContent(grid);

        // Convert result to User object
        setResultConverter(dialogButton -> {
            if (dialogButton == addButton) {
                return createUserFromInput();
            }
            return null;
        });

        // Initial update
        updateRoleSpecificFields(roleComboBox.getValue());
    }

    private void updateRoleSpecificFields(UserRole role) {
        boolean isStudent = role == UserRole.STUDENT;
        boolean isTeacher = role == UserRole.TEACHER;

        studentIdField.setVisible(isStudent);
        studentIdField.setManaged(isStudent);

        employeeIdField.setVisible(isTeacher);
        employeeIdField.setManaged(isTeacher);

        semesterField.setVisible(isStudent);
        semesterField.setManaged(isStudent);

        qualificationField.setVisible(isTeacher);
        qualificationField.setManaged(isTeacher);

        experienceField.setVisible(isTeacher);
        experienceField.setManaged(isTeacher);
    }

    private User createUserFromInput() {
        // Validate inputs
        if (!ValidationHelper.isValidEmail(emailField.getText())) {
            AlertHelper.showError("Invalid Email", "Please enter a valid email address.");
            return null;
        }

        if (!ValidationHelper.isValidUsername(usernameField.getText())) {
            AlertHelper.showError("Invalid Username",
                    "Username must be 3-20 characters and contain only letters, numbers, and underscores.");
            return null;
        }

        UserRole role = roleComboBox.getValue();

        switch (role) {
            case STUDENT:
                Student student = new Student();
                student.setFirstName(firstNameField.getText());
                student.setLastName(lastNameField.getText());
                student.setUsername(usernameField.getText());
                student.setEmail(emailField.getText());
                student.setPhoneNumber(phoneField.getText());
                student.setRole(role);
                student.setStatus(UserStatus.ACTIVE);
                student.setDepartment(departmentField.getText());
                student.setSemester(Integer.parseInt(semesterField.getText()));
                student.setStudentId(studentIdField.getText());
                student.setCgpa(0.0);
                student.setCreditsCompleted(0);
                return student;

            case TEACHER:
                Teacher teacher = new Teacher();
                teacher.setFirstName(firstNameField.getText());
                teacher.setLastName(lastNameField.getText());
                teacher.setUsername(usernameField.getText());
                teacher.setEmail(emailField.getText());
                teacher.setPhoneNumber(phoneField.getText());
                teacher.setRole(role);
                teacher.setStatus(UserStatus.ACTIVE);
                teacher.setDepartment(departmentField.getText());
                teacher.setEmployeeId(employeeIdField.getText());
                teacher.setQualification(qualificationField.getText());
                teacher.setYearsOfExperience(Integer.parseInt(experienceField.getText()));
                return teacher;

            case ADMIN:
            case SUPER_ADMIN:
                Admin admin = new Admin();
                admin.setFirstName(firstNameField.getText());
                admin.setLastName(lastNameField.getText());
                admin.setUsername(usernameField.getText());
                admin.setEmail(emailField.getText());
                admin.setPhoneNumber(phoneField.getText());
                admin.setRole(role);
                admin.setStatus(UserStatus.ACTIVE);
                admin.setAdminId("ADM-" + System.currentTimeMillis());
                admin.setAccessLevel(role == UserRole.SUPER_ADMIN ? "SUPER" : "STANDARD");
                return admin;

            default:
                return null;
        }
    }
}
