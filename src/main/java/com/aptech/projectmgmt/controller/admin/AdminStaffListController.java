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

	private final StaffService staffService = new StaffService();
	private final ObservableList<Staff> teacherList = FXCollections.observableArrayList();

	@FXML
	public void initialize() {
		setupTableColumns();
		teacherTable.setItems(teacherList);
		addTeacherBtn.setOnAction(e -> handleAddTeacher());
		loadTeachers();
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
					throw new IllegalStateException("Khong the tai avatar cell giao vien", ex);
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
				return new SimpleStringProperty("Giao vu");
			if (r == com.aptech.projectmgmt.model.UserRole.TEACHER)
				return new SimpleStringProperty("Giao vien");
			return new SimpleStringProperty("");
		});
		accountStatusColumn
				.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActive() ? "Hoat dong" : "Da khoa"));
		actionColumn.setCellFactory(col -> new TableCell<>() {
			private final Button editBtn = new Button("Sua");
			private final Button lockBtn = new Button("Khoa/Mo");

			{
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
					return;
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
			AlertUtil.showError("Loi tai danh sach giao vien: " + (ex != null ? ex.getMessage() : ""));
		}));
		new Thread(task).start();
	}

	private void handleAddTeacher() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.ADMIN_STAFF_CREATE_DIALOG));
			Parent content = loader.load();
			AdminStaffCreateDialogController controller = loader.getController();

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Them giao vu");
			DialogPane dialogPane = dialog.getDialogPane();
			dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			dialogPane.setContent(content);

			Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
			okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
				event.consume();

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
				    String successMessage = "Them nhan su thanh cong. Tai khoan mac dinh: "
				            + resultInfo.getUsername() + " / " + resultInfo.getTemporaryPassword();

				    if (resultInfo.isNotificationEmailSent()) {
				        successMessage += ". Da gui email thong bao.";
				    } else {
				        successMessage += ". Chua gui duoc email thong bao.";
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
					AlertUtil.showError("Loi: " + (ex != null ? ex.getMessage() : ""));
				}));
				new Thread(task).start();
			});

			dialog.showAndWait();
		} catch (Exception ex) {
			AlertUtil.showError("Khong the mo form them giao vien: " + ex.getMessage());
		}
	}

	private void handleEditStaff(Staff staff) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.ADMIN_STAFF_CREATE_DIALOG));
			Parent content = loader.load();
			AdminStaffCreateDialogController controller = loader.getController();

			controller.setData(staff);

			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Sua giao vu");
			DialogPane dialogPane = dialog.getDialogPane();
			dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
			dialogPane.setContent(content);

			Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
			okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
				event.consume();

				Task<Void> task = new Task<>() {
					@Override
					protected Void call() {
						staffService.updateStaffFullName(staff.getStaffId(), controller.getFullName());
						return null;
					}
				};

				okButton.setDisable(true);
				dialogPane.lookupButton(ButtonType.CANCEL).setDisable(true);

				task.setOnSucceeded(e -> Platform.runLater(() -> {
				    dialog.setResult(ButtonType.OK);
				    dialog.close();

				    Platform.runLater(() -> {
				        AlertUtil.showSuccess("Cap nhat giao vu thanh cong");
				        loadTeachers();
				    });
				}));

				task.setOnFailed(e -> Platform.runLater(() -> {
					okButton.setDisable(false);
					dialogPane.lookupButton(ButtonType.CANCEL).setDisable(false);
					Throwable ex = task.getException();
					AlertUtil.showError("Loi: " + (ex != null ? ex.getMessage() : ""));
				}));

				new Thread(task).start();
			});

			dialog.showAndWait();
		} catch (Exception ex) {
			AlertUtil.showError("Khong the mo form sua giao vu: " + ex.getMessage());
		}
	}

	private void handleToggleStatus(Staff staff) {
		String message = staff.isActive() ? "Ban co chac muon khoa tai khoan giao vu nay?"
				: "Ban co chac muon mo khoa tai khoan giao vu nay?";

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
			AlertUtil.showSuccess("Cap nhat trang thai giao vu thanh cong");
			loadTeachers();
		}));

		task.setOnFailed(e -> Platform.runLater(() -> {
			Throwable ex = task.getException();
			AlertUtil.showError("Loi cap nhat trang thai: " + (ex != null ? ex.getMessage() : ""));
		}));

		new Thread(task).start();
	}

}
