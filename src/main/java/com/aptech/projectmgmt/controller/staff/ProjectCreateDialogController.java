package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.model.Project;
import com.aptech.projectmgmt.model.ProjectStatus;
import com.aptech.projectmgmt.model.Staff;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.List;

public class ProjectCreateDialogController {

    @FXML private TextField projectNameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField semesterField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private DatePicker reportDatePicker;
    @FXML private ComboBox<Staff> supervisorCombo;

    @FXML
    public void initialize() {
        descriptionArea.setWrapText(true);
        supervisorCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Staff staff) {
                return staff != null ? staff.getFullName() : "";
            }

            @Override
            public Staff fromString(String string) {
                return null;
            }
        });

        endDatePicker.setDisable(true);
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                endDatePicker.setValue(newVal.plusMonths(1));
            } else {
                endDatePicker.setValue(null);
            }
        });
    }

    public void setSupervisors(List<Staff> staffList) {
        supervisorCombo.setItems(FXCollections.observableArrayList(staffList));
    }

    public Project buildProject(int classId) {
        Project project = new Project();
        project.setProjectName(projectNameField.getText() != null ? projectNameField.getText().trim() : "");
        project.setDescription(descriptionArea.getText() != null ? descriptionArea.getText().trim() : "");
        project.setSemester(semesterField.getText() != null ? semesterField.getText().trim() : "");
        project.setClassId(classId);
        project.setStartDate(startDatePicker.getValue());
        project.setEndDate(endDatePicker.getValue());
        project.setReportDate(reportDatePicker.getValue());
        project.setStatus(ProjectStatus.ACTIVE);
        Staff supervisor = supervisorCombo.getValue();
        if (supervisor != null) {
            project.setSupervisorId(supervisor.getStaffId());
        }
        return project;
    }
}
