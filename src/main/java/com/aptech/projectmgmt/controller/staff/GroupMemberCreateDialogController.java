package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.model.MemberRole;
import com.aptech.projectmgmt.model.Student;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

import java.util.List;

public class GroupMemberCreateDialogController {

	@FXML
	private ComboBox<Student> studentCombo;
	@FXML
	private ComboBox<MemberRole> roleCombo;

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

		roleCombo.setItems(FXCollections.observableArrayList(MemberRole.LEADER, MemberRole.MEMBER));
		roleCombo.setConverter(new StringConverter<>() {
			@Override
			public String toString(MemberRole role) {
				if (role == null) {
					return "";
				}
				return role == MemberRole.LEADER ? "Truong nhom" : "Thanh vien";
			}

			@Override
			public MemberRole fromString(String string) {
				return null;
			}
		});
		roleCombo.getSelectionModel().select(MemberRole.MEMBER);
	}

	public void setAvailableStudents(List<Student> students) {
		studentCombo.setItems(FXCollections.observableArrayList(students));
		if (!studentCombo.getItems().isEmpty()) {
			studentCombo.getSelectionModel().selectFirst();
		}
	}

	public void configureRoleOptions(boolean isFirstMember, boolean hasLeader) {
		roleCombo.getItems().clear();

		if (isFirstMember) {
			roleCombo.getItems().add(MemberRole.LEADER);
		} else {
			roleCombo.getItems().add(MemberRole.MEMBER);
		}

		if (!roleCombo.getItems().isEmpty()) {
			roleCombo.getSelectionModel().selectFirst();
		}
	}

	public Student getSelectedStudent() {
		Student student = studentCombo.getValue();
		if (student == null) {
			throw new RuntimeException("Vui long chon sinh vien");
		}
		return student;
	}

	public MemberRole getSelectedRole() {
		MemberRole role = roleCombo.getValue();
		if (role == null) {
			throw new RuntimeException("Vui long chon vai tro");
		}
		return role;
	}
}
