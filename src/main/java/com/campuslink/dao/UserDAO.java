package com.campuslink.dao;

import com.campuslink.models.User;
import com.campuslink.utils.AppDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    private final DataSource dataSource;

    public UserDAO() {
        this(AppDataSource.getInstance());
    }

    public UserDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConn() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }

    public boolean create(User user) {
        try (Connection conn = getConn()) {
            return create(user, conn);
        } catch (SQLException e) {
            logger.error("UserDAO.create failed for user {}: {}", user.getUsername(), e.getMessage(), e);
            return false;
        }
    }

    public boolean create(User user, Connection conn) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, must_change_password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.setBoolean(4, user.isMustChangePassword());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) user.setId(rs.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            logger.error("UserDAO.findById failed for ID {}: {}", id, e.getMessage(), e);
        }
        return null;
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            logger.error("UserDAO.findByUsername failed for username {}: {}", username, e.getMessage(), e);
        }
        return null;
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            logger.error("UserDAO.findAll failed: {}", e.getMessage(), e);
        }
        return list;
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET username=?, password=?, role=?, must_change_password=? WHERE id=?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.setBoolean(4, user.isMustChangePassword());
            ps.setInt(5, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("UserDAO.update failed for user ID {}: {}", user.getId(), e.getMessage(), e);
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("UserDAO.delete failed for ID {}: {}", id, e.getMessage(), e);
        }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("UserDAO.count failed: {}", e.getMessage(), e);
        }
        return 0;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setMustChangePassword(rs.getBoolean("must_change_password"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    }
}
