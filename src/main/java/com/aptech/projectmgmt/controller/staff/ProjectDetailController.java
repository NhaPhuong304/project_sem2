package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.model.Project;
import javafx.scene.layout.StackPane;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class ProjectDetailController {

	@FXML
	private TextField projectNameField;
	@FXML
	private TextField semesterField;
	@FXML
	private TextField supervisorField;
	@FXML
	private DatePicker startDatePicker;
	@FXML
	private DatePicker endDatePicker;
	@FXML
	private DatePicker reportDatePicker;
	@FXML
	private TextArea descriptionArea;
	@FXML
	private Button editBtn;
	@FXML
	private Button saveBtn;
	@FXML
	private Button cancelBtn;
	@FXML
	private Button completeBtn;
	@FXML
	private Button addGroupBtn;
	@FXML
	private StackPane groupContentPane;
	@FXML
	private Label groupPlaceholderLabel;

	@FXML
	private ComboBox<String> taskMemberCombo;
	@FXML
	private ComboBox<String> taskStatusFilterCombo;
	@FXML
	private StackPane taskContentPane;
	@FXML
	private Label taskPlaceholderLabel;
	@FXML
	private Tab groupTab;

	private final ObservableList<String> taskMembers = FXCollections.observableArrayList();
	private final ProjectService projectService = new ProjectService();
	private final GroupService groupService = new GroupService();
	private final ObservableList<ProjectGroup> groups = FXCollections.observableArrayList();
	private final ObservableList<String> taskStatusFilters = FXCollections.observableArrayList("All", "Pending",
			"In Progress", "Under Review", "Being Revised", "Completed");
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
				AlertUtil.showError("You do not have permission to view this project");
				return;
			}

			populateProjectInfo();
			updateAccessMode();
			loadGroups();
		}));

		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Error loading project: " + (ex != null ? ex.getMessage() : ""));
		}));

		new Thread(task).start();
	}

	private void configureProjectForm() {
		descriptionArea.setWrapText(true);
		taskContentPane.getChildren().setAll(taskPlaceholderLabel);
		groupContentPane.getChildren().setAll(groupPlaceholderLabel);
		setEditMode(false);
	}

	private void loadGroupDetailIntoTab() {
		if (groups.isEmpty()) {
			groupContentPane.getChildren().setAll(groupPlaceholderLabel);
			groupPlaceholderLabel.setText("This project does not have a group yet");
			return;
		}

		ProjectGroup group = groups.get(0);

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.GROUP_DETAIL));
			Parent content = loader.load();

			GroupDetailController controller = loader.getController();
			controller.setReadOnlyMode(readOnlyMode);

			if (currentProject != null) {
				controller.initData(group.getGroupId(), currentProject.getProjectId(), currentProject.getClassId(),
						group.getGroupName());
			} else {
				controller.initData(group.getGroupId());
			}

			groupContentPane.getChildren().setAll(content);
		} catch (Exception e) {
			AlertUtil.showError("Error loading group details: " + e.getMessage());
		}
	}

	private void configureGroupViews() {
		addGroupBtn.setOnAction(e -> handleAddGroup());

		taskMemberCombo.setItems(taskMembers);
		taskMemberCombo.setOnAction(e -> loadSelectedMemberTasks());

		taskStatusFilterCombo.setItems(taskStatusFilters);
		taskStatusFilterCombo.getSelectionModel().selectFirst();
		taskStatusFilterCombo.setOnAction(e -> {
			if (activeTaskListController != null) {
				activeTaskListController.setStatusFilter(taskStatusFilterCombo.getValue());
			}
		});

		groupTab.setOnSelectionChanged(event -> {
			if (groupTab.isSelected()) {
				loadGroupDetailIntoTab();
			}
		});
	}

	private void loadMembersOfFirstGroup() {
		if (groups.isEmpty()) {
			taskMembers.clear();
			taskMemberCombo.getSelectionModel().clearSelection();
			return;
		}

		ProjectGroup group = groups.get(0);

		Task<List<String>> task = new Task<>() {
			@Override
			protected List<String> call() {
				return groupService.getMemberNamesByGroupId(group.getGroupId());
			}
		};

		task.setOnSucceeded(e -> Platform.runLater(() -> {
			taskMembers.clear();
			taskMembers.add("All");
			taskMembers.addAll(task.getValue());

			taskMemberCombo.getSelectionModel().selectFirst();
			loadSelectedMemberTasks();
		}));

		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Failed to load the member list: " + (ex != null ? ex.getMessage() : ""));
		}));

		new Thread(task).start();
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
		boolean hasNotStarted = currentProject != null && (currentProject.getStartDate() == null
				|| !currentProject.getStartDate().isBefore(java.time.LocalDate.now()));
		projectNameField.setEditable(effectiveEditable);
		semesterField.setEditable(effectiveEditable);
		descriptionArea.setEditable(effectiveEditable);
		startDatePicker.setDisable(!effectiveEditable || !hasNotStarted);
		endDatePicker.setDisable(!effectiveEditable || !hasNotStarted);
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
				taskMembers.clear();
				taskMemberCombo.getSelectionModel().clearSelection();

				taskContentPane.getChildren().setAll(taskPlaceholderLabel);
				taskPlaceholderLabel.setText("This project does not have a group yet");

				groupContentPane.getChildren().setAll(groupPlaceholderLabel);
				groupPlaceholderLabel.setText("This project does not have a group yet");

				if (activeTaskListController != null) {
					activeTaskListController.onDestroy();
					activeTaskListController = null;
				}
				return;
			}

			loadMembersOfFirstGroup();

			if (groupTab.isSelected()) {
				loadGroupDetailIntoTab();
			}
		}));

		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Error loading group list: " + (ex != null ? ex.getMessage() : ""));
		}));

		new Thread(task).start();
	}

	private void loadSelectedMemberTasks() {
		String selectedMember = taskMemberCombo.getSelectionModel().getSelectedItem();
		if (selectedMember == null || selectedMember.isBlank()) {
			taskContentPane.getChildren().setAll(taskPlaceholderLabel);
			taskPlaceholderLabel.setText("Select a member to view the task list");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.TASK_LIST));
			Parent content = loader.load();
			TaskListController controller = loader.getController();
			controller.setReadOnlyMode(readOnlyMode);

			ProjectGroup group = groups.isEmpty() ? null : groups.get(0);
			if (group != null) {
				controller.initData(group.getGroupId());
			}

			controller.setMemberFilter(selectedMember);
			controller.setStatusFilter(taskStatusFilterCombo.getValue());

			if (activeTaskListController != null) {
				activeTaskListController.onDestroy();
			}

			activeTaskListController = controller;
			taskContentPane.getChildren().setAll(content);
		} catch (Exception e) {
			AlertUtil.showError("Error loading task list: " + e.getMessage());
		}
	}

	private void handleSave() {
		if (readOnlyMode) {
			AlertUtil.showError("Teacher accounts are only allowed to view project information");
			return;
		}
		if (currentProject == null)
			return;
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
			AlertUtil.showSuccess("Saved successfully");
			setEditMode(false);
			updateAccessMode();
		}));
		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Error: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(task).start();
	}

	private void handleMarkCompleted() {
		if (readOnlyMode) {
			AlertUtil.showError("Teacher accounts are only allowed to view project information");
			return;
		}
		if (currentProject == null) {
			return;
		}
		if (!projectService.canMarkCompleted(currentProject)) {
			AlertUtil.showError("Chi duoc danh dau hoan thanh khi project da qua han ngay bao cao");
			return;
		}
		if (!AlertUtil.showConfirm("Mark this project as completed?")) {
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
			AlertUtil.showSuccess("The project has been marked as completed");
			updateAccessMode();
		}));
		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Failed to mark as completed: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(task).start();
	}

	private void handleAddGroup() {
		AlertUtil.showError("The current schema only allows each project to be associated with one group. Please create a new project if you need a different group.");
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
