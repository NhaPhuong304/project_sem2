package com.aptech.projectmgmt.model;

public enum OtpPurpose {
    CHANGE_PASSWORD(1), FIRST_LOGIN(2);

    private final int value;

    OtpPurpose(int value) { this.value = value; }

    public int getValue() { return value; }

    public static OtpPurpose fromValue(int value) {
        for (OtpPurpose p : values()) {
            if (p.value == value) return p;
        }
        throw new IllegalArgumentException("Unknown OtpPurpose value: " + value);
    }
}
