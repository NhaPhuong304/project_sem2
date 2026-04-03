package com.aptech.projectmgmt.model;

import java.time.LocalDateTime;

public class OtpVerification {

    private int otpId;
    private int accountId;
    private OtpPurpose purpose;
    private String otpCode;
    private LocalDateTime expiresAt;
    private int attemptCount;
    private boolean isUsed;

    public OtpVerification() {}

    public int getOtpId() { return otpId; }
    public void setOtpId(int otpId) { this.otpId = otpId; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public OtpPurpose getPurpose() { return purpose; }
    public void setPurpose(OtpPurpose purpose) { this.purpose = purpose; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }

    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }
}
