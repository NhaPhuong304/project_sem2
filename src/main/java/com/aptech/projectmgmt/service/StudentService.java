package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.Student;
import com.aptech.projectmgmt.model.StudentCreationResult;
import com.aptech.projectmgmt.model.UserRole;
import com.aptech.projectmgmt.repository.AccountRepository;
import com.aptech.projectmgmt.repository.StudentRepository;
import com.aptech.projectmgmt.util.SessionManager;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class StudentService {

	private final StudentRepository studentRepository = new StudentRepository();
	private final AccountRepository accountRepository = new AccountRepository();
	private final ClassService classService = new ClassService();
	private final MailService mailService = new MailService();

	public List<Student> getStudentsByClass(int classId) {
		return studentRepository.findByClassId(classId);
	}
	

	public String getNextStudentCode() {
		return studentRepository.getNextStudentCode();
	}

	public List<Student> getAllStudents() {
		return studentRepository.findAll();
	}

	public List<Student> getUnassignedStudents() {
		int unassignedClassId = classService.ensureUnassignedClass();
		return studentRepository.findByClassId(unassignedClassId);
	}

	public void updateStudent(int studentId, String fullName, String email) {
		studentRepository.updateStudent(studentId, fullName, email);
	}
	public Student findById(int studentId) {
	    return studentRepository.findById(studentId);
	}

	public int createAccountsForClass(int classId) {
		List<Student> withoutAccount = studentRepository.findWithoutAccount(classId);
		int count = 0;
		for (Student student : withoutAccount) {
			String passwordHash = BCrypt.hashpw("123", BCrypt.gensalt());
			int accountId = accountRepository.insertAccount(student.getStudentCode(), passwordHash,
					UserRole.STUDENT.getValue(), true);
			if (accountId > 0) {
				studentRepository.updateAccountId(student.getStudentId(), accountId);
				count++;
			}
		}
		return count;
	}
	

	public StudentCreationResult addStudent(String studentCode, String fullName, String email) {
		String normalizedStudentCode = studentCode != null ? studentCode.trim() : "";
		String normalizedFullName = fullName != null ? fullName.trim() : "";
		String normalizedEmail = email != null ? email.trim() : "";

		if (normalizedStudentCode.isEmpty()) {
			throw new RuntimeException("Student code must not be empty");
		}
		if (normalizedFullName.isEmpty()) {
			throw new RuntimeException("Full name must not be empty");
		}
		if (normalizedEmail.isEmpty()) {
			throw new RuntimeException("Email must not be empty");
		}
		if (studentRepository.existsEmailInStudent(email) || studentRepository.existsEmailInStaff(email)) {
		    throw new RuntimeException("Email already exists.");
		}
		if (!normalizedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
			throw new RuntimeException("Invalid email address");
		}
		if (studentRepository.findByStudentCode(normalizedStudentCode) != null) {
			throw new RuntimeException("Student code already exists");
		}
		if (studentRepository.findByEmail(normalizedEmail) != null) {
			throw new RuntimeException("Email already exists");
		}
		if (accountRepository.findByUsername(normalizedStudentCode) != null) {
			throw new RuntimeException("The default username based on the student code already exists");
		}

		String temporaryPassword = "123";
		String passwordHash = BCrypt.hashpw(temporaryPassword, BCrypt.gensalt());
		int unassignedClassId = classService.ensureUnassignedClass();
		int accountId = accountRepository.insertAccount(normalizedStudentCode, passwordHash,
				UserRole.STUDENT.getValue(), true);

		Integer createdByStaffId = null;
		var currentStaff = SessionManager.getInstance().getCurrentStaff();
		if (currentStaff != null) {
			createdByStaffId = currentStaff.getStaffId();
		}

		try {
			studentRepository.create(normalizedStudentCode, normalizedFullName, normalizedEmail, unassignedClassId,
					accountId, createdByStaffId);
		} catch (RuntimeException ex) {
			if (accountId > 0) {
				try {
					accountRepository.deleteAccount(accountId);
				} catch (RuntimeException ignored) {
					// Best-effort cleanup to avoid orphan account when Student insert fails.
				}
			}
			throw ex;
		}

		boolean emailSent = mailService.sendEmailQuietly(normalizedEmail, "[Aptech] Student account created",
				"Hello " + normalizedFullName + ",\n\n" + "Your student account has been created successfully.\n"
						+ "Username: " + normalizedStudentCode + "\n" + "Temporary password: " + temporaryPassword
						+ "\n\n"
						+ "Please sign in to the system and change your password on first login to secure your account.\n"
						+ "The system will require OTP verification by email when you change your password.\n\n"
						+ "Best regards.");

		return new StudentCreationResult(normalizedStudentCode, temporaryPassword, emailSent);
	}

	public void assignStudentToClass(int studentId, int classId) {
		Student student = studentRepository.findById(studentId);
		if (student == null) {
			throw new RuntimeException("Student could not be found");
		}
		int unassignedClassId = classService.ensureUnassignedClass();
		if (student.getClassId() != unassignedClassId) {
			throw new RuntimeException("This student already has a class and cannot be added again");
		}
		studentRepository.updateClassId(studentId, classId);
	}

	public void lockStudent(int studentId) {
		Student student = studentRepository.findById(studentId);
		if (student == null) {
			throw new RuntimeException("Student could not be found");
		}

		if (student.getAccountId() == null) {
			throw new RuntimeException("This student does not have an account yet");
		}

		if (!student.isActive()) {
			throw new RuntimeException("The student account has already been locked");
		}

		if (studentRepository.isStudentWorkingOnProject(studentId)) {
			throw new RuntimeException("The student is currently involved in a project and cannot be locked");
		}

		studentRepository.updateStudentAccountStatus(studentId, false);
	}

	public void unlockStudent(int studentId) {
		Student student = studentRepository.findById(studentId);
		if (student == null) {
			throw new RuntimeException("Student could not be found");
		}

		if (student.getAccountId() == null) {
			throw new RuntimeException("This student does not have an account yet");
		}

		if (student.isActive()) {
			throw new RuntimeException("The student account is already active");
		}

		studentRepository.updateStudentAccountStatus(studentId, true);
	}

	public boolean isStudentWorkingOnProject(int studentId) {
		return studentRepository.isStudentWorkingOnProject(studentId);
	}

	public void transferStudentToClass(int studentId, int newClassId) {
		Student student = studentRepository.findById(studentId);
		if (student == null) {
			throw new RuntimeException("Student could not be found");
		}

		var newClassObj = classService.getClassById(newClassId);
		if (newClassObj == null) {
			throw new RuntimeException("The new class could not be found");
		}

		int unassignedClassId = classService.ensureUnassignedClass();
		if (newClassId == unassignedClassId) {
			throw new RuntimeException("Transfer to the backup class is not allowed");
		}

		if (student.getClassId() == newClassId) {
			throw new RuntimeException("The student is already in this class");
		}

		if (studentRepository.isStudentWorkingOnProject(studentId)) {
			throw new RuntimeException("The student is currently involved in a project and cannot be transferred");
		}

		studentRepository.updateClassId(studentId, newClassId);
	}
}
