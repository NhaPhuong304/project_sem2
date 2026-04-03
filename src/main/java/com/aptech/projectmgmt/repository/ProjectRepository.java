package com.aptech.projectmgmt.repository;

import com.aptech.projectmgmt.model.MemberRole;
import com.aptech.projectmgmt.model.Project;
import com.aptech.projectmgmt.model.ProjectStatus;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectRepository extends BaseRepository {

    private static final String BASE_SELECT = "SELECT p.ProjectID, p.Title AS ProjectName, p.[Description] AS Description, p.Semester, " +
            "pg.ClassID, p.GroupID, p.AdvisorID AS SupervisorID, p.StartDate, p.EndDate, p.ReportDate, p.[Status] AS Status, " +
            "st.FullName AS SupervisorName " +
            "FROM Project p " +
            "INNER JOIN ProjectGroup pg ON pg.GroupID = p.GroupID " +
            "LEFT JOIN Staff st ON st.StaffID = p.AdvisorID ";

    public List<Project> findByClassId(int classId) {
        String sql = BASE_SELECT +
                     "WHERE pg.ClassID = ? ORDER BY p.CreatedAt DESC, p.ProjectID DESC";
        try {
            return executeQuery(sql, rs -> {
                List<Project> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }, classId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByClassId: " + e.getMessage(), e);
        }
    }

    public List<Project> findAll() {
        String sql = BASE_SELECT + "ORDER BY p.CreatedAt DESC, p.ProjectID DESC";
        try {
            return executeQuery(sql, rs -> {
                List<Project> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            });
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findAll: " + e.getMessage(), e);
        }
    }

    public List<Project> findByAdvisorId(int advisorId) {
        String sql = BASE_SELECT +
                     "WHERE p.AdvisorID = ? ORDER BY p.CreatedAt DESC, p.ProjectID DESC";
        try {
            return executeQuery(sql, rs -> {
                List<Project> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }, advisorId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByAdvisorId: " + e.getMessage(), e);
        }
    }

    public List<Project> findByStudentId(int studentId) {
        String sql = "SELECT p.ProjectID, p.Title AS ProjectName, p.[Description] AS Description, p.Semester, pg.ClassID, " +
                     "p.GroupID, p.AdvisorID AS SupervisorID, p.StartDate, p.EndDate, p.ReportDate, p.[Status] AS Status, " +
                     "st.FullName AS SupervisorName, gm.Role AS MyRole " +
                     "FROM GroupMember gm " +
                     "INNER JOIN ProjectGroup pg ON pg.GroupID = gm.GroupID " +
                     "INNER JOIN Project p ON p.GroupID = pg.GroupID " +
                     "LEFT JOIN Staff st ON st.StaffID = p.AdvisorID " +
                     "WHERE gm.StudentID = ? AND gm.Status = 1 " +
                     "ORDER BY p.StartDate DESC";
        try {
            return executeQuery(sql, rs -> {
                List<Project> list = new ArrayList<>();
                while (rs.next()) {
                    Project p = mapRow(rs);
                    int myRoleVal = rs.getInt("MyRole");
                    if (!rs.wasNull()) p.setMyRole(MemberRole.fromValue(myRoleVal));
                    list.add(p);
                }
                return list;
            }, studentId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByStudentId: " + e.getMessage(), e);
        }
    }

    public Project findById(int projectId) {
        String sql = BASE_SELECT + "WHERE p.ProjectID = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) return mapRow(rs);
                return null;
            }, projectId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findById: " + e.getMessage(), e);
        }
    }

    public Project findByIdAndAdvisorId(int projectId, int advisorId) {
        String sql = BASE_SELECT + "WHERE p.ProjectID = ? AND p.AdvisorID = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) return mapRow(rs);
                return null;
            }, projectId, advisorId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByIdAndAdvisorId: " + e.getMessage(), e);
        }
    }

    public int create(Project project) {
        String sql = "INSERT INTO Project (GroupID, Title, [Description], Semester, StartDate, EndDate, ReportDate, " +
                     "AdvisorID, CreatedBy, [Status]) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            return executeUpdateGetKey(sql,
                    project.getGroupId(),
                    project.getProjectName(),
                    project.getDescription(),
                    project.getSemester(),
                    project.getStartDate() != null ? Date.valueOf(project.getStartDate()) : null,
                    project.getEndDate() != null ? Date.valueOf(project.getEndDate()) : null,
                    project.getReportDate() != null ? Date.valueOf(project.getReportDate()) : null,
                    project.getSupervisorId(),
                    project.getCreatedByStaffId() != null ? project.getCreatedByStaffId() : project.getSupervisorId(),
                    project.getStatus() != null ? project.getStatus().getValue() : ProjectStatus.ACTIVE.getValue());
        } catch (SQLException e) {
            throw new RuntimeException("DB error in create project: " + e.getMessage(), e);
        }
    }

    public void update(Project project) {
        String sql = "UPDATE Project SET Title=?, [Description]=?, Semester=?, AdvisorID=?, " +
                     "StartDate=?, EndDate=?, ReportDate=?, [Status]=? WHERE ProjectID=?";
        try {
            executeUpdate(sql,
                    project.getProjectName(),
                    project.getDescription(),
                    project.getSemester(),
                    project.getSupervisorId(),
                    project.getStartDate() != null ? Date.valueOf(project.getStartDate()) : null,
                    project.getEndDate() != null ? Date.valueOf(project.getEndDate()) : null,
                    project.getReportDate() != null ? Date.valueOf(project.getReportDate()) : null,
                    project.getStatus() != null ? project.getStatus().getValue() : ProjectStatus.ACTIVE.getValue(),
                    project.getProjectId());
        } catch (SQLException e) {
            throw new RuntimeException("DB error in update project: " + e.getMessage(), e);
        }
    }

    public void markCompleted(int projectId) {
        String sql = "UPDATE Project SET [Status] = ? WHERE ProjectID = ?";
        try {
            executeUpdate(sql, ProjectStatus.COMPLETED.getValue(), projectId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in markCompleted: " + e.getMessage(), e);
        }
    }

    private Project mapRow(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setProjectId(rs.getInt("ProjectID"));
        p.setProjectName(rs.getString("ProjectName"));
        p.setDescription(rs.getString("Description"));
        p.setSemester(rs.getString("Semester"));
        p.setClassId(rs.getInt("ClassID"));
        p.setGroupId(rs.getInt("GroupID"));
        int supId = rs.getInt("SupervisorID");
        if (!rs.wasNull()) p.setSupervisorId(supId);
        Date sd = rs.getDate("StartDate");
        if (sd != null) p.setStartDate(sd.toLocalDate());
        Date ed = rs.getDate("EndDate");
        if (ed != null) p.setEndDate(ed.toLocalDate());
        Date rd = rs.getDate("ReportDate");
        if (rd != null) p.setReportDate(rd.toLocalDate());
        p.setStatus(ProjectStatus.fromValue(rs.getInt("Status")));
        p.setSupervisorName(rs.getString("SupervisorName"));
        return p;
    }
}
