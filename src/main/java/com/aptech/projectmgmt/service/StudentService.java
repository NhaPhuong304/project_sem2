package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.Student;
import com.aptech.projectmgmt.model.StudentCreationResult;
import com.aptech.projectmgmt.model.UserRole;
import com.aptech.projectmgmt.repository.AccountRepository;
import com.aptech.projectmgmt.repository.StudentRepository;
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

    public int createAccountsForClass(int classId) {
        List<Student> withoutAccount = studentRepository.findWithoutAccount(classId);
        int count = 0;
        for (Student student : withoutAccount) {
            String passwordHash = BCrypt.hashpw("123", BCrypt.gensalt());
            int accountId = accountRepository.insertAccount(
                    student.getStudentCode(),
                    passwordHash,
                    UserRole.STUDENT.getValue(),
                    true
            );
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
            throw new RuntimeException("Ma sinh vien khong duoc de trong");
        }
        if (normalizedFullName.isEmpty()) {
            throw new RuntimeException("Ho ten khong duoc de trong");
        }
        if (normalizedEmail.isEmpty()) {
            throw new RuntimeException("Email khong duoc de trong");
        }
        if (!normalizedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new RuntimeException("Email khong hop le");
        }
        if (studentRepository.findByStudentCode(normalizedStudentCode) != null) {
            throw new RuntimeException("Ma sinh vien da ton tai");
        }
        if (studentRepository.findByEmail(normalizedEmail) != null) {
            throw new RuntimeException("Email da ton tai");
        }
        if (accountRepository.findByUsername(normalizedStudentCode) != null) {
            throw new RuntimeException("Username mac dinh theo ma sinh vien da ton tai");
        }

        String temporaryPassword = "123";
        String passwordHash = BCrypt.hashpw(temporaryPassword, BCrypt.gensalt());
        int unassignedClassId = classService.ensureUnassignedClass();
        int accountId = accountRepository.insertAccount(
                normalizedStudentCode,
                passwordHash,
                UserRole.STUDENT.getValue(),
                true
        );

        try {
            studentRepository.create(normalizedStudentCode, normalizedFullName, normalizedEmail, unassignedClassId, accountId);
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

        boolean emailSent = mailService.sendEmailQuietly(
                normalizedEmail,
                "[Aptech] Tai khoan sinh vien da duoc tao",
                "Xin chao " + normalizedFullName + ",\n\n" +
                "Tai khoan sinh vien cua ban da duoc tao thanh cong.\n" +
                "Username: " + normalizedStudentCode + "\n" +
                "Mat khau tam thoi: " + temporaryPassword + "\n\n" +
                "Vui long dang nhap vao he thong va doi mat khau ngay trong lan dau tien de bao mat tai khoan.\n" +
                "He thong se yeu cau xac thuc OTP qua email khi ban doi mat khau.\n\n" +
                "Tran trong."
        );

        return new StudentCreationResult(normalizedStudentCode, temporaryPassword, emailSent);
    }

    public void assignStudentToClass(int studentId, int classId) {
        Student student = studentRepository.findById(studentId);
        if (student == null) {
            throw new RuntimeException("Khong tim thay sinh vien");
        }
        int unassignedClassId = classService.ensureUnassignedClass();
        if (student.getClassId() != unassignedClassId) {
            throw new RuntimeException("Sinh vien nay da co lop, khong the them lai");
        }
        studentRepository.updateClassId(studentId, classId);
    }
}
