package com.aptech.projectmgmt.controller.student;

import com.aptech.projectmgmt.model.MemberRole;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class StudentProjectDetailController {

    @FXML private Label projectTitleLabel;
    @FXML private Label semesterLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private Label reportDateLabel;
    @FXML private Label advisorLabel;
    @FXML private Label myRoleLabel;
    @FXML private TextArea descriptionArea;

    @FXML private MyTaskListController myTaskListController;

    public void initData(
            int groupId,
            int projectId,
            MemberRole myRole,
            String projectTitle,
            String semester,
            String startDate,
            String endDate,
            String reportDate,
            String advisor,
            String description
    ) {
        projectTitleLabel.setText(projectTitle);
        semesterLabel.setText(semester);
        startDateLabel.setText(startDate);
        endDateLabel.setText(endDate);
        reportDateLabel.setText(reportDate);
        advisorLabel.setText(advisor);
        myRoleLabel.setText(myRole != null ? myRole.name() : "-");
        descriptionArea.setText(description != null ? description : "");

        if (myTaskListController != null) {
            myTaskListController.initData(groupId, projectId, myRole);
        }
    }
}