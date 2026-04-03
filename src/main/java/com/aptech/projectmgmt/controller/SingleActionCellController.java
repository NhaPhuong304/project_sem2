package com.aptech.projectmgmt.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SingleActionCellController {

    @FXML private Button actionButton;

    public void setActionText(String text) {
        actionButton.setText(text != null ? text : "");
    }

    public void setActionDisabled(boolean disabled) {
        actionButton.setDisable(disabled);
    }

    public void setOnAction(Runnable action) {
        actionButton.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }
}
