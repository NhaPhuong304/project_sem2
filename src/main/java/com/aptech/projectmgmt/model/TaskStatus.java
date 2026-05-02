package com.aptech.projectmgmt.model;

public enum TaskStatus {
    PENDING(1, "Pending"),
    IN_PROGRESS(2, "In Progress"),
    REVIEWING(3, "Reviewing"),
    REVISING(4, "Revising"),
    COMPLETED(5, "Completed");

    private final int value;
    private final String displayName;

    TaskStatus(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TaskStatus fromValue(int value) {
        for (TaskStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown TaskStatus value: " + value);
    }

    @Override
    public String toString() {
        return displayName;
    }
}