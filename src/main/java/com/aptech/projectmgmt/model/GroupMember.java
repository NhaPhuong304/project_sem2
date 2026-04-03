package com.aptech.projectmgmt.model;

import java.time.LocalDateTime;

public class GroupMember {

    private int memberId;
    private int groupId;
    private int studentId;
    private MemberRole role;
    private MemberStatus status;
    private int abandonCount;
    private Integer excludedBy;
    private LocalDateTime excludedAt;
    // Non-DB fields populated via JOIN
    private String studentCode;
    private String studentFullName;
    private String studentPhotoUrl;

    public GroupMember() {}

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public MemberRole getRole() { return role; }
    public void setRole(MemberRole role) { this.role = role; }

    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }

    public int getAbandonCount() { return abandonCount; }
    public void setAbandonCount(int abandonCount) { this.abandonCount = abandonCount; }

    public Integer getExcludedBy() { return excludedBy; }
    public void setExcludedBy(Integer excludedBy) { this.excludedBy = excludedBy; }

    public LocalDateTime getExcludedAt() { return excludedAt; }
    public void setExcludedAt(LocalDateTime excludedAt) { this.excludedAt = excludedAt; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getStudentFullName() { return studentFullName; }
    public void setStudentFullName(String studentFullName) { this.studentFullName = studentFullName; }

    public String getStudentPhotoUrl() { return studentPhotoUrl; }
    public void setStudentPhotoUrl(String studentPhotoUrl) { this.studentPhotoUrl = studentPhotoUrl; }
}
