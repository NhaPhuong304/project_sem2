package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.model.UserRole;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class TeacherCreateDialogController {

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Teacher", "Staff");
        roleComboBox.setValue("Teacher");
    }

    public String getUsername() {
        return usernameField.getText() != null ? usernameField.getText().trim() : "";
    }

    public String getFullName() {
        return fullNameField.getText() != null ? fullNameField.getText().trim() : "";
    }

    public String getEmail() {
        return emailField.getText() != null ? emailField.getText().trim() : "";
    }

    public UserRole getRole() {
        if ("Staff".equals(roleComboBox.getValue())) {
            return UserRole.STAFF;
        }
        return UserRole.TEACHER;
    }
}
