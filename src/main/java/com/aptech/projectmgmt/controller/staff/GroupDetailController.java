package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.controller.AvatarCellController;
import com.aptech.projectmgmt.controller.SingleActionCellController;
import com.aptech.projectmgmt.controller.TextPromptDialogController;
import com.aptech.projectmgmt.model.GroupMember;
import com.aptech.projectmgmt.model.MemberRole;
import com.aptech.projectmgmt.model.MemberStatus;
import com.aptech.projectmgmt.model.Student;
import com.aptech.projectmgmt.service.GroupService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.SceneManager;
import com.aptech.projectmgmt.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;

public class GroupDetailController {

	@FXML
	private Label groupNameLabel;
	@FXML
	private Button addMemberBtn;
	@FXML
	private TableView<GroupMember> memberTable;
	@FXML
	private TableColumn<GroupMember, String> avatarColumn;
	@FXML
	private TableColumn<GroupMember, String> studentCodeColumn;
	@FXML
	private TableColumn<GroupMember, String> studentNameColumn;
	@FXML
	private TableColumn<GroupMember, String> roleColumn;
	@FXML
	private TableColumn<GroupMember, String> statusColumn;
	@FXML
	private TableColumn<GroupMember, Integer> abandonCountColumn;
	@FXML
	private TableColumn<GroupMember, Void> actionColumn;

	private final GroupService groupService = new GroupService();
	private int groupId;
	private int projectId;
	private int classId;
	private String groupName;
	private final ObservableList<GroupMember> memberList = FXCollections.observableArrayList();
	private boolean readOnlyMode;

	@FXML
	public void initialize() {
		setupTableColumns();
		memberTable.setItems(memberList);
		groupNameLabel.setOnMouseClicked(event -> {
			if (!readOnlyMode && event.getClickCount() == 2) {
				handleRenameGroup();
			}
		});
		updateAccessMode();
	}

	public void initData(int groupId) {
		this.groupId = groupId;
		loadMembers();
	}

	public void initData(int groupId, int projectId, int classId, String groupName) {
		this.groupId = groupId;
		this.projectId = projectId;
		this.classId = classId;
		this.groupName = groupName;
		loadMembers();
	}

	public void setReadOnlyMode(boolean readOnlyMode) {
		this.readOnlyMode = readOnlyMode;
		updateAccessMode();
	}

	private void setupTableColumns() {
		avatarColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentPhotoUrl()));
		avatarColumn.setCellFactory(col -> new TableCell<>() {
			private final Parent avatarView;
			private final AvatarCellController controller;

			{
				try {
					FXMLLoader loader = new FXMLLoader(
							getClass().getResource(com.aptech.projectmgmt.util.SceneManager.AVATAR_CELL));
					avatarView = loader.load();
					controller = loader.getController();
				} catch (Exception ex) {
					throw new IllegalStateException("Khong the tai avatar cell nhom", ex);
				}
			}

			@Override
			protected void updateItem(String photoUrl, boolean empty) {
				super.updateItem(photoUrl, empty);
				GroupMember member = empty || getTableRow() == null ? null : getTableRow().getItem();
				if (member == null) {
					setGraphic(null);
					return;
				}
				controller.setPhoto(photoUrl);
				setGraphic(avatarView);
			}
		});
		studentCodeColumn.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
		studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentFullName"));
		roleColumn.setCellValueFactory(c -> {
			MemberRole r = c.getValue().getRole();
			return new SimpleStringProperty(r == MemberRole.LEADER ? "Truong nhom" : "Thanh vien");
		});
		statusColumn.setCellValueFactory(c -> {
			MemberStatus s = c.getValue().getStatus();
			return new SimpleStringProperty(s == MemberStatus.ACTIVE ? "Dang hoat dong" : "Da bi loai");
		});
		abandonCountColumn.setCellValueFactory(new PropertyValueFactory<>("abandonCount"));
		actionColumn.setCellFactory(col -> createActionCell());
		addMemberBtn.setOnAction(e -> handleAddMember());
	}

	private TableCell<GroupMember, Void> createActionCell() {
		return new TableCell<>() {
			private final Parent actionView;
			private final SingleActionCellController controller;

			{
				try {
					FXMLLoader loader = new FXMLLoader(
							getClass().getResource(com.aptech.projectmgmt.util.SceneManager.SINGLE_ACTION_CELL));
					actionView = loader.load();
					controller = loader.getController();
					controller.setActionText("Huy quyen");
					controller.setOnAction(() -> {
						GroupMember member = getTableRow() != null ? getTableRow().getItem() : null;
						if (member != null) {
							handleExclude(member);
						}
					});
				} catch (Exception ex) {
					throw new IllegalStateException("Khong the tai action cell thanh vien", ex);
				}
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				GroupMember member = empty || getTableRow() == null ? null : getTableRow().getItem();
				if (member == null || readOnlyMode) {
					setGraphic(null);
					return;
				}
				controller.setActionDisabled(member.getStatus() != MemberStatus.ACTIVE);
				setGraphic(actionView);
			}
		};
	}

	private void loadMembers() {
		Task<List<GroupMember>> task = new Task<>() {
			@Override
			protected List<GroupMember> call() {
				return groupService.getMembersByGroup(groupId);
			}
		};
		task.setOnSucceeded(e -> Platform.runLater(() -> {
			memberList.setAll(task.getValue());
			updateGroupNameLabel();
		}));
		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Loi tai thanh vien: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(task).start();
	}

	private void updateGroupNameLabel() {
		String displayName = groupName != null && !groupName.isBlank() ? groupName : "Nhom";
		String suffix = readOnlyMode ? "" : " - double click de doi ten";
		groupNameLabel.setText("Chi tiet " + displayName + " (" + memberList.size() + " thanh vien)" + suffix);
	}

	private void handleRenameGroup() {
		Optional<String> renameResult = showRenameDialog();
		if (renameResult.isEmpty()) {
			return;
		}
		String newName = renameResult.get();
		Task<Void> task = new Task<>() {
			@Override
			protected Void call() {
				groupService.renameGroup(groupId, newName);
				return null;
			}
		};
		task.setOnSucceeded(e -> Platform.runLater(() -> {
			groupName = newName;
			updateGroupNameLabel();
			AlertUtil.showSuccess("Doi ten nhom thanh cong");
		}));
		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Loi doi ten nhom: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(task).start();
	}

	private void handleExclude(GroupMember member) {
		if (readOnlyMode) {
			AlertUtil.showError("Tai khoan giao vien chi duoc xem thong tin nhom");
			return;
		}
		if (!AlertUtil.showConfirm("Xac nhan huy quyen tham gia cua " + member.getStudentFullName() + "?"))
			return;
		Optional<String> reasonResult = showReasonDialog(member.getStudentFullName());
		if (reasonResult.isEmpty()) {
			return;
		}
		var currentStaff = SessionManager.getInstance().getCurrentStaff();
		if (currentStaff == null) {
			AlertUtil.showError("Khong xac dinh duoc staff");
			return;
		}
		String reason = reasonResult.get();

		Task<Void> task = new Task<>() {
			@Override
			protected Void call() {
				groupService.excludeMember(member.getMemberId(), currentStaff.getStaffId(), reason);
				return null;
			}
		};
		task.setOnSucceeded(e -> Platform.runLater(() -> {
			AlertUtil.showSuccess("Da huy quyen tham gia");
			loadMembers();
		}));
		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Loi: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(task).start();
	}

	private void handleAddMember() {
		if (readOnlyMode) {
			AlertUtil.showError("Tai khoan giao vien chi duoc xem thong tin nhom");
			return;
		}
		Task<List<Student>> task = new Task<>() {
			@Override
			protected List<Student> call() {
				return groupService.getAvailableStudentsForClass(classId);
			}
		};
		task.setOnSucceeded(e -> Platform.runLater(() -> showAddMemberDialog(task.getValue())));
		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Khong tai duoc danh sach sinh vien: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(task).start();
	}

	private void showAddMemberDialog(List<Student> students) {
		if (students == null || students.isEmpty()) {
			AlertUtil.showError("Khong con sinh vien nao trong lop de them vao nhom");
			return;
		}
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.GROUP_MEMBER_CREATE_DIALOG));
			Parent content = loader.load();
			GroupMemberCreateDialogController controller = loader.getController();
			boolean hasLeader = groupService.hasActiveLeader(groupId);
			boolean isFirstMember = groupService.countActiveMembers(groupId) == 0;

			controller.setAvailableStudents(students);
			controller.configureRoleOptions(isFirstMember, hasLeader);

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Them sinh vien vao nhom");
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			dialog.getDialogPane().setContent(content);
			dialog.getDialogPane().setPrefWidth(500);

			var result = dialog.showAndWait();
			if (result.isEmpty() || result.get() != ButtonType.OK) {
				return;
			}

			Student selectedStudent = controller.getSelectedStudent();
			MemberRole selectedRole = controller.getSelectedRole();
			Task<Void> addTask = new Task<>() {
				@Override
				protected Void call() {
					groupService.addMemberToGroup(groupId, selectedStudent.getStudentId(), selectedRole);
					return null;
				}
			};
			addTask.setOnSucceeded(e -> Platform.runLater(() -> {
				AlertUtil.showSuccess("Them sinh vien vao nhom thanh cong");
				loadMembers();
			}));
			addTask.setOnFailed(e -> Platform.runLater(() -> {
				Throwable ex = addTask.getException();
				AlertUtil.showError("Loi them sinh vien vao nhom: " + (ex != null ? ex.getMessage() : ""));
			}));
			new Thread(addTask).start();
		} catch (Exception e) {
			AlertUtil.showError("Khong the mo form them sinh vien vao nhom: " + e.getMessage());
		}
	}

	private Optional<String> showReasonDialog(String studentName) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.TEXT_PROMPT_DIALOG));
			Parent content = loader.load();
			TextPromptDialogController controller = loader.getController();
			controller.initData("Ly do huy quyen tham gia", "Nhap ly do huy quyen cua " + studentName, "");

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Huy quyen tham gia");
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			dialog.getDialogPane().setContent(content);
			dialog.getDialogPane().setPrefWidth(520);

			Optional<ButtonType> result = dialog.showAndWait();
			if (result.isEmpty() || result.get() != ButtonType.OK) {
				return Optional.empty();
			}
			String reason = controller.getContent();
			if (reason.isBlank()) {
				AlertUtil.showError("Vui long nhap ly do huy quyen tham gia");
				return Optional.empty();
			}
			return Optional.of(reason);
		} catch (Exception ex) {
			AlertUtil.showError("Khong the mo form nhap ly do: " + ex.getMessage());
			return Optional.empty();
		}
	}

	private Optional<String> showRenameDialog() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.TEXT_PROMPT_DIALOG));
			Parent content = loader.load();
			TextPromptDialogController controller = loader.getController();
			controller.initData("Doi ten nhom", "Nhap ten nhom moi", groupName);

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Doi ten nhom");
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			dialog.getDialogPane().setContent(content);
			dialog.getDialogPane().setPrefWidth(520);

			Optional<ButtonType> result = dialog.showAndWait();
			if (result.isEmpty() || result.get() != ButtonType.OK) {
				return Optional.empty();
			}
			String name = controller.getContent();
			if (name.isBlank()) {
				AlertUtil.showError("Ten nhom khong duoc de trong");
				return Optional.empty();
			}
			return Optional.of(name);
		} catch (Exception ex) {
			AlertUtil.showError("Khong the mo form doi ten nhom: " + ex.getMessage());
			return Optional.empty();
		}
	}

	private void updateAccessMode() {
		if (addMemberBtn != null) {
			addMemberBtn.setVisible(!readOnlyMode);
			addMemberBtn.setManaged(!readOnlyMode);
		}
		if (actionColumn != null) {
			actionColumn.setVisible(!readOnlyMode);
		}
		if (groupNameLabel != null) {
			updateGroupNameLabel();
		}
	}
}
