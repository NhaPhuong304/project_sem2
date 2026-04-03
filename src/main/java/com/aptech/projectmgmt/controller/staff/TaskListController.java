package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.controller.PersonDisplayCellController;
import com.aptech.projectmgmt.controller.TextPromptDialogController;
import com.aptech.projectmgmt.model.*;
import com.aptech.projectmgmt.service.TaskService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.SceneManager;
import com.aptech.projectmgmt.util.SessionManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TaskListController {
    @FXML private TableView<TaskViewModel> taskTable;
    @FXML private TableColumn<TaskViewModel, String> titleColumn;
    @FXML private TableColumn<TaskViewModel, String> estimatedTimeColumn;
    @FXML private TableColumn<TaskViewModel, String> actualTimeColumn;
    @FXML private TableColumn<TaskViewModel, String> statusColumn;
    @FXML private TableColumn<TaskViewModel, String> assignedToColumn;
    @FXML private TableColumn<TaskViewModel, String> reviewedByColumn;
    @FXML private TableColumn<TaskViewModel, String> createdByColumn;
    @FXML private TableColumn<TaskViewModel, String> latestRevisionColumn;
    @FXML private TableColumn<TaskViewModel, Void> actionColumn;

    private final TaskService taskService = new TaskService();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private int groupId;
    private ObservableList<TaskViewModel> taskList = FXCollections.observableArrayList();
    private List<TaskViewModel> loadedTasks = List.of();
    private String statusFilter = "Tat ca";
    private Timeline autoRefresh;
    private boolean readOnlyMode;

    @FXML
    public void initialize() {
        configureColumns();
        setupRowFactory();
        taskTable.getStyleClass().add("task-table");
        taskTable.setItems(taskList);
        actionColumn.setCellFactory(col -> createActionCell());

        autoRefresh = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> loadTasks())
        );
        autoRefresh.setCycleCount(Animation.INDEFINITE);
        autoRefresh.play();
        taskTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                onDestroy();
            }
        });
    }

    public void initData(int groupId) {
        this.groupId = groupId;
        loadTasks();
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
    }

    public void onDestroy() {
        if (autoRefresh != null) autoRefresh.stop();
    }

    private void configureColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setCellFactory(col -> createBlackTextCell());
        estimatedTimeColumn.setCellValueFactory(c -> new SimpleStringProperty(formatDateRange(
                c.getValue().getEstimatedStartDate(),
                c.getValue().getEstimatedEndDate(),
                "?")));
        estimatedTimeColumn.setCellFactory(col -> createBlackTextCell());
        actualTimeColumn.setCellValueFactory(c -> new SimpleStringProperty(formatDateRange(
                c.getValue().getActualStartDate(),
                c.getValue().getActualEndDate(),
                "-")));
        actualTimeColumn.setCellFactory(col -> createBlackTextCell());
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusDisplay()));
        statusColumn.setCellFactory(col -> createStatusCell());
        assignedToColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAssignedToDisplayName()));
        assignedToColumn.setCellFactory(col -> createAssignedPersonCell());
        reviewedByColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReviewedByDisplayName()));
        reviewedByColumn.setCellFactory(col -> createBlackTextCell());
        createdByColumn.setCellValueFactory(c -> new SimpleStringProperty(nullToEmpty(c.getValue().getCreatedByName())));
        createdByColumn.setCellFactory(col -> createBlackTextCell());
        latestRevisionColumn.setCellValueFactory(c -> new SimpleStringProperty(nullToEmpty(c.getValue().getLatestRevisionNote())));
        latestRevisionColumn.setCellFactory(col -> createBlackTextCell());
    }

    private TableCell<TaskViewModel, String> createBlackTextCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                setText(item != null ? item : "");
                setTextFill(Color.BLACK);
            }
        };
    }

    private TableCell<TaskViewModel, String> createStatusCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll(
                        "status-pending",
                        "status-inprogress",
                        "status-reviewing",
                        "status-revising",
                        "status-completed"
                );
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                TaskViewModel taskViewModel = getTableRow().getItem();
                setText(taskViewModel.getStatusDisplay());
                setTextFill(Color.BLACK);
                switch (taskViewModel.getStatus()) {
                    case PENDING -> getStyleClass().add("status-pending");
                    case IN_PROGRESS -> getStyleClass().add("status-inprogress");
                    case REVIEWING -> getStyleClass().add("status-reviewing");
                    case REVISING -> getStyleClass().add("status-revising");
                    case COMPLETED -> getStyleClass().add("status-completed");
                    default -> {
                    }
                }
            }
        };
    }

    private TableCell<TaskViewModel, String> createAssignedPersonCell() {
        return new TableCell<>() {
            private final Parent personView;
            private final PersonDisplayCellController controller;

            {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.PERSON_DISPLAY_CELL));
                    personView = loader.load();
                    controller = loader.getController();
                } catch (Exception ex) {
                    throw new IllegalStateException("Khong the tai person cell", ex);
                }
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                TaskViewModel taskViewModel = empty || getTableRow() == null ? null : getTableRow().getItem();
                if (taskViewModel == null) {
                    setGraphic(null);
                    return;
                }
                controller.setPerson(taskViewModel.getAssignedToDisplayName(), taskViewModel.getAssignedToPhoto());
                setGraphic(personView);
            }
        };
    }

    private TableCell<TaskViewModel, Void> createActionCell() {
        return new TableCell<>() {
            private final Parent actionView;
            private final TaskActionCellController controller;

            {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.STAFF_TASK_ACTION_CELL));
                    actionView = loader.load();
                    controller = loader.getController();
                    controller.setOnMessage(() -> {
                        TaskViewModel taskViewModel = getCurrentTask();
                        if (taskViewModel != null) {
                            handleSendMessage(taskViewModel);
                        }
                    });
                    controller.setOnDetail(() -> {
                        TaskViewModel taskViewModel = getCurrentTask();
                        if (taskViewModel != null) {
                            handleViewDetail(taskViewModel);
                        }
                    });
                } catch (Exception ex) {
                    throw new IllegalStateException("Khong the tai action cell task staff", ex);
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                TaskViewModel taskViewModel = empty ? null : getCurrentTask();
                if (taskViewModel == null) {
                    setGraphic(null);
                    return;
                }
                boolean canSendReminder = taskViewModel.getDisplayColor() == DisplayColor.YELLOW
                        && !readOnlyMode
                        && taskViewModel.getAssignedToId() != null
                        && !taskViewModel.isReminderSent();
                controller.setReminderVisible(canSendReminder);
                setGraphic(actionView);
            }

            private TaskViewModel getCurrentTask() {
                return getTableRow() != null ? getTableRow().getItem() : null;
            }
        };
    }

    private void setupRowFactory() {
        taskTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(TaskViewModel item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-green", "row-red", "row-yellow");
                if (item == null || empty) return;
                switch (item.getDisplayColor()) {
                    case GREEN: getStyleClass().add("row-green"); break;
                    case RED: getStyleClass().add("row-red"); break;
                    case YELLOW: getStyleClass().add("row-yellow"); break;
                    default: break;
                }
            }
        });
    }

    private void loadTasks() {
        if (groupId <= 0) return;
        Task<List<TaskViewModel>> task = new Task<>() {
            @Override
            protected List<TaskViewModel> call() {
                return taskService.getTasksByGroup(groupId);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            loadedTasks = new ArrayList<>(task.getValue());
            applyFilters();
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Loi tai tasks: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private String formatDateRange(java.time.LocalDateTime start, java.time.LocalDateTime end, String fallback) {
        return (start != null ? start.format(dateTimeFormatter) : fallback)
                + " - "
                + (end != null ? end.format(dateTimeFormatter) : fallback);
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter != null ? statusFilter : "Tat ca";
        applyFilters();
    }

    private void applyFilters() {
        if (loadedTasks == null) {
            taskList.clear();
            return;
        }
        if (statusFilter == null || statusFilter.equals("Tat ca")) {
            taskList.setAll(loadedTasks);
            return;
        }
        taskList.setAll(loadedTasks.stream()
                .filter(taskViewModel -> statusFilter.equals(taskViewModel.getStatusDisplay()))
                .toList());
    }

    private void handleSendMessage(TaskViewModel task) {
        if (task.getAssignedToId() == null) {
            AlertUtil.showError("Task chua co nguoi duoc phan cong");
            return;
        }
        Optional<String> result = showTextPromptDialog(
                "Nhan tin nhac nho",
                "Gui tin nhan nhac nho cho " + task.getAssignedToName(),
                "Noi dung");
        result.ifPresent(content -> {
            if (content.trim().isEmpty()) { AlertUtil.showError("Noi dung khong duoc de trong"); return; }
            var staff = SessionManager.getInstance().getCurrentStaff();
            if (staff == null) { AlertUtil.showError("Khong xac dinh duoc staff"); return; }
            Task<Void> sendTask = new Task<>() {
                @Override
                protected Void call() {
                    taskService.sendReminderMessage(staff.getStaffId(), task.getAssignedToId(), task.getTaskId(), content.trim());
                    return null;
                }
            };
            sendTask.setOnSucceeded(e -> Platform.runLater(() -> {
                AlertUtil.showSuccess("Da gui nhac nho");
                loadTasks();
            }));
            sendTask.setOnFailed(e -> Platform.runLater(() -> AlertUtil.showError("Loi: " + sendTask.getException().getMessage())));
            new Thread(sendTask).start();
        });
    }

    private void handleViewDetail(TaskViewModel taskVm) {
        Task<TaskDetailData> loadTask = new Task<>() {
            @Override
            protected TaskDetailData call() {
                return new TaskDetailData(
                        taskService.getStatusHistory(taskVm.getTaskId()),
                        taskService.getRevisions(taskVm.getTaskId()),
                        taskService.getAbandonLogs(taskVm.getTaskId())
                );
            }
        };
        loadTask.setOnSucceeded(e -> Platform.runLater(() -> {
            TaskDetailData taskDetailData = loadTask.getValue();
            StringBuilder detailBuilder = new StringBuilder();
            detailBuilder.append("=== LICH SU TRANG THAI ===\n");
            for (TaskStatusHistory history : taskDetailData.history()) {
                detailBuilder.append(history.getChangedAt() != null ? history.getChangedAt().format(dateTimeFormatter) : "?")
                        .append(" | ")
                        .append(history.getFromStatus() != null ? history.getFromStatus() : "MOI")
                        .append(" -> ")
                        .append(history.getToStatus())
                        .append(" | ")
                        .append(history.getChangerName() != null ? history.getChangerName() : "")
                        .append("\n");
            }
            detailBuilder.append("\n=== YEU CAU CHINH SUA ===\n");
            for (TaskRevision revision : taskDetailData.revisions()) {
                detailBuilder.append(revision.getCreatedAt() != null ? revision.getCreatedAt().format(dateTimeFormatter) : "?")
                        .append(" | ")
                        .append(revision.getNote())
                        .append("\n");
            }
            detailBuilder.append("\n=== LICH SU BO TASK ===\n");
            for (TaskAbandonLog abandonLog : taskDetailData.abandonLogs()) {
                detailBuilder.append(abandonLog.getAbandonedAt() != null ? abandonLog.getAbandonedAt().format(dateTimeFormatter) : "?")
                        .append(" | ")
                        .append(abandonLog.getStudentName() != null ? abandonLog.getStudentName() : "")
                        .append("\n");
            }
            showTaskDetailModal(taskVm.getTitle(), detailBuilder.toString());
        }));
        loadTask.setOnFailed(e -> Platform.runLater(() -> AlertUtil.showError("Loi tai chi tiet: " + loadTask.getException().getMessage())));
        new Thread(loadTask).start();
    }

    private Optional<String> showTextPromptDialog(String title, String headerText, String promptText) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.TEXT_PROMPT_DIALOG));
            Parent content = loader.load();
            TextPromptDialogController controller = loader.getController();
            controller.initData(headerText, promptText, "");

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(title);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setPrefWidth(540);
            if (taskTable.getScene() != null) {
                dialog.initOwner(taskTable.getScene().getWindow());
            }

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                return Optional.of(controller.getContent());
            }
        } catch (Exception ex) {
            AlertUtil.showError("Khong the mo form nhap noi dung: " + ex.getMessage());
        }
        return Optional.empty();
    }

    private void showTaskDetailModal(String taskTitle, String detailText) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.TASK_DETAIL));
            Parent root = loader.load();
            TaskDetailController controller = loader.getController();
            controller.initData(taskTitle, detailText);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle("Chi tiet Task: " + taskTitle);
            Scene scene = new Scene(root);
            String css = getClass().getResource("/css/style.css") != null
                    ? getClass().getResource("/css/style.css").toExternalForm()
                    : null;
            if (css != null) {
                scene.getStylesheets().add(css);
            }
            modal.setScene(scene);
            if (taskTable.getScene() != null && taskTable.getScene().getWindow() instanceof Stage owner) {
                modal.initOwner(owner);
            }
            modal.showAndWait();
        } catch (Exception e) {
            AlertUtil.showError("Khong the mo chi tiet task: " + e.getMessage());
        }
    }

    private record TaskDetailData(
            List<TaskStatusHistory> history,
            List<TaskRevision> revisions,
            List<TaskAbandonLog> abandonLogs
    ) {}
}
