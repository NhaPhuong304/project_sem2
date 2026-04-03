package com.aptech.projectmgmt.repository;

import com.aptech.projectmgmt.config.DatabaseConfig;
import com.aptech.projectmgmt.model.*;
import com.aptech.projectmgmt.util.NotificationUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository extends BaseRepository {

    public List<TaskViewModel> findByGroupId(int groupId) {
        String sql = "SELECT p.ProjectID, p.Title AS ProjectTitle, p.Semester, c.ClassName, pg.GroupID, pg.GroupName, " +
                     "t.TaskID, t.Title, t.[Description] AS TaskDetail, t.EstimatedStartDate, t.EstimatedEndDate, " +
                     "t.ActualStartDate, t.ActualEndDate, t.[Status] AS Status, t.IsLate, " +
                     "assigned.FullName AS AssignedToName, assignedAccount.PhotoUrl AS AssignedToPhoto, " +
                     "reviewed.FullName AS ReviewedByName, created.FullName AS CreatedByName, " +
                     "lr.Note AS LatestRevisionNote, lr.CreatedAt AS LatestRevisionAt, " +
                     "t.AssignedTo AS AssignedToID, t.ReviewedBy AS ReviewedByID, t.CreatedBy AS CreatedByID, " +
                     "gmAssigned.Status AS AssignedMemberStatus, gmReviewed.Status AS ReviewedMemberStatus, " +
                     "CASE WHEN reminder.TaskID IS NULL THEN 0 ELSE 1 END AS HasReminderSent " +
                     "FROM Task t " +
                     "INNER JOIN ProjectGroup pg ON pg.GroupID = t.GroupID " +
                     "INNER JOIN Project p ON p.GroupID = pg.GroupID " +
                     "INNER JOIN Class c ON c.ClassID = pg.ClassID " +
                     "LEFT JOIN Student assigned ON assigned.StudentID = t.AssignedTo " +
                     "LEFT JOIN Account assignedAccount ON assignedAccount.AccountID = assigned.AccountID " +
                     "LEFT JOIN Student reviewed ON reviewed.StudentID = t.ReviewedBy " +
                     "LEFT JOIN Student created ON created.StudentID = t.CreatedBy " +
                     "LEFT JOIN GroupMember gmAssigned ON gmAssigned.GroupID = pg.GroupID AND gmAssigned.StudentID = t.AssignedTo " +
                     "LEFT JOIN GroupMember gmReviewed ON gmReviewed.GroupID = pg.GroupID AND gmReviewed.StudentID = t.ReviewedBy " +
                     "LEFT JOIN TaskRevision lr ON lr.RevisionID = ( " +
                     "    SELECT TOP 1 tr.RevisionID FROM TaskRevision tr WHERE tr.TaskID = t.TaskID ORDER BY tr.CreatedAt DESC, tr.RevisionID DESC" +
                     ") " +
                     "LEFT JOIN (SELECT TaskID, ReceiverID, MAX(SentAt) AS LastReminderSentAt " +
                     "           FROM Message " +
                     "           WHERE Content LIKE ? " +
                     "           GROUP BY TaskID, ReceiverID) reminder " +
                     "       ON reminder.TaskID = t.TaskID AND reminder.ReceiverID = t.AssignedTo " +
                     "WHERE pg.GroupID = ? " +
                     "ORDER BY t.EstimatedStartDate ASC";
        try {
            return executeQuery(sql, rs -> {
                List<TaskViewModel> list = new ArrayList<>();
                while (rs.next()) list.add(mapViewModelRow(rs));
                return list;
            }, NotificationUtil.REMINDER_PREFIX + "%", groupId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByGroupId tasks: " + e.getMessage(), e);
        }
    }

    public Task findById(int taskId) {
        String sql = "SELECT TaskID, GroupID, Title, Description, Status, AssignedTo, ReviewedBy, CreatedBy, " +
                     "EstimatedStartDate, EstimatedEndDate, ActualStartDate, ActualEndDate, IsLate " +
                     "FROM Task WHERE TaskID = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) return mapTaskRow(rs);
                return null;
            }, taskId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findById task: " + e.getMessage(), e);
        }
    }

    public int create(Task task) {
        String sql = "INSERT INTO Task (GroupID, Title, Description, Status, AssignedTo, ReviewedBy, CreatedBy, " +
                     "EstimatedStartDate, EstimatedEndDate, IsLate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
        try {
            return executeUpdateGetKey(sql,
                    task.getGroupId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus().getValue(),
                    task.getAssignedTo(),
                    task.getReviewedBy(),
                    task.getCreatedBy(),
                    task.getEstimatedStartDate() != null ? Timestamp.valueOf(task.getEstimatedStartDate()) : null,
                    task.getEstimatedEndDate() != null ? Timestamp.valueOf(task.getEstimatedEndDate()) : null);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in create task: " + e.getMessage(), e);
        }
    }

    public void updateStatus(int taskId, int newStatus) {
        String sql = "UPDATE Task SET Status = ? WHERE TaskID = ?";
        try {
            executeUpdate(sql, newStatus, taskId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in updateStatus: " + e.getMessage(), e);
        }
    }

    public void setActualStartDate(int taskId, LocalDateTime dateTime) {
        String sql = "UPDATE Task SET ActualStartDate = ? WHERE TaskID = ?";
        try {
            executeUpdate(sql, Timestamp.valueOf(dateTime), taskId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in setActualStartDate: " + e.getMessage(), e);
        }
    }

    public void setActualEndDate(int taskId, LocalDateTime dateTime) {
        String sql = "UPDATE Task SET ActualEndDate = ?, IsLate = CASE WHEN ? > EstimatedEndDate THEN 1 ELSE 0 END WHERE TaskID = ?";
        try {
            Timestamp ts = Timestamp.valueOf(dateTime);
            executeUpdate(sql, ts, ts, taskId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in setActualEndDate: " + e.getMessage(), e);
        }
    }

    public void insertStatusHistory(int taskId, int fromStatus, int toStatus, int changedBy, LocalDateTime changedAt) {
        String sql = "INSERT INTO TaskStatusHistory (TaskID, FromStatus, ToStatus, ChangedBy, ChangedAt) VALUES (?, ?, ?, ?, ?)";
        try {
            executeUpdate(sql, taskId, fromStatus, toStatus, changedBy,
                    changedAt != null ? Timestamp.valueOf(changedAt) : null);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in insertStatusHistory: " + e.getMessage(), e);
        }
    }

    public void insertRevision(int taskId, String note, int reviewedBy, LocalDateTime createdAt) {
        String sql = "INSERT INTO TaskRevision (TaskID, Note, ReviewedBy, CreatedAt) VALUES (?, ?, ?, ?)";
        try {
            executeUpdate(sql, taskId, note, reviewedBy,
                    createdAt != null ? Timestamp.valueOf(createdAt) : null);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in insertRevision: " + e.getMessage(), e);
        }
    }

    public List<TaskStatusHistory> getStatusHistory(int taskId) {
        String sql = "SELECT tsh.HistoryID, tsh.TaskID, tsh.FromStatus, tsh.ToStatus, tsh.ChangedBy, tsh.ChangedAt, " +
                     "COALESCE(s.FullName, st.FullName, a.Username) AS ChangerName " +
                     "FROM TaskStatusHistory tsh " +
                     "INNER JOIN Account a ON a.AccountID = tsh.ChangedBy " +
                     "LEFT JOIN Student s ON s.AccountID = a.AccountID " +
                     "LEFT JOIN Staff st ON st.AccountID = a.AccountID " +
                     "WHERE tsh.TaskID = ? ORDER BY tsh.ChangedAt ASC";
        try {
            return executeQuery(sql, rs -> {
                List<TaskStatusHistory> list = new ArrayList<>();
                while (rs.next()) {
                    TaskStatusHistory h = new TaskStatusHistory();
                    h.setHistoryId(rs.getInt("HistoryID"));
                    h.setTaskId(rs.getInt("TaskID"));
                    int from = rs.getInt("FromStatus");
                    if (!rs.wasNull()) h.setFromStatus(TaskStatus.fromValue(from));
                    h.setToStatus(TaskStatus.fromValue(rs.getInt("ToStatus")));
                    h.setChangedBy(rs.getInt("ChangedBy"));
                    Timestamp ts = rs.getTimestamp("ChangedAt");
                    if (ts != null) h.setChangedAt(ts.toLocalDateTime());
                    h.setChangerName(rs.getString("ChangerName"));
                    list.add(h);
                }
                return list;
            }, taskId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in getStatusHistory: " + e.getMessage(), e);
        }
    }

    public List<TaskRevision> getRevisions(int taskId) {
        String sql = "SELECT RevisionID, TaskID, Note, ReviewedBy, CreatedAt FROM TaskRevision WHERE TaskID = ? ORDER BY CreatedAt ASC";
        try {
            return executeQuery(sql, rs -> {
                List<TaskRevision> list = new ArrayList<>();
                while (rs.next()) {
                    TaskRevision r = new TaskRevision();
                    r.setRevisionId(rs.getInt("RevisionID"));
                    r.setTaskId(rs.getInt("TaskID"));
                    r.setNote(rs.getString("Note"));
                    r.setReviewedBy(rs.getInt("ReviewedBy"));
                    Timestamp ts = rs.getTimestamp("CreatedAt");
                    if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
                    list.add(r);
                }
                return list;
            }, taskId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in getRevisions: " + e.getMessage(), e);
        }
    }

    public List<TaskAbandonLog> getAbandonLogs(int taskId) {
        String sql = "SELECT tal.LogID, tal.TaskID, tal.StudentID, tal.AbandonedAt, s.FullName AS StudentName " +
                     "FROM TaskAbandonLog tal " +
                     "INNER JOIN Student s ON s.StudentID = tal.StudentID " +
                     "WHERE tal.TaskID = ? ORDER BY tal.AbandonedAt ASC";
        try {
            return executeQuery(sql, rs -> {
                List<TaskAbandonLog> list = new ArrayList<>();
                while (rs.next()) {
                    TaskAbandonLog log = new TaskAbandonLog();
                    log.setLogId(rs.getInt("LogID"));
                    log.setTaskId(rs.getInt("TaskID"));
                    log.setStudentId(rs.getInt("StudentID"));
                    Timestamp ts = rs.getTimestamp("AbandonedAt");
                    if (ts != null) log.setAbandonedAt(ts.toLocalDateTime());
                    log.setStudentName(rs.getString("StudentName"));
                    list.add(log);
                }
                return list;
            }, taskId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in getAbandonLogs: " + e.getMessage(), e);
        }
    }

    public void callResetOverdueTasks() {
        try {
            executeStoredProc("sp_ResetOverdueTasks");
        } catch (Exception e) {
            throw new RuntimeException("DB error in resetOverdueTasks: " + e.getMessage(), e);
        }
    }

    public int resetOverdueTasks(LocalDateTime currentTime) {
        String selectSql = "SELECT TaskID, GroupID, AssignedTo " +
                           "FROM Task " +
                           "WHERE [Status] = 1 AND AssignedTo IS NOT NULL AND EstimatedStartDate <= ?";
        String insertLogSql = "INSERT INTO TaskAbandonLog (TaskID, StudentID, AbandonedAt, Note) VALUES (?, ?, ?, ?)";
        String updateTaskSql = "UPDATE Task SET AssignedTo = NULL WHERE TaskID = ?";
        String normalizeExcludeSql = "UPDATE GroupMember SET ExcludedAt = ? WHERE GroupID = ? AND StudentID = ? AND [Status] = 2";

        LocalDateTime threshold = currentTime.minusHours(1);
        List<int[]> affectedMembers = new ArrayList<>();
        int resetCount = 0;

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement selectPs = conn.prepareStatement(selectSql);
                 PreparedStatement insertLogPs = conn.prepareStatement(insertLogSql);
                 PreparedStatement updateTaskPs = conn.prepareStatement(updateTaskSql);
                 PreparedStatement normalizeExcludePs = conn.prepareStatement(normalizeExcludeSql)) {
                selectPs.setTimestamp(1, Timestamp.valueOf(threshold));
                try (ResultSet rs = selectPs.executeQuery()) {
                    while (rs.next()) {
                        int taskId = rs.getInt("TaskID");
                        int groupId = rs.getInt("GroupID");
                        int studentId = rs.getInt("AssignedTo");

                        insertLogPs.setInt(1, taskId);
                        insertLogPs.setInt(2, studentId);
                        insertLogPs.setTimestamp(3, Timestamp.valueOf(currentTime));
                        insertLogPs.setString(4, "Tu dong: qua 1 gio khong xac nhan thuc hien");
                        insertLogPs.addBatch();

                        updateTaskPs.setInt(1, taskId);
                        updateTaskPs.addBatch();

                        affectedMembers.add(new int[]{groupId, studentId});
                        resetCount++;
                    }
                }

                if (resetCount > 0) {
                    insertLogPs.executeBatch();
                    updateTaskPs.executeBatch();
                    for (int[] affectedMember : affectedMembers) {
                        normalizeExcludePs.setTimestamp(1, Timestamp.valueOf(currentTime));
                        normalizeExcludePs.setInt(2, affectedMember[0]);
                        normalizeExcludePs.setInt(3, affectedMember[1]);
                        normalizeExcludePs.addBatch();
                    }
                    normalizeExcludePs.executeBatch();
                }

                conn.commit();
                return resetCount;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new RuntimeException("DB error in resetOverdueTasks: " + e.getMessage(), e);
        }
    }

    private TaskViewModel mapViewModelRow(ResultSet rs) throws SQLException {
        TaskViewModel t = new TaskViewModel();
        t.setTaskId(rs.getInt("TaskID"));
        t.setGroupId(rs.getInt("GroupID"));
        t.setTitle(rs.getString("Title"));
        Timestamp es = rs.getTimestamp("EstimatedStartDate");
        if (es != null) t.setEstimatedStartDate(es.toLocalDateTime());
        Timestamp ee = rs.getTimestamp("EstimatedEndDate");
        if (ee != null) t.setEstimatedEndDate(ee.toLocalDateTime());
        Timestamp as = rs.getTimestamp("ActualStartDate");
        if (as != null) t.setActualStartDate(as.toLocalDateTime());
        Timestamp ae = rs.getTimestamp("ActualEndDate");
        if (ae != null) t.setActualEndDate(ae.toLocalDateTime());
        t.setStatus(TaskStatus.fromValue(rs.getInt("Status")));
        int assignedToId = rs.getInt("AssignedToID");
        if (!rs.wasNull()) t.setAssignedToId(assignedToId);
        t.setAssignedToName(rs.getString("AssignedToName"));
        t.setAssignedToPhoto(rs.getString("AssignedToPhoto"));
        int assignedStatus = rs.getInt("AssignedMemberStatus");
        if (!rs.wasNull()) t.setAssignedMemberStatus(MemberStatus.fromValue(assignedStatus));
        int reviewedById = rs.getInt("ReviewedByID");
        if (!rs.wasNull()) t.setReviewedById(reviewedById);
        t.setReviewedByName(rs.getString("ReviewedByName"));
        int reviewedStatus = rs.getInt("ReviewedMemberStatus");
        if (!rs.wasNull()) t.setReviewedMemberStatus(MemberStatus.fromValue(reviewedStatus));
        int createdById = rs.getInt("CreatedByID");
        if (!rs.wasNull()) t.setCreatedById(createdById);
        t.setCreatedByName(rs.getString("CreatedByName"));
        t.setLate(rs.getBoolean("IsLate"));
        t.setLatestRevisionNote(rs.getString("LatestRevisionNote"));
        t.setReminderSent(rs.getInt("HasReminderSent") == 1);
        return t;
    }

    private Task mapTaskRow(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setTaskId(rs.getInt("TaskID"));
        t.setGroupId(rs.getInt("GroupID"));
        t.setTitle(rs.getString("Title"));
        t.setDescription(rs.getString("Description"));
        t.setStatus(TaskStatus.fromValue(rs.getInt("Status")));
        int assignedTo = rs.getInt("AssignedTo");
        if (!rs.wasNull()) t.setAssignedTo(assignedTo);
        int reviewedBy = rs.getInt("ReviewedBy");
        if (!rs.wasNull()) t.setReviewedBy(reviewedBy);
        int createdBy = rs.getInt("CreatedBy");
        if (!rs.wasNull()) t.setCreatedBy(createdBy);
        Timestamp es = rs.getTimestamp("EstimatedStartDate");
        if (es != null) t.setEstimatedStartDate(es.toLocalDateTime());
        Timestamp ee = rs.getTimestamp("EstimatedEndDate");
        if (ee != null) t.setEstimatedEndDate(ee.toLocalDateTime());
        Timestamp as = rs.getTimestamp("ActualStartDate");
        if (as != null) t.setActualStartDate(as.toLocalDateTime());
        Timestamp ae = rs.getTimestamp("ActualEndDate");
        if (ae != null) t.setActualEndDate(ae.toLocalDateTime());
        t.setLate(rs.getBoolean("IsLate"));
        return t;
    }

}
