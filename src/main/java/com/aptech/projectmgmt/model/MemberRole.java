package com.aptech.projectmgmt.model;

public enum MemberRole {
    LEADER(1), MEMBER(2);

    private final int value;

    MemberRole(int value) { this.value = value; }

    public int getValue() { return value; }

    public static MemberRole fromValue(int value) {
        for (MemberRole r : values()) {
            if (r.value == value) return r;
        }
        throw new IllegalArgumentException("Unknown MemberRole value: " + value);
    }
}
