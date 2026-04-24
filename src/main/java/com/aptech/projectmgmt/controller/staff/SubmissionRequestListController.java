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
        titleField.setText("Required materials for final project submission after presentation");
        descriptionArea.setText("Please submit all required files before the deadline.");
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
            statusLabel.setText("No logged-in staff account was found.");
            sendRequestBtn.setDisable(true);
            return;
        }

        setLoading(true, "Loading data...");
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
            setLoading(false, "Failed to load data.");
            setMonitorLoading(false, "Failed to load data.");
            AlertUtil.showError("Failed to load project submission requests: " + (ex != null ? ex.getMessage() : ""));
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
            requirementBox.getChildren().add(new Label("No submission requirement templates were found in the database."));
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
            recipientBox.getChildren().add(new Label("No groups with a leader were found in the classes you manage."));
        }
    }

    private void handleSendRequest() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        LocalDateTime deadline = readDeadline();
        List<SubmissionRequirementTemplate> requirements = getSelectedRequirements();
        List<Integer> groupIds = getSelectedGroupIds();

        if (deadline == null) {
            AlertUtil.showError("Please select a valid deadline.");
            return;
        }

        setLoading(true, "Sending request...");
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                return submissionService.createRequest(title, description, deadline, staffId, requirements, groupIds);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            setLoading(false, "Project submission request sent.");
            AlertUtil.showSuccess("The request was created and notifications were sent to the selected group leaders.");
            loadScreenData();
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            setLoading(false, "Request sending failed.");
            AlertUtil.showError("Could not create the request: " + (ex != null ? ex.getMessage() : ""));
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

        setMonitorLoading(true, "Loading groups...");
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
            setMonitorLoading(false, targets.isEmpty() ? "No groups have received this request yet." : "");
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            setMonitorLoading(false, "Failed to load groups.");
            AlertUtil.showError("Failed to load recipient groups: " + (ex != null ? ex.getMessage() : ""));
        }));
        startTask(task, "submission-load-targets");
    }

    private void loadFilesForTarget(SubmissionTarget target) {
        submittedFileTable.getItems().clear();
        if (target == null) {
            return;
        }

        setMonitorLoading(true, "Loading submitted files...");
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
            setMonitorLoading(false, files.isEmpty() ? "This group has not submitted any files yet." : "");
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            setMonitorLoading(false, "Failed to load submitted files.");
            AlertUtil.showError("Failed to load submitted files: " + (ex != null ? ex.getMessage() : ""));
        }));
        startTask(task, "submission-load-files");
    }

    private void handleDownloadFiles() {
        if (selectedMonitorTarget == null) {
            AlertUtil.showError("Please select a group to download files for.");
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose a folder to save the submitted files");
        Stage stage = (Stage) downloadFilesBtn.getScene().getWindow();
        File destination = directoryChooser.showDialog(stage);
        if (destination == null) {
            return;
        }

        setMonitorLoading(true, "Downloading files...");
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                return submissionService.downloadFilesForTarget(selectedMonitorTarget, destination.toPath());
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            int copied = task.getValue();
            setMonitorLoading(false, "Downloaded " + copied + " files.");
            AlertUtil.showSuccess("Downloaded " + copied + " files to the selected folder.");
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            setMonitorLoading(false, "Download failed.");
            AlertUtil.showError("Could not download the files: " + (ex != null ? ex.getMessage() : ""));
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
        return status == 1 ? "Open" : "Closed";
    }

    private String formatTargetStatus(int status) {
        return switch (status) {
            case 3 -> "Submitted";
            case 2 -> "In Progress";
            default -> "Incomplete";
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
