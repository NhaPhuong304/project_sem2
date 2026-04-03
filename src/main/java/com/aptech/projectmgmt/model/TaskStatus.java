package com.aptech.projectmgmt.model;

public enum TaskStatus {
    PENDING(1), IN_PROGRESS(2), REVIEWING(3), REVISING(4), COMPLETED(5);

    private final int value;

    TaskStatus(int value) { this.value = value; }

    public int getValue() { return value; }

    public static TaskStatus fromValue(int value) {
        for (TaskStatus s : values()) {
            if (s.value == value) return s;
        }
        throw new IllegalArgumentException("Unknown TaskStatus value: " + value);
    }
}
