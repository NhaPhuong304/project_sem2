package com.aptech.projectmgmt.controller.student;

import com.aptech.projectmgmt.model.MemberRole;
import com.aptech.projectmgmt.model.Student;
import com.aptech.projectmgmt.service.AccountService;
import com.aptech.projectmgmt.service.MessageService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.AvatarUtil;
import com.aptech.projectmgmt.util.ChatbotUiContextUtil;
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
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;

public class StudentDashboardController {

    @FXML private Label studentNameLabel;
    @FXML private ImageView avatarImageView;
    @FXML private Label msgBadge;
    @FXML private Button logoutBtn;
    @FXML private Button overviewBtn;
    @FXML private Button myProjectsBtn;
    @FXML private Button submissionBtn;
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
        setActiveMenu(overviewBtn);

        Platform.runLater(() -> {
            if (contentArea.getScene() != null && contentArea.getScene().getRoot() != null) {
                contentArea.getScene().getRoot().getProperties().put("controller", this);
            }
        });

        loadContent(SceneManager.STUDENT_OVERVIEW);
    }

    public void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            Object controller = loader.getController();

            if (controller instanceof MessageInboxController inboxController) {
                inboxController.setDashboardController(this);
            }

            if (controller instanceof MyProjectListController myProjectListController) {
                myProjectListController.setDashboardController(this);
            }

            if (content instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) content).prefWidthProperty().bind(contentArea.widthProperty());
                ((javafx.scene.layout.Region) content).prefHeightProperty().bind(contentArea.heightProperty());
            }
            contentArea.getChildren().setAll(content);
            ChatbotUiContextUtil.updateCurrentScreen(fxmlPath, controller);
        } catch (Exception e) {
            AlertUtil.showError("Unable to load content: " + e.getMessage());
        }
    }

    public StackPane getContentArea() {
        return contentArea;
    }

    private void startMessagePoller() {
        msgPoller = new Timeline(new KeyFrame(Duration.seconds(15), e -> checkUnreadMessages()));
        msgPoller.setCycleCount(Animation.INDEFINITE);
        msgPoller.play();
        checkUnreadMessages();
    }

    private void checkUnreadMessages() {
        if (currentStudentId <= 0) {
            return;
        }
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                return messageService.countUnread(currentStudentId);
            }
        };
        task.setOnSucceeded(e -> {
            unreadCount = task.getValue();
            Platform.runLater(this::refreshBadgeView);
        });
        new Thread(task).start();
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
        if (msgPoller != null) {
            msgPoller.stop();
        }
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
        loadContent(SceneManager.STUDENT_OVERVIEW);
    }

    @FXML
    public void onMyProjectsClick() {
        setActiveMenu(myProjectsBtn);
        loadContent(SceneManager.MY_PROJECT_LIST);
    }

    @FXML
    public void onSubmissionClick() {
        setActiveMenu(submissionBtn);
        loadContent(SceneManager.STUDENT_SUBMISSION);
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
        if (overviewBtn != null) {
            overviewBtn.getStyleClass().remove("sidebar-btn-active");
        }
        if (myProjectsBtn != null) {
            myProjectsBtn.getStyleClass().remove("sidebar-btn-active");
        }
        if (submissionBtn != null) {
            submissionBtn.getStyleClass().remove("sidebar-btn-active");
        }
        if (inboxBtn != null) {
            inboxBtn.getStyleClass().remove("sidebar-btn-active");
        }
        if (activeButton != null && !activeButton.getStyleClass().contains("sidebar-btn-active")) {
            activeButton.getStyleClass().add("sidebar-btn-active");
        }
    }

    public void loadProjectDetail(int groupId, int projectId, MemberRole myRole, String projectTitle, String semester,
                                  String startDate, String endDate, String reportDate, String advisor, String description) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student/student-project-detail.fxml"));
            Parent view = loader.load();

            StudentProjectDetailController controller = loader.getController();
            controller.initData(groupId, projectId, myRole, projectTitle, semester, startDate, endDate, reportDate,
                    advisor, description);

            contentArea.getChildren().setAll(view);
            ChatbotUiContextUtil.updateCurrentScreen("/fxml/student/student-project-detail.fxml", controller);

        } catch (IOException e) {
            AlertUtil.showError("Unable to load project details: " + e.getMessage());
        }
    }
}
