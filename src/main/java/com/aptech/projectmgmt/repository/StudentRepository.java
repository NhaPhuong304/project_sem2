package com.aptech.projectmgmt.repository;

import com.aptech.projectmgmt.model.Student;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentRepository extends BaseRepository {

    public Student findByAccountId(int accountId) {
        String sql = "SELECT s.StudentID, s.StudentCode, s.FullName, s.Email, s.ClassID, s.AccountID, a.PhotoUrl, c.ClassName " +
                     "FROM Student s " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
                     "LEFT JOIN Class c ON c.ClassID = s.ClassID " +
                     "WHERE s.AccountID = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) return mapRow(rs);
                return null;
            }, accountId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByAccountId: " + e.getMessage(), e);
        }
    }

    public Student findById(int studentId) {
        String sql = "SELECT s.StudentID, s.StudentCode, s.FullName, s.Email, s.ClassID, s.AccountID, a.PhotoUrl, c.ClassName " +
                     "FROM Student s " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
                     "LEFT JOIN Class c ON c.ClassID = s.ClassID " +
                     "WHERE s.StudentID = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) return mapRow(rs);
                return null;
            }, studentId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findById: " + e.getMessage(), e);
        }
    }

    public List<Student> findByClassId(int classId) {
        String sql = "SELECT s.StudentID, s.StudentCode, s.FullName, s.Email, s.ClassID, s.AccountID, a.PhotoUrl, c.ClassName " +
                     "FROM Student s " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
                     "LEFT JOIN Class c ON c.ClassID = s.ClassID " +
                     "WHERE s.ClassID = ? ORDER BY s.FullName";
        try {
            return executeQuery(sql, rs -> {
                List<Student> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }, classId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByClassId: " + e.getMessage(), e);
        }
    }

    public List<Student> findWithoutAccount(int classId) {
        String sql = "SELECT s.StudentID, s.StudentCode, s.FullName, s.Email, s.ClassID, s.AccountID, a.PhotoUrl, c.ClassName " +
                     "FROM Student s " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
                     "LEFT JOIN Class c ON c.ClassID = s.ClassID " +
                     "WHERE s.ClassID = ? AND s.AccountID IS NULL";
        try {
            return executeQuery(sql, rs -> {
                List<Student> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }, classId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findWithoutAccount: " + e.getMessage(), e);
        }
    }

    public Student findByStudentCode(String studentCode) {
        String sql = "SELECT s.StudentID, s.StudentCode, s.FullName, s.Email, s.ClassID, s.AccountID, a.PhotoUrl, c.ClassName " +
                     "FROM Student s " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
                     "LEFT JOIN Class c ON c.ClassID = s.ClassID " +
                     "WHERE s.StudentCode = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }, studentCode);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByStudentCode: " + e.getMessage(), e);
        }
    }

    public Student findByEmail(String email) {
        String sql = "SELECT s.StudentID, s.StudentCode, s.FullName, s.Email, s.ClassID, s.AccountID, a.PhotoUrl, c.ClassName " +
                     "FROM Student s " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
                     "LEFT JOIN Class c ON c.ClassID = s.ClassID " +
                     "WHERE s.Email = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }, email);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByEmail: " + e.getMessage(), e);
        }
    }

    public String getNextStudentCode() {
        String sql = "SELECT TOP 1 StudentCode " +
                     "FROM Student " +
                     "WHERE StudentCode LIKE 'ST%' " +
                     "AND TRY_CAST(SUBSTRING(StudentCode, 3, LEN(StudentCode) - 2) AS INT) IS NOT NULL " +
                     "ORDER BY TRY_CAST(SUBSTRING(StudentCode, 3, LEN(StudentCode) - 2) AS INT) DESC";
        try {
            String latestCode = executeQuery(sql, rs -> rs.next() ? rs.getString("StudentCode") : null);
            if (latestCode == null || latestCode.length() < 3) {
                return "ST001";
            }
            int numericPart = Integer.parseInt(latestCode.substring(2));
            return String.format("ST%03d", numericPart + 1);
        } catch (Exception e) {
            throw new RuntimeException("DB error in getNextStudentCode: " + e.getMessage(), e);
        }
    }

    public int create(String studentCode, String fullName, String email, int classId, int accountId) {
        String sql = "INSERT INTO Student (StudentCode, FullName, Email, ClassID, AccountID) VALUES (?, ?, ?, ?, ?)";
        try {
            return executeUpdateGetKey(sql, studentCode, fullName, email, classId, accountId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in create student: " + e.getMessage(), e);
        }
    }

    public void updateAccountId(int studentId, int accountId) {
        String sql = "UPDATE Student SET AccountID = ? WHERE StudentID = ?";
        try {
            executeUpdate(sql, accountId, studentId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in updateAccountId: " + e.getMessage(), e);
        }
    }

    public void updateClassId(int studentId, int classId) {
        String sql = "UPDATE Student SET ClassID = ? WHERE StudentID = ?";
        try {
            executeUpdate(sql, classId, studentId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in updateClassId: " + e.getMessage(), e);
        }
    }

    public List<Student> findAll() {
        String sql = "SELECT s.StudentID, s.StudentCode, s.FullName, s.Email, s.ClassID, s.AccountID, a.PhotoUrl, c.ClassName " +
                     "FROM Student s " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
                     "LEFT JOIN Class c ON c.ClassID = s.ClassID " +
                     "ORDER BY s.StudentCode ASC";
        try {
            return executeQuery(sql, rs -> {
                List<Student> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            });
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findAll students: " + e.getMessage(), e);
        }
    }

    public String getEmail(int studentId) {
        String sql = "SELECT Email FROM Student WHERE StudentID = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) return rs.getString("Email");
                return null;
            }, studentId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in getEmail: " + e.getMessage(), e);
        }
    }

    private Student mapRow(java.sql.ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("StudentID"));
        s.setStudentCode(rs.getString("StudentCode"));
        s.setFullName(rs.getString("FullName"));
        s.setEmail(rs.getString("Email"));
        s.setClassId(rs.getInt("ClassID"));
        int accId = rs.getInt("AccountID");
        if (!rs.wasNull()) s.setAccountId(accId);
        s.setPhotoUrl(rs.getString("PhotoUrl"));
        s.setClassName(rs.getString("ClassName"));
        return s;
    }
}
