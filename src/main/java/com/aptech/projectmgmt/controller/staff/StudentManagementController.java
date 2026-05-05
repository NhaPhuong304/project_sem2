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
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class StudentManagementController {
	@FXML
	private TextField searchField;
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
	private TableColumn<Student, String> createdByColumn;
	@FXML
	private TableColumn<Student, Void> actionColumn;

	private final ClassService classService = new ClassService();
	private final StudentService studentService = new StudentService();
	private final ObservableList<Student> students = FXCollections.observableArrayList();
	private FilteredList<Student> filteredStudents;

	@FXML
	public void initialize() {
		setupTableColumns();
		setupSearch();
		addStudentBtn.setOnAction(e -> handleAddStudent());
		loadStudents();
	}

	private void setupSearch() {
		filteredStudents = new FilteredList<>(students, p -> true);
		studentTable.setItems(filteredStudents);

		searchField.textProperty().addListener((obs, oldVal, newVal) -> {
			filteredStudents.setPredicate(student -> {
				if (newVal == null || newVal.isEmpty())
					return true;
				String lower = newVal.trim().toLowerCase();

				return (student.getStudentCode() != null && student.getStudentCode().toLowerCase().contains(lower))
						|| (student.getFullName() != null && student.getFullName().toLowerCase().contains(lower))
						|| (student.getEmail() != null && student.getEmail().toLowerCase().contains(lower))
						|| ((student.getAccountId() != null ? "has account" : "no account").toLowerCase()
								.contains(lower));
			});
		});
	}

	private void setupTableColumns() {
		actionColumn.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(null));
		actionColumn.setCellFactory(col -> new TableCell<>() {
			private final Button editBtn = new Button("✎");
			private final Button transferBtn = new Button("🔄");
			private final Button lockBtn = new Button("🔒");
			private final Button unlockBtn = new Button("🔓");
			private final javafx.scene.layout.HBox actionBox = new javafx.scene.layout.HBox(8);

			{
				editBtn.setStyle(
						"-fx-text-fill: #f59e0b; -fx-font-size: 14px; -fx-background-color: #fef3c7; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
				editBtn.setTooltip(new javafx.scene.control.Tooltip("Edit"));
				transferBtn.setStyle(
						"-fx-text-fill: #8b5cf6; -fx-font-size: 14px; -fx-background-color: #f5f3ff; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
				transferBtn.setTooltip(new javafx.scene.control.Tooltip("Transfer"));
				lockBtn.setStyle(
						"-fx-text-fill: #ef4444; -fx-font-size: 14px; -fx-background-color: #fef2f2; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
				lockBtn.setTooltip(new javafx.scene.control.Tooltip("Lock"));
				unlockBtn.setStyle(
						"-fx-text-fill: #10b981; -fx-font-size: 14px; -fx-background-color: #ecfdf5; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
				unlockBtn.setTooltip(new javafx.scene.control.Tooltip("Unlock"));
				editBtn.setOnAction(e -> {
					Student student = getTableView().getItems().get(getIndex());
					handleEditStudent(student);
				});

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

				if (empty || getTableRow() == null || getTableRow().getItem() == null) {
					setGraphic(null);
					if (getTableRow() != null)
						getTableRow().setStyle("");
					return;
				}
				Student student = getTableRow().getItem();
				actionBox.getChildren().clear();

				actionBox.getChildren().add(editBtn);
				actionBox.getChildren().add(transferBtn);

				if (student.getAccountId() != null) {
					if (student.isActive()) {
						actionBox.getChildren().add(lockBtn);
						getTableRow().setStyle("");
					} else {
						actionBox.getChildren().add(unlockBtn);
						getTableRow().setStyle("-fx-opacity: 0.6; -fx-background-color: #f3f4f6;");
					}
				} else {
					getTableRow().setStyle("");
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
					throw new IllegalStateException("Could not load the student avatar cell", ex);
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
		createdByColumn.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getCreatedByStaffName() != null ? c.getValue().getCreatedByStaffName() : ""));
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
			AlertUtil.showError("Failed to load the student list: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(task).start();
	}

	private void handleEditStudent(Student student) {
		if (student == null) {
			AlertUtil.showError("Student could not be found");
			return;
		}

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.STUDENT_CREATE_DIALOG));
			Parent content = loader.load();
			StudentCreateDialogController controller = loader.getController();

			controller.setData(student);

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Edit student");
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			dialog.getDialogPane().setContent(content);
			Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

			okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
				String error = controller.validate();
				if (error != null) {
					AlertUtil.showError(error);
					event.consume();
				}
			});
			Optional<ButtonType> result = dialog.showAndWait();
			if (result.isEmpty() || result.get() != ButtonType.OK) {
				return;
			}

			Task<Void> task = new Task<>() {
				@Override
				protected Void call() {
					studentService.updateStudent(student.getStudentId(), controller.getFullName(),
							controller.getEmail());
					return null;
				}
			};

			task.setOnSucceeded(e -> Platform.runLater(() -> {
				AlertUtil.showSuccess("Student updated successfully");
				loadStudents();
			}));

			task.setOnFailed(e -> Platform.runLater(() -> {
				Throwable ex = task.getException();
				AlertUtil.showError("Error: " + (ex != null ? ex.getMessage() : ""));
			}));

			new Thread(task).start();

		} catch (Exception e) {
			AlertUtil.showError("Could not open the edit student form: " + e.getMessage());
		}
	}

	private void handleTransferStudent(Student student) {
		if (student == null) {
			AlertUtil.showError("Student could not be found");
			return;
		}

		Task<java.util.List<SchoolClass>> classTask = new Task<>() {
			@Override
			protected java.util.List<SchoolClass> call() {

				var account = com.aptech.projectmgmt.util.SessionManager.getInstance().getCurrentAccount();

				return classService.getClassesByManager(account.getAccountId());
			}
		};

		classTask.setOnSucceeded(e -> Platform.runLater(() -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.STUDENT_TRANSFER_DIALOG));
				Parent content = loader.load();

				StudentTransferDialogController controller = loader.getController();
				controller.setClasses(classTask.getValue(), student.getClassId());

				Dialog<ButtonType> dialog = new Dialog<>();
				dialog.setTitle("Transfer student to another class");
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
					AlertUtil.showSuccess("Class transfer completed successfully");
					loadStudents();
				}));

				transferTask.setOnFailed(ev -> Platform.runLater(() -> {
					Throwable ex = transferTask.getException();
					AlertUtil.showError("Error: " + (ex != null ? ex.getMessage() : ""));
				}));

				new Thread(transferTask).start();

			} catch (Exception ex) {
				AlertUtil.showError("Could not open the class transfer form: " + ex.getMessage());
			}
		}));

		classTask.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = classTask.getException();
			AlertUtil.showError("Could not load the class list: " + (ex != null ? ex.getMessage() : ""));
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
			AlertUtil.showError("Could not generate a new student code: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(codeTask).start();
	}

	private void handleLockStudent(Student student) {
		if (student == null) {
			AlertUtil.showError("Student could not be found");
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
			AlertUtil.showSuccess("Student account locked successfully");
			loadStudents();
		}));

		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Error: " + (ex != null ? ex.getMessage() : ""));
		}));

		new Thread(task).start();
	}

	private void handleUnlockStudent(Student student) {
		if (student == null) {
			AlertUtil.showError("Student could not be found");
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
			AlertUtil.showSuccess("Student account unlocked successfully");
			loadStudents();
		}));

		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Error: " + (ex != null ? ex.getMessage() : ""));
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
			dialog.setTitle("Add student");
			dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			dialog.getDialogPane().setContent(content);
			Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);

			okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
				String error = controller.validate();
				if (error != null) {
					AlertUtil.showError(error);
					event.consume();
				}
			});
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
				String message = "Add student thanh cong. Tai khoan mac dinh: " + resultInfo.getUsername() + " / "
						+ resultInfo.getTemporaryPassword()
						+ ". The student is currently in the unassigned class state.";
				if (resultInfo.isNotificationEmailSent()) {
					message += " Notification email sent.";
				} else {
					message += " Notification email could not be sent.";
				}
				AlertUtil.showSuccess(message);
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
}