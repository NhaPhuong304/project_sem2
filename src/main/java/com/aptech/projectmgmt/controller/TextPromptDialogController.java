package com.aptech.projectmgmt.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class TextPromptDialogController {

    @FXML private Label headerLabel;
    @FXML private Label promptLabel;
    @FXML private TextArea contentArea;

    public void initData(String headerText, String promptText, String initialValue) {
        headerLabel.setText(headerText != null ? headerText : "");
        promptLabel.setText(promptText != null ? promptText : "");
        contentArea.setText(initialValue != null ? initialValue : "");
        contentArea.positionCaret(contentArea.getText().length());
    }

    public String getContent() {
        return contentArea.getText() != null ? contentArea.getText().trim() : "";
    }
}
