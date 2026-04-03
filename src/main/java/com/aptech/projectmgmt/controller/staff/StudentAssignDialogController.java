package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.model.Student;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

import java.util.List;

public class StudentAssignDialogController {

    @FXML private ComboBox<Student> studentCombo;

    @FXML
    public void initialize() {
        studentCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Student student) {
                if (student == null) {
                    return "";
                }
                return student.getStudentCode() + " - " + student.getFullName();
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        });
        studentCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getStudentCode() + " - " + item.getFullName());
            }
        });
    }

    public void setStudents(List<Student> students) {
        studentCombo.setItems(FXCollections.observableArrayList(students));
        if (!studentCombo.getItems().isEmpty()) {
            studentCombo.getSelectionModel().selectFirst();
        }
    }

    public Student getSelectedStudent() {
        Student student = studentCombo.getValue();
        if (student == null) {
            throw new RuntimeException("Vui long chon sinh vien");
        }
        return student;
    }
}
