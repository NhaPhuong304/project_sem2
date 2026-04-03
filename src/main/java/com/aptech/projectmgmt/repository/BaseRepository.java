package com.aptech.projectmgmt.repository;

import com.aptech.projectmgmt.config.DatabaseConfig;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseRepository {

    protected <T> T executeQuery(String sql, ResultSetMapper<T> mapper, Object... params) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return mapper.map(rs);
            }
        }
    }

    protected int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, params);
            return ps.executeUpdate();
        }
    }

    protected int executeUpdateGetKey(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    protected Map<String, Object> executeStoredProc(String procName, Object... params) throws SQLException {
        StringBuilder sb = new StringBuilder("{CALL ").append(procName).append("(");
        for (int i = 0; i < params.length; i++) {
            sb.append(i == 0 ? "?" : ", ?");
        }
        sb.append(")}");

        try (Connection conn = DatabaseConfig.getConnection();
             CallableStatement cs = conn.prepareCall(sb.toString())) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof OutParam) {
                    OutParam out = (OutParam) params[i];
                    cs.registerOutParameter(i + 1, out.getSqlType());
                } else {
                    cs.setObject(i + 1, params[i]);
                }
            }
            cs.execute();
            Map<String, Object> result = new HashMap<>();
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof OutParam) {
                    OutParam out = (OutParam) params[i];
                    result.put(out.getName(), cs.getObject(i + 1));
                }
            }
            return result;
        }
    }

    private void setParams(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    @FunctionalInterface
    protected interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    protected static class OutParam {
        private final String name;
        private final int sqlType;

        public OutParam(String name, int sqlType) {
            this.name = name;
            this.sqlType = sqlType;
        }

        public String getName() { return name; }
        public int getSqlType() { return sqlType; }
    }
}
