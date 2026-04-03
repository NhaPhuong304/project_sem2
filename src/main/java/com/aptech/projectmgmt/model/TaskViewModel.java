package com.aptech.projectmgmt.model;

import java.time.LocalDateTime;

public class TaskViewModel {

    private int taskId;
    private int groupId;
    private String title;
    private LocalDateTime estimatedStartDate;
    private LocalDateTime estimatedEndDate;
    private LocalDateTime actualStartDate;
    private LocalDateTime actualEndDate;
    private TaskStatus status;
    private DisplayColor displayColor;
    private Integer assignedToId;
    private String assignedToName;
    private String assignedToPhoto;
    private MemberStatus assignedMemberStatus;
    private Integer reviewedById;
    private String reviewedByName;
    private MemberStatus reviewedMemberStatus;
    private Integer createdById;
    private String createdByName;
    private boolean isLate;
    private String latestRevisionNote;
    private boolean reminderSent;

    public TaskViewModel() {}

    public DisplayColor getDisplayColor() {
        if (displayColor != null) {
            return displayColor;
        }
        if (status == TaskStatus.COMPLETED && !isLate) return DisplayColor.GREEN;
        if (isLate || (status != TaskStatus.COMPLETED
                && estimatedEndDate != null && estimatedEndDate.isBefore(LocalDateTime.now()))) {
            return DisplayColor.RED;
        }
        if (status == TaskStatus.PENDING
                && estimatedStartDate != null && estimatedStartDate.isBefore(LocalDateTime.now())) {
            return DisplayColor.YELLOW;
        }
        return DisplayColor.NORMAL;
    }

    public String getStatusDisplay() {
        if (status == null) return "";
        switch (status) {
            case PENDING: return "Cho xu ly";
            case IN_PROGRESS: return "Dang thuc hien";
            case REVIEWING: return "Dang kiem tra";
            case REVISING: return "Dang chinh sua";
            case COMPLETED: return "Hoan thanh";
            default: return status.name();
        }
    }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getEstimatedStartDate() { return estimatedStartDate; }
    public void setEstimatedStartDate(LocalDateTime estimatedStartDate) { this.estimatedStartDate = estimatedStartDate; }

    public LocalDateTime getEstimatedEndDate() { return estimatedEndDate; }
    public void setEstimatedEndDate(LocalDateTime estimatedEndDate) { this.estimatedEndDate = estimatedEndDate; }

    public LocalDateTime getActualStartDate() { return actualStartDate; }
    public void setActualStartDate(LocalDateTime actualStartDate) { this.actualStartDate = actualStartDate; }

    public LocalDateTime getActualEndDate() { return actualEndDate; }
    public void setActualEndDate(LocalDateTime actualEndDate) { this.actualEndDate = actualEndDate; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public DisplayColor getDisplayColorValue() { return displayColor; }
    public void setDisplayColor(DisplayColor displayColor) { this.displayColor = displayColor; }

    public Integer getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Integer assignedToId) { this.assignedToId = assignedToId; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public String getAssignedToDisplayName() {
        if (assignedToName == null || assignedToName.isBlank()) {
            return "";
        }
        return assignedMemberStatus == MemberStatus.EXCLUDED
                ? assignedToName + " (Da bi loai)"
                : assignedToName;
    }

    public String getAssignedToPhoto() { return assignedToPhoto; }
    public void setAssignedToPhoto(String assignedToPhoto) { this.assignedToPhoto = assignedToPhoto; }

    public MemberStatus getAssignedMemberStatus() { return assignedMemberStatus; }
    public void setAssignedMemberStatus(MemberStatus assignedMemberStatus) { this.assignedMemberStatus = assignedMemberStatus; }

    public Integer getReviewedById() { return reviewedById; }
    public void setReviewedById(Integer reviewedById) { this.reviewedById = reviewedById; }

    public String getReviewedByName() { return reviewedByName; }
    public void setReviewedByName(String reviewedByName) { this.reviewedByName = reviewedByName; }

    public String getReviewedByDisplayName() {
        if (reviewedByName == null || reviewedByName.isBlank()) {
            return "";
        }
        return reviewedMemberStatus == MemberStatus.EXCLUDED
                ? reviewedByName + " (Da bi loai)"
                : reviewedByName;
    }

    public MemberStatus getReviewedMemberStatus() { return reviewedMemberStatus; }
    public void setReviewedMemberStatus(MemberStatus reviewedMemberStatus) { this.reviewedMemberStatus = reviewedMemberStatus; }

    public Integer getCreatedById() { return createdById; }
    public void setCreatedById(Integer createdById) { this.createdById = createdById; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public boolean isLate() { return isLate; }
    public void setLate(boolean late) { isLate = late; }

    public String getLatestRevisionNote() { return latestRevisionNote; }
    public void setLatestRevisionNote(String latestRevisionNote) { this.latestRevisionNote = latestRevisionNote; }

    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }
}
