package com.aptech.projectmgmt.controller;

import com.aptech.projectmgmt.model.Account;
import com.aptech.projectmgmt.model.OtpPurpose;
import com.aptech.projectmgmt.model.UserRole;
import com.aptech.projectmgmt.service.AuthService;
import com.aptech.projectmgmt.util.SceneManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

	@FXML
	private TextField usernameField;
	@FXML
	private PasswordField passwordField;
	@FXML
	private Button loginBtn;
	@FXML
	private Label errorLabel;
	@FXML
	private Hyperlink forgotPasswordLink;
	@FXML
	private TextField visiblePasswordField;
	@FXML
	private Button togglePasswordBtn;

	private final AuthService authService = new AuthService();
	private boolean passwordVisible = false;

	@FXML
	public void initialize() {
		errorLabel.setVisible(false);
		visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
		togglePasswordBtn.setOnAction(e -> togglePasswordVisibility());
		loginBtn.setOnAction(e -> handleLogin());
		forgotPasswordLink.setOnAction(e -> handleForgotPassword());
	}

	private void togglePasswordVisibility() {
		passwordVisible = !passwordVisible;

		if (passwordVisible) {
			visiblePasswordField.setVisible(true);
			visiblePasswordField.setManaged(true);
			passwordField.setVisible(false);
			passwordField.setManaged(false);
			togglePasswordBtn.setText("🙈");
		} else {
			visiblePasswordField.setVisible(false);
			visiblePasswordField.setManaged(false);
			passwordField.setVisible(true);
			passwordField.setManaged(true);
			togglePasswordBtn.setText("👁");
		}
	}

	private void handleLogin() {
		String username = usernameField.getText().trim();
		String password = passwordVisible ? visiblePasswordField.getText() : passwordField.getText();

		if (username.isEmpty() || password.isEmpty()) {
			showError("Vui long nhap ten dang nhap va mat khau");
			return;
		}

		loginBtn.setDisable(true);
		errorLabel.setVisible(false);

		Task<Account> task = new Task<>() {
			@Override
			protected Account call() {
				return authService.login(username, password);
			}
		};

		task.setOnSucceeded(e -> {
			loginBtn.setDisable(false);
			Account account = task.getValue();

			if (account.isFirstLogin()) {
				navigateToOtp(account.getAccountId(), OtpPurpose.FIRST_LOGIN);
			} else if (account.getRole() == UserRole.ADMIN) {
				navigateToStaffDashboard();
			} else if (account.getRole() == UserRole.TEACHER) {
				navigateToTeacherDashboard();
			} else {
				navigateToStudentDashboard();
			}
		});

		task.setOnFailed(e -> {
			loginBtn.setDisable(false);
			Throwable ex = task.getException();
			showError(ex != null ? ex.getMessage() : "Dang nhap that bai");
		});

		new Thread(task).start();
	}

	private void handleForgotPassword() {
		String username = usernameField.getText().trim();
		if (username.isEmpty()) {
			showError("Vui long nhap ten dang nhap truoc");
			return;
		}
		Task<Account> task = new Task<>() {
			@Override
			protected Account call() {
				return authService.findAccountForPasswordReset(username);
			}
		};
		task.setOnSucceeded(e -> {
			Account account = task.getValue();
			navigateToOtp(account.getAccountId(), OtpPurpose.CHANGE_PASSWORD);
		});
		task.setOnFailed(e -> {
			Throwable ex = task.getException();
			showError(ex != null ? ex.getMessage() : "Loi he thong");
		});
		new Thread(task).start();
	}

	private void navigateToOtp(int accountId, OtpPurpose purpose) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.OTP));
			Parent root = loader.load();
			OtpController controller = loader.getController();
			controller.initData(accountId, purpose);
			Stage stage = (Stage) loginBtn.getScene().getWindow();
			javafx.scene.Scene scene = new javafx.scene.Scene(root);
			stage.setScene(scene);
			stage.show();
		} catch (Exception ex) {
			showError("Loi chuyen man hinh OTP: " + ex.getMessage());
		}
	}

	private void navigateToStaffDashboard() {
		try {
			Stage stage = (Stage) loginBtn.getScene().getWindow();
			SceneManager.switchScene(stage, SceneManager.STAFF_DASHBOARD);
		} catch (Exception ex) {
			showError("Loi chuyen man hinh: " + ex.getMessage());
		}
	}

	private void navigateToStudentDashboard() {
		try {
			Stage stage = (Stage) loginBtn.getScene().getWindow();
			SceneManager.switchScene(stage, SceneManager.STUDENT_DASHBOARD);
		} catch (Exception ex) {
			showError("Loi chuyen man hinh: " + ex.getMessage());
		}
	}

	private void navigateToTeacherDashboard() {
		try {
			Stage stage = (Stage) loginBtn.getScene().getWindow();
			SceneManager.switchScene(stage, SceneManager.TEACHER_DASHBOARD);
		} catch (Exception ex) {
			showError("Loi chuyen man hinh: " + ex.getMessage());
		}
	}

	private void showError(String message) {
		errorLabel.setText(message);
		errorLabel.setVisible(true);
	}
}
