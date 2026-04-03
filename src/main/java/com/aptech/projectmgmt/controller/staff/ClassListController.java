package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.controller.SingleActionCellController;
import com.aptech.projectmgmt.model.SchoolClass;
import com.aptech.projectmgmt.service.ClassService;
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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ClassListController {

    @FXML private TableView<SchoolClass> classTable;
    @FXML private Button addClassBtn;
    @FXML private TextField searchField;
    @FXML private TableColumn<SchoolClass, Number> noColumn;
    @FXML private TableColumn<SchoolClass, String> classNameColumn;
    @FXML private TableColumn<SchoolClass, String> semesterColumn;
    @FXML private TableColumn<SchoolClass, String> academicYearColumn;
    @FXML private TableColumn<SchoolClass, Integer> studentCountColumn;
    @FXML private TableColumn<SchoolClass, String> createdAtColumn;
    @FXML private TableColumn<SchoolClass, Void> actionColumn;

    private final ClassService classService = new ClassService();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private ObservableList<SchoolClass> allClasses = FXCollections.observableArrayList();
    private FilteredList<SchoolClass> filteredClasses;
    private boolean readOnlyMode;
    private Integer teacherStaffId;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearch();
        addClassBtn.setOnAction(e -> handleAddClass());
        refreshReadOnlyState();
        Platform.runLater(this::loadClasses);

        classTable.setRowFactory(tv -> {
            TableRow<SchoolClass> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    navigateToStudentList(row.getItem().getClassId());
                }
            });
            return row;
        });
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
        refreshReadOnlyState();
    }

    public void setTeacherStaffId(Integer teacherStaffId) {
        this.teacherStaffId = teacherStaffId;
    }

    private void setupTableColumns() {
        noColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                setText(String.valueOf(getIndex() + 1));
            }
        });
        classNameColumn.setCellValueFactory(new PropertyValueFactory<>("className"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        academicYearColumn.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        studentCountColumn.setCellValueFactory(new PropertyValueFactory<>("studentCount"));
        createdAtColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().format(dateTimeFormatter) : ""));
        actionColumn.setCellFactory(col -> createActionCell());
    }

    private TableCell<SchoolClass, Void> createActionCell() {
        return new TableCell<>() {
            private final Parent actionView;
            private final SingleActionCellController controller;

            {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.SINGLE_ACTION_CELL));
                    actionView = loader.load();
                    controller = loader.getController();
                    controller.setActionText("Xem SV");
                    controller.setOnAction(() -> {
                        SchoolClass schoolClass = getTableRow() != null ? getTableRow().getItem() : null;
                        if (schoolClass != null) {
                            navigateToStudentList(schoolClass.getClassId());
                        }
                    });
                } catch (Exception ex) {
                    throw new IllegalStateException("Khong the tai action cell lop", ex);
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                SchoolClass schoolClass = empty || getTableRow() == null ? null : getTableRow().getItem();
                if (schoolClass == null) {
                    setGraphic(null);
                    return;
                }
                controller.setActionDisabled(false);
                setGraphic(actionView);
            }
        };
    }

    private void setupSearch() {
        filteredClasses = new FilteredList<>(allClasses, p -> true);
        classTable.setItems(filteredClasses);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredClasses.setPredicate(sc -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return sc.getClassName().toLowerCase().contains(lower);
            });
        });
    }

    private void loadClasses() {
        Task<List<SchoolClass>> task = new Task<>() {
            @Override
            protected List<SchoolClass> call() {
                if (readOnlyMode && teacherStaffId != null) {
                    return classService.getClassesByAdvisor(teacherStaffId);
                }
                return classService.getAllClasses();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            allClasses.setAll(task.getValue());
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Loi tai danh sach lop: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private void handleAddClass() {
        if (readOnlyMode) {
            AlertUtil.showError("Tai khoan giao vien chi duoc xem thong tin lop");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.CLASS_CREATE_DIALOG));
            Parent content = loader.load();
            ClassCreateDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Them lop moi");
            dialog.setHeaderText("Nhap thong tin lop moi");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(content);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            String className = controller.getClassName();
            String semester = controller.getSemester();
            String academicYear = controller.getAcademicYear();
            if (className.isEmpty()) {
                AlertUtil.showError("Ten lop khong duoc de trong");
                return;
            }

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    classService.createClass(className, semester, academicYear);
                    return null;
                }
            };
            task.setOnSucceeded(e -> Platform.runLater(() -> {
                AlertUtil.showSuccess("Tao lop thanh cong");
                loadClasses();
            }));
            task.setOnFailed(e -> Platform.runLater(() -> {
                Throwable ex = task.getException();
                AlertUtil.showError("Loi: " + (ex != null ? ex.getMessage() : ""));
            }));
            new Thread(task).start();
        } catch (Exception e) {
            AlertUtil.showError("Khong the mo form tao lop: " + e.getMessage());
        }
    }

    private void navigateToStudentList(int classId) {
        javafx.scene.Node node = classTable;
        while (node.getParent() != null) {
            node = node.getParent();
            if (node instanceof javafx.scene.layout.StackPane) {
                javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) node;
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                            getClass().getResource(com.aptech.projectmgmt.util.SceneManager.STUDENT_LIST));
                    javafx.scene.Node content = loader.load();
                    StudentListController controller = loader.getController();
                    if (readOnlyMode) {
                        controller.setReadOnlyMode(true);
                    }
                    controller.initData(classId);
                    contentArea.getChildren().setAll(content);
                } catch (Exception e) {
                    AlertUtil.showError("Loi chuyen man hinh: " + e.getMessage());
                }
                break;
            }
        }
    }

    private void refreshReadOnlyState() {
        if (addClassBtn == null) {
            return;
        }
        addClassBtn.setVisible(!readOnlyMode);
        addClassBtn.setManaged(!readOnlyMode);
        addClassBtn.setDisable(readOnlyMode);
    }
}
