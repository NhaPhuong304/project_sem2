package com.aptech.projectmgmt.controller.teacher;

import com.aptech.projectmgmt.controller.TextPromptDialogController;
import com.aptech.projectmgmt.model.Question;
import com.aptech.projectmgmt.model.Student;
import com.aptech.projectmgmt.service.MailService;
import com.aptech.projectmgmt.service.QuestionService;
import com.aptech.projectmgmt.service.StudentService;
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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TeacherInboxController {

    @FXML
    private TableView<Question> questionTable;
    @FXML
    private TableColumn<Question, String> studentNameColumn;
    @FXML
    private TableColumn<Question, String> taskTitleColumn;
    @FXML
    private TableColumn<Question, String> questionContentColumn;
    @FXML
    private TableColumn<Question, String> answerContentColumn;
    @FXML
    private TableColumn<Question, String> createdAtColumn;
    @FXML
    private TableColumn<Question, String> statusColumn;
    @FXML
    private Button refreshBtn;
    @FXML
    private Button replyBtn;

    private final MailService mailService = new MailService();
    private final StudentService studentService = new StudentService();
    private final QuestionService questionService = new QuestionService();
    private final ObservableList<Question> questionList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        taskTitleColumn.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getTaskTitle() != null ? c.getValue().getTaskTitle() : ""));
        questionContentColumn.setCellValueFactory(new PropertyValueFactory<>("questionContent"));
        answerContentColumn.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getAnswerContent() != null ? c.getValue().getAnswerContent() : ""));
        createdAtColumn.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().format(formatter) : ""));
        statusColumn.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().isAnswered() ? "Da tra loi" : "Chua tra loi"));

        questionTable.setItems(questionList);

        refreshBtn.setOnAction(e -> loadQuestions());
        replyBtn.setOnAction(e -> handleReply());

        loadQuestions();
    }

    private void loadQuestions() {
        int teacherId = SessionManager.getInstance().getCurrentStaff().getStaffId();

        Task<List<Question>> task = new Task<>() {
            @Override
            protected List<Question> call() {
                return questionService.getQuestionsByTeacher(teacherId);
            }
        };

        task.setOnSucceeded(e -> Platform.runLater(() -> questionList.setAll(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Loi tai hop thu: " + (ex != null ? ex.getMessage() : ""));
        }));

        new Thread(task).start();
    }

    private void handleReply() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Vui long chon 1 cau hoi de tra loi");
            return;
        }

        Optional<String> result = showTextPromptDialog(
                "Tra loi sinh vien",
                "Nhap noi dung tra loi",
                "Noi dung tra loi"
        );

        result.ifPresent(answer -> {
            String trimmed = answer != null ? answer.trim() : "";
            if (trimmed.isEmpty()) {
                AlertUtil.showError("Noi dung tra loi khong duoc de trong");
                return;
            }

            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    questionService.answerQuestion(selected.getQuestionId(), trimmed);

                    Student student = studentService.findById(selected.getStudentId());

                    if (student == null) {
                        throw new RuntimeException("Khong tim thay sinh vien voi ID = " + selected.getStudentId());
                    }

                    if (student.getEmail() == null || student.getEmail().isBlank()) {
                        throw new RuntimeException("Sinh vien chua co email");
                    }

                    System.out.println("=== REPLY MAIL DEBUG ===");
                    System.out.println("Student ID = " + student.getStudentId());
                    System.out.println("Student email = " + student.getEmail());
                    System.out.println("Question ID = " + selected.getQuestionId());

                    String subject = "[Tra loi cau hoi] "
                            + (selected.getTaskTitle() != null ? selected.getTaskTitle() : "Task");
                    String body = "Giao vien da tra loi cau hoi cua ban.\n\n"
                            + "Cau hoi:\n" + selected.getQuestionContent() + "\n\n"
                            + "Tra loi:\n" + trimmed;

                    mailService.sendEmail(student.getEmail(), subject, body);
                    return true;
                }
            };

            task.setOnSucceeded(e -> Platform.runLater(() -> {
                AlertUtil.showSuccess("Tra loi thanh cong va da gui email cho sinh vien");
                loadQuestions();
            }));

            task.setOnFailed(e -> Platform.runLater(() -> {
                Throwable ex = task.getException();
                AlertUtil.showError(ex != null ? ex.getMessage() : "Tra loi that bai");
            }));

            new Thread(task).start();
        });
    }

    private Optional<String> showTextPromptDialog(String title, String headerText, String promptText) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.TEXT_PROMPT_DIALOG));
            Parent content = loader.load();
            TextPromptDialogController controller = loader.getController();
            controller.initData(headerText, promptText, "");

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(title);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setPrefWidth(540);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                return Optional.of(controller.getContent());
            }
        } catch (Exception ex) {
            AlertUtil.showError("Khong the mo form nhap noi dung: " + ex.getMessage());
        }
        return Optional.empty();
    }
}