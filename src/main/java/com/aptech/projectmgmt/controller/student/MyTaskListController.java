package com.aptech.projectmgmt.controller.student;

import com.aptech.projectmgmt.controller.TextPromptDialogController;
import com.aptech.projectmgmt.model.*;
import com.aptech.projectmgmt.service.GroupService;
import com.aptech.projectmgmt.service.ProjectService;
import com.aptech.projectmgmt.service.TaskService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.SceneManager;
import com.aptech.projectmgmt.util.SessionManager;
import com.aptech.projectmgmt.controller.staff.TaskDetailController;
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
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MyTaskListController {

    @FXML private Label projectNameLabel;
    @FXML private TableView<TaskViewModel> taskTable;
    @FXML private Button addTaskBtn;
    @FXML private TableColumn<TaskViewModel, String> titleColumn;
    @FXML private TableColumn<TaskViewModel, String> estimatedTimeColumn;
    @FXML private TableColumn<TaskViewModel, String> statusColumn;
    @FXML private TableColumn<TaskViewModel, String> assignedToColumn;
    @FXML private TableColumn<TaskViewModel, String> reviewedByColumn;
    @FXML private TableColumn<TaskViewModel, String> revisionNoteColumn;
    @FXML private TableColumn<TaskViewModel, Void> actionColumn;

    private final TaskService taskService = new TaskService();
    private final ProjectService projectService = new ProjectService();
    private final GroupService groupService = new GroupService();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private int groupId;
    private int projectId;
    private MemberRole myRole;
    private boolean currentMemberActive;
    private boolean canCreateTask;
    private int currentStudentId;
    private ObservableList<TaskViewModel> taskList = FXCollections.observableArrayList();
    private Timeline autoRefresh;

    @FXML
    public void initialize() {
        configureColumns();
        setupRowFactory();
        taskTable.setItems(taskList);
        actionColumn.setCellFactory(col -> createActionCell());
        addTaskBtn.setVisible(false);
        addTaskBtn.setManaged(false);
        addTaskBtn.setOnAction(e -> handleAddTask());

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

    public void initData(int groupId, int projectId, MemberRole myRole) {
        this.groupId = groupId;
        this.projectId = projectId;
        this.myRole = myRole;
        var student = SessionManager.getInstance().getCurrentStudent();
        if (student != null) currentStudentId = student.getStudentId();
        refreshCurrentMemberState();

        Task<Project> task = new Task<>() {
            @Override
            protected Project call() {
                return projectService.getProjectById(projectId);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            Project p = task.getValue();
            if (p != null) projectNameLabel.setText("Project: " + p.getProjectName());
        }));
        new Thread(task).start();

        loadTasks();
    }

    public void onDestroy() {
        if (autoRefresh != null) autoRefresh.stop();
    }

    private void configureColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        estimatedTimeColumn.setCellValueFactory(c -> new SimpleStringProperty(formatDateRange(
                c.getValue().getEstimatedStartDate(),
                c.getValue().getEstimatedEndDate())));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusDisplay()));
        assignedToColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAssignedToDisplayName()));
        reviewedByColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReviewedByDisplayName()));
        revisionNoteColumn.setCellValueFactory(c -> new SimpleStringProperty(nullToEmpty(c.getValue().getLatestRevisionNote())));
    }

    private TableCell<TaskViewModel, Void> createActionCell() {
        return new TableCell<>() {
            private final Parent actionView;
            private final MyTaskActionCellController controller;

            {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.MY_TASK_ACTION_CELL));
                    actionView = loader.load();
                    controller = loader.getController();
                } catch (Exception ex) {
                    throw new IllegalStateException("Khong the tai action cell task sinh vien", ex);
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                TaskViewModel taskViewModel = empty || getTableRow() == null ? null : getTableRow().getItem();
                if (taskViewModel == null) {
                    setGraphic(null);
                    return;
                }
                bindActionCell(controller, taskViewModel);
                setGraphic(actionView);
            }
        };
    }

    private void bindActionCell(MyTaskActionCellController controller, TaskViewModel taskViewModel) {
        LocalDateTime now = LocalDateTime.now();
        boolean assignedToCurrentStudent = currentMemberActive
                && taskViewModel.getAssignedToId() != null
                && taskViewModel.getAssignedToId() == currentStudentId;
        boolean reviewedByCurrentStudent = currentMemberActive
                && taskViewModel.getReviewedById() != null
                && taskViewModel.getReviewedById() == currentStudentId;
        boolean hasEstimatedStart = taskViewModel.getEstimatedStartDate() != null;

        boolean showDisabledStart = taskViewModel.getStatus() == TaskStatus.PENDING
                && assignedToCurrentStudent
                && hasEstimatedStart
                && now.isBefore(taskViewModel.getEstimatedStartDate());

        boolean canStart = taskViewModel.getStatus() == TaskStatus.PENDING
                && assignedToCurrentStudent
                && hasEstimatedStart
                && !now.isBefore(taskViewModel.getEstimatedStartDate())
                && now.isBefore(taskViewModel.getEstimatedStartDate().plusHours(1));

        boolean canSubmitForReview = taskViewModel.getStatus() == TaskStatus.IN_PROGRESS
                && assignedToCurrentStudent;
        boolean canRequestRevision = taskViewModel.getStatus() == TaskStatus.REVIEWING
                && reviewedByCurrentStudent;
        boolean canConfirmCompleted = canRequestRevision;
        boolean canSubmitRevised = taskViewModel.getStatus() == TaskStatus.REVISING
                && assignedToCurrentStudent;
        boolean canViewDetail = currentMemberActive && myRole == MemberRole.LEADER;

        controller.configure(
                showDisabledStart,
                canStart,
                canSubmitForReview,
                canRequestRevision,
                canConfirmCompleted,
                canSubmitRevised,
                canViewDetail,
                () -> handleStartTask(taskViewModel.getTaskId()),
                () -> handleSubmitForReview(taskViewModel.getTaskId()),
                () -> handleRequestRevision(taskViewModel.getTaskId()),
                () -> handleConfirmCompleted(taskViewModel.getTaskId()),
                () -> handleSubmitRevised(taskViewModel.getTaskId()),
                () -> handleViewDetail(taskViewModel));
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
            taskList.setAll(task.getValue());
            refreshCurrentMemberState();
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Loi tai tasks: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private String formatDateRange(LocalDateTime start, LocalDateTime end) {
        return (start != null ? start.format(dateTimeFormatter) : "?")
                + " - "
                + (end != null ? end.format(dateTimeFormatter) : "?");
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private void handleStartTask(int taskId) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() { taskService.startTask(taskId, currentStudentId); return null; }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> { AlertUtil.showSuccess("Da bat dau task"); loadTasks(); }));
        task.setOnFailed(e -> Platform.runLater(() -> AlertUtil.showError(task.getException().getMessage())));
        new Thread(task).start();
    }

    private void handleSubmitForReview(int taskId) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() { taskService.submitForReview(taskId, currentStudentId); return null; }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> { AlertUtil.showSuccess("Da gui de kiem tra"); loadTasks(); }));
        task.setOnFailed(e -> Platform.runLater(() -> AlertUtil.showError(task.getException().getMessage())));
        new Thread(task).start();
    }

    private void handleRequestRevision(int taskId) {
        Optional<String> result = showTextPromptDialog(
                "Yeu cau chinh sua",
                "Nhap noi dung yeu cau chinh sua",
                "Noi dung");
        result.ifPresent(note -> {
            if (note.trim().isEmpty()) { AlertUtil.showError("Noi dung khong duoc de trong"); return; }
            Task<Void> task = new Task<>() {
                @Override protected Void call() { taskService.requestRevision(taskId, currentStudentId, note); return null; }
            };
            task.setOnSucceeded(e -> Platform.runLater(() -> { AlertUtil.showSuccess("Da gui yeu cau chinh sua"); loadTasks(); }));
            task.setOnFailed(e -> Platform.runLater(() -> AlertUtil.showError(task.getException().getMessage())));
            new Thread(task).start();
        });
    }

    private void handleConfirmCompleted(int taskId) {
        if (!AlertUtil.showConfirm("Xac nhan task da hoan thanh?")) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() { taskService.confirmCompleted(taskId, currentStudentId); return null; }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> { AlertUtil.showSuccess("Task da hoan thanh"); loadTasks(); }));
        task.setOnFailed(e -> Platform.runLater(() -> AlertUtil.showError(task.getException().getMessage())));
        new Thread(task).start();
    }

    private void handleSubmitRevised(int taskId) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() { taskService.submitRevised(taskId, currentStudentId); return null; }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> { AlertUtil.showSuccess("Da gui lai de kiem tra"); loadTasks(); }));
        task.setOnFailed(e -> Platform.runLater(() -> AlertUtil.showError(task.getException().getMessage())));
        new Thread(task).start();
    }

    private void handleAddTask() {
        if (!canCreateTask) {
            AlertUtil.showError("Ban khong con quyen tao task trong nhom nay");
            return;
        }
        Task<List<GroupMember>> loadMembersTask = new Task<>() {
            @Override
            protected List<GroupMember> call() {
                return groupService.getActiveMembersByGroup(groupId);
            }
        };
        loadMembersTask.setOnSucceeded(e -> Platform.runLater(() -> showAddTaskDialog(loadMembersTask.getValue())));
        loadMembersTask.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = loadMembersTask.getException();
            AlertUtil.showError("Khong tai duoc thanh vien nhom: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(loadMembersTask).start();
    }

    private void refreshCurrentMemberState() {
        if (groupId <= 0 || currentStudentId <= 0) {
            updateAddTaskPermission(null);
            return;
        }
        Task<GroupMember> memberTask = new Task<>() {
            @Override
            protected GroupMember call() {
                return groupService.getMemberByStudentAndGroup(currentStudentId, groupId);
            }
        };
        memberTask.setOnSucceeded(e -> Platform.runLater(() -> updateAddTaskPermission(memberTask.getValue())));
        memberTask.setOnFailed(e -> Platform.runLater(() -> updateAddTaskPermission(null)));
        new Thread(memberTask).start();
    }

    private void updateAddTaskPermission(GroupMember member) {
        currentMemberActive = member != null && member.getStatus() == MemberStatus.ACTIVE;
        if (member != null) {
            myRole = member.getRole();
        }
        canCreateTask = currentMemberActive && myRole == MemberRole.LEADER;
        addTaskBtn.setVisible(canCreateTask);
        addTaskBtn.setManaged(canCreateTask);
        addTaskBtn.setDisable(!canCreateTask);
    }

    private void showAddTaskDialog(List<GroupMember> members) {
        try {
            if (members == null || members.size() < 2) {
                AlertUtil.showError("Nhom phai co toi thieu 2 sinh vien dang hoat dong moi duoc tao task");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.TASK_CREATE_DIALOG));
            Parent content = loader.load();
            TaskCreateDialogController controller = loader.getController();
            controller.setMembers(members);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Them cong viec moi");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setPrefWidth(560);
            if (taskTable.getScene() != null) {
                dialog.initOwner(taskTable.getScene().getWindow());
            }

            final com.aptech.projectmgmt.model.Task[] pendingTask = new com.aptech.projectmgmt.model.Task[1];
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                try {
                    pendingTask[0] = controller.buildTask(groupId, currentStudentId);
                } catch (IllegalArgumentException ex) {
                    AlertUtil.showError(ex.getMessage());
                    event.consume();
                }
            });

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
            if (pendingTask[0] == null) {
                AlertUtil.showError("Khong doc duoc thong tin task vua nhap");
                return;
            }
            com.aptech.projectmgmt.model.Task newTask = pendingTask[0];
            Task<Void> taskOp = new Task<>() {
                @Override
                protected Void call() {
                    taskService.createTask(newTask);
                    return null;
                }
            };
            taskOp.setOnSucceeded(e -> Platform.runLater(() -> {
                AlertUtil.showSuccess("Them task thanh cong");
                loadTasks();
            }));
            taskOp.setOnFailed(e -> Platform.runLater(() -> AlertUtil.showError(taskOp.getException().getMessage())));
            new Thread(taskOp).start();
        } catch (IllegalArgumentException ex) {
            AlertUtil.showError(ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Khong the mo form them task: " + ex.getMessage());
        }
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
        loadTask.setOnFailed(e -> Platform.runLater(() ->
                AlertUtil.showError("Loi tai chi tiet task: " + loadTask.getException().getMessage())));
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
        } catch (Exception ex) {
            AlertUtil.showError("Khong the mo chi tiet task: " + ex.getMessage());
        }
    }

    private record TaskDetailData(
            List<TaskStatusHistory> history,
            List<TaskRevision> revisions,
            List<TaskAbandonLog> abandonLogs
    ) {}
}
