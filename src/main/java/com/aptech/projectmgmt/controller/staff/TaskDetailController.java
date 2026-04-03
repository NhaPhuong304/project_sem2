package com.aptech.projectmgmt.controller.staff;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class TaskDetailController {

    @FXML private Label taskTitleLabel;
    @FXML private TextArea taskDetailArea;

    public void initData(String taskTitle, String detailText) {
        taskTitleLabel.setText(taskTitle != null ? taskTitle : "Chi tiet task");
        taskDetailArea.setText(detailText != null ? detailText : "");
        taskDetailArea.positionCaret(0);
    }
}
