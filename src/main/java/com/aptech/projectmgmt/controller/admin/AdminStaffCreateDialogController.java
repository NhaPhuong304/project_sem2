package com.aptech.projectmgmt.controller.admin;

import com.aptech.projectmgmt.model.Staff;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AdminStaffCreateDialogController {

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;

    public void setData(Staff staff) {
        if (staff == null) {
            return;
        }

        usernameField.setText(staff.getUsername());
        fullNameField.setText(staff.getFullName());
        emailField.setText(staff.getEmail());

        usernameField.setEditable(false);
        usernameField.setDisable(true);
    }
    public String validate() {
        if (getFullName() == null || getFullName().trim().isEmpty()) {
            return "Full name is required";
        }

        if (getEmail() == null || getEmail().trim().isEmpty()) {
            return "Email is required";
        }

        if (!getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return "Invalid email format";
        }

        return null;
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
}