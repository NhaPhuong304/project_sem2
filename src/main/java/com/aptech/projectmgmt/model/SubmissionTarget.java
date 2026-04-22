package com.aptech.projectmgmt.model;

import java.time.LocalDateTime;

public class SubmissionTarget {

    private int targetId;
    private int requestId;
    private int groupId;
    private int leaderStudentId;
    private int status;
    private LocalDateTime notifiedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    private String groupName;
    private String leaderName;
    private String requestTitle;
    private String requestDescription;
    private LocalDateTime requestDeadline;

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getLeaderStudentId() {
        return leaderStudentId;
    }

    public void setLeaderStudentId(int leaderStudentId) {
        this.leaderStudentId = leaderStudentId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(LocalDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getRequestTitle() {
        return requestTitle;
    }

    public void setRequestTitle(String requestTitle) {
        this.requestTitle = requestTitle;
    }

    public String getRequestDescription() {
        return requestDescription;
    }

    public void setRequestDescription(String requestDescription) {
        this.requestDescription = requestDescription;
    }

    public LocalDateTime getRequestDeadline() {
        return requestDeadline;
    }

    public void setRequestDeadline(LocalDateTime requestDeadline) {
        this.requestDeadline = requestDeadline;
    }
}
