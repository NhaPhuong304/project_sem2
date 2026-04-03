package com.aptech.projectmgmt.controller;

import com.aptech.projectmgmt.model.OtpDispatchInfo;
import com.aptech.projectmgmt.model.OtpPurpose;
import com.aptech.projectmgmt.model.OtpVerificationResult;
import com.aptech.projectmgmt.service.OtpService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;

public class OtpController {

    @FXML private Label emailLabel;
    @FXML private TextField otpField;
    @FXML private Button confirmBtn;
    @FXML private Button resendBtn;
    @FXML private Label countdownLabel;
    @FXML private Label expiryLabel;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private final OtpService otpService = new OtpService();
    private int accountId;
    private OtpPurpose purpose;
    private Timeline resendTimeline;
    private Timeline expiryTimeline;
    private int resendCountdown;
    private int expiryCountdown;

    @FXML
    public void initialize() {
        confirmBtn.setOnAction(e -> handleConfirm());
        resendBtn.setOnAction(e -> handleResend());
    }

    public void initData(int accountId, OtpPurpose purpose) {
        this.accountId = accountId;
        this.purpose = purpose;
        emailLabel.setText("Dang gui ma OTP...");
        generateOtpAsync();
    }

    private void generateOtpAsync() {
        resendBtn.setDisable(true);
        Task<OtpDispatchInfo> task = new Task<>() {
            @Override
            protected OtpDispatchInfo call() {
                return otpService.generateAndSendOtp(accountId, purpose);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            emailLabel.setText(task.getValue().getMaskedEmail());
            startCountdowns(task.getValue());
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            emailLabel.setText("Loi: " + (ex != null ? ex.getMessage() : "Khong the gui OTP"));
            resendBtn.setDisable(false);
        }));
        new Thread(task).start();
    }

    private void startCountdowns(OtpDispatchInfo dispatchInfo) {
        startResendCountdown(dispatchInfo.getResendCooldownSeconds());
        startExpiryCountdown(dispatchInfo.getOtpExpirySeconds());
    }

    private void startResendCountdown(int seconds) {
        if (resendTimeline != null) {
            resendTimeline.stop();
        }
        resendCountdown = seconds;
        resendBtn.setDisable(true);
        resendTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            resendCountdown--;
            if (resendCountdown > 0) {
                countdownLabel.setText("Gui lai sau: " + resendCountdown + "s");
            } else {
                countdownLabel.setText("");
                resendBtn.setDisable(false);
                resendTimeline.stop();
            }
        }));
        resendTimeline.setCycleCount(seconds);
        resendTimeline.play();
        countdownLabel.setText("Gui lai sau: " + seconds + "s");
    }

    private void startExpiryCountdown(int seconds) {
        if (expiryTimeline != null) {
            expiryTimeline.stop();
        }
        expiryCountdown = seconds;
        expiryLabel.setText("Ma OTP con hieu luc: " + formatSeconds(expiryCountdown));
        expiryTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            expiryCountdown--;
            if (expiryCountdown > 0) {
                expiryLabel.setText("Ma OTP con hieu luc: " + formatSeconds(expiryCountdown));
            } else {
                expiryLabel.setText("Ma OTP da het han. Vui long gui lai ma moi.");
                expiryTimeline.stop();
            }
        }));
        expiryTimeline.setCycleCount(seconds);
        expiryTimeline.play();
    }

    private String formatSeconds(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void handleConfirm() {
        String otp = otpField.getText().trim();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (otp.isEmpty()) {
            AlertUtil.showError("Vui long nhap ma OTP");
            return;
        }
        if (newPass.length() < 6) {
            AlertUtil.showError("Mat khau moi phai co it nhat 6 ky tu");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            AlertUtil.showError("Mat khau xac nhan khong khop");
            return;
        }

        String hashedPassword = BCrypt.hashpw(newPass, BCrypt.gensalt());
        confirmBtn.setDisable(true);

        Task<OtpVerificationResult> task = new Task<>() {
            @Override
            protected OtpVerificationResult call() {
                return otpService.verifyOtp(accountId, purpose, otp, hashedPassword);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> {
            confirmBtn.setDisable(false);
            OtpVerificationResult result = task.getValue();
            int resultCode = result.getResultCode();
            switch (resultCode) {
                case 0:
                    if (resendTimeline != null) resendTimeline.stop();
                    if (expiryTimeline != null) expiryTimeline.stop();
                    AlertUtil.showSuccess("Doi mat khau thanh cong. Vui long dang nhap lai.");
                    navigateToLogin();
                    break;
                case 1:
                    AlertUtil.showError("Ma OTP khong dung, con " + result.getRemainingAttempts() + " lan thu.");
                    break;
                case 2:
                    AlertUtil.showError("Da nhap sai qua 5 lan. Vui long yeu cau ma moi.");
                    break;
                case 3:
                    AlertUtil.showError("Ma OTP da het han. Vui long yeu cau ma moi.");
                    break;
                case 4:
                    AlertUtil.showError("Khong tim thay yeu cau OTP.");
                    break;
                default:
                    AlertUtil.showError("Loi khong xac dinh (code: " + resultCode + ")");
            }
        }));

        task.setOnFailed(e -> Platform.runLater(() -> {
            confirmBtn.setDisable(false);
            Throwable ex = task.getException();
            AlertUtil.showError(ex != null ? ex.getMessage() : "Loi he thong");
        }));

        new Thread(task).start();
    }

    private void handleResend() {
        generateOtpAsync();
    }

    private void navigateToLogin() {
        try {
            Stage stage = (Stage) confirmBtn.getScene().getWindow();
            SceneManager.switchScene(stage, SceneManager.LOGIN);
        } catch (Exception ex) {
            AlertUtil.showError("Loi chuyen man hinh: " + ex.getMessage());
        }
    }
}
