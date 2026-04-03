package com.aptech.projectmgmt.repository;

import com.aptech.projectmgmt.model.GroupMember;
import com.aptech.projectmgmt.model.MemberRole;
import com.aptech.projectmgmt.model.MemberStatus;
import com.aptech.projectmgmt.model.ProjectGroup;
import com.aptech.projectmgmt.model.ProjectStatus;
import com.aptech.projectmgmt.model.Student;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GroupRepository extends BaseRepository {

    public List<ProjectGroup> findByProjectId(int projectId) {
        String sql = "SELECT pg.GroupID, pg.ClassID, pg.GroupName, p.ProjectID " +
                     "FROM Project p " +
                     "INNER JOIN ProjectGroup pg ON pg.GroupID = p.GroupID " +
                     "WHERE p.ProjectID = ? " +
                     "ORDER BY pg.GroupName";
        try {
            return executeQuery(sql, rs -> {
                List<ProjectGroup> list = new ArrayList<>();
                while (rs.next()) {
                    ProjectGroup g = new ProjectGroup();
                    g.setGroupId(rs.getInt("GroupID"));
                    g.setClassId(rs.getInt("ClassID"));
                    g.setProjectId(rs.getInt("ProjectID"));
                    g.setGroupName(rs.getString("GroupName"));
                    list.add(g);
                }
                return list;
            }, projectId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByProjectId: " + e.getMessage(), e);
        }
    }

    public ProjectGroup findById(int groupId) {
        String sql = "SELECT pg.GroupID, pg.ClassID, pg.GroupName, p.ProjectID " +
                     "FROM ProjectGroup pg " +
                     "LEFT JOIN Project p ON p.GroupID = pg.GroupID " +
                     "WHERE pg.GroupID = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) {
                    ProjectGroup group = new ProjectGroup();
                    group.setGroupId(rs.getInt("GroupID"));
                    group.setClassId(rs.getInt("ClassID"));
                    int projectId = rs.getInt("ProjectID");
                    if (!rs.wasNull()) {
                        group.setProjectId(projectId);
                    }
                    group.setGroupName(rs.getString("GroupName"));
                    return group;
                }
                return null;
            }, groupId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findById: " + e.getMessage(), e);
        }
    }

    public int createStandaloneGroup(int classId, String groupName) {
        String sql = "INSERT INTO ProjectGroup (ClassID, GroupName) VALUES (?, ?)";
        try {
            return executeUpdateGetKey(sql, classId, groupName);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in createStandaloneGroup: " + e.getMessage(), e);
        }
    }

    public boolean existsGroupName(int classId, String groupName) {
        String sql = "SELECT COUNT(*) AS Cnt FROM ProjectGroup WHERE ClassID = ? AND GroupName = ?";
        try {
            Integer count = executeQuery(sql, rs -> rs.next() ? rs.getInt("Cnt") : 0, classId, groupName);
            return count != null && count > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB error in existsGroupName: " + e.getMessage(), e);
        }
    }

    public boolean existsGroupNameExcluding(int classId, int groupId, String groupName) {
        String sql = "SELECT COUNT(*) AS Cnt FROM ProjectGroup WHERE ClassID = ? AND GroupID <> ? AND GroupName = ?";
        try {
            Integer count = executeQuery(sql, rs -> rs.next() ? rs.getInt("Cnt") : 0, classId, groupId, groupName);
            return count != null && count > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB error in existsGroupNameExcluding: " + e.getMessage(), e);
        }
    }

    public void updateGroupName(int groupId, String groupName) {
        String sql = "UPDATE ProjectGroup SET GroupName = ? WHERE GroupID = ?";
        try {
            executeUpdate(sql, groupName, groupId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in updateGroupName: " + e.getMessage(), e);
        }
    }

    public boolean existsStudentInProject(int projectId, int studentId) {
        String sql = "SELECT COUNT(*) AS Cnt " +
                     "FROM GroupMember gm " +
                     "INNER JOIN ProjectGroup pg ON pg.GroupID = gm.GroupID " +
                     "INNER JOIN Project p ON p.GroupID = pg.GroupID " +
                     "WHERE p.ProjectID = ? AND gm.StudentID = ? AND gm.Status = ? AND p.Status = ?";
        try {
            Integer count = executeQuery(sql, rs -> rs.next() ? rs.getInt("Cnt") : 0,
                    projectId, studentId, MemberStatus.ACTIVE.getValue(), ProjectStatus.ACTIVE.getValue());
            return count != null && count > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB error in existsStudentInProject: " + e.getMessage(), e);
        }
    }

    public boolean hasActiveLeader(int groupId) {
        String sql = "SELECT COUNT(*) AS Cnt FROM GroupMember WHERE GroupID = ? AND Role = ? AND Status = ?";
        try {
            Integer count = executeQuery(sql, rs -> rs.next() ? rs.getInt("Cnt") : 0,
                    groupId, MemberRole.LEADER.getValue(), MemberStatus.ACTIVE.getValue());
            return count != null && count > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB error in hasActiveLeader: " + e.getMessage(), e);
        }
    }

    public void addMember(int groupId, int studentId, MemberRole role) {
        String sql = "INSERT INTO GroupMember (GroupID, StudentID, [Role], [Status]) VALUES (?, ?, ?, ?)";
        try {
            executeUpdate(sql, groupId, studentId, role.getValue(), MemberStatus.ACTIVE.getValue());
        } catch (SQLException e) {
            throw new RuntimeException("DB error in addMember: " + e.getMessage(), e);
        }
    }

    public List<GroupMember> findMembersByGroupId(int groupId) {
        String sql = "SELECT gm.MemberID, gm.GroupID, gm.StudentID, gm.Role, gm.Status, " +
                     "gm.AbandonCount, gm.ExcludedBy, gm.ExcludedAt, " +
                     "s.StudentCode, s.FullName AS StudentFullName, a.PhotoUrl AS StudentPhotoUrl " +
                     "FROM GroupMember gm " +
                     "INNER JOIN Student s ON s.StudentID = gm.StudentID " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
                     "WHERE gm.GroupID = ? " +
                     "ORDER BY gm.Role ASC, s.FullName ASC";
        try {
            return executeQuery(sql, rs -> {
                List<GroupMember> list = new ArrayList<>();
                while (rs.next()) list.add(mapMemberRow(rs));
                return list;
            }, groupId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findMembersByGroupId: " + e.getMessage(), e);
        }
    }

    public void excludeMember(int memberId, int staffId, String reason) {
        String sql = "UPDATE GroupMember SET Status = 2, ExcludedBy = ?, ExcludedAt = GETDATE(), ExcludedReason = ? WHERE MemberID = ?";
        try {
            executeUpdate(sql, staffId, reason, memberId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in excludeMember: " + e.getMessage(), e);
        }
    }

    public List<Student> findAvailableStudentsForClass(int classId) {
        String sql = "SELECT s.StudentID, s.StudentCode, s.FullName, s.Email, s.ClassID, s.AccountID, a.PhotoUrl " +
                     "FROM Student s " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
                     "WHERE s.ClassID = ? " +
                     "AND NOT EXISTS ( " +
                     "    SELECT 1 " +
                     "    FROM GroupMember gm " +
                     "    INNER JOIN ProjectGroup pg ON pg.GroupID = gm.GroupID " +
                     "    INNER JOIN Project p ON p.GroupID = pg.GroupID " +
                     "    WHERE pg.ClassID = ? AND gm.StudentID = s.StudentID AND gm.Status = ? AND p.Status = ?" +
                     ") " +
                     "ORDER BY s.FullName ASC";
        try {
            return executeQuery(sql, rs -> {
                List<Student> list = new ArrayList<>();
                while (rs.next()) {
                    Student student = new Student();
                    student.setStudentId(rs.getInt("StudentID"));
                    student.setStudentCode(rs.getString("StudentCode"));
                    student.setFullName(rs.getString("FullName"));
                    student.setEmail(rs.getString("Email"));
                    student.setClassId(rs.getInt("ClassID"));
                    int accountId = rs.getInt("AccountID");
                    if (!rs.wasNull()) {
                        student.setAccountId(accountId);
                    }
                    student.setPhotoUrl(rs.getString("PhotoUrl"));
                    list.add(student);
                }
                return list;
            }, classId, classId, MemberStatus.ACTIVE.getValue(), ProjectStatus.ACTIVE.getValue());
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findAvailableStudentsForClass: " + e.getMessage(), e);
        }
    }

    public GroupMember findMemberByStudentAndGroup(int studentId, int groupId) {
        String sql = "SELECT gm.MemberID, gm.GroupID, gm.StudentID, gm.Role, gm.Status, " +
                     "gm.AbandonCount, gm.ExcludedBy, gm.ExcludedAt, " +
                     "s.StudentCode, s.FullName AS StudentFullName, a.PhotoUrl AS StudentPhotoUrl " +
                     "FROM GroupMember gm " +
                     "INNER JOIN Student s ON s.StudentID = gm.StudentID " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
                     "WHERE gm.StudentID = ? AND gm.GroupID = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) return mapMemberRow(rs);
                return null;
            }, studentId, groupId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findMemberByStudentAndGroup: " + e.getMessage(), e);
        }
    }

    public GroupMember findMemberById(int memberId) {
        String sql = "SELECT gm.MemberID, gm.GroupID, gm.StudentID, gm.Role, gm.Status, " +
                     "gm.AbandonCount, gm.ExcludedBy, gm.ExcludedAt, " +
                     "s.StudentCode, s.FullName AS StudentFullName, a.PhotoUrl AS StudentPhotoUrl " +
                     "FROM GroupMember gm " +
                     "INNER JOIN Student s ON s.StudentID = gm.StudentID " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
                     "WHERE gm.MemberID = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) {
                    return mapMemberRow(rs);
                }
                return null;
            }, memberId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findMemberById: " + e.getMessage(), e);
        }
    }

    public int countActiveMembers(int groupId) {
        String sql = "SELECT COUNT(*) AS Cnt FROM GroupMember WHERE GroupID = ? AND Status = ?";
        try {
            Integer count = executeQuery(sql, rs -> rs.next() ? rs.getInt("Cnt") : 0,
                    groupId, MemberStatus.ACTIVE.getValue());
            return count != null ? count : 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB error in countActiveMembers: " + e.getMessage(), e);
        }
    }

    private GroupMember mapMemberRow(ResultSet rs) throws SQLException {
        GroupMember m = new GroupMember();
        m.setMemberId(rs.getInt("MemberID"));
        m.setGroupId(rs.getInt("GroupID"));
        m.setStudentId(rs.getInt("StudentID"));
        m.setRole(MemberRole.fromValue(rs.getInt("Role")));
        m.setStatus(MemberStatus.fromValue(rs.getInt("Status")));
        m.setAbandonCount(rs.getInt("AbandonCount"));
        int excludedBy = rs.getInt("ExcludedBy");
        if (!rs.wasNull()) m.setExcludedBy(excludedBy);
        Timestamp excludedAt = rs.getTimestamp("ExcludedAt");
        if (excludedAt != null) m.setExcludedAt(excludedAt.toLocalDateTime());
        m.setStudentCode(rs.getString("StudentCode"));
        m.setStudentFullName(rs.getString("StudentFullName"));
        m.setStudentPhotoUrl(rs.getString("StudentPhotoUrl"));
        return m;
    }
}
