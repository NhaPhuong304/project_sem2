package com.aptech.projectmgmt.controller.teacher;

import com.aptech.projectmgmt.controller.TextPromptDialogController;
import com.aptech.projectmgmt.model.Question;
import com.aptech.projectmgmt.model.Student;
import com.aptech.projectmgmt.service.MailService;
import com.aptech.projectmgmt.service.MessageService;
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
    private final MessageService messageService = new MessageService();
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
                c -> new SimpleStringProperty(c.getValue().isAnswered() ? "Answered" : "Pending"));

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
            AlertUtil.showError("Failed to load the inbox: " + (ex != null ? ex.getMessage() : ""));
        }));

        new Thread(task).start();
    }

    private void handleReply() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Please select a question to answer.");
            return;
        }

        Optional<String> result = showTextPromptDialog(
                "Reply to Student",
                "Enter your reply",
                "Reply content"
        );

        result.ifPresent(answer -> {
            String trimmed = answer != null ? answer.trim() : "";
            if (trimmed.isEmpty()) {
                AlertUtil.showError("Reply content must not be empty.");
                return;
            }

            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    questionService.answerQuestion(selected.getQuestionId(), trimmed);
                    messageService.createAnswerMessageForStudent(
                            SessionManager.getInstance().getCurrentStaff().getStaffId(),
                            selected.getStudentId(),
                            selected.getTaskId(),
                            trimmed
                    );

                    Student student = studentService.findById(selected.getStudentId());

                    if (student == null) {
                        throw new RuntimeException("Could not find the student with ID = " + selected.getStudentId());
                    }

                    if (student.getEmail() == null || student.getEmail().isBlank()) {
                        throw new RuntimeException("The student does not have an email address yet.");
                    }

                    String subject = "[Question Reply] "
                            + (selected.getTaskTitle() != null ? selected.getTaskTitle() : "Task");
                    String body = "Your teacher has replied to your question.\n\n"
                            + "Question:\n" + selected.getQuestionContent() + "\n\n"
                            + "Reply:\n" + trimmed;

                    mailService.sendEmail(student.getEmail(), subject, body);
                    return true;
                }
            };

            task.setOnSucceeded(e -> Platform.runLater(() -> {
                AlertUtil.showSuccess("Reply sent successfully and email notification delivered to the student.");
                loadQuestions();
            }));

            task.setOnFailed(e -> Platform.runLater(() -> {
                Throwable ex = task.getException();
                AlertUtil.showError(ex != null ? ex.getMessage() : "Reply failed.");
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
            AlertUtil.showError("Could not open the input form: " + ex.getMessage());
        }
        return Optional.empty();
    }
}
