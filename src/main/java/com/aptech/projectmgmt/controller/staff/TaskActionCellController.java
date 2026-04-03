package com.aptech.projectmgmt.controller.staff;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class TaskActionCellController {

    @FXML private Button messageButton;
    @FXML private Button detailButton;

    public void setOnMessage(Runnable action) {
        messageButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setOnDetail(Runnable action) {
        detailButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }

    public void setReminderVisible(boolean visible) {
        messageButton.setVisible(visible);
        messageButton.setManaged(visible);
    }
}
