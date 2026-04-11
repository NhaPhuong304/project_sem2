package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.model.SchoolClass;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

import java.util.List;

public class StudentTransferDialogController {

    @FXML
    private ComboBox<SchoolClass> classCombo;

    @FXML
    public void initialize() {
        classCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(SchoolClass schoolClass) {
                if (schoolClass == null) return "";
                return schoolClass.getClassName() + " (" +
                        schoolClass.getAcademicYear() + ")";
            }

            @Override
            public SchoolClass fromString(String string) {
                return null;
            }
        });

        classCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SchoolClass item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? null
                        : item.getClassName() + " (" + " - " + item.getAcademicYear() + ")");
            }
        });
    }

    public void setClasses(List<SchoolClass> classes, int currentClassId) {
        classCombo.setItems(FXCollections.observableArrayList(
                classes.stream().filter(c -> c.getClassId() != currentClassId).toList()
        ));
        if (!classCombo.getItems().isEmpty()) {
            classCombo.getSelectionModel().selectFirst();
        }
    }

    public SchoolClass getSelectedClass() {
        SchoolClass selected = classCombo.getValue();
        if (selected == null) {
            throw new RuntimeException("Vui long chon lop moi");
        }
        return selected;
    }
}