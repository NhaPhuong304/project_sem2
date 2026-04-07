package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.controller.AvatarCellController;
import com.aptech.projectmgmt.model.SchoolClass;
import com.aptech.projectmgmt.service.ClassService;
import javafx.scene.control.TableColumn;
import com.aptech.projectmgmt.model.Student;
import com.aptech.projectmgmt.model.StudentCreationResult;
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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class StudentManagementController {

	@FXML
	private TableView<Student> studentTable;
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
	private TableColumn<Student, String> classNameColumn;
	@FXML
	private TableColumn<Student, String> accountStatusColumn;
	@FXML
	private TableColumn<Student, Void> actionColumn;

	private final ClassService classService = new ClassService();
	private final StudentService studentService = new StudentService();
	private final ObservableList<Student> students = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		setupTableColumns();
		studentTable.setItems(students);
		addStudentBtn.setOnAction(e -> handleAddStudent());
		loadStudents();
	}

	private void setupTableColumns() {
		actionColumn.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(null));
		actionColumn.setCellFactory(col -> new TableCell<>() {
			private final Button transferBtn = new Button("Transfer");
			private final Button lockBtn = new Button("Lock");
			private final Button unlockBtn = new Button("Unlock");
			private final javafx.scene.layout.HBox actionBox = new javafx.scene.layout.HBox(8);

			{
				transferBtn.setOnAction(e -> {
					Student student = getTableView().getItems().get(getIndex());
					handleTransferStudent(student);
				});

				lockBtn.setOnAction(e -> {
					Student student = getTableView().getItems().get(getIndex());
					handleLockStudent(student);
				});

				unlockBtn.setOnAction(e -> {
					Student student = getTableView().getItems().get(getIndex());
					handleUnlockStudent(student);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);

				if (empty || getIndex() >= getTableView().getItems().size()) {
					setGraphic(null);
					return;
				}
				Student student = getTableView().getItems().get(getIndex());
				actionBox.getChildren().clear();

				actionBox.getChildren().add(transferBtn);

				if (student.getAccountId() != null) {
					if (student.isActive()) {
						actionBox.getChildren().add(lockBtn);
					} else {
						actionBox.getChildren().add(unlockBtn);
					}
				}

				setGraphic(actionBox);
			}
		});
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
					throw new IllegalStateException("Khong the tai avatar cell sinh vien", ex);
				}
			}

			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				Student student = empty || getTableRow() == null ? null : getTableRow().getItem();
				if (student == null) {
					setGraphic(null);
					return;
				}
				controller.setPhoto(item);
				setGraphic(avatarView);
			}
		});
		studentCodeColumn.setCellValueFactory(new PropertyValueFactory<>("studentCode"));
		fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
		emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
		classNameColumn.setCellValueFactory(
				c -> new SimpleStringProperty(c.getValue().getClassName() != null ? c.getValue().getClassName() : ""));
		accountStatusColumn.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getAccountId() != null ? "Co tai khoan" : "Chua co tai khoan"));
	}

	private void loadStudents() {
		Task<java.util.List<Student>> task = new Task<>() {
			@Override
			protected java.util.List<Student> call() {
				return studentService.getAllStudents();
			}
		};
		task.setOnSucceeded(e -> Platform.runLater(() -> students.setAll(task.getValue())));
		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Loi tai danh sach sinh vien: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(task).start();
	}

	private void handleTransferStudent(Student student) {
		if (student == null) {
			AlertUtil.showError("Khong tim thay sinh vien");
			return;
		}

		Task<java.util.List<SchoolClass>> classTask = new Task<>() {
			@Override
			protected java.util.List<SchoolClass> call() {
				return classService.getAllClasses();
			}
		};

		classTask.setOnSucceeded(e -> Platform.runLater(() -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.STUDENT_TRANSFER_DIALOG));
				Parent content = loader.load();

				StudentTransferDialogController controller = loader.getController();
				controller.setClasses(classTask.getValue(), student.getClassId());

				Dialog<ButtonType> dialog = new Dialog<>();
				dialog.setTitle("Chuyen lop cho sinh vien");
				dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
				dialog.getDialogPane().setContent(content);

				Optional<ButtonType> result = dialog.showAndWait();
				if (result.isEmpty() || result.get() != ButtonType.OK) {
					return;
				}

				SchoolClass selectedClass = controller.getSelectedClass();

				Task<Void> transferTask = new Task<>() {
					@Override
					protected Void call() {
						studentService.transferStudentToClass(student.getStudentId(), selectedClass.getClassId());
						return null;
					}
				};

				transferTask.setOnSucceeded(ev -> Platform.runLater(() -> {
					AlertUtil.showSuccess("Chuyen lop thanh cong");
					loadStudents();
				}));

				transferTask.setOnFailed(ev -> Platform.runLater(() -> {
					Throwable ex = transferTask.getException();
					AlertUtil.showError("Loi: " + (ex != null ? ex.getMessage() : ""));
				}));

				new Thread(transferTask).start();

			} catch (Exception ex) {
				AlertUtil.showError("Khong the mo form chuyen lop: " + ex.getMessage());
			}
		}));

		classTask.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = classTask.getException();
			AlertUtil.showError("Khong the tai danh sach lop: " + (ex != null ? ex.getMessage() : ""));
		}));

		new Thread(classTask).start();
	}

	private void handleAddStudent() {
		Task<String> codeTask = new Task<>() {
			@Override
			protected String call() {
				return studentService.getNextStudentCode();
			}
		};
		codeTask.setOnSucceeded(e -> Platform.runLater(() -> openAddStudentDialog(codeTask.getValue())));
		codeTask.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = codeTask.getException();
			AlertUtil.showError("Khong the tao ma sinh vien moi: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(codeTask).start();
	}

	private void handleLockStudent(Student student) {
		if (student == null) {
			AlertUtil.showError("Khong tim thay sinh vien");
			return;
		}

		Task<Void> task = new Task<>() {
			@Override
			protected Void call() {
				studentService.lockStudent(student.getStudentId());
				return null;
			}
		};

		task.setOnSucceeded(e -> Platform.runLater(() -> {
			AlertUtil.showSuccess("Khoa tai khoan sinh vien thanh cong");
			loadStudents();
		}));

		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Loi: " + (ex != null ? ex.getMessage() : ""));
		}));

		new Thread(task).start();
	}

	private void handleUnlockStudent(Student student) {
		if (student == null) {
			AlertUtil.showError("Khong tim thay sinh vien");
			return;
		}

		Task<Void> task = new Task<>() {
			@Override
			protected Void call() {
				studentService.unlockStudent(student.getStudentId());
				return null;
			}
		};

		task.setOnSucceeded(e -> Platform.runLater(() -> {
			AlertUtil.showSuccess("Mo khoa tai khoan sinh vien thanh cong");
			loadStudents();
		}));

		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Loi: " + (ex != null ? ex.getMessage() : ""));
		}));

		new Thread(task).start();
	}

	private void openAddStudentDialog(String nextStudentCode) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.STUDENT_CREATE_DIALOG));
			Parent content = loader.load();
			StudentCreateDialogController controller = loader.getController();
			controller.setStudentCode(nextStudentCode);

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Them sinh vien");
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			dialog.getDialogPane().setContent(content);

			Optional<ButtonType> result = dialog.showAndWait();
			if (result.isEmpty() || result.get() != ButtonType.OK) {
				return;
			}

			Task<StudentCreationResult> task = new Task<>() {
				@Override
				protected StudentCreationResult call() {
					return studentService.addStudent(controller.getStudentCode(), controller.getFullName(),
							controller.getEmail());
				}
			};
			task.setOnSucceeded(e -> Platform.runLater(() -> {
				StudentCreationResult resultInfo = task.getValue();
				String message = "Them sinh vien thanh cong. Tai khoan mac dinh: " + resultInfo.getUsername() + " / "
						+ resultInfo.getTemporaryPassword() + ". Sinh vien dang o trang thai chua xep lop.";
				if (resultInfo.isNotificationEmailSent()) {
					message += " Da gui email thong bao.";
				} else {
					message += " Chua gui duoc email thong bao.";
				}
				AlertUtil.showSuccess(message);
				loadStudents();
			}));
			task.setOnFailed(e -> Platform.runLater(() -> {
				Throwable ex = task.getException();
				AlertUtil.showError("Loi: " + (ex != null ? ex.getMessage() : ""));
			}));
			new Thread(task).start();
		} catch (Exception e) {
			AlertUtil.showError("Khong the mo form them sinh vien: " + e.getMessage());
		}
	}
}
