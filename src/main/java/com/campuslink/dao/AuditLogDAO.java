package com.campuslink.dao;

import com.campuslink.utils.AppDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the {@code audit_log} table.
 *
 * <p>Deliberately append-only: this class exposes {@link #insert} and read methods only.
 * There is no update or delete path, by design — an audit trail that can be edited by the
 * same application it's auditing isn't an audit trail.
 */
public class AuditLogDAO {
    private static final Logger logger = LoggerFactory.getLogger(AuditLogDAO.class);
    private final DataSource dataSource;

    public AuditLogDAO() {
        this(AppDataSource.getInstance());
    }

    public AuditLogDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConn() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }

    public boolean insert(String event, String username, String outcome, String details) {
        String sql = "INSERT INTO audit_log (event, username, outcome, details) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event);
            ps.setString(2, username);
            ps.setString(3, outcome);
            ps.setString(4, details);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("AuditLogDAO.insert failed for event {}: {}", event, e.getMessage(), e);
            return false;
        }
    }

    public List<String> findRecent(int limit) {
        String sql = "SELECT event_time, event, username, outcome, details FROM audit_log " +
                     "ORDER BY audit_id DESC LIMIT ?";
        List<String> rows = new ArrayList<>();
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(String.format("%s event=%s username=%s outcome=%s details=%s",
                        rs.getTimestamp("event_time"),
                        rs.getString("event"),
                        rs.getString("username"),
                        rs.getString("outcome"),
                        rs.getString("details")));
                }
            }
        } catch (SQLException e) {
            logger.error("AuditLogDAO.findRecent failed: {}", e.getMessage(), e);
        }
        return rows;
    }

    public List<com.campuslink.models.AuditLog> findAll() {
        List<com.campuslink.models.AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_log ORDER BY audit_id DESC";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                com.campuslink.models.AuditLog log = new com.campuslink.models.AuditLog();
                log.setAuditId(rs.getInt("audit_id"));
                Timestamp ts = rs.getTimestamp("event_time");
                if (ts != null) log.setEventTime(ts.toLocalDateTime());
                log.setEvent(rs.getString("event"));
                log.setUsername(rs.getString("username"));
                log.setOutcome(rs.getString("outcome"));
                log.setDetails(rs.getString("details"));
                list.add(log);
            }
        } catch (SQLException e) {
            logger.error("AuditLogDAO.findAll failed: {}", e.getMessage(), e);
        }
        return list;
    }
}
