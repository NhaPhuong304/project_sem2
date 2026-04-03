package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.Staff;
import com.aptech.projectmgmt.model.TeacherCreationResult;
import com.aptech.projectmgmt.model.UserRole;
import com.aptech.projectmgmt.repository.AccountRepository;
import com.aptech.projectmgmt.repository.StaffRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class StaffService {

    private final StaffRepository staffRepository = new StaffRepository();
    private final AccountRepository accountRepository = new AccountRepository();
    private final MailService mailService = new MailService();

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public List<Staff> getTeachers() {
        return staffRepository.findByRole(UserRole.TEACHER);
    }

    public TeacherCreationResult createTeacher(String username, String fullName, String email) {
        String normalizedUsername = username != null ? username.trim() : "";
        String normalizedFullName = fullName != null ? fullName.trim() : "";
        String normalizedEmail = email != null ? email.trim() : "";

        if (normalizedUsername.isEmpty()) {
            throw new RuntimeException("Username khong duoc de trong");
        }
        if (normalizedFullName.isEmpty()) {
            throw new RuntimeException("Ho ten giao vien khong duoc de trong");
        }
        if (normalizedEmail.isEmpty()) {
            throw new RuntimeException("Email khong duoc de trong");
        }
        if (!normalizedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new RuntimeException("Email khong hop le");
        }
        if (accountRepository.findByUsername(normalizedUsername) != null) {
            throw new RuntimeException("Username da ton tai");
        }
        if (staffRepository.findByEmail(normalizedEmail) != null) {
            throw new RuntimeException("Email giao vien da ton tai");
        }

        String temporaryPassword = "123";
        String passwordHash = BCrypt.hashpw(temporaryPassword, BCrypt.gensalt());
        int accountId;
        try {
            accountId = accountRepository.insertAccount(
                    normalizedUsername,
                    passwordHash,
                    UserRole.TEACHER.getValue(),
                    true
            );
        } catch (RuntimeException ex) {
            throw translateTeacherCreationError(ex);
        }

        try {
            staffRepository.create(normalizedFullName, normalizedEmail, accountId);
        } catch (RuntimeException ex) {
            if (accountId > 0) {
                try {
                    accountRepository.deleteAccount(accountId);
                } catch (RuntimeException ignored) {
                    // Best-effort cleanup to avoid orphan account when Staff insert fails.
                }
            }
            throw translateTeacherCreationError(ex);
        }

        boolean emailSent = mailService.sendEmailQuietly(
                normalizedEmail,
                "[Aptech] Tai khoan giao vien da duoc tao",
                "Xin chao " + normalizedFullName + ",\n\n" +
                "Tai khoan giao vien cua ban da duoc tao thanh cong.\n" +
                "Username: " + normalizedUsername + "\n" +
                "Mat khau tam thoi: " + temporaryPassword + "\n\n" +
                "Vui long dang nhap vao he thong va doi mat khau ngay trong lan dau tien de bao mat tai khoan.\n" +
                "Sau khi dang nhap, ban se chi xem duoc lop va project ma minh dang huong dan.\n\n" +
                "Tran trong."
        );

        return new TeacherCreationResult(normalizedUsername, temporaryPassword, emailSent);
    }

    private RuntimeException translateTeacherCreationError(RuntimeException ex) {
        String message = ex.getMessage();
        if (message != null && (
                message.contains("CK_Account_Role")
                        || message.contains("Role")
                        || message.contains("CHECK constraint")
        )) {
            return new RuntimeException(
                    "Database chua ho tro role TEACHER. Hay chay file migrate_account_role_teacher.sql truoc.",
                    ex
            );
        }
        return ex;
    }
}
