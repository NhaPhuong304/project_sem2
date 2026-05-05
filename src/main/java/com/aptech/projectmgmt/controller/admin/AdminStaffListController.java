package com.aptech.projectmgmt.controller.admin;

import com.aptech.projectmgmt.controller.AvatarCellController;
import com.aptech.projectmgmt.model.Staff;
import com.aptech.projectmgmt.model.TeacherCreationResult;
import com.aptech.projectmgmt.service.StaffService;
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
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class AdminStaffListController {

	@FXML
	private TableView<Staff> teacherTable;
	@FXML
	private Button addTeacherBtn;
	@FXML
	private TableColumn<Staff, String> avatarColumn;
	@FXML
	private TableColumn<Staff, String> usernameColumn;
	@FXML
	private TableColumn<Staff, String> fullNameColumn;
	@FXML
	private TableColumn<Staff, String> emailColumn;
	@FXML
	private TableColumn<Staff, String> roleColumn;
	@FXML
	private TableColumn<Staff, String> accountStatusColumn;
	@FXML
	private TableColumn<Staff, Void> actionColumn;
	@FXML
	private TextField usernameField;
	@FXML
	private TextField fullNameField;
	@FXML
	private TextField emailField;
	@FXML
	private TextField searchField;

	private final StaffService staffService = new StaffService();
	private final ObservableList<Staff> teacherList = FXCollections.observableArrayList();
	private FilteredList<Staff> filteredTeachers;

	@FXML
	public void initialize() {
		setupTableColumns();
		teacherTable.setItems(teacherList);
		addTeacherBtn.setOnAction(e -> handleAddTeacher());
		loadTeachers();
		setupSearch();
	}

	private void setupSearch() {
		filteredTeachers = new FilteredList<>(teacherList, p -> true);
		teacherTable.setItems(filteredTeachers);

		searchField.textProperty().addListener((obs, oldVal, newVal) -> {
			filteredTeachers.setPredicate(staff -> {
				if (newVal == null || newVal.isEmpty())
					return true;
				String lower = newVal.toLowerCase();
				return staff.getUsername().toLowerCase().contains(lower)
						|| staff.getFullName().toLowerCase().contains(lower)
						|| staff.getEmail().toLowerCase().contains(lower);
			});
		});
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
					throw new IllegalStateException("Cannot load teacher avatar cell", ex);
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
		usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
		fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
		emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
		roleColumn.setCellValueFactory(c -> {
			com.aptech.projectmgmt.model.UserRole r = c.getValue().getRole();
			if (r == com.aptech.projectmgmt.model.UserRole.STAFF)
				return new SimpleStringProperty("Staff");
			if (r == com.aptech.projectmgmt.model.UserRole.TEACHER)
				return new SimpleStringProperty("Teacher");
			return new SimpleStringProperty("");
		});
		accountStatusColumn
				.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Active" : "Inactive"));
		actionColumn.setCellFactory(col -> new TableCell<>() {
			private final Button editBtn = new Button("✎");
			private final Button lockBtn = new Button("🔒");

			{
				editBtn.setStyle(
						"-fx-text-fill: #f59e0b; -fx-font-size: 14px; -fx-background-color: #fef3c7; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
				editBtn.setTooltip(new javafx.scene.control.Tooltip("Edit Staff"));
				lockBtn.setStyle(
						"-fx-text-fill: #ef4444; -fx-font-size: 14px; -fx-background-color: #fef2f2; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
				lockBtn.setTooltip(new javafx.scene.control.Tooltip("Lock/Unlock Staff"));
				editBtn.setOnAction(e -> {
					Staff staff = getTableRow().getItem();
					if (staff != null) {
						handleEditStaff(staff);
					}
				});

				lockBtn.setOnAction(e -> {
					Staff staff = getTableRow().getItem();
					if (staff != null) {
						handleToggleStatus(staff);
					}
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

				Staff staff = getTableRow().getItem();
				if (staff.isActive()) {
					lockBtn.setText("🔒");
					lockBtn.setStyle(
							"-fx-text-fill: #ef4444; -fx-font-size: 14px; -fx-background-color: #fef2f2; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
					lockBtn.setTooltip(new javafx.scene.control.Tooltip("Lock Staff"));
					getTableRow().setStyle("");
				} else {
					lockBtn.setText("🔓");
					lockBtn.setStyle(
							"-fx-text-fill: #10b981; -fx-font-size: 14px; -fx-background-color: #ecfdf5; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
					lockBtn.setTooltip(new javafx.scene.control.Tooltip("Unlock Staff"));
					getTableRow().setStyle("-fx-opacity: 0.6; -fx-background-color: #f3f4f6;");
				}

				javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(8, editBtn, lockBtn);
				setGraphic(box);
			}
		});
	}

	private void loadTeachers() {
		Task<List<Staff>> task = new Task<>() {
			@Override
			protected List<Staff> call() {
				return staffService.getStaffs();
			}
		};
		task.setOnSucceeded(e -> Platform.runLater(() -> teacherList.setAll(task.getValue())));
		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Failed to load teacher list: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(task).start();
	}

	private void handleAddTeacher() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.ADMIN_STAFF_CREATE_DIALOG));
			Parent content = loader.load();
			AdminStaffCreateDialogController controller = loader.getController();

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Create Staff");
			DialogPane dialogPane = dialog.getDialogPane();
			dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			dialogPane.setContent(content);

			Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
			okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
				event.consume();
				String error = controller.validate();
				if (error != null) {
					AlertUtil.showError(error);
					return;
				}

				Task<TeacherCreationResult> task = new Task<>() {
					@Override
					protected TeacherCreationResult call() {
						return staffService.createStaffMember(controller.getUsername(), controller.getFullName(),
								controller.getEmail(), com.aptech.projectmgmt.model.UserRole.STAFF);
					}
				};

				okButton.setDisable(true);
				dialogPane.lookupButton(ButtonType.CANCEL).setDisable(true);

				task.setOnSucceeded(e -> Platform.runLater(() -> {
					okButton.setDisable(false);
					dialogPane.lookupButton(ButtonType.CANCEL).setDisable(false);

					TeacherCreationResult resultInfo = task.getValue();
					String successMessage = "Staff added successfully. Default account created.: "
							+ resultInfo.getUsername() + " / " + resultInfo.getTemporaryPassword();

					if (resultInfo.isNotificationEmailSent()) {
						successMessage += ". Notification email sent.";
					} else {
						successMessage += ". Failed to send notification email.";
					}

					String finalSuccessMessage = successMessage;

					dialog.setResult(ButtonType.OK);
					dialog.close();

					Platform.runLater(() -> {
						AlertUtil.showSuccess(finalSuccessMessage);
						loadTeachers();
					});
				}));
				task.setOnFailed(e -> Platform.runLater(() -> {
					okButton.setDisable(false);
					dialogPane.lookupButton(ButtonType.CANCEL).setDisable(false);
					Throwable ex = task.getException();
					AlertUtil.showError("Error: " + (ex != null ? ex.getMessage() : ""));
				}));
				new Thread(task).start();
			});

			dialog.showAndWait();
		} catch (Exception ex) {
			AlertUtil.showError("Unable to open Created Staff form: " + ex.getMessage());
		}
	}

	private void handleEditStaff(Staff staff) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.ADMIN_STAFF_CREATE_DIALOG));
			Parent content = loader.load();
			AdminStaffCreateDialogController controller = loader.getController();

			controller.setData(staff);

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Edit staff member");
			DialogPane dialogPane = dialog.getDialogPane();
			dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			dialogPane.setContent(content);

			Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
			okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
				event.consume();

				String error = controller.validate();
				if (error != null) {
					AlertUtil.showError(error);
					return;
				}

				Task<Void> task = new Task<>() {
					@Override
					protected Void call() {
						staffService.updateStaff(staff.getStaffId(), staff.getUsername(), controller.getFullName(),
								controller.getEmail());
						return null;
					}
				};

				okButton.setDisable(true);
				dialogPane.lookupButton(ButtonType.CANCEL).setDisable(true);

				task.setOnSucceeded(e -> Platform.runLater(() -> {
					dialog.setResult(ButtonType.OK);
					dialog.close();

					Platform.runLater(() -> {
						AlertUtil.showSuccess("Staff member updated successfully");
						loadTeachers();
					});
				}));

				task.setOnFailed(e -> Platform.runLater(() -> {
					okButton.setDisable(false);
					dialogPane.lookupButton(ButtonType.CANCEL).setDisable(false);
					Throwable ex = task.getException();
					AlertUtil.showError("Error: " + (ex != null ? ex.getMessage() : ""));
				}));

				new Thread(task).start();
			});

			dialog.showAndWait();
		} catch (Exception ex) {
			AlertUtil.showError("Unable to open Edit Academic Staff form: " + ex.getMessage());
		}
	}

	private void handleToggleStatus(Staff staff) {
		String message = staff.isActive() ? "Are you sure you want to lock this academic staff account?"
				: "Are you sure you want to unlock this academic staff account?";

		boolean confirmed = AlertUtil.showConfirm(message);
		if (!confirmed) {
			return;
		}

		Task<Void> task = new Task<>() {
			@Override
			protected Void call() {
				staffService.toggleStaffStatus(staff.getStaffId());
				return null;
			}
		};

		task.setOnSucceeded(e -> Platform.runLater(() -> {
			AlertUtil.showSuccess("Academic staff status updated successfully");
			loadTeachers();
		}));

		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Failed to update status: " + (ex != null ? ex.getMessage() : ""));
		}));

		new Thread(task).start();
	}

}
