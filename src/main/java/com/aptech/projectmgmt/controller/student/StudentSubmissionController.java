package com.aptech.projectmgmt.controller.student;

import com.aptech.projectmgmt.model.Student;
import com.aptech.projectmgmt.model.SubmissionFile;
import com.aptech.projectmgmt.model.SubmissionRequirement;
import com.aptech.projectmgmt.model.SubmissionTarget;
import com.aptech.projectmgmt.service.SubmissionService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentSubmissionController {

    @FXML
    private TableView<SubmissionTarget> targetTable;
    @FXML
    private TableColumn<SubmissionTarget, String> requestTitleColumn;
    @FXML
    private TableColumn<SubmissionTarget, String> deadlineColumn;
    @FXML
    private TableColumn<SubmissionTarget, String> targetStatusColumn;
    @FXML
    private Label selectedTitleLabel;
    @FXML
    private Label selectedGroupLabel;
    @FXML
    private Label selectedDeadlineLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private VBox fileBox;
    @FXML
    private Button completeButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Label statusLabel;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SubmissionService submissionService = new SubmissionService();
    private int studentId;
    private SubmissionTarget selectedTarget;
    private List<SubmissionRequirement> currentRequirements = List.of();
    private Map<Integer, SubmissionFile> currentFiles = Map.of();

    @FXML
    public void initialize() {
        Student student = SessionManager.getInstance().getCurrentStudent();
        studentId = student != null ? student.getStudentId() : 0;

        setupTargetTable();
        refreshButton.setOnAction(e -> loadTargets());
        completeButton.setOnAction(e -> handleCompleteSubmission());
        loadTargets();
    }

    private void setupTargetTable() {
        requestTitleColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRequestTitle()));
        deadlineColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatDateTime(data.getValue().getRequestDeadline())));
        targetStatusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatStatus(data.getValue().getStatus())));

        targetTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                selectedTarget = newValue;
                loadTargetDetail(newValue);
            }
        });
    }

    private void loadTargets() {
        if (studentId <= 0) {
            statusLabel.setText("Không tìm thấy tài khoản sinh viên đang đăng nhập.");
            completeButton.setDisable(true);
            return;
        }

        setLoading(true, "Đang tải yêu cầu nộp đồ án...");
        Task<List<SubmissionTarget>> task = new Task<>() {
            @Override
            protected List<SubmissionTarget> call() {
                return submissionService.getTargetsForLeader(studentId);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<SubmissionTarget> targets = task.getValue();
            targetTable.setItems(FXCollections.observableArrayList(targets));
            if (targets.isEmpty()) {
                clearDetail("Bạn chưa có yêu cầu nộp đồ án nào. Chỉ leader của nhóm mới thấy yêu cầu ở đây.");
                setLoading(false, "");
                return;
            }
            targetTable.getSelectionModel().selectFirst();
            setLoading(false, "");
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            setLoading(false, "Lỗi tải yêu cầu.");
            AlertUtil.showError("Lỗi tải yêu cầu nộp đồ án: " + (ex != null ? ex.getMessage() : ""));
        }));
        startTask(task, "student-submission-load-targets");
    }

    private void loadTargetDetail(SubmissionTarget target) {
        setLoading(true, "Đang tải nội dung cần nộp...");
        Task<DetailData> task = new Task<>() {
            @Override
            protected DetailData call() {
                return new DetailData(
                        submissionService.getRequirements(target.getRequestId()),
                        submissionService.getFiles(target.getTargetId()));
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            DetailData data = task.getValue();
            currentRequirements = data.requirements();
            currentFiles = mapFiles(data.files());
            renderDetail(target);
            setLoading(false, "");
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            setLoading(false, "Lỗi tải chi tiết yêu cầu.");
            AlertUtil.showError("Lỗi tải nội dung cần nộp: " + (ex != null ? ex.getMessage() : ""));
        }));
        startTask(task, "student-submission-load-detail");
    }

    private void renderDetail(SubmissionTarget target) {
        selectedTitleLabel.setText(target.getRequestTitle() != null ? target.getRequestTitle() : "");
        selectedGroupLabel.setText("Nhóm: " + (target.getGroupName() != null ? target.getGroupName() : ""));
        selectedDeadlineLabel.setText("Hạn nộp: " + formatDateTime(target.getRequestDeadline()));
        descriptionLabel.setText(target.getRequestDescription() != null ? target.getRequestDescription() : "");

        fileBox.getChildren().clear();
        int uploadedCount = 0;
        for (SubmissionRequirement requirement : currentRequirements) {
            SubmissionFile uploadedFile = currentFiles.get(requirement.getRequirementId());
            if (uploadedFile != null) {
                uploadedCount++;
            }
            fileBox.getChildren().add(buildFileRow(requirement, uploadedFile, target.getStatus() == 3));
        }

        boolean complete = !currentRequirements.isEmpty() && uploadedCount == currentRequirements.size();
        completeButton.setDisable(target.getStatus() == 3 || !complete);
        if (target.getStatus() == 3) {
            statusLabel.setText("Yêu cầu này đã được hoàn tất nộp.");
        } else {
            statusLabel.setText("Đã nộp " + uploadedCount + "/" + currentRequirements.size() + " file.");
        }
    }

    private VBox buildFileRow(SubmissionRequirement requirement, SubmissionFile uploadedFile, boolean locked) {
        Label nameLabel = new Label(requirement.getRequirementName() + "  " + requirement.getRequiredExtension());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a;");

        String fileText = uploadedFile != null
                ? uploadedFile.getOriginalFileName() + " (" + formatSize(uploadedFile.getFileSize()) + ")"
                : "Chưa nộp";
        Label fileLabel = new Label(fileText);
        fileLabel.setWrapText(true);
        fileLabel.setStyle("-fx-text-fill: " + (uploadedFile != null ? "#166534" : "#64748b") + ";");

        Button chooseButton = new Button(uploadedFile == null ? "Chọn file" : "Nộp lại");
        chooseButton.setDisable(locked);
        chooseButton.setOnAction(e -> chooseAndUpload(requirement));

        HBox header = new HBox(10, nameLabel, chooseButton);
        header.setStyle("-fx-alignment: center-left;");

        VBox row = new VBox(5, header, fileLabel);
        row.setStyle("-fx-padding: 10; -fx-background-color: #f8fafc; -fx-background-radius: 10; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 10;");
        return row;
    }

    private void chooseAndUpload(SubmissionRequirement requirement) {
        if (selectedTarget == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file " + requirement.getRequirementName());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                requirement.getRequirementName() + " (" + requirement.getRequiredExtension() + ")",
                "*" + requirement.getRequiredExtension()));
        Stage stage = (Stage) fileBox.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }

        setLoading(true, "Đang lưu file...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                submissionService.saveSubmittedFile(
                        selectedTarget.getTargetId(), requirement, selectedFile, studentId);
                return null;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            setLoading(false, "Đã lưu file.");
            loadTargetDetail(selectedTarget);
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            setLoading(false, "Lưu file thất bại.");
            AlertUtil.showError("Không lưu được file: " + (ex != null ? ex.getMessage() : ""));
        }));
        startTask(task, "student-submission-save-file");
    }

    private void handleCompleteSubmission() {
        if (selectedTarget == null) {
            return;
        }
        if (!AlertUtil.showConfirm("Xác nhận hoàn tất nộp đồ án? Sau khi hoàn tất, bạn sẽ không nộp lại trong màn hình này.")) {
            return;
        }

        setLoading(true, "Đang hoàn tất nộp đồ án...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                submissionService.completeSubmission(selectedTarget.getTargetId(), selectedTarget.getRequestId());
                return null;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            setLoading(false, "Đã hoàn tất nộp đồ án.");
            AlertUtil.showSuccess("Đã hoàn tất nộp đồ án.");
            loadTargets();
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            setLoading(false, "Hoàn tất thất bại.");
            AlertUtil.showError("Không thể hoàn tất nộp đồ án: " + (ex != null ? ex.getMessage() : ""));
        }));
        startTask(task, "student-submission-complete");
    }

    private void clearDetail(String message) {
        selectedTarget = null;
        selectedTitleLabel.setText("");
        selectedGroupLabel.setText("");
        selectedDeadlineLabel.setText("");
        descriptionLabel.setText(message);
        fileBox.getChildren().clear();
        completeButton.setDisable(true);
    }

    private void setLoading(boolean loading, String message) {
        refreshButton.setDisable(loading);
        if (loading) {
            completeButton.setDisable(true);
        }
        statusLabel.setText(message);
    }

    private Map<Integer, SubmissionFile> mapFiles(List<SubmissionFile> files) {
        Map<Integer, SubmissionFile> result = new HashMap<>();
        for (SubmissionFile file : files) {
            result.put(file.getRequirementId(), file);
        }
        return result;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }

    private String formatStatus(int status) {
        return switch (status) {
            case 3 -> "Đã nộp";
            case 2 -> "Đang nộp";
            default -> "Chưa hoàn tất";
        };
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.1f KB", kb);
        }
        return String.format("%.1f MB", kb / 1024.0);
    }

    private void startTask(Task<?> task, String name) {
        Thread thread = new Thread(task, name);
        thread.setDaemon(true);
        thread.start();
    }

    private record DetailData(List<SubmissionRequirement> requirements, List<SubmissionFile> files) {
    }
}
