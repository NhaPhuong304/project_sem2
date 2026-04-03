package com.aptech.projectmgmt.model;

public enum MemberStatus {
    ACTIVE(1), EXCLUDED(2);

    private final int value;

    MemberStatus(int value) { this.value = value; }

    public int getValue() { return value; }

    public static MemberStatus fromValue(int value) {
        for (MemberStatus s : values()) {
            if (s.value == value) return s;
        }
        throw new IllegalArgumentException("Unknown MemberStatus value: " + value);
    }
}
