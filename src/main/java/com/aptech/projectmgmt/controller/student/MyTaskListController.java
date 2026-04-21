package com.aptech.projectmgmt.controller.student;

import com.aptech.projectmgmt.controller.TextPromptDialogController;
import com.aptech.projectmgmt.model.*;
import com.aptech.projectmgmt.repository.TaskRepository;
import com.aptech.projectmgmt.service.GroupService;
import com.aptech.projectmgmt.service.MailService;
import com.aptech.projectmgmt.service.ProjectService;
import com.aptech.projectmgmt.service.QuestionService;
import com.aptech.projectmgmt.service.StaffService;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import com.aptech.projectmgmt.service.MessageService;

public class MyTaskListController {

	@FXML
	private Label projectNameLabel;
	@FXML
	private TableView<TaskViewModel> taskTable;
	@FXML
	private Button addTaskBtn;
	@FXML
	private TableColumn<TaskViewModel, String> titleColumn;
	@FXML
	private TableColumn<TaskViewModel, String> estimatedTimeColumn;
	@FXML
	private TableColumn<TaskViewModel, String> statusColumn;
	@FXML
	private TableColumn<TaskViewModel, String> assignedToColumn;
	@FXML
	private TableColumn<TaskViewModel, String> reviewedByColumn;
	@FXML
	private TableColumn<TaskViewModel, String> revisionNoteColumn;
	@FXML
	private TableColumn<TaskViewModel, Void> actionColumn;

	private final TaskService taskService = new TaskService();
	private final ProjectService projectService = new ProjectService();
	private final GroupService groupService = new GroupService();
	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private int groupId;
	private MemberRole myRole;
	private boolean currentMemberActive;
	private boolean canCreateTask;
	private int currentStudentId;
	private ObservableList<TaskViewModel> taskList = FXCollections.observableArrayList();
	private final QuestionService questionService = new QuestionService();
	private int projectId;
	private Timeline autoRefresh;
	@FXML
	private Button askTeacherBtn;
	private final MailService mailService = new MailService();
	private final StaffService staffService = new StaffService();

	@FXML
	public void initialize() {
		configureColumns();
		setupRowFactory();
		taskTable.setItems(taskList);
		actionColumn.setCellFactory(col -> createActionCell());

		addTaskBtn.setVisible(false);
		addTaskBtn.setManaged(false);
		addTaskBtn.setOnAction(e -> handleAddTask());

		askTeacherBtn.setOnAction(e -> handleAskTeacherGeneral());

		autoRefresh = new Timeline(new KeyFrame(Duration.seconds(30), e -> loadTasks()));
		autoRefresh.setCycleCount(Animation.INDEFINITE);
		autoRefresh.play();

		taskTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
			if (newScene == null) {
				onDestroy();
			}
		});
	}

	@FXML
	private void handleAskTeacherGeneral() {
		TaskViewModel selectedTask = taskTable.getSelectionModel().getSelectedItem();

		if (selectedTask == null) {
			AlertUtil.showError("Vui long chon 1 task truoc khi hoi giao vien");
			return;
		}

		Task<Staff> teacherTask = new Task<>() {
			@Override
			protected Staff call() {
				Project project = projectService.getProjectById(projectId);
				if (project == null || project.getSupervisorId() == null) {
					throw new RuntimeException("Khong tim thay giao vien huong dan cua project");
				}
				return staffService.findById(project.getSupervisorId());
			}
		};

		teacherTask.setOnSucceeded(e -> {
			Staff teacher = teacherTask.getValue();
			if (teacher == null) {
				AlertUtil.showError("Khong tim thay thong tin giao vien");
				return;
			}

			Optional<String> result = showTextPromptDialog("Hoi giao vien", "Nhap noi dung cau hoi gui giao vien",
					"Noi dung cau hoi");

			result.ifPresent(content -> {
				String trimmed = content != null ? content.trim() : "";
				if (trimmed.isEmpty()) {
					AlertUtil.showError("Noi dung cau hoi khong duoc de trong");
					return;
				}

				Task<Boolean> askTask = new Task<>() {
					@Override
					protected Boolean call() {
						questionService.createQuestion(currentStudentId, teacher.getStaffId(), selectedTask.getTaskId(),
								trimmed);

						if (teacher == null) {
							throw new RuntimeException("Khong tim thay giao vien");
						}

						if (teacher.getEmail() == null || teacher.getEmail().isBlank()) {
							throw new RuntimeException("Giao vien chua co email");
						}

						System.out.println("=== ASK MAIL DEBUG ===");
						System.out.println("Teacher ID = " + teacher.getStaffId());
						System.out.println("Teacher email = " + teacher.getEmail());
						System.out.println("Task ID = " + selectedTask.getTaskId());
						System.out.println("Task title = " + selectedTask.getTitle());

						String subject = "[Hoi bai] " + selectedTask.getTitle();
						String body = "Sinh vien gui cau hoi cho giao vien.\n\n" + "Task: " + selectedTask.getTitle()
								+ "\n" + "Sinh vien ID: " + currentStudentId + "\n\n" + "Noi dung cau hoi:\n" + trimmed;

						mailService.sendEmail(teacher.getEmail(), subject, body);
						return true;
					}
				};

				askTask.setOnSucceeded(ev -> Platform.runLater(() -> {
					Boolean mailSent = askTask.getValue();
					if (Boolean.TRUE.equals(mailSent)) {
						AlertUtil.showSuccess("Gui cau hoi thanh cong va da gui email cho giao vien");
					} else {
						AlertUtil.showSuccess("Gui cau hoi thanh cong, nhung chua gui duoc email");
					}
				}));

				askTask.setOnFailed(ev -> Platform.runLater(() -> {
					Throwable ex = askTask.getException();
					AlertUtil.showError(ex != null ? ex.getMessage() : "Gui cau hoi that bai");
				}));

				new Thread(askTask).start();
			});
		});

		teacherTask.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = teacherTask.getException();
			AlertUtil.showError(ex != null ? ex.getMessage() : "Khong lay duoc giao vien huong dan");
		}));

		new Thread(teacherTask).start();
	}

	public void initData(int groupId, int projectId, MemberRole myRole) {
		this.projectId = projectId;
		this.myRole = myRole;
		this.groupId = groupId;
		var student = SessionManager.getInstance().getCurrentStudent();
		if (student != null)
			currentStudentId = student.getStudentId();
		refreshCurrentMemberState();

		Task<Project> task = new Task<>() {
			@Override
			protected Project call() {
				return projectService.getProjectById(projectId);
			}
		};
		task.setOnSucceeded(e -> Platform.runLater(() -> {
			Project p = task.getValue();
			if (p != null)
				projectNameLabel.setText("Project: " + p.getProjectName());
		}));
		new Thread(task).start();

		loadTasks();
	}

	public void onDestroy() {
		if (autoRefresh != null)
			autoRefresh.stop();
	}

	private void configureColumns() {
		titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
		estimatedTimeColumn.setCellValueFactory(c -> new SimpleStringProperty(
				formatDateRange(c.getValue().getEstimatedStartDate(), c.getValue().getEstimatedEndDate())));
		statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusDisplay()));
		assignedToColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAssignedToDisplayName()));
		reviewedByColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getReviewedByDisplayName()));
		revisionNoteColumn
				.setCellValueFactory(c -> new SimpleStringProperty(nullToEmpty(c.getValue().getLatestRevisionNote())));
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
		boolean assignedToCurrentStudent = currentMemberActive && taskViewModel.getAssignedToId() != null
				&& taskViewModel.getAssignedToId() == currentStudentId;
		boolean reviewedByCurrentStudent = currentMemberActive && taskViewModel.getReviewedById() != null
				&& taskViewModel.getReviewedById() == currentStudentId;
		boolean hasEstimatedStart = taskViewModel.getEstimatedStartDate() != null;

		boolean showDisabledStart = taskViewModel.getStatus() == TaskStatus.PENDING && assignedToCurrentStudent
				&& hasEstimatedStart && now.isBefore(taskViewModel.getEstimatedStartDate());

		boolean canStart = taskViewModel.getStatus() == TaskStatus.PENDING && assignedToCurrentStudent
				&& hasEstimatedStart && !now.isBefore(taskViewModel.getEstimatedStartDate())
				&& now.isBefore(taskViewModel.getEstimatedStartDate().plusHours(1));

		boolean canSubmitForReview = taskViewModel.getStatus() == TaskStatus.IN_PROGRESS && assignedToCurrentStudent;
		boolean canRequestRevision = taskViewModel.getStatus() == TaskStatus.REVIEWING && reviewedByCurrentStudent;
		boolean canConfirmCompleted = canRequestRevision;
		boolean canSubmitRevised = taskViewModel.getStatus() == TaskStatus.REVISING && assignedToCurrentStudent;
		boolean canViewDetail = currentMemberActive && myRole == MemberRole.LEADER;
		boolean canAssign = currentMemberActive && myRole == MemberRole.LEADER
				&& taskViewModel.getStatus() == TaskStatus.PENDING && taskViewModel.getAssignedToId() == null;

		controller.configure(showDisabledStart, canStart, canSubmitForReview, canRequestRevision, canConfirmCompleted,
				canSubmitRevised, canViewDetail, canAssign, () -> handleStartTask(taskViewModel.getTaskId()),
				() -> handleSubmitForReview(taskViewModel.getTaskId()),
				() -> handleRequestRevision(taskViewModel.getTaskId()),
				() -> handleConfirmCompleted(taskViewModel.getTaskId()),
				() -> handleSubmitRevised(taskViewModel.getTaskId()), () -> handleViewDetail(taskViewModel),
				() -> handleAssignTask(taskViewModel));
	}

	private void setupRowFactory() {
		taskTable.setRowFactory(tv -> new TableRow<>() {
			@Override
			protected void updateItem(TaskViewModel item, boolean empty) {
				super.updateItem(item, empty);
				getStyleClass().removeAll("row-green", "row-red", "row-yellow");
				if (item == null || empty)
					return;
				switch (item.getDisplayColor()) {
				case GREEN:
					getStyleClass().add("row-green");
					break;
				case RED:
					getStyleClass().add("row-red");
					break;
				case YELLOW:
					getStyleClass().add("row-yellow");
					break;
				default:
					break;
				}
			}
		});
	}

	private void handleAssignTask(TaskViewModel taskVm) {
		Task<List<GroupMember>> loadMembersTask = new Task<>() {
			@Override
			protected List<GroupMember> call() {
				return groupService.getActiveMembersByGroup(groupId);
			}
		};

		loadMembersTask.setOnSucceeded(e -> Platform.runLater(() -> {
			try {
				List<GroupMember> members = loadMembersTask.getValue();
				if (members == null || members.isEmpty()) {
					AlertUtil.showError("Khong co thanh vien hoat dong de phan cong");
					return;
				}

				Dialog<ButtonType> dialog = new Dialog<>();
				dialog.setTitle("Phan cong task");
				dialog.setHeaderText("Chon nguoi thuc hien va thoi gian du kien");

				ButtonType saveButtonType = new ButtonType("Luu", ButtonBar.ButtonData.OK_DONE);
				dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

				ComboBox<GroupMember> memberCombo = new ComboBox<>();
				memberCombo.getItems().addAll(members);
				memberCombo.setValue(members.get(0));
				memberCombo.setCellFactory(list -> new ListCell<>() {
					@Override
					protected void updateItem(GroupMember item, boolean empty) {
						super.updateItem(item, empty);
						setText(empty || item == null ? "" : item.getStudentFullName());
					}
				});
				memberCombo.setButtonCell(new ListCell<>() {
					@Override
					protected void updateItem(GroupMember item, boolean empty) {
						super.updateItem(item, empty);
						setText(empty || item == null ? "" : item.getStudentFullName());
					}
				});

				DatePicker startDatePicker = new DatePicker(
						taskVm.getEstimatedStartDate() != null ? taskVm.getEstimatedStartDate().toLocalDate() : null);
				Spinner<Integer> startHour = new Spinner<>(0, 23,
						taskVm.getEstimatedStartDate() != null ? taskVm.getEstimatedStartDate().getHour() : 8);
				Spinner<Integer> startMinute = new Spinner<>(0, 59,
						taskVm.getEstimatedStartDate() != null ? taskVm.getEstimatedStartDate().getMinute() : 0);

				DatePicker endDatePicker = new DatePicker(
						taskVm.getEstimatedEndDate() != null ? taskVm.getEstimatedEndDate().toLocalDate() : null);
				Spinner<Integer> endHour = new Spinner<>(0, 23,
						taskVm.getEstimatedEndDate() != null ? taskVm.getEstimatedEndDate().getHour() : 17);
				Spinner<Integer> endMinute = new Spinner<>(0, 59,
						taskVm.getEstimatedEndDate() != null ? taskVm.getEstimatedEndDate().getMinute() : 0);

				GridPane grid = new GridPane();
				grid.setHgap(10);
				grid.setVgap(10);

				grid.add(new Label("Nguoi thuc hien:"), 0, 0);
				grid.add(memberCombo, 1, 0);

				grid.add(new Label("Bat dau du kien:"), 0, 1);
				grid.add(new HBox(8, startDatePicker, startHour, startMinute), 1, 1);

				grid.add(new Label("Ket thuc du kien:"), 0, 2);
				grid.add(new HBox(8, endDatePicker, endHour, endMinute), 1, 2);

				dialog.getDialogPane().setContent(grid);

				final ReassignTaskData[] selectedData = new ReassignTaskData[1];

				Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
				saveButton.addEventFilter(ActionEvent.ACTION, event -> {
					GroupMember selected = memberCombo.getValue();
					if (selected == null) {
						AlertUtil.showError("Phai chon nguoi thuc hien");
						event.consume();
						return;
					}

					if (taskVm.getReviewedById() != null && taskVm.getReviewedById() == selected.getStudentId()) {
						AlertUtil.showError("Nguoi thuc hien khong duoc trung voi nguoi kiem tra");
						event.consume();
						return;
					}

					if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
						AlertUtil.showError("Phai chon du ngay bat dau va ket thuc");
						event.consume();
						return;
					}

					LocalDateTime start = LocalDateTime.of(startDatePicker.getValue(),
							java.time.LocalTime.of(startHour.getValue(), startMinute.getValue()));

					LocalDateTime end = LocalDateTime.of(endDatePicker.getValue(),
							java.time.LocalTime.of(endHour.getValue(), endMinute.getValue()));

					if (!end.isAfter(start)) {
						AlertUtil.showError("Thoi gian ket thuc phai sau thoi gian bat dau");
						event.consume();
						return;
					}

					selectedData[0] = new ReassignTaskData(selected.getStudentId(), start, end);
				});

				Optional<ButtonType> result = dialog.showAndWait();
				if (result.isEmpty() || result.get() != saveButtonType || selectedData[0] == null) {
					return;
				}

				ReassignTaskData data = selectedData[0];

				Task<Void> assignTask = new Task<>() {
					@Override
					protected Void call() {
						taskService.reassignTask(taskVm.getTaskId(), data.getAssignedStudentId(),
								data.getEstimatedStartDate(), data.getEstimatedEndDate(), currentStudentId);
						return null;
					}
				};

				assignTask.setOnSucceeded(ev -> Platform.runLater(() -> {
					AlertUtil.showSuccess("Phan cong task thanh cong");
					loadTasks();
				}));

				assignTask.setOnFailed(ev -> Platform.runLater(() -> {
					Throwable ex = assignTask.getException();
					AlertUtil.showError(ex != null ? ex.getMessage() : "Loi phan cong task");
				}));

				new Thread(assignTask).start();

			} catch (Exception ex) {
				AlertUtil.showError("Khong the phan cong task: " + ex.getMessage());
			}
		}));

		loadMembersTask.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = loadMembersTask.getException();
			AlertUtil.showError("Khong tai duoc thanh vien nhom: " + (ex != null ? ex.getMessage() : ""));
		}));

		new Thread(loadMembersTask).start();
	}

	private void loadTasks() {
		if (groupId <= 0)
			return;

		Task<List<TaskViewModel>> task = new Task<>() {
			@Override
			protected List<TaskViewModel> call() {
				if (myRole == MemberRole.LEADER) {
					return taskService.getTasksByGroup(groupId);
				}
				return taskService.getTasksByStudentInGroup(groupId, currentStudentId);
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
		return (start != null ? start.format(dateTimeFormatter) : "?") + " - "
				+ (end != null ? end.format(dateTimeFormatter) : "?");
	}

	private String nullToEmpty(String value) {
		return value != null ? value : "";
	}

	private void handleStartTask(int taskId) {
		Task<Void> task = new Task<>() {
			@Override
			protected Void call() {
				taskService.startTask(taskId, currentStudentId);
				return null;
			}
		};
		task.setOnSucceeded(e -> Platform.runLater(() -> {
			AlertUtil.showSuccess("Da bat dau task");
			loadTasks();
		}));
		task.setOnFailed(e -> Platform.runLater(() -> AlertUtil.showError(task.getException().getMessage())));
		new Thread(task).start();
	}

	private void handleSubmitForReview(int taskId) {
		Task<Void> task = new Task<>() {
			@Override
			protected Void call() {
				taskService.submitForReview(taskId, currentStudentId);
				return null;
			}
		};
		task.setOnSucceeded(e -> Platform.runLater(() -> {
			AlertUtil.showSuccess("Da gui de kiem tra");
			loadTasks();
		}));
		task.setOnFailed(e -> Platform.runLater(() -> AlertUtil.showError(task.getException().getMessage())));
		new Thread(task).start();
	}

	private void handleRequestRevision(int taskId) {
		Optional<String> result = showTextPromptDialog("Yeu cau chinh sua", "Nhap noi dung yeu cau chinh sua",
				"Noi dung");
		result.ifPresent(note -> {
			if (note.trim().isEmpty()) {
				AlertUtil.showError("Noi dung khong duoc de trong");
				return;
			}
			Task<Void> task = new Task<>() {
				@Override
				protected Void call() {
					taskService.requestRevision(taskId, currentStudentId, note);
					return null;
				}
			};
			task.setOnSucceeded(e -> Platform.runLater(() -> {
				AlertUtil.showSuccess("Da gui yeu cau chinh sua");
				loadTasks();
			}));
			task.setOnFailed(e -> Platform.runLater(() -> AlertUtil.showError(task.getException().getMessage())));
			new Thread(task).start();
		});
	}

	private void handleConfirmCompleted(int taskId) {
		if (!AlertUtil.showConfirm("Xac nhan task da hoan thanh?"))
			return;
		Task<Void> task = new Task<>() {
			@Override
			protected Void call() {
				taskService.confirmCompleted(taskId, currentStudentId);
				return null;
			}
		};
		task.setOnSucceeded(e -> Platform.runLater(() -> {
			AlertUtil.showSuccess("Task da hoan thanh");
			loadTasks();
		}));
		task.setOnFailed(e -> Platform.runLater(() -> AlertUtil.showError(task.getException().getMessage())));
		new Thread(task).start();
	}

	private void handleSubmitRevised(int taskId) {
		Task<Void> task = new Task<>() {
			@Override
			protected Void call() {
				taskService.submitRevised(taskId, currentStudentId);
				return null;
			}
		};
		task.setOnSucceeded(e -> Platform.runLater(() -> {
			AlertUtil.showSuccess("Da gui lai de kiem tra");
			loadTasks();
		}));
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

		askTeacherBtn.setVisible(currentMemberActive);
		askTeacherBtn.setManaged(currentMemberActive);
		askTeacherBtn.setDisable(!currentMemberActive);
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
				return new TaskDetailData(taskService.getStatusHistory(taskVm.getTaskId()),
						taskService.getRevisions(taskVm.getTaskId()), taskService.getAbandonLogs(taskVm.getTaskId()));
			}
		};
		loadTask.setOnSucceeded(e -> Platform.runLater(() -> {
			TaskDetailData taskDetailData = loadTask.getValue();
			StringBuilder detailBuilder = new StringBuilder();
			detailBuilder.append("=== LICH SU TRANG THAI ===\n");
			for (TaskStatusHistory history : taskDetailData.history()) {
				detailBuilder
						.append(history.getChangedAt() != null ? history.getChangedAt().format(dateTimeFormatter) : "?")
						.append(" | ").append(history.getFromStatus() != null ? history.getFromStatus() : "MOI")
						.append(" -> ").append(history.getToStatus()).append(" | ")
						.append(history.getChangerName() != null ? history.getChangerName() : "").append("\n");
			}
			detailBuilder.append("\n=== YEU CAU CHINH SUA ===\n");
			for (TaskRevision revision : taskDetailData.revisions()) {
				detailBuilder.append(
						revision.getCreatedAt() != null ? revision.getCreatedAt().format(dateTimeFormatter) : "?")
						.append(" | ").append(revision.getNote()).append("\n");
			}
			detailBuilder.append("\n=== LICH SU BO TASK ===\n");
			for (TaskAbandonLog abandonLog : taskDetailData.abandonLogs()) {
				detailBuilder
						.append(abandonLog.getAbandonedAt() != null
								? abandonLog.getAbandonedAt().format(dateTimeFormatter)
								: "?")
						.append(" | ").append(abandonLog.getStudentName() != null ? abandonLog.getStudentName() : "")
						.append("\n");
			}
			showTaskDetailModal(taskVm.getTitle(), detailBuilder.toString());
		}));
		loadTask.setOnFailed(e -> Platform
				.runLater(() -> AlertUtil.showError("Loi tai chi tiet task: " + loadTask.getException().getMessage())));
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

	private static class ReassignTaskData {
		private final int assignedStudentId;
		private final LocalDateTime estimatedStartDate;
		private final LocalDateTime estimatedEndDate;

		public ReassignTaskData(int assignedStudentId, LocalDateTime estimatedStartDate,
				LocalDateTime estimatedEndDate) {
			this.assignedStudentId = assignedStudentId;
			this.estimatedStartDate = estimatedStartDate;
			this.estimatedEndDate = estimatedEndDate;
		}

		public int getAssignedStudentId() {
			return assignedStudentId;
		}

		public LocalDateTime getEstimatedStartDate() {
			return estimatedStartDate;
		}

		public LocalDateTime getEstimatedEndDate() {
			return estimatedEndDate;
		}
	}

	private record TaskDetailData(List<TaskStatusHistory> history, List<TaskRevision> revisions,
			List<TaskAbandonLog> abandonLogs) {
	}
}
