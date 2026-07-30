package com.campuslink.dao;

import com.campuslink.models.Resource;
import com.campuslink.utils.AppDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {
    private static final Logger logger = LoggerFactory.getLogger(ResourceDAO.class);
    private final DataSource dataSource;

    public ResourceDAO() {
        this(AppDataSource.getInstance());
    }

    public ResourceDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConn() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }

    public boolean create(Resource resource) {
        String sql = "INSERT INTO resources (title, description, file_path) VALUES (?, ?, ?)";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, resource.getTitle());
            ps.setString(2, resource.getDescription());
            ps.setString(3, resource.getFilePath());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) resource.setResourceId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("ResourceDAO.create failed for resource {}: {}", resource.getTitle(), e.getMessage(), e);
        }
        return false;
    }

    public Resource findById(int resourceId) {
        String sql = "SELECT * FROM resources WHERE resource_id = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            logger.error("ResourceDAO.findById failed for ID {}: {}", resourceId, e.getMessage(), e);
        }
        return null;
    }

    public List<Resource> findAll() {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources ORDER BY uploaded_at DESC";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            logger.error("ResourceDAO.findAll failed: {}", e.getMessage(), e);
        }
        return list;
    }

    public boolean update(Resource resource) {
        String sql = "UPDATE resources SET title=?, description=?, file_path=? WHERE resource_id=?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resource.getTitle());
            ps.setString(2, resource.getDescription());
            ps.setString(3, resource.getFilePath());
            ps.setInt(4, resource.getResourceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("ResourceDAO.update failed for resource ID {}: {}", resource.getResourceId(), e.getMessage(), e);
        }
        return false;
    }

    public boolean delete(int resourceId) {
        String sql = "DELETE FROM resources WHERE resource_id=?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("ResourceDAO.delete failed for resource ID {}: {}", resourceId, e.getMessage(), e);
        }
        return false;
    }

    public List<Resource> search(String keyword) {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources WHERE title LIKE ? OR description LIKE ? ORDER BY uploaded_at DESC";
        String pattern = "%" + keyword + "%";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("ResourceDAO.search failed for keyword {}: {}", keyword, e.getMessage(), e);
        }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM resources";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("ResourceDAO.count failed: {}", e.getMessage(), e);
        }
        return 0;
    }

    private Resource mapRow(ResultSet rs) throws SQLException {
        Resource r = new Resource();
        r.setResourceId(rs.getInt("resource_id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setFilePath(rs.getString("file_path"));
        Timestamp ts = rs.getTimestamp("uploaded_at");
        if (ts != null) r.setUploadedAt(ts.toLocalDateTime());
        return r;
    }
}
