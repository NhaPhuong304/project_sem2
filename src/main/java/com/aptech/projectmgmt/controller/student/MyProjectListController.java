package com.aptech.projectmgmt.controller.student;

import com.aptech.projectmgmt.model.MemberRole;
import com.aptech.projectmgmt.model.Project;
import com.aptech.projectmgmt.model.ProjectStatus;
import com.aptech.projectmgmt.service.ProjectService;
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
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyProjectListController {

    @FXML private TableView<Project> projectTable;
    @FXML private TableColumn<Project, String> projectNameColumn;
    @FXML private TableColumn<Project, String> semesterColumn;
    @FXML private TableColumn<Project, String> supervisorColumn;
    @FXML private TableColumn<Project, String> dateRangeColumn;
    @FXML private TableColumn<Project, String> statusColumn;
    @FXML private TableColumn<Project, String> roleColumn;

    private final ProjectService projectService = new ProjectService();
    private final ObservableList<Project> allProjects = FXCollections.observableArrayList();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupTable();
        loadProjects();
    }

    private void setupTable() {
        projectNameColumn.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        supervisorColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSupervisorName() != null ? c.getValue().getSupervisorName() : ""));
        dateRangeColumn.setCellValueFactory(c -> new SimpleStringProperty(
                (c.getValue().getStartDate() != null ? c.getValue().getStartDate().format(dateTimeFormatter) : "?")
                        + " - "
                        + (c.getValue().getEndDate() != null ? c.getValue().getEndDate().format(dateTimeFormatter) : "?")));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStatus() == ProjectStatus.ACTIVE ? "Dang hoat dong" : "Hoan thanh"));
        roleColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getMyRole() == MemberRole.LEADER ? "Truong nhom" : "Thanh vien"));

        projectTable.setItems(allProjects);
        projectTable.setRowFactory(tv -> {
            TableRow<Project> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    navigateToTaskList(row.getItem());
                }
            });
            return row;
        });
    }

    private void loadProjects() {
        var student = SessionManager.getInstance().getCurrentStudent();
        if (student == null) {
            return;
        }
        Task<List<Project>> task = new Task<>() {
            @Override
            protected List<Project> call() {
                return projectService.getProjectsByStudent(student.getStudentId());
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> allProjects.setAll(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Loi tai du an: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private void navigateToTaskList(Project project) {
        Node node = projectTable;
        while (node.getParent() != null) {
            node = node.getParent();
            if (node instanceof StackPane contentArea) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.MY_TASK_LIST));
                    Node content = loader.load();
                    MyTaskListController controller = loader.getController();
                    MemberRole role = project.getMyRole() != null ? project.getMyRole() : MemberRole.MEMBER;
                    controller.initData(project.getGroupId(), project.getProjectId(), role);
                    contentArea.getChildren().setAll(content);
                } catch (Exception e) {
                    AlertUtil.showError("Loi chuyen man hinh: " + e.getMessage());
                }
                break;
            }
        }
    }
}
