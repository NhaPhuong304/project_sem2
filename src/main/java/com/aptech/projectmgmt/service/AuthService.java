package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.Account;
import com.aptech.projectmgmt.repository.AccountRepository;
import com.aptech.projectmgmt.repository.StaffRepository;
import com.aptech.projectmgmt.repository.StudentRepository;
import com.aptech.projectmgmt.util.SessionManager;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

	private final AccountRepository accountRepository = new AccountRepository();
	private final StudentRepository studentRepository = new StudentRepository();
	private final StaffRepository staffRepository = new StaffRepository();

	public Account login(String username, String password) {
		Account account = accountRepository.findByUsername(username);

		if (account == null) {
			throw new AuthException("Tai khoan khong ton tai");
		}

		if (!account.isActive()) {
			throw new AuthException("Tai khoan da bi khoa");
		}

		if (!BCrypt.checkpw(password, account.getPasswordHash())) {
			throw new AuthException("Sai mat khau");
		}

		SessionManager sessionManager = SessionManager.getInstance();
		sessionManager.clearSession();
		sessionManager.setCurrentAccount(account);

		if (account.getRole().isStaffRole()) {
			var staff = staffRepository.findByAccountId(account.getAccountId());
			if (staff == null) {
				throw new AuthException("Khong tim thay thong tin nhan su");
			}
			sessionManager.setCurrentStaff(staff);
		} else {
			var student = studentRepository.findByAccountId(account.getAccountId());
			if (student == null) {
				throw new AuthException("Khong tim thay thong tin sinh vien");
			}
			sessionManager.setCurrentStudent(student);
		}

		return account;
	}

	public Account findAccountForPasswordReset(String username) {
		Account account = accountRepository.findByUsername(username);
		if (account == null || !account.isActive()) {
			throw new AuthException("Khong tim thay tai khoan voi ten dang nhap nay");
		}
		return account;
	}
}
