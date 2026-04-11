package com.aptech.projectmgmt.repository;

import com.aptech.projectmgmt.model.SchoolClass;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ClassRepository extends BaseRepository {

    public static final String UNASSIGNED_CLASS_NAME = "Chua xep lop";

    public List<SchoolClass> findAll() {
        String sql = "SELECT c.ClassID, c.ClassName, c.AcademicYear, c.CreatedAt, " +
                     "COUNT(s.StudentID) AS StudentCount " +
                     "FROM Class c LEFT JOIN Student s ON s.ClassID = c.ClassID " +
                     "WHERE c.ClassName <> ? " +
                     "GROUP BY c.ClassID, c.ClassName, c.AcademicYear, c.CreatedAt " +
                     "ORDER BY c.CreatedAt DESC";
        try {
            return executeQuery(sql, rs -> {
                List<SchoolClass> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }, UNASSIGNED_CLASS_NAME);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findAll classes: " + e.getMessage(), e);
        }
    }

    public List<SchoolClass> findByAdvisorId(int staffId) {
        String sql = "SELECT c.ClassID, c.ClassName, c.AcademicYear, c.CreatedAt, " +
                     "(SELECT COUNT(*) FROM Student s WHERE s.ClassID = c.ClassID) AS StudentCount " +
                     "FROM Class c " +
                     "WHERE c.ClassName <> ? " +
                     "AND EXISTS (" +
                     "    SELECT 1 FROM Project p " +
                     "    INNER JOIN ProjectGroup pg ON pg.GroupID = p.GroupID " +
                     "    WHERE pg.ClassID = c.ClassID AND p.AdvisorID = ?" +
                     ") " +
                     "ORDER BY c.CreatedAt DESC";
        try {
            return executeQuery(sql, rs -> {
                List<SchoolClass> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }, UNASSIGNED_CLASS_NAME, staffId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByAdvisorId classes: " + e.getMessage(), e);
        }
    }

    public int create(String className, String academicYear) {
        String sql = "INSERT INTO Class (ClassName, AcademicYear) VALUES (?, ?)";
        try {
            return executeUpdateGetKey(sql, className, academicYear);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in create class: " + e.getMessage(), e);
        }
    }

    public SchoolClass findById(int classId) {
        String sql = "SELECT c.ClassID, c.ClassName, c.AcademicYear, c.CreatedAt, " +
                     "COUNT(s.StudentID) AS StudentCount " +
                     "FROM Class c LEFT JOIN Student s ON s.ClassID = c.ClassID " +
                     "WHERE c.ClassID = ? " +
                     "GROUP BY c.ClassID, c.ClassName,c.AcademicYear, c.CreatedAt";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) return mapRow(rs);
                return null;
            }, classId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findById class: " + e.getMessage(), e);
        }
    }

    public SchoolClass findByName(String className) {
        String sql = "SELECT c.ClassID, c.ClassName, c.AcademicYear, c.CreatedAt, " +
                     "(SELECT COUNT(*) FROM Student s WHERE s.ClassID = c.ClassID) AS StudentCount " +
                     "FROM Class c WHERE c.ClassName = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }, className);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByName class: " + e.getMessage(), e);
        }
    }

    public int ensureUnassignedClass() {
        SchoolClass existing = findByName(UNASSIGNED_CLASS_NAME);
        if (existing != null) {
            return existing.getClassId();
        }
        return create(UNASSIGNED_CLASS_NAME, "N/A");
    }

    private SchoolClass mapRow(ResultSet rs) throws SQLException {
        SchoolClass c = new SchoolClass();
        c.setClassId(rs.getInt("ClassID"));
        c.setClassName(rs.getString("ClassName"));
        c.setAcademicYear(rs.getString("AcademicYear"));
        Timestamp ts = rs.getTimestamp("CreatedAt");
        if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        c.setStudentCount(rs.getInt("StudentCount"));
        return c;
    }
}
