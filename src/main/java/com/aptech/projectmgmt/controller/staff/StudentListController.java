package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.controller.AvatarCellController;
import com.aptech.projectmgmt.model.Student;
import com.aptech.projectmgmt.service.ClassService;
import com.aptech.projectmgmt.service.StudentService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.SceneManager;
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

public class StudentListController {

	@FXML
	private Label classNameLabel;
	@FXML
	private TableView<Student> studentTable;
	@FXML
	private Button createAccountsBtn;
	@FXML
	private Button addStudentBtn;
	@FXML
	private TableColumn<Student, String> avatarColumn;
	@FXML
	private TableColumn<Student, String> studentCodeColumn;
	@FXML
	private TableColumn<Student, String> fullNameColumn;
	@FXML
	private TableColumn<Student, String> emailColumn;
	@FXML
	private TableColumn<Student, String> accountStatusColumn;

	private final StudentService studentService = new StudentService();
	private final ClassService classService = new ClassService();
	private int classId;
	private ObservableList<Student> studentList = FXCollections.observableArrayList();
	private boolean readOnlyMode;

	@FXML
	public void initialize() {
		setupTableColumns();
		studentTable.setItems(studentList);
		createAccountsBtn.setVisible(false);
		createAccountsBtn.setManaged(false);
		createAccountsBtn.setOnAction(e -> handleCreateAccounts());
		addStudentBtn.setOnAction(e -> handleAddStudent());
		refreshActionButtons();
	}

	public void setReadOnlyMode(boolean readOnlyMode) {
		this.readOnlyMode = readOnlyMode;
		refreshActionButtons();
	}

	public void initData(int classId) {
		this.classId = classId;
		Task<String> task = new Task<>() {
			@Override
			protected String call() {
				var sc = classService.getClassById(classId);
				return sc != null ? sc.getClassName() : "Class #" + classId;
			}
		};
		task.setOnSucceeded(e -> Platform.runLater(() -> classNameLabel.setText("Danh sach SV - " + task.getValue())));
		new Thread(task).start();
		loadStudents();
	}

	private void setupTableColumns() {
		avatarColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhotoUrl()));
		avatarColumn.setCellFactory(col -> new TableCell<>() {
			private final Parent avatarView;
			private final AvatarCellController controller;

			{
				try {
					FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.AVATAR_CELL));
					avatarView = loader.load();
					controller = loader.getController();
				} catch (Exception ex) {
					throw new IllegalStateException("Could not load the student avatar cell", ex);
				}
			}

			@Override
			protected void updateItem(String photoUrl, boolean empty) {
				super.updateItem(photoUrl, empty);
				if (empty || getTableRow() == null || getTableRow().getItem() == null) {
					setGraphic(null);
					return;
				}
				controller.setPhoto(photoUrl);
				setGraphic(avatarView);
			}
		});
		studentCodeColumn.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
		fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
		emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
		accountStatusColumn.setCellValueFactory(c -> {
			Integer accId = c.getValue().getAccountId();
			String status = accId != null ? "Co tai khoan" : "Chua co tai khoan";
			return new SimpleStringProperty(status);
		});
	}

	private void loadStudents() {
		Task<List<Student>> task = new Task<>() {
			@Override
			protected List<Student> call() {
				return studentService.getStudentsByClass(classId);
			}
		};
		task.setOnSucceeded(e -> Platform.runLater(() -> {
			studentList.setAll(task.getValue());
			updateCreateAccountsButton();
		}));
		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Failed to load the student list: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(task).start();
	}

	private void updateCreateAccountsButton() {
		boolean hasStudentWithoutAccount = studentList.stream().anyMatch(student -> student.getAccountId() == null);
		createAccountsBtn.setVisible(!readOnlyMode && hasStudentWithoutAccount);
		createAccountsBtn.setManaged(!readOnlyMode && hasStudentWithoutAccount);
		createAccountsBtn.setDisable(readOnlyMode || !hasStudentWithoutAccount);
		createAccountsBtn.setTooltip(!readOnlyMode && hasStudentWithoutAccount
				? new Tooltip("Create accounts for legacy records that do not have accounts yet")
				: null);
		refreshActionButtons();
	}

	private void handleCreateAccounts() {
		if (readOnlyMode) {
			AlertUtil.showError("Teacher accounts can only view the student list");
			return;
		}
		if (!AlertUtil.showConfirm("Create accounts for all students who do not have an account yet?"))
			return;
		createAccountsBtn.setDisable(true);
		Task<Integer> task = new Task<>() {
			@Override
			protected Integer call() {
				return studentService.createAccountsForClass(classId);
			}
		};
		task.setOnSucceeded(e -> Platform.runLater(() -> {
			createAccountsBtn.setDisable(false);
			int count = task.getValue();
			AlertUtil.showSuccess("Da tao " + count + " tai khoan thanh cong");
			loadStudents();
		}));
		task.setOnFailed(e -> Platform.runLater(() -> {
			createAccountsBtn.setDisable(false);
			Throwable ex = task.getException();
			AlertUtil.showError("Error: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(task).start();
	}

	private void handleAddStudent() {
		if (readOnlyMode) {
			AlertUtil.showError("Teacher accounts can only view the student list");
			return;
		}
		addStudentBtn.setDisable(true);
		Task<List<Student>> studentsTask = new Task<>() {
			@Override
			protected List<Student> call() {
				return studentService.getUnassignedStudents();
			}
		};
		studentsTask.setOnSucceeded(e -> Platform.runLater(() -> {
			addStudentBtn.setDisable(false);
			openAssignStudentDialog(studentsTask.getValue());
		}));
		studentsTask.setOnFailed(e -> Platform.runLater(() -> {
			addStudentBtn.setDisable(false);
			Throwable ex = studentsTask.getException();
			AlertUtil.showError(
					"Could not load students without a class: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(studentsTask).start();
	}

	private void openAssignStudentDialog(List<Student> unassignedStudents) {
		if (unassignedStudents == null || unassignedStudents.isEmpty()) {
			AlertUtil.showError("There are no students left without a class assignment");
			return;
		}
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.STUDENT_ASSIGN_DIALOG));
			Parent content = loader.load();
			StudentAssignDialogController controller = loader.getController();
			controller.setStudents(unassignedStudents);

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Add Student to Class");
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			dialog.getDialogPane().setContent(content);

			Optional<ButtonType> result = dialog.showAndWait();
			if (result.isEmpty() || result.get() != ButtonType.OK) {
				return;
			}

			Student selectedStudent = controller.getSelectedStudent();
			Task<Void> task = new Task<>() {
				@Override
				protected Void call() {
					studentService.assignStudentToClass(selectedStudent.getStudentId(), classId);
					return null;
				}
			};
			task.setOnSucceeded(e -> Platform.runLater(() -> {
				AlertUtil.showSuccess("Student added to the class successfully");
				loadStudents();
			}));
			task.setOnFailed(e -> Platform.runLater(() -> {
				Throwable ex = task.getException();
				AlertUtil.showError("Error: " + (ex != null ? ex.getMessage() : ""));
			}));
			new Thread(task).start();
		} catch (Exception e) {
			AlertUtil.showError("Could not open the add student form: " + e.getMessage());
		}
	}

	private void refreshActionButtons() {
		var account = com.aptech.projectmgmt.util.SessionManager.getInstance().getCurrentAccount();
		boolean isStaff = account != null && account.getRole() == com.aptech.projectmgmt.model.UserRole.STAFF;

		if (addStudentBtn != null) {
			boolean canAddStudent = isStaff && !readOnlyMode;
			addStudentBtn.setVisible(canAddStudent);
			addStudentBtn.setManaged(canAddStudent);
			addStudentBtn.setDisable(!canAddStudent);
		}

		if (createAccountsBtn != null) {
			boolean hasStudentWithoutAccount = studentList.stream().anyMatch(student -> student.getAccountId() == null);

			boolean canCreateAccounts = isStaff && !readOnlyMode && hasStudentWithoutAccount;
			createAccountsBtn.setVisible(canCreateAccounts);
			createAccountsBtn.setManaged(canCreateAccounts);
			createAccountsBtn.setDisable(!canCreateAccounts);
			createAccountsBtn.setTooltip(
					canCreateAccounts ? new Tooltip("Create accounts for legacy records that do not have accounts yet") : null);
		}
	}
}
