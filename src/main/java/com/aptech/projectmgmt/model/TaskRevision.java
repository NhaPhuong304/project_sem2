package com.aptech.projectmgmt.model;

import java.time.LocalDateTime;

public class TaskRevision {

    private int revisionId;
    private int taskId;
    private String note;
    private int reviewedBy;
    private LocalDateTime createdAt;

    public TaskRevision() {}

    public int getRevisionId() { return revisionId; }
    public void setRevisionId(int revisionId) { this.revisionId = revisionId; }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(int reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
