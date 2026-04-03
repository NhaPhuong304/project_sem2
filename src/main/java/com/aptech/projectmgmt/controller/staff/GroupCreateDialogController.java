package com.aptech.projectmgmt.controller.staff;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class GroupCreateDialogController {

    @FXML private TextField groupNameField;

    public String buildGroupName() {
        String groupName = groupNameField.getText() != null ? groupNameField.getText().trim() : "";
        if (groupName.isEmpty()) {
            throw new RuntimeException("Ten nhom khong duoc de trong");
        }
        return groupName;
    }
}
