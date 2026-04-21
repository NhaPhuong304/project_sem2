package com.aptech.projectmgmt.controller.staff;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class DoubleActionCellController {

    @FXML
    private Button primaryBtn;

    @FXML
    private Button secondaryBtn;

    private Runnable onPrimary;
    private Runnable onSecondary;

    @FXML
    public void initialize() {
        primaryBtn.setOnAction(e -> {
            if (onPrimary != null) {
                onPrimary.run();
            }
        });

        secondaryBtn.setOnAction(e -> {
            if (onSecondary != null) {
                onSecondary.run();
            }
        });
    }

    public void setPrimaryText(String text) {
        primaryBtn.setText(text);
    }

    public void setSecondaryText(String text) {
        secondaryBtn.setText(text);
    }

    public void setOnPrimary(Runnable onPrimary) {
        this.onPrimary = onPrimary;
    }

    public void setOnSecondary(Runnable onSecondary) {
        this.onSecondary = onSecondary;
    }

    public void setPrimaryVisible(boolean visible) {
        primaryBtn.setVisible(visible);
        primaryBtn.setManaged(visible);
    }

    public void setSecondaryVisible(boolean visible) {
        secondaryBtn.setVisible(visible);
        secondaryBtn.setManaged(visible);
    }

    public void setPrimaryDisabled(boolean disabled) {
        primaryBtn.setDisable(disabled);
    }

    public void setSecondaryDisabled(boolean disabled) {
        secondaryBtn.setDisable(disabled);
    }
}