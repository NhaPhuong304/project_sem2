package com.aptech.projectmgmt.controller.staff;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ClassCreateDialogController {

    @FXML private TextField classNameField;
    @FXML private TextField academicYearField;

    public String getClassName() {
        return classNameField.getText() != null ? classNameField.getText().trim() : "";
    }


    public String getAcademicYear() {
        return academicYearField.getText() != null ? academicYearField.getText().trim() : "";
    }
}