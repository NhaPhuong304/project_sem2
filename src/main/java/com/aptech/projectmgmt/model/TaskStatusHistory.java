package com.aptech.projectmgmt.model;

import java.time.LocalDateTime;

public class TaskStatusHistory {

    private int historyId;
    private int taskId;
    private TaskStatus fromStatus;
    private TaskStatus toStatus;
    private int changedBy;
    private LocalDateTime changedAt;
    private String changerName;

    public TaskStatusHistory() {}

    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public TaskStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(TaskStatus fromStatus) { this.fromStatus = fromStatus; }

    public TaskStatus getToStatus() { return toStatus; }
    public void setToStatus(TaskStatus toStatus) { this.toStatus = toStatus; }

    public int getChangedBy() { return changedBy; }
    public void setChangedBy(int changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getChangerName() { return changerName; }
    public void setChangerName(String changerName) { this.changerName = changerName; }
}
