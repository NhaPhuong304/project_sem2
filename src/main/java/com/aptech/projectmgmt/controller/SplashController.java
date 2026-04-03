package com.aptech.projectmgmt.controller;

import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashController {

    private static final int MAX_PROGRESS = 100;
    private static final Duration STEP_DURATION = Duration.millis(50);

    @FXML private ImageView logoImageView;
    @FXML private ProgressBar loadingProgressBar;
    @FXML private Label progressLabel;

    private Timeline loadingTimeline;
    private Stage stage;
    private int progressValue;

    public void initData(Stage stage) {
        this.stage = stage;
        startLoadingAnimation();
    }

    private void startLoadingAnimation() {
        stopLoadingAnimation();
        progressValue = 0;
        updateProgressView();

        loadingTimeline = new Timeline(new KeyFrame(STEP_DURATION, e -> {
            progressValue++;
            updateProgressView();
            if (progressValue >= MAX_PROGRESS) {
                stopLoadingAnimation();
                openLoginScreen();
            }
        }));
        loadingTimeline.setCycleCount(MAX_PROGRESS);
        loadingTimeline.playFromStart();
    }

    private void updateProgressView() {
        loadingProgressBar.setProgress(progressValue / 100.0);
        progressLabel.setText(progressValue + "%");
    }

    private void openLoginScreen() {
        if (stage == null) {
            return;
        }
        Platform.runLater(() -> {
            try {
                SceneManager.switchScene(stage, SceneManager.LOGIN);
            } catch (Exception ex) {
                AlertUtil.showError("Loi mo man hinh dang nhap: " + ex.getMessage());
            }
        });
    }

    public void stopLoadingAnimation() {
        if (loadingTimeline != null) {
            loadingTimeline.stop();
            loadingTimeline = null;
        }
    }
}
