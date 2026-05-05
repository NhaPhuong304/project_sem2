package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.model.Student;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class StudentCreateDialogController {

	@FXML
	private TextField studentCodeField;
	@FXML
	private TextField fullNameField;
	@FXML
	private TextField emailField;

	public void setStudentCode(String studentCode) {
		studentCodeField.setText(studentCode);
	}

	public String validate() {
		String studentCode = getStudentCode();
		String fullName = getFullName();
		String email = getEmail();

		// Trim chống nhập space
		if (studentCode.trim().isEmpty()) {
			return "Student code is required";
		}

		if (fullName.trim().isEmpty()) {
			return "Full name is required";
		}

		if (email.trim().isEmpty()) {
			return "Email is required";
		}

		String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		if (!email.matches(emailRegex)) {
			return "Invalid email format";
		}

		if (fullName.length() > 100) {
			return "Full name must be less than 100 characters";
		}

		if (email.length() > 150) {
			return "Email must be less than 150 characters";
		}

		return null;
	}

	public void setData(Student student) {
		if (student == null) {
			return;
		}

		studentCodeField.setText(student.getStudentCode());
		fullNameField.setText(student.getFullName());
		emailField.setText(student.getEmail());

		studentCodeField.setEditable(false);
		studentCodeField.setDisable(true);
	}

	public String getStudentCode() {
		return studentCodeField.getText() != null ? studentCodeField.getText().trim() : "";
	}

	public String getFullName() {
		return fullNameField.getText() != null ? fullNameField.getText().trim() : "";
	}

	public String getEmail() {
		return emailField.getText() != null ? emailField.getText().trim() : "";
	}
}