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

    @FXML private Label classNameLabel;
    @FXML private TableView<Student> studentTable;
    @FXML private Button createAccountsBtn;
    @FXML private Button addStudentBtn;
    @FXML private TableColumn<Student, String> avatarColumn;
    @FXML private TableColumn<Student, String> studentCodeColumn;
    @FXML private TableColumn<Student, String> fullNameColumn;
    @FXML private TableColumn<Student, String> emailColumn;
    @FXML private TableColumn<Student, String> accountStatusColumn;

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
                return sc != null ? sc.getClassName() : "Lop #" + classId;
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
                    throw new IllegalStateException("Khong the tai avatar cell sinh vien", ex);
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
            AlertUtil.showError("Loi tai danh sach SV: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private void updateCreateAccountsButton() {
        boolean hasStudentWithoutAccount = studentList.stream().anyMatch(student -> student.getAccountId() == null);
        createAccountsBtn.setVisible(!readOnlyMode && hasStudentWithoutAccount);
        createAccountsBtn.setManaged(!readOnlyMode && hasStudentWithoutAccount);
        createAccountsBtn.setDisable(readOnlyMode || !hasStudentWithoutAccount);
        createAccountsBtn.setTooltip(!readOnlyMode && hasStudentWithoutAccount
                ? new Tooltip("Tao tai khoan cho cac du lieu cu chua co account")
                : null);
        refreshActionButtons();
    }

    private void handleCreateAccounts() {
        if (readOnlyMode) {
            AlertUtil.showError("Tai khoan giao vien chi duoc xem danh sach sinh vien");
            return;
        }
        if (!AlertUtil.showConfirm("Tao tai khoan cho toan bo sinh vien chua co tai khoan?")) return;
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
            AlertUtil.showError("Loi: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private void handleAddStudent() {
        if (readOnlyMode) {
            AlertUtil.showError("Tai khoan giao vien chi duoc xem danh sach sinh vien");
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
            AlertUtil.showError("Khong tai duoc danh sach sinh vien chua co lop: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(studentsTask).start();
    }

    private void openAssignStudentDialog(List<Student> unassignedStudents) {
        if (unassignedStudents == null || unassignedStudents.isEmpty()) {
            AlertUtil.showError("Khong con sinh vien nao chua xep lop");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.STUDENT_ASSIGN_DIALOG));
            Parent content = loader.load();
            StudentAssignDialogController controller = loader.getController();
            controller.setStudents(unassignedStudents);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Them sinh vien vao lop");
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
                AlertUtil.showSuccess("Da them sinh vien vao lop thanh cong");
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

    private void refreshActionButtons() {
        if (addStudentBtn != null) {
            addStudentBtn.setVisible(!readOnlyMode);
            addStudentBtn.setManaged(!readOnlyMode);
            addStudentBtn.setDisable(readOnlyMode);
        }
        if (createAccountsBtn != null && readOnlyMode) {
            createAccountsBtn.setVisible(false);
            createAccountsBtn.setManaged(false);
            createAccountsBtn.setDisable(true);
        }
    }
}
