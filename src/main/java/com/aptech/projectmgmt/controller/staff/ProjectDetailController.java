package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.model.Project;
import com.aptech.projectmgmt.model.ProjectGroup;
import com.aptech.projectmgmt.service.GroupService;
import com.aptech.projectmgmt.service.ProjectService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.SceneManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class ProjectDetailController {

    @FXML private TextField projectNameField;
    @FXML private TextField semesterField;
    @FXML private TextField supervisorField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private DatePicker reportDatePicker;
    @FXML private TextArea descriptionArea;
    @FXML private Button editBtn;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Button completeBtn;
    @FXML private Button addGroupBtn;
    @FXML private ListView<ProjectGroup> groupListView;
    @FXML private ComboBox<ProjectGroup> taskGroupCombo;
    @FXML private ComboBox<String> taskStatusFilterCombo;
    @FXML private StackPane taskContentPane;
    @FXML private Label taskPlaceholderLabel;

    private final ProjectService projectService = new ProjectService();
    private final GroupService groupService = new GroupService();
    private final ObservableList<ProjectGroup> groups = FXCollections.observableArrayList();
    private final ObservableList<String> taskStatusFilters = FXCollections.observableArrayList(
            "Tat ca",
            "Cho xu ly",
            "Dang thuc hien",
            "Dang kiem tra",
            "Dang chinh sua",
            "Hoan thanh"
    );
    private int projectId;
    private Project currentProject;
    private TaskListController activeTaskListController;
    private boolean readOnlyMode;
    private Integer teacherStaffId;

    @FXML
    public void initialize() {
        configureProjectForm();
        configureGroupViews();
        updateAccessMode();

        editBtn.setOnAction(e -> {
            if (currentProject != null) {
                setEditMode(true);
            }
        });

        saveBtn.setOnAction(e -> handleSave());
        cancelBtn.setOnAction(e -> {
            populateProjectInfo();
            setEditMode(false);
        });
        completeBtn.setOnAction(e -> handleMarkCompleted());
    }

    public void initData(int projectId) {
        this.projectId = projectId;
        loadProject();
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
        updateAccessMode();
    }

    public void setTeacherStaffId(Integer teacherStaffId) {
        this.teacherStaffId = teacherStaffId;
    }

    private void loadProject() {
        Task<Project> task = new Task<>() {
            @Override
            protected Project call() {
                if (readOnlyMode && teacherStaffId != null) {
                    return projectService.getProjectByAdvisor(projectId, teacherStaffId);
                }
                return projectService.getProjectById(projectId);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            currentProject = task.getValue();
            if (currentProject == null) {
                AlertUtil.showError("Ban khong co quyen xem project nay");
                return;
            }
            populateProjectInfo();
            updateAccessMode();
            loadGroups();
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Loi tai project: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private void configureProjectForm() {
        descriptionArea.setWrapText(true);
        taskContentPane.getChildren().setAll(taskPlaceholderLabel);
        setEditMode(false);
    }

    private void configureGroupViews() {
        groupListView.setItems(groups);
        groupListView.setPlaceholder(new Label("Project nay chua co nhom"));
        groupListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProjectGroup item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getGroupName());
            }
        });
        addGroupBtn.setOnAction(e -> handleAddGroup());
        groupListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ProjectGroup selected = groupListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openGroupDetail(selected.getGroupId(), selected.getGroupName());
                }
            }
        });

        taskGroupCombo.setItems(groups);
        taskGroupCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProjectGroup item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getGroupName());
            }
        });
        taskGroupCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ProjectGroup item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getGroupName());
            }
        });
        taskGroupCombo.setOnAction(e -> loadSelectedGroupTasks());

        taskStatusFilterCombo.setItems(taskStatusFilters);
        taskStatusFilterCombo.getSelectionModel().selectFirst();
        taskStatusFilterCombo.setOnAction(e -> {
            if (activeTaskListController != null) {
                activeTaskListController.setStatusFilter(taskStatusFilterCombo.getValue());
            }
        });
    }

    private void populateProjectInfo() {
        if (currentProject == null) {
            return;
        }
        projectNameField.setText(currentProject.getProjectName());
        semesterField.setText(currentProject.getSemester());
        supervisorField.setText(currentProject.getSupervisorName());
        startDatePicker.setValue(currentProject.getStartDate());
        endDatePicker.setValue(currentProject.getEndDate());
        reportDatePicker.setValue(currentProject.getReportDate());
        descriptionArea.setText(currentProject.getDescription());
    }

    private void setEditMode(boolean editable) {
        boolean effectiveEditable = editable && !readOnlyMode;
        projectNameField.setEditable(effectiveEditable);
        semesterField.setEditable(effectiveEditable);
        descriptionArea.setEditable(effectiveEditable);
        startDatePicker.setDisable(!effectiveEditable);
        endDatePicker.setDisable(!effectiveEditable);
        reportDatePicker.setDisable(!effectiveEditable);
        supervisorField.setEditable(false);
        saveBtn.setVisible(effectiveEditable);
        saveBtn.setManaged(effectiveEditable);
        cancelBtn.setVisible(effectiveEditable);
        cancelBtn.setManaged(effectiveEditable);
        editBtn.setVisible(!readOnlyMode && !effectiveEditable);
        editBtn.setManaged(!readOnlyMode && !effectiveEditable);
    }

    private void loadGroups() {
        Task<List<ProjectGroup>> task = new Task<>() {
            @Override
            protected List<ProjectGroup> call() {
                return groupService.getGroupsByProject(projectId);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            groups.setAll(task.getValue());
            if (groups.isEmpty()) {
                taskGroupCombo.getSelectionModel().clearSelection();
                taskContentPane.getChildren().setAll(taskPlaceholderLabel);
                taskPlaceholderLabel.setText("Project nay chua co nhom");
                if (activeTaskListController != null) {
                    activeTaskListController.onDestroy();
                    activeTaskListController = null;
                }
                return;
            }

            ProjectGroup currentSelection = taskGroupCombo.getSelectionModel().getSelectedItem();
            if (currentSelection == null || groups.stream().noneMatch(group -> group.getGroupId() == currentSelection.getGroupId())) {
                taskGroupCombo.getSelectionModel().selectFirst();
            } else {
                taskGroupCombo.getSelectionModel().select(currentSelection);
            }
            loadSelectedGroupTasks();
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Loi tai danh sach nhom: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private void loadSelectedGroupTasks() {
        ProjectGroup selectedGroup = taskGroupCombo.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            taskContentPane.getChildren().setAll(taskPlaceholderLabel);
            taskPlaceholderLabel.setText("Chon nhom de xem danh sach cong viec");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.TASK_LIST));
            Parent content = loader.load();
            TaskListController controller = loader.getController();
            controller.setReadOnlyMode(readOnlyMode);
            controller.initData(selectedGroup.getGroupId());
            controller.setStatusFilter(taskStatusFilterCombo.getValue());
            if (activeTaskListController != null) {
                activeTaskListController.onDestroy();
            }
            activeTaskListController = controller;
            taskContentPane.getChildren().setAll(content);
        } catch (Exception e) {
            AlertUtil.showError("Loi tai danh sach cong viec: " + e.getMessage());
        }
    }

    private void openGroupDetail(int groupId, String groupName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.GROUP_DETAIL));
            Parent root = loader.load();
            GroupDetailController controller = loader.getController();
            controller.setReadOnlyMode(readOnlyMode);
            if (currentProject != null) {
                controller.initData(groupId, currentProject.getProjectId(), currentProject.getClassId(), groupName);
            } else {
                controller.initData(groupId);
            }

            Stage modal = new Stage();
            modal.setTitle("Chi tiet Nhom: " + groupName);
            modal.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            modal.setScene(scene);
            modal.showAndWait();
            loadGroups();
        } catch (Exception e) {
            AlertUtil.showError("Loi mo chi tiet nhom: " + e.getMessage());
        }
    }

    private void handleSave() {
        if (readOnlyMode) {
            AlertUtil.showError("Tai khoan giao vien chi duoc xem thong tin project");
            return;
        }
        if (currentProject == null) return;
        currentProject.setProjectName(projectNameField.getText().trim());
        currentProject.setSemester(semesterField.getText().trim());
        currentProject.setDescription(descriptionArea.getText() != null ? descriptionArea.getText().trim() : null);
        currentProject.setStartDate(startDatePicker.getValue());
        currentProject.setEndDate(endDatePicker.getValue());
        currentProject.setReportDate(reportDatePicker.getValue());
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                projectService.updateProject(currentProject);
                return null;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            AlertUtil.showSuccess("Luu thanh cong");
            setEditMode(false);
            updateAccessMode();
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Loi luu: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private void handleMarkCompleted() {
        if (readOnlyMode) {
            AlertUtil.showError("Tai khoan giao vien chi duoc xem thong tin project");
            return;
        }
        if (currentProject == null) {
            return;
        }
        if (!projectService.canMarkCompleted(currentProject)) {
            AlertUtil.showError("Chi duoc danh dau hoan thanh khi project da qua han ngay bao cao");
            return;
        }
        if (!AlertUtil.showConfirm("Danh dau project nay la hoan thanh?")) {
            return;
        }
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                projectService.markProjectCompleted(currentProject.getProjectId());
                return null;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            currentProject.setStatus(com.aptech.projectmgmt.model.ProjectStatus.COMPLETED);
            AlertUtil.showSuccess("Project da duoc danh dau hoan thanh");
            updateAccessMode();
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Loi danh dau hoan thanh: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private void handleAddGroup() {
        AlertUtil.showError("Schema moi chi cho moi project gan voi 1 nhom. Hay tao project moi neu can nhom khac.");
    }

    private void updateAccessMode() {
        if (readOnlyMode) {
            setEditMode(false);
        } else if (editBtn != null) {
            editBtn.setVisible(true);
            editBtn.setManaged(true);
        }
        if (addGroupBtn != null) {
            boolean canAddGroup = !readOnlyMode && currentProject != null && currentProject.getGroupId() <= 0;
            addGroupBtn.setVisible(canAddGroup);
            addGroupBtn.setManaged(canAddGroup);
        }
        if (completeBtn != null) {
            boolean canComplete = !readOnlyMode && projectService.canMarkCompleted(currentProject);
            completeBtn.setVisible(canComplete);
            completeBtn.setManaged(canComplete);
            completeBtn.setDisable(!canComplete);
        }
    }
}
