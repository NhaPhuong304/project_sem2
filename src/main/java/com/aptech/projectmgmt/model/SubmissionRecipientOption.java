package com.aptech.projectmgmt.model;

public class SubmissionRecipientOption {

    private int groupId;
    private String groupName;
    private String className;
    private Integer projectId;
    private String projectTitle;
    private int leaderStudentId;
    private String leaderName;
    private String leaderStudentCode;

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public int getLeaderStudentId() {
        return leaderStudentId;
    }

    public void setLeaderStudentId(int leaderStudentId) {
        this.leaderStudentId = leaderStudentId;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getLeaderStudentCode() {
        return leaderStudentCode;
    }

    public void setLeaderStudentCode(String leaderStudentCode) {
        this.leaderStudentCode = leaderStudentCode;
    }

    public String getDisplayText() {
        StringBuilder text = new StringBuilder();
        text.append(className != null ? className : "No class");
        text.append(" - ");
        text.append(groupName != null ? groupName : "No group");
        text.append(" - Leader: ");
        text.append(leaderName != null ? leaderName : "Unknown");
        if (leaderStudentCode != null && !leaderStudentCode.isBlank()) {
            text.append(" (").append(leaderStudentCode).append(")");
        }
        if (projectTitle != null && !projectTitle.isBlank()) {
            text.append(" - Project: ").append(projectTitle);
        }
        return text.toString();
    }
}
