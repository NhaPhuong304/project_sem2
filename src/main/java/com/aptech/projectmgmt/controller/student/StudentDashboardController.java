package com.aptech.projectmgmt.controller.student;

import com.aptech.projectmgmt.model.Student;
import com.aptech.projectmgmt.service.AccountService;
import com.aptech.projectmgmt.service.MessageService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.AvatarUtil;
import com.aptech.projectmgmt.util.SceneManager;
import com.aptech.projectmgmt.util.SessionManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.io.File;

public class StudentDashboardController {

    @FXML private Label studentNameLabel;
    @FXML private ImageView avatarImageView;
    @FXML private Label msgBadge;
    @FXML private Button logoutBtn;
    @FXML private Button myProjectsBtn;
    @FXML private Button inboxBtn;
    @FXML private StackPane contentArea;

    private final MessageService messageService = new MessageService();
    private final AccountService accountService = new AccountService();
    private Timeline msgPoller;
    private int currentStudentId;
    private int unreadCount;

    @FXML
    public void initialize() {
        Student student = SessionManager.getInstance().getCurrentStudent();
        if (student != null) {
            studentNameLabel.setText(student.getFullName());
            currentStudentId = student.getStudentId();
        }
        loadAvatar();
        avatarImageView.setOnMouseClicked(e -> handleChangeAvatar());
        logoutBtn.setOnAction(e -> handleLogout());
        msgBadge.setVisible(false);
        msgBadge.setManaged(false);
        startMessagePoller();
        setActiveMenu(myProjectsBtn);
        loadContent(SceneManager.MY_PROJECT_LIST);
    }

    public void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            Object controller = loader.getController();
            if (controller instanceof MessageInboxController inboxController) {
                inboxController.setDashboardController(this);
            }
            contentArea.getChildren().setAll(content);
        } catch (Exception e) {
            AlertUtil.showError("Loi tai noi dung: " + e.getMessage());
        }
    }

    public StackPane getContentArea() { return contentArea; }

    private void startMessagePoller() {
        msgPoller = new Timeline(new KeyFrame(Duration.seconds(15), e -> checkUnreadMessages()));
        msgPoller.setCycleCount(Animation.INDEFINITE);
        msgPoller.play();
        checkUnreadMessages();
    }

    private void checkUnreadMessages() {
        if (currentStudentId <= 0) return;
        Task<Integer> t = new Task<>() {
            @Override
            protected Integer call() {
                return messageService.countUnread(currentStudentId);
            }
        };
        t.setOnSucceeded(e -> {
            unreadCount = t.getValue();
            Platform.runLater(() -> {
                refreshBadgeView();
            });
        });
        new Thread(t).start();
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
            AlertUtil.showSuccess("Cap nhat avatar thanh cong");
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Loi cap nhat avatar: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private File chooseAvatarFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chon avatar");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        Stage stage = (Stage) avatarImageView.getScene().getWindow();
        return fileChooser.showOpenDialog(stage);
    }

    private void handleLogout() {
        if (msgPoller != null) msgPoller.stop();
        SessionManager.getInstance().clearSession();
        try {
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            SceneManager.switchScene(stage, SceneManager.LOGIN);
        } catch (Exception e) {
            AlertUtil.showError("Loi dang xuat: " + e.getMessage());
        }
    }

    @FXML
    public void onMyProjectsClick() {
        setActiveMenu(myProjectsBtn);
        loadContent(SceneManager.MY_PROJECT_LIST);
    }

    @FXML
    public void onInboxClick() {
        setActiveMenu(inboxBtn);
        loadContent(SceneManager.MESSAGE_INBOX);
    }

    public void adjustUnreadBadge(int delta) {
        unreadCount = Math.max(0, unreadCount + delta);
        refreshBadgeView();
    }

    private void refreshBadgeView() {
        msgBadge.setText(unreadCount > 0 ? String.valueOf(unreadCount) : "");
        msgBadge.setVisible(unreadCount > 0);
        msgBadge.setManaged(unreadCount > 0);
    }

    private void setActiveMenu(Button activeButton) {
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
