package com.aptech.projectmgmt.controller.student;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

public class MyTaskActionCellController {

    @FXML private Button startButton;
    @FXML private Button submitReviewButton;
    @FXML private Button requestRevisionButton;
    @FXML private Button completeButton;
    @FXML private Button resubmitButton;
    @FXML private Button detailButton;
    @FXML private Button assignButton;
    @FXML private Tooltip startTooltip;

    public void configure(
            boolean showDisabledStart,
            boolean canStart,
            boolean canSubmitReview,
            boolean canRequestRevision,
            boolean canComplete,
            boolean canResubmit,
            boolean canViewDetail,
            boolean canAssign,
            Runnable onStart,
            Runnable onSubmitReview,
            Runnable onRequestRevision,
            Runnable onComplete,
            Runnable onResubmit,
            Runnable onViewDetail,
            Runnable onAssign) {

        boolean showStartButton = showDisabledStart || canStart;
        configureButton(startButton, showStartButton, canStart, onStart);
        startButton.setTooltip(showDisabledStart && !canStart ? startTooltip : null);

        configureButton(submitReviewButton, canSubmitReview, true, onSubmitReview);
        configureButton(requestRevisionButton, canRequestRevision, true, onRequestRevision);
        configureButton(completeButton, canComplete, true, onComplete);
        configureButton(resubmitButton, canResubmit, true, onResubmit);
        configureButton(detailButton, canViewDetail, true, onViewDetail);
        configureButton(assignButton, canAssign, true, onAssign);
    }

    private void configureButton(Button button, boolean visible, boolean enabled, Runnable action) {
        button.setVisible(visible);
        button.setManaged(visible);
        button.setDisable(!enabled);
        button.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });
    }
}