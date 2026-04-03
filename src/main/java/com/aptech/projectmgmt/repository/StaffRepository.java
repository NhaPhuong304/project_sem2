package com.aptech.projectmgmt.repository;

import com.aptech.projectmgmt.model.Staff;
import com.aptech.projectmgmt.model.UserRole;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StaffRepository extends BaseRepository {

    public Staff findByAccountId(int accountId) {
        String sql = "SELECT s.StaffID, s.FullName, s.Email, s.AccountID, " +
                     "a.Username, a.PhotoUrl, a.IsActive " +
                     "FROM Staff s " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
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

    public Staff findById(int staffId) {
        String sql = "SELECT s.StaffID, s.FullName, s.Email, s.AccountID, " +
                     "a.Username, a.PhotoUrl, a.IsActive " +
                     "FROM Staff s " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
                     "WHERE s.StaffID = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) return mapRow(rs);
                return null;
            }, staffId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findById: " + e.getMessage(), e);
        }
    }

    public List<Staff> findAll() {
        String sql = "SELECT s.StaffID, s.FullName, s.Email, s.AccountID, " +
                     "a.Username, a.PhotoUrl, a.IsActive " +
                     "FROM Staff s " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
                     "ORDER BY s.FullName";
        try {
            return executeQuery(sql, rs -> {
                List<Staff> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            });
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findAll: " + e.getMessage(), e);
        }
    }

    public List<Staff> findByRole(UserRole role) {
        String sql = "SELECT s.StaffID, s.FullName, s.Email, s.AccountID, " +
                     "a.Username, a.PhotoUrl, a.IsActive " +
                     "FROM Staff s " +
                     "INNER JOIN Account a ON a.AccountID = s.AccountID " +
                     "WHERE a.[Role] = ? " +
                     "ORDER BY s.FullName";
        try {
            return executeQuery(sql, rs -> {
                List<Staff> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }, role.getValue());
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByRole: " + e.getMessage(), e);
        }
    }

    public Staff findByEmail(String email) {
        String sql = "SELECT s.StaffID, s.FullName, s.Email, s.AccountID, " +
                     "a.Username, a.PhotoUrl, a.IsActive " +
                     "FROM Staff s " +
                     "LEFT JOIN Account a ON a.AccountID = s.AccountID " +
                     "WHERE s.Email = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) return mapRow(rs);
                return null;
            }, email);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in findByEmail: " + e.getMessage(), e);
        }
    }

    public int create(String fullName, String email, int accountId) {
        String sql = "INSERT INTO Staff (FullName, Email, AccountID) VALUES (?, ?, ?)";
        try {
            return executeUpdateGetKey(sql, fullName, email, accountId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in create staff: " + e.getMessage(), e);
        }
    }

    public String getEmail(int staffId) {
        String sql = "SELECT Email FROM Staff WHERE StaffID = ?";
        try {
            return executeQuery(sql, rs -> {
                if (rs.next()) return rs.getString("Email");
                return null;
            }, staffId);
        } catch (SQLException e) {
            throw new RuntimeException("DB error in getEmail: " + e.getMessage(), e);
        }
    }

    private Staff mapRow(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setStaffId(rs.getInt("StaffID"));
        s.setFullName(rs.getString("FullName"));
        s.setEmail(rs.getString("Email"));
        s.setAccountId(rs.getInt("AccountID"));
        s.setUsername(rs.getString("Username"));
        s.setPhotoUrl(rs.getString("PhotoUrl"));
        s.setActive(rs.getBoolean("IsActive"));
        return s;
    }
}
