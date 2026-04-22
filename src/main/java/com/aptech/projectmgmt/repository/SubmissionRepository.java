package com.aptech.projectmgmt.repository;

import com.aptech.projectmgmt.config.DatabaseConfig;
import com.aptech.projectmgmt.model.SubmissionFile;
import com.aptech.projectmgmt.model.SubmissionRecipientOption;
import com.aptech.projectmgmt.model.SubmissionRequirement;
import com.aptech.projectmgmt.model.SubmissionRequirementTemplate;
import com.aptech.projectmgmt.model.SubmissionRequest;
import com.aptech.projectmgmt.model.SubmissionTarget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubmissionRepository extends BaseRepository {

    public List<SubmissionRequirementTemplate> findActiveTemplates() {
        String sql = "SELECT TemplateID, RequirementName, RequiredExtension, SortOrder, IsActive "
                + "FROM SubmissionRequirementTemplate WHERE IsActive = 1 ORDER BY SortOrder";
        try {
            return executeQuery(sql, rs -> {
                List<SubmissionRequirementTemplate> list = new ArrayList<>();
                while (rs.next()) {
                    SubmissionRequirementTemplate item = new SubmissionRequirementTemplate();
                    item.setTemplateId(rs.getInt("TemplateID"));
                    item.setRequirementName(rs.getString("RequirementName"));
                    item.setRequiredExtension(rs.getString("RequiredExtension"));
                    item.setSortOrder(rs.getInt("SortOrder"));
                    item.setActive(rs.getBoolean("IsActive"));
                    list.add(item);
                }
                return list;
            });
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findActiveTemplates: " + e.getMessage(), e);
        }
    }

    public List<SubmissionRecipientOption> findRecipientOptionsForStaff(int staffId) {
        String sql = "SELECT pg.GroupID, pg.GroupName, c.ClassName, p.ProjectID, p.Title AS ProjectTitle, "
                + "s.StudentID AS LeaderStudentID, s.FullName AS LeaderName, s.StudentCode AS LeaderStudentCode "
                + "FROM ProjectGroup pg "
                + "INNER JOIN Class c ON c.ClassID = pg.ClassID "
                + "INNER JOIN GroupMember gm ON gm.GroupID = pg.GroupID AND gm.[Role] = 1 AND gm.[Status] = 1 "
                + "INNER JOIN Student s ON s.StudentID = gm.StudentID "
                + "LEFT JOIN Project p ON p.GroupID = pg.GroupID "
                + "WHERE c.ManagerID = ? "
                + "ORDER BY c.ClassName, pg.GroupName";
        try {
            return executeQuery(sql, rs -> {
                List<SubmissionRecipientOption> list = new ArrayList<>();
                while (rs.next()) {
                    SubmissionRecipientOption item = new SubmissionRecipientOption();
                    item.setGroupId(rs.getInt("GroupID"));
                    item.setGroupName(rs.getString("GroupName"));
                    item.setClassName(rs.getString("ClassName"));
                    int projectId = rs.getInt("ProjectID");
                    if (!rs.wasNull()) {
                        item.setProjectId(projectId);
                    }
                    item.setProjectTitle(rs.getString("ProjectTitle"));
                    item.setLeaderStudentId(rs.getInt("LeaderStudentID"));
                    item.setLeaderName(rs.getString("LeaderName"));
                    item.setLeaderStudentCode(rs.getString("LeaderStudentCode"));
                    list.add(item);
                }
                return list;
            }, staffId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findRecipientOptionsForStaff: " + e.getMessage(), e);
        }
    }

    public int createRequestWithRequirements(SubmissionRequest request,
            List<SubmissionRequirementTemplate> requirements) {
        String requestSql = "INSERT INTO SubmissionRequest "
                + "(Title, Description, Deadline, CreatedByStaffID, Status, CreatedAt) "
                + "VALUES (?, ?, ?, ?, 1, GETDATE())";
        String requirementSql = "INSERT INTO SubmissionRequirement "
                + "(RequestID, RequirementName, RequiredExtension, SortOrder, IsRequired) "
                + "VALUES (?, ?, ?, ?, 1)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement requestPs = conn.prepareStatement(requestSql, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement requirementPs = conn.prepareStatement(requirementSql)) {
                requestPs.setString(1, request.getTitle());
                requestPs.setString(2, request.getDescription());
                requestPs.setTimestamp(3, Timestamp.valueOf(request.getDeadline()));
                requestPs.setInt(4, request.getCreatedByStaffId());
                requestPs.executeUpdate();

                int requestId;
                try (ResultSet keys = requestPs.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Cannot read generated request id");
                    }
                    requestId = keys.getInt(1);
                }

                for (SubmissionRequirementTemplate template : requirements) {
                    requirementPs.setInt(1, requestId);
                    requirementPs.setString(2, template.getRequirementName());
                    requirementPs.setString(3, template.getRequiredExtension());
                    requirementPs.setInt(4, template.getSortOrder());
                    requirementPs.addBatch();
                }
                requirementPs.executeBatch();
                conn.commit();
                return requestId;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("DB error in createRequestWithRequirements: " + e.getMessage(), e);
        }
    }

    public void addTarget(int requestId, int groupId, int leaderStudentId) {
        String sql = "IF NOT EXISTS (SELECT 1 FROM SubmissionTarget WHERE RequestID = ? AND GroupID = ?) "
                + "INSERT INTO SubmissionTarget (RequestID, GroupID, LeaderStudentID, Status, NotifiedAt) "
                + "VALUES (?, ?, ?, 1, GETDATE())";
        try {
            executeUpdate(sql, requestId, groupId, requestId, groupId, leaderStudentId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in addTarget: " + e.getMessage(), e);
        }
    }

    public List<SubmissionRequest> findRequestsForStaff(int staffId) {
        String sql = "SELECT r.RequestID, r.Title, r.Description, r.Deadline, r.CreatedByStaffID, "
                + "r.Status, r.CreatedAt, s.FullName AS CreatedByStaffName "
                + "FROM SubmissionRequest r LEFT JOIN Staff s ON s.StaffID = r.CreatedByStaffID "
                + "WHERE r.CreatedByStaffID = ? ORDER BY r.CreatedAt DESC";
        try {
            return executeQuery(sql, rs -> {
                List<SubmissionRequest> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRequest(rs));
                }
                return list;
            }, staffId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findRequestsForStaff: " + e.getMessage(), e);
        }
    }

    public List<SubmissionTarget> findTargetsForLeader(int leaderStudentId) {
        String sql = "SELECT t.TargetID, t.RequestID, t.GroupID, t.LeaderStudentID, t.Status, "
                + "t.NotifiedAt, t.SubmittedAt, t.CreatedAt, "
                + "pg.GroupName, s.FullName AS LeaderName, r.Title AS RequestTitle, "
                + "r.Description AS RequestDescription, r.Deadline AS RequestDeadline "
                + "FROM SubmissionTarget t "
                + "INNER JOIN SubmissionRequest r ON r.RequestID = t.RequestID "
                + "LEFT JOIN ProjectGroup pg ON pg.GroupID = t.GroupID "
                + "LEFT JOIN Student s ON s.StudentID = t.LeaderStudentID "
                + "WHERE t.LeaderStudentID = ? ORDER BY r.Deadline ASC";
        try {
            return executeQuery(sql, rs -> {
                List<SubmissionTarget> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapTarget(rs));
                }
                return list;
            }, leaderStudentId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findTargetsForLeader: " + e.getMessage(), e);
        }
    }

    public List<SubmissionTarget> findTargetsByRequest(int requestId) {
        String sql = "SELECT t.TargetID, t.RequestID, t.GroupID, t.LeaderStudentID, t.Status, "
                + "t.NotifiedAt, t.SubmittedAt, t.CreatedAt, "
                + "pg.GroupName, s.FullName AS LeaderName, r.Title AS RequestTitle, "
                + "r.Description AS RequestDescription, r.Deadline AS RequestDeadline "
                + "FROM SubmissionTarget t "
                + "INNER JOIN SubmissionRequest r ON r.RequestID = t.RequestID "
                + "LEFT JOIN ProjectGroup pg ON pg.GroupID = t.GroupID "
                + "LEFT JOIN Student s ON s.StudentID = t.LeaderStudentID "
                + "WHERE t.RequestID = ? "
                + "ORDER BY t.Status DESC, t.SubmittedAt DESC, pg.GroupName";
        try {
            return executeQuery(sql, rs -> {
                List<SubmissionTarget> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapTarget(rs));
                }
                return list;
            }, requestId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findTargetsByRequest: " + e.getMessage(), e);
        }
    }

    public List<SubmissionRequirement> findRequirementsByRequest(int requestId) {
        String sql = "SELECT RequirementID, RequestID, RequirementName, RequiredExtension, SortOrder, IsRequired "
                + "FROM SubmissionRequirement WHERE RequestID = ? ORDER BY SortOrder";
        try {
            return executeQuery(sql, rs -> {
                List<SubmissionRequirement> list = new ArrayList<>();
                while (rs.next()) {
                    SubmissionRequirement item = new SubmissionRequirement();
                    item.setRequirementId(rs.getInt("RequirementID"));
                    item.setRequestId(rs.getInt("RequestID"));
                    item.setRequirementName(rs.getString("RequirementName"));
                    item.setRequiredExtension(rs.getString("RequiredExtension"));
                    item.setSortOrder(rs.getInt("SortOrder"));
                    item.setRequired(rs.getBoolean("IsRequired"));
                    list.add(item);
                }
                return list;
            }, requestId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findRequirementsByRequest: " + e.getMessage(), e);
        }
    }

    public void upsertFile(SubmissionFile file) {
        String sql = "IF EXISTS (SELECT 1 FROM SubmissionFile WHERE TargetID = ? AND RequirementID = ?) "
                + "UPDATE SubmissionFile SET OriginalFileName = ?, StoredFileName = ?, FilePath = ?, "
                + "FileSize = ?, UploadedByStudentID = ?, UploadedAt = GETDATE() "
                + "WHERE TargetID = ? AND RequirementID = ? "
                + "ELSE INSERT INTO SubmissionFile "
                + "(TargetID, RequirementID, OriginalFileName, StoredFileName, FilePath, FileSize, UploadedByStudentID) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            executeUpdate(sql,
                    file.getTargetId(), file.getRequirementId(),
                    file.getOriginalFileName(), file.getStoredFileName(), file.getFilePath(),
                    file.getFileSize(), file.getUploadedByStudentId(),
                    file.getTargetId(), file.getRequirementId(),
                    file.getTargetId(), file.getRequirementId(), file.getOriginalFileName(),
                    file.getStoredFileName(), file.getFilePath(), file.getFileSize(), file.getUploadedByStudentId());
        } catch (SQLException e) {
            throw new RuntimeException("DB error in upsertFile: " + e.getMessage(), e);
        }
    }

    public List<SubmissionFile> findFilesByTarget(int targetId) {
        String sql = "SELECT f.FileID, f.TargetID, f.RequirementID, f.OriginalFileName, f.StoredFileName, "
                + "f.FilePath, f.FileSize, f.UploadedByStudentID, f.UploadedAt, "
                + "r.RequirementName, r.RequiredExtension "
                + "FROM SubmissionFile f "
                + "INNER JOIN SubmissionRequirement r ON r.RequirementID = f.RequirementID "
                + "WHERE f.TargetID = ? ORDER BY r.SortOrder";
        try {
            return executeQuery(sql, rs -> {
                List<SubmissionFile> list = new ArrayList<>();
                while (rs.next()) {
                    SubmissionFile item = new SubmissionFile();
                    item.setFileId(rs.getInt("FileID"));
                    item.setTargetId(rs.getInt("TargetID"));
                    item.setRequirementId(rs.getInt("RequirementID"));
                    item.setOriginalFileName(rs.getString("OriginalFileName"));
                    item.setStoredFileName(rs.getString("StoredFileName"));
                    item.setFilePath(rs.getString("FilePath"));
                    item.setFileSize(rs.getLong("FileSize"));
                    item.setUploadedByStudentId(rs.getInt("UploadedByStudentID"));
                    Timestamp uploadedAt = rs.getTimestamp("UploadedAt");
                    if (uploadedAt != null) {
                        item.setUploadedAt(uploadedAt.toLocalDateTime());
                    }
                    item.setRequirementName(rs.getString("RequirementName"));
                    item.setRequiredExtension(rs.getString("RequiredExtension"));
                    list.add(item);
                }
                return list;
            }, targetId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findFilesByTarget: " + e.getMessage(), e);
        }
    }

    public void markTargetSubmitted(int targetId) {
        String sql = "UPDATE SubmissionTarget SET Status = 3, SubmittedAt = GETDATE() WHERE TargetID = ?";
        try {
            executeUpdate(sql, targetId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in markTargetSubmitted: " + e.getMessage(), e);
        }
    }

    public Integer findLeaderStudentIdByGroup(int groupId) {
        String sql = "SELECT TOP 1 StudentID FROM GroupMember WHERE GroupID = ? AND [Role] = 1 AND [Status] = 1";
        try {
            return executeQuery(sql, rs -> rs.next() ? rs.getInt("StudentID") : null, groupId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findLeaderStudentIdByGroup: " + e.getMessage(), e);
        }
    }

    private SubmissionRequest mapRequest(ResultSet rs) throws SQLException {
        SubmissionRequest item = new SubmissionRequest();
        item.setRequestId(rs.getInt("RequestID"));
        item.setTitle(rs.getString("Title"));
        item.setDescription(rs.getString("Description"));
        Timestamp deadline = rs.getTimestamp("Deadline");
        if (deadline != null) {
            item.setDeadline(deadline.toLocalDateTime());
        }
        item.setCreatedByStaffId(rs.getInt("CreatedByStaffID"));
        item.setStatus(rs.getInt("Status"));
        Timestamp createdAt = rs.getTimestamp("CreatedAt");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }
        item.setCreatedByStaffName(rs.getString("CreatedByStaffName"));
        return item;
    }

    private SubmissionTarget mapTarget(ResultSet rs) throws SQLException {
        SubmissionTarget item = new SubmissionTarget();
        item.setTargetId(rs.getInt("TargetID"));
        item.setRequestId(rs.getInt("RequestID"));
        item.setGroupId(rs.getInt("GroupID"));
        item.setLeaderStudentId(rs.getInt("LeaderStudentID"));
        item.setStatus(rs.getInt("Status"));
        Timestamp notifiedAt = rs.getTimestamp("NotifiedAt");
        if (notifiedAt != null) {
            item.setNotifiedAt(notifiedAt.toLocalDateTime());
        }
        Timestamp submittedAt = rs.getTimestamp("SubmittedAt");
        if (submittedAt != null) {
            item.setSubmittedAt(submittedAt.toLocalDateTime());
        }
        Timestamp createdAt = rs.getTimestamp("CreatedAt");
        if (createdAt != null) {
            item.setCreatedAt(createdAt.toLocalDateTime());
        }
        item.setGroupName(rs.getString("GroupName"));
        item.setLeaderName(rs.getString("LeaderName"));
        item.setRequestTitle(rs.getString("RequestTitle"));
        item.setRequestDescription(rs.getString("RequestDescription"));
        Timestamp requestDeadline = rs.getTimestamp("RequestDeadline");
        if (requestDeadline != null) {
            item.setRequestDeadline(requestDeadline.toLocalDateTime());
        }
        return item;
    }
}
