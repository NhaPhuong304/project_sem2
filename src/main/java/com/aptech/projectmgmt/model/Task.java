package com.aptech.projectmgmt.model;

import java.time.LocalDateTime;

public class Task {

    private int taskId;
    private int groupId;
    private String title;
    private String description;
    private TaskStatus status;
    private Integer assignedTo;
    private Integer reviewedBy;
    private Integer createdBy;
    private LocalDateTime estimatedStartDate;
    private LocalDateTime estimatedEndDate;
    private LocalDateTime actualStartDate;
    private LocalDateTime actualEndDate;
    private boolean isLate;

    public Task() {}

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Integer getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Integer assignedTo) { this.assignedTo = assignedTo; }

    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getEstimatedStartDate() { return estimatedStartDate; }
    public void setEstimatedStartDate(LocalDateTime estimatedStartDate) { this.estimatedStartDate = estimatedStartDate; }

    public LocalDateTime getEstimatedEndDate() { return estimatedEndDate; }
    public void setEstimatedEndDate(LocalDateTime estimatedEndDate) { this.estimatedEndDate = estimatedEndDate; }

    public LocalDateTime getActualStartDate() { return actualStartDate; }
    public void setActualStartDate(LocalDateTime actualStartDate) { this.actualStartDate = actualStartDate; }

    public LocalDateTime getActualEndDate() { return actualEndDate; }
    public void setActualEndDate(LocalDateTime actualEndDate) { this.actualEndDate = actualEndDate; }

    public boolean isLate() { return isLate; }
    public void setLate(boolean late) { isLate = late; }
}
