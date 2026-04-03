package com.aptech.projectmgmt.repository;

import com.aptech.projectmgmt.model.Message;
import com.aptech.projectmgmt.util.NotificationUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository extends BaseRepository {

    public List<Message> findByReceiverId(int receiverId) {
        String sql = "SELECT m.MessageID, m.SenderID, m.ReceiverID, m.TaskID, m.Content, m.IsRead, m.SentAt, " +
                     "COALESCE(s.FullName, st.FullName) AS SenderName, " +
                     "t.Title AS TaskTitle " +
                     "FROM Message m " +
                     "LEFT JOIN Student s ON s.StudentID = m.SenderID " +
                     "LEFT JOIN Staff st ON st.StaffID = m.SenderID " +
                     "LEFT JOIN Task t ON t.TaskID = m.TaskID " +
                     "WHERE m.ReceiverID = ? ORDER BY m.SentAt DESC";
        try {
            return executeQuery(sql, rs -> {
                List<Message> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }, receiverId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByReceiverId: " + e.getMessage(), e);
        }
    }

    public int countUnread(int receiverId) {
        String sql = "SELECT COUNT(*) FROM Message WHERE ReceiverID = ? AND IsRead = 0";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) return rs.getInt(1);
                return 0;
            }, receiverId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in countUnread: " + e.getMessage(), e);
        }
    }

    public void markAsRead(int messageId) {
        String sql = "UPDATE Message SET IsRead = 1 WHERE MessageID = ?";
        try {
            executeUpdate(sql, messageId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in markAsRead: " + e.getMessage(), e);
        }
    }

    public void insert(Message message, LocalDateTime sentAt) {
        String sql = "INSERT INTO Message (SenderID, ReceiverID, TaskID, Content, IsRead, SentAt) VALUES (?, ?, ?, ?, 0, ?)";
        try {
            executeUpdate(sql,
                    message.getSenderId(),
                    message.getReceiverId(),
                    message.getTaskId(),
                    message.getContent(),
                    sentAt != null ? Timestamp.valueOf(sentAt) : null);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in insert message: " + e.getMessage(), e);
        }
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setMessageId(rs.getInt("MessageID"));
        m.setSenderId(rs.getInt("SenderID"));
        m.setReceiverId(rs.getInt("ReceiverID"));
        int taskId = rs.getInt("TaskID");
        if (!rs.wasNull()) m.setTaskId(taskId);
        m.setContent(NotificationUtil.stripSystemPrefix(rs.getString("Content")));
        m.setRead(rs.getBoolean("IsRead"));
        Timestamp ts = rs.getTimestamp("SentAt");
        if (ts != null) m.setSentAt(ts.toLocalDateTime());
        m.setSenderName(rs.getString("SenderName"));
        m.setTaskTitle(rs.getString("TaskTitle"));
        return m;
    }
}
