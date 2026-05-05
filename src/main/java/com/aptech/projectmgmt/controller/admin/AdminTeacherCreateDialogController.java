package com.aptech.projectmgmt.controller.admin;

import com.aptech.projectmgmt.model.Staff;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AdminTeacherCreateDialogController {

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;

    public void setData(Staff teacher) {
        if (teacher == null) {
            return;
        }

        usernameField.setText(teacher.getUsername());
        fullNameField.setText(teacher.getFullName());
        emailField.setText(teacher.getEmail());
        usernameField.setEditable(false);
        usernameField.setDisable(true);
    }

    public String getUsername() {
        return usernameField.getText() != null ? usernameField.getText().trim() : "";
    }

    public String getFullName() {
        return fullNameField.getText() != null ? fullNameField.getText().trim() : "";
    }
    public String validate() {

        if (getUsername().isEmpty()) {
            return "Username is required";
        }

        if (getFullName().isEmpty()) {
            return "Full name is required";
        }

        if (getEmail().isEmpty()) {
            return "Email is required";
        }
        if (!getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return "Invalid email format";
        }

        return null;
    }

    public String getEmail() {
        return emailField.getText() != null ? emailField.getText().trim() : "";
    }
}