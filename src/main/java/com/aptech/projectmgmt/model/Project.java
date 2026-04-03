package com.aptech.projectmgmt.model;

import java.time.LocalDate;

public class Project {

    private int projectId;
    private String projectName;
    private String description;
    private String semester;
    // Derived from ProjectGroup -> Class for filtering/display compatibility.
    private int classId;
    private Integer supervisorId;
    private Integer createdByStaffId;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate reportDate;
    private ProjectStatus status;
    // Non-DB fields (populated via JOIN)
    private String supervisorName;
    private int groupId;
    private MemberRole myRole;

    public Project() {}

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public Integer getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Integer supervisorId) { this.supervisorId = supervisorId; }

    public Integer getCreatedByStaffId() { return createdByStaffId; }
    public void setCreatedByStaffId(Integer createdByStaffId) { this.createdByStaffId = createdByStaffId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }

    public String getSupervisorName() { return supervisorName; }
    public void setSupervisorName(String supervisorName) { this.supervisorName = supervisorName; }

    public int getGroupId() { return groupId; }
    public void setGroupId(int groupId) { this.groupId = groupId; }

    public MemberRole getMyRole() { return myRole; }
    public void setMyRole(MemberRole myRole) { this.myRole = myRole; }
}
