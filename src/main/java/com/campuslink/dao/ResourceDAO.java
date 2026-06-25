package com.campuslink.dao;

import com.campuslink.models.Resource;
import com.campuslink.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {
    private Connection getConn() { return DBConnection.getInstance().getConnection(); }

    public boolean create(Resource resource) {
        String sql = "INSERT INTO resources (title, description, file_path) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        } catch (SQLException e) { System.err.println("ResourceDAO.create: " + e.getMessage()); }
        return false;
    }

    public Resource findById(int resourceId) {
        String sql = "SELECT * FROM resources WHERE resource_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { System.err.println("ResourceDAO.findById: " + e.getMessage()); }
        return null;
    }

    public List<Resource> findAll() {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources ORDER BY uploaded_at DESC";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("ResourceDAO.findAll: " + e.getMessage()); }
        return list;
    }

    public boolean update(Resource resource) {
        String sql = "UPDATE resources SET title=?, description=?, file_path=? WHERE resource_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, resource.getTitle());
            ps.setString(2, resource.getDescription());
            ps.setString(3, resource.getFilePath());
            ps.setInt(4, resource.getResourceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("ResourceDAO.update: " + e.getMessage()); }
        return false;
    }

    public boolean delete(int resourceId) {
        String sql = "DELETE FROM resources WHERE resource_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("ResourceDAO.delete: " + e.getMessage()); }
        return false;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM resources";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("ResourceDAO.count: " + e.getMessage()); }
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
