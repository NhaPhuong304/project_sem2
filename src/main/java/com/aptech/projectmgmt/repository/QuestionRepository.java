package com.aptech.projectmgmt.repository;

import com.aptech.projectmgmt.model.Question;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class QuestionRepository extends BaseRepository {

    public int createQuestion(int studentId, int teacherId, Integer taskId, String questionContent) {
        String sql = "INSERT INTO Question (StudentID, TeacherID, TaskID, QuestionContent, CreatedAt, IsAnswered) "
                   + "VALUES (?, ?, ?, ?, GETDATE(), 0)";
        try {
            return executeUpdateGetKey(sql, studentId, teacherId, taskId, questionContent);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in createQuestion: " + e.getMessage(), e);
        }
    }

    public void answerQuestion(int questionId, String answerContent) {
        String sql = "UPDATE Question "
                   + "SET AnswerContent = ?, AnsweredAt = GETDATE(), IsAnswered = 1 "
                   + "WHERE QuestionID = ?";
        try {
            executeUpdate(sql, answerContent, questionId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in answerQuestion: " + e.getMessage(), e);
        }
    }

    public List<Question> findByTeacherId(int teacherId) {
        String sql = "SELECT q.QuestionID, q.StudentID, q.TeacherID, q.TaskID, q.QuestionContent, q.AnswerContent, "
                   + "q.CreatedAt, q.AnsweredAt, q.IsAnswered, "
                   + "s.FullName AS StudentName, "
                   + "st.FullName AS TeacherName, "
                   + "t.Title AS TaskTitle "
                   + "FROM Question q "
                   + "LEFT JOIN Student s ON s.StudentID = q.StudentID "
                   + "LEFT JOIN Staff st ON st.StaffID = q.TeacherID "
                   + "LEFT JOIN Task t ON t.TaskID = q.TaskID "
                   + "WHERE q.TeacherID = ? "
                   + "ORDER BY q.CreatedAt DESC";
        try {
            return executeQuery(sql, rs -> {
                List<Question> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }, teacherId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByTeacherId: " + e.getMessage(), e);
        }
    }

    public List<Question> findByStudentId(int studentId) {
        String sql = "SELECT q.QuestionID, q.StudentID, q.TeacherID, q.TaskID, q.QuestionContent, q.AnswerContent, "
                   + "q.CreatedAt, q.AnsweredAt, q.IsAnswered, "
                   + "s.FullName AS StudentName, "
                   + "st.FullName AS TeacherName, "
                   + "t.Title AS TaskTitle "
                   + "FROM Question q "
                   + "LEFT JOIN Student s ON s.StudentID = q.StudentID "
                   + "LEFT JOIN Staff st ON st.StaffID = q.TeacherID "
                   + "LEFT JOIN Task t ON t.TaskID = q.TaskID "
                   + "WHERE q.StudentID = ? "
                   + "ORDER BY q.CreatedAt DESC";
        try {
            return executeQuery(sql, rs -> {
                List<Question> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }, studentId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByStudentId: " + e.getMessage(), e);
        }
    }

    public Question findById(int questionId) {
        String sql = "SELECT q.QuestionID, q.StudentID, q.TeacherID, q.TaskID, q.QuestionContent, q.AnswerContent, "
                   + "q.CreatedAt, q.AnsweredAt, q.IsAnswered, "
                   + "s.FullName AS StudentName, "
                   + "st.FullName AS TeacherName, "
                   + "t.Title AS TaskTitle "
                   + "FROM Question q "
                   + "LEFT JOIN Student s ON s.StudentID = q.StudentID "
                   + "LEFT JOIN Staff st ON st.StaffID = q.TeacherID "
                   + "LEFT JOIN Task t ON t.TaskID = q.TaskID "
                   + "WHERE q.QuestionID = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }, questionId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findById: " + e.getMessage(), e);
        }
    }

    private Question mapRow(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setQuestionId(rs.getInt("QuestionID"));
        q.setStudentId(rs.getInt("StudentID"));
        q.setTeacherId(rs.getInt("TeacherID"));

        int taskId = rs.getInt("TaskID");
        if (rs.wasNull()) {
            q.setTaskId(null);
        } else {
            q.setTaskId(taskId);
        }

        q.setQuestionContent(rs.getString("QuestionContent"));
        q.setAnswerContent(rs.getString("AnswerContent"));
        q.setAnswered(rs.getBoolean("IsAnswered"));

        Timestamp createdAt = rs.getTimestamp("CreatedAt");
        if (createdAt != null) {
            q.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp answeredAt = rs.getTimestamp("AnsweredAt");
        if (answeredAt != null) {
            q.setAnsweredAt(answeredAt.toLocalDateTime());
        }

        q.setStudentName(rs.getString("StudentName"));
        q.setTeacherName(rs.getString("TeacherName"));
        q.setTaskTitle(rs.getString("TaskTitle"));

        return q;
    }
}