package com.aptech.projectmgmt.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    // FXML path constants
    public static final String LOGIN          = "/fxml/login.fxml";
    public static final String SPLASH         = "/fxml/splash.fxml";
    public static final String OTP            = "/fxml/otp.fxml";
    public static final String AVATAR_CELL    = "/fxml/avatar-cell.fxml";
    public static final String PERSON_DISPLAY_CELL = "/fxml/person-display-cell.fxml";
    public static final String SINGLE_ACTION_CELL = "/fxml/single-action-cell.fxml";
    public static final String TEXT_PROMPT_DIALOG = "/fxml/text-prompt-dialog.fxml";
    public static final String STAFF_DASHBOARD  = "/fxml/staff/staff-dashboard.fxml";
    public static final String TEACHER_DASHBOARD = "/fxml/teacher/teacher-dashboard.fxml";
    public static final String CLASS_LIST       = "/fxml/staff/class-list.fxml";
    public static final String CLASS_CREATE_DIALOG = "/fxml/staff/class-create-dialog.fxml";
    public static final String STUDENT_LIST     = "/fxml/staff/student-list.fxml";
    public static final String STUDENT_CREATE_DIALOG = "/fxml/staff/student-create-dialog.fxml";
    public static final String STUDENT_ASSIGN_DIALOG = "/fxml/staff/student-assign-dialog.fxml";
    public static final String STUDENT_MANAGEMENT = "/fxml/staff/student-management.fxml";
    public static final String TEACHER_LIST     = "/fxml/staff/teacher-list.fxml";
    public static final String TEACHER_CREATE_DIALOG = "/fxml/staff/teacher-create-dialog.fxml";
    public static final String PROJECT_LIST     = "/fxml/staff/project-list.fxml";
    public static final String PROJECT_CREATE_DIALOG = "/fxml/staff/project-create-dialog.fxml";
    public static final String PROJECT_DETAIL   = "/fxml/staff/project-detail.fxml";
    public static final String GROUP_CREATE_DIALOG = "/fxml/staff/group-create-dialog.fxml";
    public static final String GROUP_MEMBER_CREATE_DIALOG = "/fxml/staff/group-member-create-dialog.fxml";
    public static final String GROUP_DETAIL     = "/fxml/staff/group-detail.fxml";
    public static final String TASK_LIST        = "/fxml/staff/task-list.fxml";
    public static final String STAFF_TASK_ACTION_CELL = "/fxml/staff/task-action-cell.fxml";
    public static final String TASK_DETAIL      = "/fxml/staff/task-detail.fxml";
    public static final String STUDENT_DASHBOARD = "/fxml/student/student-dashboard.fxml";
    public static final String MY_PROJECT_LIST   = "/fxml/student/my-project-list.fxml";
    public static final String MY_TASK_LIST      = "/fxml/student/my-task-list.fxml";
    public static final String MY_TASK_ACTION_CELL = "/fxml/student/my-task-action-cell.fxml";
    public static final String TASK_CREATE_DIALOG = "/fxml/student/task-create-dialog.fxml";
    public static final String MESSAGE_INBOX     = "/fxml/student/message-inbox.fxml";
    public static final String STUDENT_TRANSFER_DIALOG = "/fxml/staff/student-transfer-dialog.fxml";

    public static void switchScene(Stage stage, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        addStylesheet(scene);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
        stage.centerOnScreen();
    }

    public static void openModal(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        Stage modal = new Stage();
        modal.setTitle(title);
        modal.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(root);
        addStylesheet(scene);
        modal.setScene(scene);
        modal.showAndWait();
    }

    public static <T> T loadController(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        loader.load();
        return loader.getController();
    }

    public static <T> T loadFxml(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        loader.load();
        return loader.getController();
    }

    private static void addStylesheet(Scene scene) {
        String css = SceneManager.class.getResource("/css/style.css") != null
                ? SceneManager.class.getResource("/css/style.css").toExternalForm()
                : null;
        if (css != null) {
            scene.getStylesheets().add(css);
        }
    }
}
