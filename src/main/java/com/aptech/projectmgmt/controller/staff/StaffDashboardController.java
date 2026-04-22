package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.model.Staff;
import com.aptech.projectmgmt.service.AccountService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.AvatarUtil;
import com.aptech.projectmgmt.util.SceneManager;
import com.aptech.projectmgmt.util.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class StaffDashboardController {

    @FXML private Label staffNameLabel;
    @FXML private ImageView avatarImageView;
    @FXML private Button logoutBtn;
    @FXML private Button overviewBtn;
    @FXML private Button classListBtn;
    @FXML private Button projectListBtn;
    @FXML private Button submissionRequestBtn;
    @FXML private Button studentManagementBtn;
    @FXML private Button teacherListBtn;
    @FXML private StackPane contentArea;

    private final AccountService accountService = new AccountService();

    @FXML
    public void initialize() {
        Staff staff = SessionManager.getInstance().getCurrentStaff();
        if (staff != null) {
            staffNameLabel.setText(staff.getFullName());
        }
        loadAvatar();
        avatarImageView.setOnMouseClicked(e -> handleChangeAvatar());
        logoutBtn.setOnAction(e -> handleLogout());

        teacherListBtn.setVisible(false);
        teacherListBtn.setManaged(false);
        setActiveMenu(overviewBtn);
        loadContent(SceneManager.STAFF_OVERVIEW);
    }

    public void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();

            if (SceneManager.PROJECT_LIST.equals(fxmlPath)) {
                ProjectListController controller = loader.getController();
                Staff staff = SessionManager.getInstance().getCurrentStaff();
                if (staff != null) {
                    controller.setStaffId(staff.getStaffId());
                }
            }

            contentArea.getChildren().setAll(content);
        } catch (Exception e) {
            AlertUtil.showError("Unable to load content: " + e.getMessage());
        }
    }

    public void loadStudentList(int classId) {
        setActiveMenu(classListBtn);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.STUDENT_LIST));
            Node content = loader.load();
            StudentListController controller = loader.getController();
            controller.initData(classId);
            contentArea.getChildren().setAll(content);
        } catch (Exception e) {
            AlertUtil.showError("Unable to load students: " + e.getMessage());
        }
    }

    public void loadProjectList(int classId) {
        setActiveMenu(projectListBtn);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.PROJECT_LIST));
            Node content = loader.load();
            ProjectListController controller = loader.getController();

            Staff staff = SessionManager.getInstance().getCurrentStaff();
            if (staff != null) {
                controller.setStaffId(staff.getStaffId());
            }

            controller.initData(classId);
            contentArea.getChildren().setAll(content);
        } catch (Exception e) {
            AlertUtil.showError("Unable to load projects: " + e.getMessage());
        }
    }

    public void loadProjectDetail(int projectId) {
        setActiveMenu(projectListBtn);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.PROJECT_DETAIL));
            Node content = loader.load();
            ProjectDetailController controller = loader.getController();
            controller.initData(projectId);
            contentArea.getChildren().setAll(content);
        } catch (Exception e) {
            AlertUtil.showError("Unable to load project details: " + e.getMessage());
        }
    }

    private void loadAvatar() {
        var account = SessionManager.getInstance().getCurrentAccount();
        AvatarUtil.applyAvatar(avatarImageView, account != null ? account.getPhotoUrl() : null);
    }

    private void handleChangeAvatar() {
        File selectedFile = chooseAvatarFile();
        if (selectedFile == null) {
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                accountService.updateCurrentAvatar(selectedFile.getAbsolutePath());
                return null;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            loadAvatar();
            AlertUtil.showSuccess("Avatar updated successfully");
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Unable to update avatar: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private File chooseAvatarFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose avatar");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        Stage stage = (Stage) avatarImageView.getScene().getWindow();
        return fileChooser.showOpenDialog(stage);
    }

    private void handleLogout() {
        SessionManager.getInstance().clearSession();
        try {
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            SceneManager.switchScene(stage, SceneManager.LOGIN);
        } catch (Exception e) {
            AlertUtil.showError("Unable to log out: " + e.getMessage());
        }
    }

    @FXML
    public void onOverviewClick() {
        setActiveMenu(overviewBtn);
        loadContent(SceneManager.STAFF_OVERVIEW);
    }

    @FXML
    public void onClassListClick() {
        setActiveMenu(classListBtn);
        loadContent(SceneManager.CLASS_LIST);
    }

    @FXML
    public void onProjectListClick() {
        setActiveMenu(projectListBtn);
        loadContent(SceneManager.PROJECT_LIST);
    }

    @FXML
    public void onSubmissionRequestClick() {
        setActiveMenu(submissionRequestBtn);
        loadContent(SceneManager.SUBMISSION_REQUEST_LIST);
    }

    @FXML
    public void onStudentManagementClick() {
        setActiveMenu(studentManagementBtn);
        loadContent(SceneManager.STUDENT_MANAGEMENT);
    }

    @FXML
    public void onTeacherListClick() {
        setActiveMenu(teacherListBtn);
        loadContent(SceneManager.TEACHER_LIST);
    }

    private void setActiveMenu(Button activeButton) {
        if (overviewBtn != null) {
            overviewBtn.getStyleClass().remove("sidebar-btn-active");
        }
        if (classListBtn != null) {
            classListBtn.getStyleClass().remove("sidebar-btn-active");
        }
        if (projectListBtn != null) {
            projectListBtn.getStyleClass().remove("sidebar-btn-active");
        }
        if (submissionRequestBtn != null) {
            submissionRequestBtn.getStyleClass().remove("sidebar-btn-active");
        }
        if (studentManagementBtn != null) {
            studentManagementBtn.getStyleClass().remove("sidebar-btn-active");
        }
        if (teacherListBtn != null) {
            teacherListBtn.getStyleClass().remove("sidebar-btn-active");
        }
        if (activeButton != null && !activeButton.getStyleClass().contains("sidebar-btn-active")) {
            activeButton.getStyleClass().add("sidebar-btn-active");
        }
    }
}
