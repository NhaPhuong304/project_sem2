package com.aptech.projectmgmt.controller.student;

import com.aptech.projectmgmt.model.GroupMember;
import com.aptech.projectmgmt.model.Task;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class TaskCreateDialogController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker startDatePicker;
    @FXML private TextField startTimeField;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField endTimeField;
    @FXML private ComboBox<GroupMember> assignedCombo;
    @FXML private ComboBox<GroupMember> reviewerCombo;

    @FXML
    public void initialize() {
        StringConverter<GroupMember> memberConverter = new StringConverter<>() {
            @Override
            public String toString(GroupMember member) {
                return member != null ? member.getStudentFullName() : "";
            }

            @Override
            public GroupMember fromString(String string) {
                return null;
            }
        };
        assignedCombo.setConverter(memberConverter);
        reviewerCombo.setConverter(memberConverter);
        descriptionArea.setWrapText(true);

        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime suggestedEnd = now.plusHours(1);
        startDatePicker.setValue(now.toLocalDate());
        endDatePicker.setValue(suggestedEnd.toLocalDate());
        startTimeField.setText(now.toLocalTime().toString());
        endTimeField.setText(suggestedEnd.toLocalTime().toString());
    }

    public void setMembers(List<GroupMember> members) {
        assignedCombo.setItems(FXCollections.observableArrayList(members));
        reviewerCombo.setItems(FXCollections.observableArrayList(members));
    }

    public Task buildTask(int groupId, int currentStudentId) {
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Tieu de task khong duoc de trong");
        }
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            throw new IllegalArgumentException("Vui long chon ngay bat dau va ket thuc");
        }
        if (assignedCombo.getValue() == null || reviewerCombo.getValue() == null) {
            throw new IllegalArgumentException("Vui long chon nguoi thuc hien va nguoi kiem tra");
        }

        LocalTime startTime = parseTime(startTimeField.getText(), "Gio bat dau khong hop le. Dung dinh dang HH:mm");
        LocalTime endTime = parseTime(endTimeField.getText(), "Gio ket thuc khong hop le. Dung dinh dang HH:mm");
        LocalDateTime estimatedStart = startDatePicker.getValue().atTime(startTime);
        LocalDateTime estimatedEnd = endDatePicker.getValue().atTime(endTime);

        if (!estimatedEnd.isAfter(estimatedStart)) {
            throw new IllegalArgumentException("Ngay gio ket thuc phai sau ngay gio bat dau");
        }

        if (estimatedStart.isBefore(LocalDateTime.now().minusHours(1))) {
            throw new IllegalArgumentException("Ngay gio bat dau da qua hon 1 gio. Task se bi he thong tu dong bo phan cong.");
        }

        Task task = new Task();
        task.setGroupId(groupId);
        task.setTitle(titleField.getText().trim());
        task.setDescription(descriptionArea.getText() != null ? descriptionArea.getText().trim() : null);
        task.setCreatedBy(currentStudentId);
        task.setAssignedTo(assignedCombo.getValue().getStudentId());
        task.setReviewedBy(reviewerCombo.getValue().getStudentId());
        task.setEstimatedStartDate(estimatedStart);
        task.setEstimatedEndDate(estimatedEnd);
        return task;
    }

    private LocalTime parseTime(String value, String errorMessage) {
        try {
            return LocalTime.parse(value != null ? value.trim() : "");
        } catch (Exception ex) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
