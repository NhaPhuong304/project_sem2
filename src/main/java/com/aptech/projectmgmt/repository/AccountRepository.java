package com.aptech.projectmgmt.repository;

import com.aptech.projectmgmt.model.Account;
import com.aptech.projectmgmt.model.UserRole;

import java.sql.SQLException;

public class AccountRepository extends BaseRepository {

	private static final String DEFAULT_ACCOUNT_PHOTO = "no-image.jpg";

	public Account findByUsername(String username) {
		String sql = "SELECT AccountID, Username, PasswordHash, Role, IsActive, IsFirstLogin, PhotoUrl "
				+ "FROM Account WHERE Username = ?";
		try {
			return executeQuery(sql, rs -> {
				if (rs.next()) {
					Account a = new Account();
					a.setAccountId(rs.getInt("AccountID"));
					a.setUsername(rs.getString("Username"));
					a.setPasswordHash(rs.getString("PasswordHash"));
					a.setRole(UserRole.fromValue(rs.getInt("Role")));
					a.setActive(rs.getBoolean("IsActive"));
					a.setFirstLogin(rs.getBoolean("IsFirstLogin"));
					a.setPhotoUrl(rs.getString("PhotoUrl"));
					return a;
				}
				return null;
			}, username);
		} catch (SQLException e) {
			throw new RuntimeException("DB error in findByUsername: " + e.getMessage(), e);
		}
	}

	public void updatePhoto(int accountId, String photoUrl) {
		String sql = "UPDATE Account SET PhotoUrl = ? WHERE AccountID = ?";
		try {
			executeUpdate(sql, photoUrl, accountId);
		} catch (SQLException e) {
			throw new RuntimeException("DB error in updatePhoto: " + e.getMessage(), e);
		}
	}

	public int insertAccount(String username, String passwordHash, int role, boolean isFirstLogin) {
		String sql = "INSERT INTO Account (Username, PasswordHash, Role, IsActive, IsFirstLogin, PhotoUrl) "
				+ "VALUES (?, ?, ?, 1, ?, ?)";
		try {
			return executeUpdateGetKey(sql, username, passwordHash, role, isFirstLogin ? 1 : 0, DEFAULT_ACCOUNT_PHOTO);
		} catch (SQLException e) {
			throw new RuntimeException("DB error in insertAccount: " + e.getMessage(), e);
		}
	}

	public void deleteAccount(int accountId) {
		String sql = "DELETE FROM Account WHERE AccountID = ?";
		try {
			executeUpdate(sql, accountId);
		} catch (SQLException e) {
			throw new RuntimeException("DB error in deleteAccount: " + e.getMessage(), e);
		}
	}

	public void updateActive(int accountId, boolean isActive) {
		String sql = "UPDATE Account SET IsActive = ? WHERE AccountID = ?";
		try {
			executeUpdate(sql, isActive ? 1 : 0, accountId);
		} catch (SQLException e) {
			throw new RuntimeException("DB error in updateActive: " + e.getMessage(), e);
		}
	}
}
