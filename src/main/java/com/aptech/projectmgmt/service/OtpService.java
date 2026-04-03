package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.OtpDispatchInfo;
import com.aptech.projectmgmt.model.OtpPurpose;
import com.aptech.projectmgmt.model.OtpVerificationResult;
import com.aptech.projectmgmt.repository.OtpRepository;
import com.aptech.projectmgmt.repository.StaffRepository;
import com.aptech.projectmgmt.repository.StudentRepository;

public class OtpService {

    private final OtpRepository otpRepository = new OtpRepository();
    private final StudentRepository studentRepository = new StudentRepository();
    private final StaffRepository staffRepository = new StaffRepository();
    private final MailService mailService = new MailService();
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final int OTP_EXPIRY_SECONDS = 5 * 60;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    public OtpDispatchInfo generateAndSendOtp(int accountId, OtpPurpose purpose) {
        String otpCode = otpRepository.generateOtp(accountId, purpose.getValue());
        if (otpCode == null) {
            throw new RuntimeException("Khong the tao ma OTP. Vui long thu lai.");
        }
        String email = getEmailByAccountId(accountId);
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Khong tim thay email de gui OTP.");
        }
        sendEmail(email, otpCode);
        return new OtpDispatchInfo(maskEmail(email), OTP_EXPIRY_SECONDS, RESEND_COOLDOWN_SECONDS);
    }

    public OtpVerificationResult verifyOtp(int accountId, OtpPurpose purpose, String otpCode, String newPasswordHash) {
        int resultCode = otpRepository.verifyOtp(accountId, purpose.getValue(), otpCode, newPasswordHash);
        int remainingAttempts = -1;
        if (resultCode == 1 || resultCode == 2) {
            int attemptCount = otpRepository.getLatestAttemptCount(accountId, purpose.getValue());
            remainingAttempts = Math.max(0, MAX_OTP_ATTEMPTS - attemptCount);
        }
        return new OtpVerificationResult(resultCode, remainingAttempts);
    }

    private String getEmailByAccountId(int accountId) {
        // Try student first
        var student = studentRepository.findByAccountId(accountId);
        if (student != null) return student.getEmail();
        // Try staff
        var staff = staffRepository.findByAccountId(accountId);
        if (staff != null) return staff.getEmail();
        return null;
    }

    private void sendEmail(String toEmail, String otpCode) {
        mailService.sendEmail(
                toEmail,
                "[Aptech] Ma xac thuc OTP",
                "Ma OTP cua ban la: " + otpCode + "\n" +
                "Ma co hieu luc trong 5 phut.\n" +
                "Ban co the yeu cau gui lai ma moi sau 60 giay tren ung dung.\n" +
                "Khong chia se ma nay cho bat ky ai."
        );
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "***";
        }

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);
        if (localPart.length() == 1) {
            return "*" + domainPart;
        }
        return localPart.charAt(0) + "***" + domainPart;
    }
}
