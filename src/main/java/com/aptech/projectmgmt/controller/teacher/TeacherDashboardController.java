package com.aptech.projectmgmt.controller.teacher;

import com.aptech.projectmgmt.controller.staff.ClassListController;
import com.aptech.projectmgmt.controller.staff.ProjectListController;
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
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class TeacherDashboardController {

    @FXML private Label teacherNameLabel;
    @FXML private ImageView avatarImageView;
    @FXML private Button logoutBtn;
    @FXML private Button overviewBtn;
    @FXML private Button myClassesBtn;
    @FXML private Button myProjectsBtn;
    @FXML private StackPane contentArea;
    @FXML private Button inboxBtn;
    @FXML private Label msgBadge;

    private final AccountService accountService = new AccountService();
    private Integer currentTeacherStaffId;

    @FXML
    public void initialize() {
        Staff staff = SessionManager.getInstance().getCurrentStaff();
        if (staff != null) {
            teacherNameLabel.setText(staff.getFullName());
            currentTeacherStaffId = staff.getStaffId();
        }
        loadAvatar();
        avatarImageView.setOnMouseClicked(e -> handleChangeAvatar());
        logoutBtn.setOnAction(e -> handleLogout());
        setActiveMenu(overviewBtn);
        loadContent(SceneManager.TEACHER_OVERVIEW);
    }

    @FXML
    public void onInboxClick() {
        setActiveMenu(inboxBtn);
        loadContent("/fxml/teacher/teacher-inbox.fxml");
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Unable to load screen: " + e.getMessage());
        }
    }

    @FXML
    public void onOverviewClick() {
        setActiveMenu(overviewBtn);
        loadContent(SceneManager.TEACHER_OVERVIEW);
    }

    @FXML
    public void onMyClassesClick() {
        setActiveMenu(myClassesBtn);
        loadTeacherClasses();
    }

    @FXML
    public void onMyProjectsClick() {
        setActiveMenu(myProjectsBtn);
        loadTeacherProjects();
    }

    private void loadTeacherClasses() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.CLASS_LIST));
            Node content = loader.load();
            ClassListController controller = loader.getController();
            controller.setReadOnlyMode(true);
            controller.setTeacherStaffId(currentTeacherStaffId);
            contentArea.getChildren().setAll(content);
        } catch (Exception e) {
            AlertUtil.showError("Unable to load classes: " + e.getMessage());
        }
    }

    private void loadTeacherProjects() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.PROJECT_LIST));
            Node content = loader.load();
            ProjectListController controller = loader.getController();
            controller.setReadOnlyMode(true);
            controller.setTeacherStaffId(currentTeacherStaffId);
            contentArea.getChildren().setAll(content);
        } catch (Exception e) {
            AlertUtil.showError("Unable to load supervised projects: " + e.getMessage());
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

    private void setActiveMenu(Button activeButton) {
        if (overviewBtn != null) {
            overviewBtn.getStyleClass().remove("sidebar-btn-active");
        }
        if (myClassesBtn != null) {
            myClassesBtn.getStyleClass().remove("sidebar-btn-active");
        }
        if (myProjectsBtn != null) {
            myProjectsBtn.getStyleClass().remove("sidebar-btn-active");
        }
        if (inboxBtn != null) {
            inboxBtn.getStyleClass().remove("sidebar-btn-active");
        }
        if (activeButton != null && !activeButton.getStyleClass().contains("sidebar-btn-active")) {
            activeButton.getStyleClass().add("sidebar-btn-active");
        }
    }
}
