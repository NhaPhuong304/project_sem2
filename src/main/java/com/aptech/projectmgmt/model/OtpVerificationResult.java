package com.aptech.projectmgmt.model;

public class OtpVerificationResult {

    private final int resultCode;
    private final int remainingAttempts;

    public OtpVerificationResult(int resultCode, int remainingAttempts) {
        this.resultCode = resultCode;
        this.remainingAttempts = remainingAttempts;
    }

    public int getResultCode() {
        return resultCode;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }
}
