package com.aptech.projectmgmt.model;

public enum ProjectStatus {
    ACTIVE(1), COMPLETED(2);

    private final int value;

    ProjectStatus(int value) { this.value = value; }

    public int getValue() { return value; }

    public static ProjectStatus fromValue(int value) {
        for (ProjectStatus s : values()) {
            if (s.value == value) return s;
        }
        throw new IllegalArgumentException("Unknown ProjectStatus value: " + value);
    }
}
