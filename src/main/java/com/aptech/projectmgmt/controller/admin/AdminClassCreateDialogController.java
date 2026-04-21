package com.aptech.projectmgmt.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import com.aptech.projectmgmt.model.Staff;
import com.aptech.projectmgmt.model.UserRole;
import com.aptech.projectmgmt.service.StaffService;

public class AdminClassCreateDialogController {

    @FXML private TextField classNameField;
    @FXML private TextField academicYearField;
    @FXML private Label managerLabel;
    @FXML private ComboBox<Staff> managerComboBox;

    private final StaffService staffService = new StaffService();

    public void initData(boolean isAdmin) {
        if (isAdmin) {
            managerLabel.setVisible(true);
            managerComboBox.setVisible(true);
            
            managerComboBox.setConverter(new StringConverter<Staff>() {
                @Override
                public String toString(Staff s) {
                    if (s == null) return "Chưa phân công";
                    return s.getFullName() + " (" + s.getUsername() + ")";
                }

                @Override
                public Staff fromString(String s) {
                    return null;
                }
            });
            
            managerComboBox.getItems().add(null);
            staffService.getTeachersAndStaffs().forEach(s -> {
                if (s.getRole() == UserRole.STAFF) {
                    managerComboBox.getItems().add(s);
                }
            });
            managerComboBox.setValue(null);
        } else {
            managerLabel.setVisible(false);
            managerComboBox.setVisible(false);
        }
    }

    public String getClassName() {
        return classNameField.getText() != null ? classNameField.getText().trim() : "";
    }
    public String getAcademicYear() {
        return academicYearField.getText() != null ? academicYearField.getText().trim() : "";
    }
    public Integer getManagerId() {
        Staff s = managerComboBox.getValue();
        return s != null ? s.getStaffId() : null;
    }
}
