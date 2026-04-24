package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.model.Student;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class StudentCreateDialogController {

    @FXML private TextField studentCodeField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;

    public void setStudentCode(String studentCode) {
        studentCodeField.setText(studentCode);
    }

    public void setData(Student student) {
        if (student == null) {
            return;
        }

        studentCodeField.setText(student.getStudentCode());
        fullNameField.setText(student.getFullName());
        emailField.setText(student.getEmail());

        studentCodeField.setEditable(false);
        studentCodeField.setDisable(true);
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