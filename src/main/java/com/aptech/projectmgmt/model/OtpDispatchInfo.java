package com.aptech.projectmgmt.model;

public class OtpDispatchInfo {

    private final String maskedEmail;
    private final int otpExpirySeconds;
    private final int resendCooldownSeconds;

    public OtpDispatchInfo(String maskedEmail, int otpExpirySeconds, int resendCooldownSeconds) {
        this.maskedEmail = maskedEmail;
        this.otpExpirySeconds = otpExpirySeconds;
        this.resendCooldownSeconds = resendCooldownSeconds;
    }

    public String getMaskedEmail() {
        return maskedEmail;
    }

    public int getOtpExpirySeconds() {
        return otpExpirySeconds;
    }

    public int getResendCooldownSeconds() {
        return resendCooldownSeconds;
    }
}
