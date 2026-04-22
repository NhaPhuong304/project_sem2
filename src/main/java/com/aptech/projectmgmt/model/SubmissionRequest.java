package com.aptech.projectmgmt.model;

import java.time.LocalDateTime;

public class SubmissionRequest {

    private int requestId;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private int createdByStaffId;
    private int status;
    private LocalDateTime createdAt;
    private String createdByStaffName;

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public int getCreatedByStaffId() {
        return createdByStaffId;
    }

    public void setCreatedByStaffId(int createdByStaffId) {
        this.createdByStaffId = createdByStaffId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedByStaffName() {
        return createdByStaffName;
    }

    public void setCreatedByStaffName(String createdByStaffName) {
        this.createdByStaffName = createdByStaffName;
    }
}
