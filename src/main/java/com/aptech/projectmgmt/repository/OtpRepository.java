package com.aptech.projectmgmt.repository;

import java.sql.Types;
import java.util.Map;

public class OtpRepository extends BaseRepository {

    public String generateOtp(int accountId, int purpose) {
        try {
            Map<String, Object> result = executeStoredProc("sp_GenerateOtp",
                    accountId,
                    purpose,
                    new OutParam("OtpCode", Types.NVARCHAR));
            Object code = result.get("OtpCode");
            return code != null ? code.toString() : null;
        } catch (Exception e) {
            throw new RuntimeException("DB error in generateOtp: " + e.getMessage(), e);
        }
    }

    public int verifyOtp(int accountId, int purpose, String otpCode, String newPasswordHash) {
        try {
            Map<String, Object> result = executeStoredProc("sp_VerifyOtp",
                    accountId,
                    purpose,
                    otpCode,
                    newPasswordHash,
                    new OutParam("ResultCode", Types.INTEGER));
            Object code = result.get("ResultCode");
            return code != null ? ((Number) code).intValue() : 4;
        } catch (Exception e) {
            throw new RuntimeException("DB error in verifyOtp: " + e.getMessage(), e);
        }
    }

    public int getLatestAttemptCount(int accountId, int purpose) {
        String sql = "SELECT TOP 1 AttemptCount " +
                     "FROM OtpVerification " +
                     "WHERE AccountID = ? AND Purpose = ? " +
                     "ORDER BY CreatedAt DESC, OtpID DESC";
        try {
            return executeQuery(sql, rs -> rs.next() ? rs.getInt("AttemptCount") : 0, accountId, purpose);
        } catch (Exception e) {
            throw new RuntimeException("DB error in getLatestAttemptCount: " + e.getMessage(), e);
        }
    }
}
