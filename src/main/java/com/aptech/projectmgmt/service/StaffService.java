package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.Staff;
import com.aptech.projectmgmt.model.TeacherCreationResult;
import com.aptech.projectmgmt.model.UserRole;
import com.aptech.projectmgmt.repository.AccountRepository;
import com.aptech.projectmgmt.repository.StaffRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.stream.Collectors;

public class StaffService {

    private final StaffRepository staffRepository = new StaffRepository();
    private final AccountRepository accountRepository = new AccountRepository();
    private final MailService mailService = new MailService();

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public List<Staff> getStaffs() {
        return staffRepository.findByRole(UserRole.STAFF);
    }

    public List<Staff> getTeachers() {
        return staffRepository.findByRole(UserRole.TEACHER);
    }

    public List<Staff> getTeachersAndStaffs() {
        return staffRepository.findAll().stream()
                .filter(s -> s.getRole() == UserRole.TEACHER || s.getRole() == UserRole.STAFF)
                .collect(Collectors.toList());
    }

    public TeacherCreationResult createStaffMember(String username, String fullName, String email, UserRole role) {
        String normalizedUsername = username != null ? username.trim() : "";
        String normalizedFullName = fullName != null ? fullName.trim() : "";
        String normalizedEmail = email != null ? email.trim() : "";

        if (normalizedUsername.isEmpty()) {
            throw new RuntimeException("Username must not be empty");
        }
        if (normalizedFullName.isEmpty()) {
            throw new RuntimeException("Teacher name must not be empty");
        }
        if (normalizedEmail.isEmpty()) {
            throw new RuntimeException("Email must not be empty");
        }
        if (!normalizedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new RuntimeException("Invalid email address");
        }
        if (accountRepository.findByUsername(normalizedUsername) != null) {
            throw new RuntimeException("Username da ton tai");
        }
        if (staffRepository.findByEmail(normalizedEmail) != null) {
            throw new RuntimeException("Teacher email already exists");
        }
       
        String temporaryPassword = "123";
        String passwordHash = BCrypt.hashpw(temporaryPassword, BCrypt.gensalt());
        int accountId;
        try {
            accountId = accountRepository.insertAccount(
                    normalizedUsername,
                    passwordHash,
                    role.getValue(),
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
                "[Aptech] Teacher account created",
                "Hello " + normalizedFullName + ",\n\n" +
                "Your account has been created successfully in the system.\n" +
                "Username: " + normalizedUsername + "\n" +
                "Temporary password: " + temporaryPassword + "\n\n" +
                "Please sign in to the system and change your password on first login to secure your account.\n\n" +
                "Best regards."
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
    public Staff findById(int staffId) {
        return staffRepository.findById(staffId);
    }
    public void updateStaffFullName(int staffId, String fullName) {
        staffRepository.updateStaffFullName(staffId, fullName);
    }
    public void updateStaff(int staffId, String username, String fullName, String email) {
        staffRepository.updateStaff(staffId, username, fullName, email);
    }
    public void updateTeacherFullName(int staffId, String fullName) {
        staffRepository.updateTeacherFullName(staffId, fullName);
    }

    public void toggleStaffStatus(int staffId) {
        staffRepository.toggleStaffStatus(staffId);
    }
}
