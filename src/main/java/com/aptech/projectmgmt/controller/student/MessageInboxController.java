package com.aptech.projectmgmt.controller.student;

import com.aptech.projectmgmt.model.Message;
import com.aptech.projectmgmt.service.MessageService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessageInboxController {

    @FXML private ListView<Message> messageList;
    @FXML private TextArea messageDetailArea;

    private final MessageService messageService = new MessageService();
    private int currentStudentId;
    private ObservableList<Message> messages = FXCollections.observableArrayList();
    private StudentDashboardController dashboardController;

    @FXML
    public void initialize() {
        var student = SessionManager.getInstance().getCurrentStudent();
        if (student != null) currentStudentId = student.getStudentId();

        setupListView();
        loadMessages();
    }

    private void setupListView() {
        messageList.setItems(messages);
        messageList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Message m, boolean empty) {
                super.updateItem(m, empty);
                if (empty || m == null) { setText(null); setStyle(""); return; }
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                String badge = m.isRead() ? "" : " [MOI]";
                String shortContent = m.getContent() != null && m.getContent().length() > 50
                        ? m.getContent().substring(0, 50) + "..." : m.getContent();
                setText((m.getSenderName() != null ? m.getSenderName() : "?") + badge + "\n"
                        + shortContent + "\n"
                        + (m.getSentAt() != null ? m.getSentAt().format(fmt) : ""));
                setStyle(m.isRead() ? "" : "-fx-font-weight: bold;");
            }
        });

        messageList.setOnMouseClicked(event -> {
            Message selected = messageList.getSelectionModel().getSelectedItem();
            if (selected != null) handleSelectMessage(selected);
        });
    }

    private void loadMessages() {
        if (currentStudentId <= 0) return;
        Task<List<Message>> task = new Task<>() {
            @Override
            protected List<Message> call() {
                return messageService.getInboxByStudent(currentStudentId);
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> messages.setAll(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Loi tai tin nhan: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task).start();
    }

    private void handleSelectMessage(Message msg) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("Tu: ").append(msg.getSenderName() != null ? msg.getSenderName() : "?").append("\n");
        sb.append("Luc: ").append(msg.getSentAt() != null ? msg.getSentAt().format(fmt) : "?").append("\n");
        if (msg.getTaskTitle() != null) {
            sb.append("Task lien quan: ").append(msg.getTaskTitle()).append("\n");
        }
        sb.append("\n").append(msg.getContent());
        messageDetailArea.setText(sb.toString());

        if (!msg.isRead()) {
            msg.setRead(true);
            messageList.refresh();
            if (dashboardController != null) {
                dashboardController.adjustUnreadBadge(-1);
            }
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    messageService.markAsRead(msg.getMessageId());
                    return null;
                }
            };
            task.setOnSucceeded(e -> Platform.runLater(messageList::refresh));
            task.setOnFailed(e -> Platform.runLater(() -> {
                msg.setRead(false);
                messageList.refresh();
                if (dashboardController != null) {
                    dashboardController.adjustUnreadBadge(1);
                }
                Throwable ex = task.getException();
                AlertUtil.showError("Loi cap nhat trang thai tin nhan: " + (ex != null ? ex.getMessage() : ""));
            }));
            new Thread(task).start();
        }
    }

    public void setDashboardController(StudentDashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }
}
