package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.controller.SingleActionCellController;
import com.aptech.projectmgmt.model.Project;
import com.aptech.projectmgmt.model.ProjectStatus;
import com.aptech.projectmgmt.model.SchoolClass;
import com.aptech.projectmgmt.model.Staff;
import com.aptech.projectmgmt.service.ClassService;
import com.aptech.projectmgmt.service.ProjectService;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ButtonType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class ProjectListController {

    private static final int ALL_CLASSES_ID = 0;
    private static final int NEAR_END_THRESHOLD_DAYS = 3;

    @FXML private ComboBox<SchoolClass> classCombo;
    @FXML private ComboBox<String> semesterCombo;
    @FXML private TableView<Project> projectTable;
    @FXML private Button addProjectBtn;
    @FXML private TableColumn<Project, String> projectNameColumn;
    @FXML private TableColumn<Project, String> semesterColumn;
    @FXML private TableColumn<Project, String> supervisorColumn;
    @FXML private TableColumn<Project, String> startDateColumn;
    @FXML private TableColumn<Project, String> endDateColumn;
    @FXML private TableColumn<Project, String> statusColumn;
    @FXML private TableColumn<Project, Void> actionColumn;

    private final ProjectService projectService = new ProjectService();
    private final ClassService classService = new ClassService();
    private final StaffService staffService = new StaffService();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private int classId;
    private int requestedClassId;
    private final ObservableList<Project> allProjects = FXCollections.observableArrayList();
    private FilteredList<Project> filteredProjects;
    private boolean readOnlyMode;
    private Integer teacherStaffId;

    @FXML
    public void initialize() {
        setupTableColumns();
        configureClassCombo();
        filteredProjects = new FilteredList<>(allProjects, p -> true);
        projectTable.setItems(filteredProjects);
        semesterCombo.setOnAction(e -> applyFilters());
        addProjectBtn.setOnAction(e -> handleAddProject());
        refreshReadOnlyState();
        projectTable.setPlaceholder(new Label("Dang tai danh sach project..."));
        Platform.runLater(() -> {
            loadClasses();
            loadProjects();
        });
    }

    public void initData(int classId) {
        this.requestedClassId = classId;
        this.classId = classId;
        if (!classCombo.getItems().isEmpty()) {
            selectRequestedClass();
        }
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
        refreshReadOnlyState();
    }

    public void setTeacherStaffId(Integer teacherStaffId) {
        this.teacherStaffId = teacherStaffId;
    }

    private void setupTableColumns() {
        projectNameColumn.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        supervisorColumn.setCellValueFactory(new PropertyValueFactory<>("supervisorName"));
        startDateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStartDate() != null ? c.getValue().getStartDate().format(dateTimeFormatter) : ""));
        endDateColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getEndDate() != null ? c.getValue().getEndDate().format(dateTimeFormatter) : ""));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(getProjectStatusDisplay(c.getValue())));
        statusColumn.setCellFactory(col -> createStatusCell());
        actionColumn.setCellFactory(col -> createActionCell());
    }

    private void configureClassCombo() {
        classCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(SchoolClass schoolClass) {
                if (schoolClass == null) {
                    return "";
                }
                if (schoolClass.getClassId() == ALL_CLASSES_ID) {
                    return schoolClass.getClassName();
                }
                return schoolClass.getClassName() + " - " + schoolClass.getSemester();
            }

            @Override
            public SchoolClass fromString(String string) {
                return null;
            }
        });
        classCombo.setOnAction(e -> {
            SchoolClass selected = classCombo.getValue();
            classId = selected != null ? selected.getClassId() : ALL_CLASSES_ID;
            addProjectBtn.setDisable(readOnlyMode || selected == null || selected.getClassId() == ALL_CLASSES_ID);
            refreshSemesterOptions();
            applyFilters();
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
            ObservableList<SchoolClass> items = FXCollections.observableArrayList();
            SchoolClass allClasses = new SchoolClass();
            allClasses.setClassId(ALL_CLASSES_ID);
            allClasses.setClassName("Tat ca lop");
            items.add(allClasses);
            items.addAll(task.getValue());
            classCombo.setItems(items);
            selectRequestedClass();
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Loi tai danh sach lop: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private void selectRequestedClass() {
        if (requestedClassId > 0) {
            for (SchoolClass schoolClass : classCombo.getItems()) {
                if (schoolClass.getClassId() == requestedClassId) {
                    classCombo.getSelectionModel().select(schoolClass);
                    return;
                }
            }
        }
        if (!classCombo.getItems().isEmpty()) {
            classCombo.getSelectionModel().selectFirst();
            classId = ALL_CLASSES_ID;
            addProjectBtn.setDisable(true);
            refreshSemesterOptions();
            applyFilters();
        }
    }

    private TableCell<Project, Void> createActionCell() {
        return new TableCell<>() {
            private final Parent actionView;
            private final SingleActionCellController controller;

            {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.SINGLE_ACTION_CELL));
                    actionView = loader.load();
                    controller = loader.getController();
                    controller.setActionText("Xem");
                    controller.setOnAction(() -> {
                        Project project = getTableRow() != null ? getTableRow().getItem() : null;
                        if (project != null) {
                            navigateToProjectDetail(project.getProjectId());
                        }
                    });
                } catch (Exception ex) {
                    throw new IllegalStateException("Khong the tai action cell project", ex);
                }
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                Project project = empty || getTableRow() == null ? null : getTableRow().getItem();
                if (project == null) {
                    setGraphic(null);
                    return;
                }
                controller.setActionDisabled(false);
                setGraphic(actionView);
            }
        };
    }

    private TableCell<Project, String> createStatusCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("project-status-green", "project-status-yellow", "project-status-red");
                Project project = empty || getTableRow() == null ? null : getTableRow().getItem();
                if (project == null) {
                    setText(null);
                    return;
                }
                setText(getProjectStatusDisplay(project));
                setTextFill(Color.BLACK);
                getStyleClass().add(getProjectStatusStyle(project));
            }
        };
    }

    private void loadProjects() {
        Task<List<Project>> task = new Task<>() {
            @Override
            protected List<Project> call() {
                if (readOnlyMode && teacherStaffId != null) {
                    return projectService.getProjectsByAdvisor(teacherStaffId);
                }
                return projectService.getAllProjects();
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            allProjects.setAll(task.getValue());
            refreshSemesterOptions();
            applyFilters();
            if (allProjects.isEmpty()) {
                projectTable.setPlaceholder(new Label("Chua co project nao"));
            }
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Loi tai danh sach project: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private void refreshSemesterOptions() {
        String currentSelection = semesterCombo.getValue();
        SchoolClass selectedClass = classCombo.getValue();
        ObservableList<String> semesters = FXCollections.observableArrayList("Tat ca");
        allProjects.stream()
                .filter(project -> selectedClass == null
                        || selectedClass.getClassId() == ALL_CLASSES_ID
                        || project.getClassId() == selectedClass.getClassId())
                .map(Project::getSemester)
                .filter(semester -> semester != null && !semester.isBlank())
                .distinct()
                .forEach(semesters::add);
        semesterCombo.setItems(semesters);
        if (currentSelection != null && semesters.contains(currentSelection)) {
            semesterCombo.getSelectionModel().select(currentSelection);
        } else {
            semesterCombo.getSelectionModel().selectFirst();
        }
    }

    private void applyFilters() {
        SchoolClass selectedClass = classCombo.getValue();
        String selectedSemester = semesterCombo.getValue();
        filteredProjects.setPredicate(project -> {
            boolean classMatch = selectedClass == null
                    || selectedClass.getClassId() == ALL_CLASSES_ID
                    || project.getClassId() == selectedClass.getClassId();
            boolean semesterMatch = selectedSemester == null
                    || "Tat ca".equals(selectedSemester)
                    || selectedSemester.equals(project.getSemester());
            return classMatch && semesterMatch;
        });
        if (filteredProjects.isEmpty()) {
            projectTable.setPlaceholder(new Label("Khong co project phu hop"));
        }
    }

    private void handleAddProject() {
        if (readOnlyMode) {
            AlertUtil.showError("Tai khoan giao vien chi duoc xem project huong dan");
            return;
        }
        if (classId <= ALL_CLASSES_ID) {
            AlertUtil.showError("Vui long chon lop truoc khi tao project");
            return;
        }
        Task<List<Staff>> loadStaffTask = new Task<>() {
            @Override
            protected List<Staff> call() {
                return staffService.getTeachers();
            }
        };
        loadStaffTask.setOnSucceeded(e -> Platform.runLater(() -> showAddProjectDialog(loadStaffTask.getValue())));
        loadStaffTask.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = loadStaffTask.getException();
            AlertUtil.showError("Khong tai duoc danh sach staff: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(loadStaffTask).start();
    }

    private void showAddProjectDialog(List<Staff> staffList) {
        if (staffList == null || staffList.isEmpty()) {
            AlertUtil.showError("Chua co tai khoan giao vien nao. Hay tao account role TEACHER truoc.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.PROJECT_CREATE_DIALOG));
            Parent content = loader.load();
            ProjectCreateDialogController controller = loader.getController();
            controller.setSupervisors(staffList);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Tao Project moi");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setPrefWidth(560);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            Project project = controller.buildProject(classId);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    projectService.createProject(project);
                    return null;
                }
            };
            task.setOnSucceeded(e -> Platform.runLater(() -> {
                AlertUtil.showSuccess("Tao project thanh cong");
                requestedClassId = classId;
                loadProjects();
            }));
            task.setOnFailed(e -> Platform.runLater(() -> {
                Throwable ex = task.getException();
                AlertUtil.showError("Loi: " + (ex != null ? ex.getMessage() : ""));
            }));
            new Thread(task).start();
        } catch (Exception e) {
            AlertUtil.showError("Khong the mo form tao project: " + e.getMessage());
        }
    }

    private String getProjectStatusDisplay(Project project) {
        if (project == null) {
            return "";
        }
        LocalDate today = LocalDate.now();
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            return "Hoan thanh";
        }
        if (project.getStartDate() != null && today.isBefore(project.getStartDate())) {
            return "Chua bat dau";
        }
        if (project.getEndDate() == null) {
            return "Dang hoat dong";
        }
        if (today.isAfter(project.getEndDate())) {
            return "Da qua han";
        }
        long remainingDays = ChronoUnit.DAYS.between(today, project.getEndDate());
        if (remainingDays <= NEAR_END_THRESHOLD_DAYS) {
            return "Sap het han";
        }
        return "Dang hoat dong";
    }

    private String getProjectStatusStyle(Project project) {
        if (project == null) {
            return "project-status-red";
        }
        LocalDate today = LocalDate.now();
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            return "project-status-green";
        }
        if (project.getStartDate() != null && today.isBefore(project.getStartDate())) {
            return "project-status-red";
        }
        if (project.getEndDate() == null) {
            return "project-status-green";
        }
        if (today.isAfter(project.getEndDate())) {
            return "project-status-red";
        }
        long remainingDays = ChronoUnit.DAYS.between(today, project.getEndDate());
        if (remainingDays <= NEAR_END_THRESHOLD_DAYS) {
            return "project-status-yellow";
        }
        return "project-status-green";
    }

    private void navigateToProjectDetail(int projectId) {
        javafx.scene.Node node = projectTable;
        while (node.getParent() != null) {
            node = node.getParent();
            if (node instanceof javafx.scene.layout.StackPane) {
                javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) node;
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.PROJECT_DETAIL));
                    javafx.scene.Node content = loader.load();
                    ProjectDetailController controller = loader.getController();
                    if (readOnlyMode) {
                        controller.setReadOnlyMode(true);
                        controller.setTeacherStaffId(teacherStaffId);
                    }
                    controller.initData(projectId);
                    contentArea.getChildren().setAll(content);
                } catch (Exception e) {
                    AlertUtil.showError("Loi: " + e.getMessage());
                }
                break;
            }
        }
    }

    private void refreshReadOnlyState() {
        if (addProjectBtn == null) {
            return;
        }
        addProjectBtn.setVisible(!readOnlyMode);
        addProjectBtn.setManaged(!readOnlyMode);
        addProjectBtn.setDisable(readOnlyMode || classId <= ALL_CLASSES_ID);
    }
}
