package com.aptech.projectmgmt.model;

public enum UserRole {
    ADMIN(1), STUDENT(2), TEACHER(3);

    private final int value;

    UserRole(int value) { this.value = value; }

    public int getValue() { return value; }

    public boolean isStaffRole() {
        return this == ADMIN || this == TEACHER;
    }

    public static UserRole fromValue(int value) {
        for (UserRole r : values()) {
            if (r.value == value) return r;
        }
        throw new IllegalArgumentException("Unknown UserRole value: " + value);
    }
}
