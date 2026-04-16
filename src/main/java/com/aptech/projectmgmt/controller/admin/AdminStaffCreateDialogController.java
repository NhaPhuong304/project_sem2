package com.aptech.projectmgmt.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;



public class AdminStaffCreateDialogController {

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    

    

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
