package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.model.Staff;
import com.aptech.projectmgmt.model.SubmissionFile;
import com.aptech.projectmgmt.model.SubmissionRecipientOption;
import com.aptech.projectmgmt.model.SubmissionRequirementTemplate;
import com.aptech.projectmgmt.model.SubmissionRequest;
import com.aptech.projectmgmt.model.SubmissionTarget;
import com.aptech.projectmgmt.service.SubmissionService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SubmissionRequestListController {

    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private DatePicker deadlineDatePicker;
    @FXML
    private ComboBox<String> deadlineTimeCombo;
    @FXML
    private VBox requirementBox;
    @FXML
    private VBox recipientBox;
    @FXML
    private Button sendRequestBtn;
    @FXML
    private Label statusLabel;
    @FXML
    private TableView<SubmissionRequest> requestTable;
    @FXML
    private TableColumn<SubmissionRequest, String> titleColumn;
    @FXML
    private TableColumn<SubmissionRequest, String> deadlineColumn;
    @FXML
    private TableColumn<SubmissionRequest, String> statusColumn;
    @FXML
    private TableColumn<SubmissionRequest, String> createdAtColumn;
    @FXML
    private TableView<SubmissionRequest> monitorRequestTable;
    @FXML
    private TableColumn<SubmissionRequest, String> monitorTitleColumn;
    @FXML
    private TableColumn<SubmissionRequest, String> monitorDeadlineColumn;
    @FXML
    private TableColumn<SubmissionRequest, String> monitorStatusColumn;
    @FXML
    private TableView<SubmissionTarget> targetTable;
    @FXML
    private TableColumn<SubmissionTarget, String> groupColumn;
    @FXML
    private TableColumn<SubmissionTarget, String> leaderColumn;
    @FXML
    private TableColumn<SubmissionTarget, String> targetStatusColumn;
    @FXML
    private TableColumn<SubmissionTarget, String> submittedAtColumn;
    @FXML
    private TableView<SubmissionFile> submittedFileTable;
    @FXML
    private TableColumn<SubmissionFile, String> requirementColumn;
    @FXML
    private TableColumn<SubmissionFile, String> submittedFileNameColumn;
    @FXML
    private TableColumn<SubmissionFile, String> submittedFileSizeColumn;
    @FXML
    private TableColumn<SubmissionFile, String> uploadedAtColumn;
    @FXML
    private Button downloadFilesBtn;
    @FXML
    private Button refreshMonitorBtn;
    @FXML
    private Label monitorStatusLabel;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SubmissionService submissionService = new SubmissionService();
    private final Map<CheckBox, SubmissionRequirementTemplate> requirementChecks = new LinkedHashMap<>();
    private final Map<CheckBox, SubmissionRecipientOption> recipientChecks = new LinkedHashMap<>();

    private int staffId;
    private SubmissionTarget selectedMonitorTarget;

    @FXML
    public void initialize() {
        Staff staff = SessionManager.getInstance().getCurrentStaff();
        staffId = staff != null ? staff.getStaffId() : 0;

        setupDefaults();
        setupRequestTable();
        setupMonitorTables();
        sendRequestBtn.setOnAction(e -> handleSendRequest());
        refreshMonitorBtn.setOnAction(e -> loadScreenData());
        downloadFilesBtn.setOnAction(e -> handleDownloadFiles());
        loadScreenData();
    }

    private void setupDefaults() {
        titleField.setText("Các nội dung cần chuẩn bị để nộp đồ án sau khi báo cáo");
        descriptionArea.setText("Vui lòng nộp đầy đủ các file theo yêu cầu trước hạn.");
        deadlineDatePicker.setValue(LocalDate.now().plusDays(7));
        deadlineTimeCombo.setItems(FXCollections.observableArrayList(
                "08:00", "12:00", "17:00", "20:00", "23:59"));
        deadlineTimeCombo.getSelectionModel().select("17:00");
    }

    private void setupRequestTable() {
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        deadlineColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatDateTime(data.getValue().getDeadline())));
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatRequestStatus(data.getValue().getStatus())));
        createdAtColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatDateTime(data.getValue().getCreatedAt())));
    }

    private void setupMonitorTables() {
        monitorTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        monitorDeadlineColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatDateTime(data.getValue().getDeadline())));
        monitorStatusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatRequestStatus(data.getValue().getStatus())));

        groupColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));
        leaderColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLeaderName()));
        targetStatusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatTargetStatus(data.getValue().getStatus())));
        submittedAtColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatDateTime(data.getValue().getSubmittedAt())));

        requirementColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRequirementName()));
        submittedFileNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOriginalFileName()));
        submittedFileSizeColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatSize(data.getValue().getFileSize())));
        uploadedAtColumn.setCellValueFactory(data ->
                new SimpleStringProperty(formatDateTime(data.getValue().getUploadedAt())));

        monitorRequestTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                loadTargetsForRequest(newValue);
            }
        });
        targetTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedMonitorTarget = newValue;
            downloadFilesBtn.setDisable(newValue == null);
            if (newValue != null) {
                loadFilesForTarget(newValue);
            } else {
                submittedFileTable.getItems().clear();
            }
        });
    }

    private void loadScreenData() {
        if (staffId <= 0) {
            statusLabel.setText("Không tìm thấy tài khoản giáo vụ đang đăng nhập.");
            sendRequestBtn.setDisable(true);
            return;
        }

        setLoading(true, "Đang tải dữ liệu...");
        Task<ScreenData> task = new Task<>() {
            @Override
            protected ScreenData call() {
                return new ScreenData(
                        submissionService.getDefaultRequirements(),
                        submissionService.getRecipientOptionsForStaff(staffId),
                        submissionService.getRequestsForStaff(staffId));
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            ScreenData data = task.getValue();
            renderRequirements(data.requirements());
            renderRecipients(data.recipients());
            ObservableList<SubmissionRequest> requests = FXCollections.observableArrayList(data.requests());
            requestTable.setItems(FXCollections.observableArrayList(data.requests()));
            monitorRequestTable.setItems(requests);
            if (!requests.isEmpty()) {
                monitorRequestTable.getSelectionModel().selectFirst();
            }
            setLoading(false, "");
            setMonitorLoading(false, "");
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            setLoading(false, "Lỗi tải dữ liệu.");
            setMonitorLoading(false, "Lỗi tải dữ liệu.");
            AlertUtil.showError("Lỗi tải yêu cầu nộp đồ án: " + (ex != null ? ex.getMessage() : ""));
        }));
        startTask(task, "submission-load-screen");
    }

    private void renderRequirements(List<SubmissionRequirementTemplate> requirements) {
        requirementBox.getChildren().clear();
        requirementChecks.clear();

        for (SubmissionRequirementTemplate template : requirements) {
            CheckBox checkBox = new CheckBox(template.getRequirementName() + "  " + template.getRequiredExtension());
            checkBox.setSelected(true);
            checkBox.setWrapText(true);
            requirementChecks.put(checkBox, template);
            requirementBox.getChildren().add(checkBox);
        }

        if (requirements.isEmpty()) {
            requirementBox.getChildren().add(new Label("Chưa có mẫu nội dung cần nộp trong database."));
        }
    }

    private void renderRecipients(List<SubmissionRecipientOption> recipients) {
        recipientBox.getChildren().clear();
        recipientChecks.clear();

        for (SubmissionRecipientOption recipient : recipients) {
            CheckBox checkBox = new CheckBox(recipient.getDisplayText());
            checkBox.setSelected(true);
            checkBox.setWrapText(true);
            recipientChecks.put(checkBox, recipient);
            recipientBox.getChildren().add(checkBox);
        }

        if (recipients.isEmpty()) {
            recipientBox.getChildren().add(new Label("Chưa có nhóm nào có leader trong các lớp bạn quản lý."));
        }
    }

    private void handleSendRequest() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        LocalDateTime deadline = readDeadline();
        List<SubmissionRequirementTemplate> requirements = getSelectedRequirements();
        List<Integer> groupIds = getSelectedGroupIds();

        if (deadline == null) {
            AlertUtil.showError("Vui lòng chọn hạn nộp hợp lệ.");
            return;
        }

        setLoading(true, "Đang gửi yêu cầu...");
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                return submissionService.createRequest(title, description, deadline, staffId, requirements, groupIds);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            setLoading(false, "Đã gửi yêu cầu nộp đồ án.");
            AlertUtil.showSuccess("Đã tạo yêu cầu và gửi thông báo cho leader của các nhóm đã chọn.");
            loadScreenData();
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            setLoading(false, "Gửi yêu cầu thất bại.");
            AlertUtil.showError("Không tạo được yêu cầu: " + (ex != null ? ex.getMessage() : ""));
        }));
        startTask(task, "submission-create-request");
    }

    private void loadTargetsForRequest(SubmissionRequest request) {
        selectedMonitorTarget = null;
        targetTable.getItems().clear();
        submittedFileTable.getItems().clear();
        downloadFilesBtn.setDisable(true);
        if (request == null) {
            return;
        }

        setMonitorLoading(true, "Đang tải danh sách nhóm...");
        Task<List<SubmissionTarget>> task = new Task<>() {
            @Override
            protected List<SubmissionTarget> call() {
                return submissionService.getTargetsByRequest(request.getRequestId());
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<SubmissionTarget> targets = task.getValue();
            targetTable.setItems(FXCollections.observableArrayList(targets));
            if (!targets.isEmpty()) {
                targetTable.getSelectionModel().selectFirst();
            }
            setMonitorLoading(false, targets.isEmpty() ? "Chưa có nhóm nhận yêu cầu này." : "");
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            setMonitorLoading(false, "Lỗi tải danh sách nhóm.");
            AlertUtil.showError("Lỗi tải nhóm nhận yêu cầu: " + (ex != null ? ex.getMessage() : ""));
        }));
        startTask(task, "submission-load-targets");
    }

    private void loadFilesForTarget(SubmissionTarget target) {
        submittedFileTable.getItems().clear();
        if (target == null) {
            return;
        }

        setMonitorLoading(true, "Đang tải file đã nộp...");
        Task<List<SubmissionFile>> task = new Task<>() {
            @Override
            protected List<SubmissionFile> call() {
                return submissionService.getFiles(target.getTargetId());
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            List<SubmissionFile> files = task.getValue();
            submittedFileTable.setItems(FXCollections.observableArrayList(files));
            downloadFilesBtn.setDisable(files.isEmpty());
            setMonitorLoading(false, files.isEmpty() ? "Nhóm này chưa nộp file nào." : "");
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            setMonitorLoading(false, "Lỗi tải file đã nộp.");
            AlertUtil.showError("Lỗi tải file đã nộp: " + (ex != null ? ex.getMessage() : ""));
        }));
        startTask(task, "submission-load-files");
    }

    private void handleDownloadFiles() {
        if (selectedMonitorTarget == null) {
            AlertUtil.showError("Vui lòng chọn nhóm cần tải file.");
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Chọn thư mục lưu file đã nộp");
        Stage stage = (Stage) downloadFilesBtn.getScene().getWindow();
        File destination = directoryChooser.showDialog(stage);
        if (destination == null) {
            return;
        }

        setMonitorLoading(true, "Đang tải file về máy...");
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                return submissionService.downloadFilesForTarget(selectedMonitorTarget, destination.toPath());
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            int copied = task.getValue();
            setMonitorLoading(false, "Đã tải " + copied + " file.");
            AlertUtil.showSuccess("Đã tải " + copied + " file về thư mục bạn chọn.");
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            setMonitorLoading(false, "Tải file thất bại.");
            AlertUtil.showError("Không tải được file: " + (ex != null ? ex.getMessage() : ""));
        }));
        startTask(task, "submission-download-files");
    }

    private LocalDateTime readDeadline() {
        LocalDate date = deadlineDatePicker.getValue();
        String timeText = deadlineTimeCombo.getValue();
        if (date == null || timeText == null || timeText.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.of(date, LocalTime.parse(timeText));
        } catch (Exception e) {
            return null;
        }
    }

    private List<SubmissionRequirementTemplate> getSelectedRequirements() {
        List<SubmissionRequirementTemplate> selected = new ArrayList<>();
        for (Map.Entry<CheckBox, SubmissionRequirementTemplate> entry : requirementChecks.entrySet()) {
            if (entry.getKey().isSelected()) {
                selected.add(entry.getValue());
            }
        }
        return selected;
    }

    private List<Integer> getSelectedGroupIds() {
        List<Integer> selected = new ArrayList<>();
        for (Map.Entry<CheckBox, SubmissionRecipientOption> entry : recipientChecks.entrySet()) {
            if (entry.getKey().isSelected()) {
                selected.add(entry.getValue().getGroupId());
            }
        }
        return selected;
    }

    private void setLoading(boolean loading, String message) {
        sendRequestBtn.setDisable(loading);
        statusLabel.setText(message);
    }

    private void setMonitorLoading(boolean loading, String message) {
        refreshMonitorBtn.setDisable(loading);
        downloadFilesBtn.setDisable(loading || selectedMonitorTarget == null || submittedFileTable.getItems().isEmpty());
        monitorStatusLabel.setText(message);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }

    private String formatRequestStatus(int status) {
        return status == 1 ? "Đang mở" : "Đã đóng";
    }

    private String formatTargetStatus(int status) {
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

    private record ScreenData(
            List<SubmissionRequirementTemplate> requirements,
            List<SubmissionRecipientOption> recipients,
            List<SubmissionRequest> requests) {
    }
}
