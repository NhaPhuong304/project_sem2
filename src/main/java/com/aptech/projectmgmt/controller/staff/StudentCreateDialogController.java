package com.aptech.projectmgmt.controller.staff;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class StudentCreateDialogController {

    @FXML private TextField studentCodeField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;

    @FXML
    public void initialize() {
        studentCodeField.setEditable(false);
        studentCodeField.setMouseTransparent(true);
        studentCodeField.setFocusTraversable(false);
    }

    public void setStudentCode(String studentCode) {
        studentCodeField.setText(studentCode != null ? studentCode : "");
    }

    public String getStudentCode() {
        return studentCodeField.getText() != null ? studentCodeField.getText().trim() : "";
    }

    public String getFullName() {
        return fullNameField.getText() != null ? fullNameField.getText().trim() : "";
    }

    public String getEmail() {
        return emailField.getText() != null ? emailField.getText().trim() : "";
    }
}
