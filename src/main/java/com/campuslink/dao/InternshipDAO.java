package com.campuslink.dao;

import com.campuslink.models.Internship;
import com.campuslink.utils.AppDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InternshipDAO {
    private static final Logger logger = LoggerFactory.getLogger(InternshipDAO.class);
    private final DataSource dataSource;

    public InternshipDAO() {
        this(AppDataSource.getInstance());
    }

    public InternshipDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConn() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }

    public boolean create(Internship internship) {
        String sql = "INSERT INTO internships (title, description, requirements, deadline, employer_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, internship.getTitle());
            ps.setString(2, internship.getDescription());
            ps.setString(3, internship.getRequirements());
            ps.setDate(4, internship.getDeadline() != null ? Date.valueOf(internship.getDeadline()) : null);
            if (internship.getEmployerId() > 0) {
                ps.setInt(5, internship.getEmployerId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) internship.setInternshipId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("InternshipDAO.create failed for internship {}: {}", internship.getTitle(), e.getMessage(), e);
        }
        return false;
    }

    public Internship findById(int internshipId) {
        String sql = "SELECT i.*, e.company_name FROM internships i " +
                     "LEFT JOIN employers e ON i.employer_id = e.employer_id WHERE i.internship_id = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, internshipId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            logger.error("InternshipDAO.findById failed for ID {}: {}", internshipId, e.getMessage(), e);
        }
        return null;
    }

    public List<Internship> findAll() {
        List<Internship> list = new ArrayList<>();
        String sql = "SELECT i.*, e.company_name FROM internships i " +
                     "LEFT JOIN employers e ON i.employer_id = e.employer_id ORDER BY i.internship_id DESC";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            logger.error("InternshipDAO.findAll failed: {}", e.getMessage(), e);
        }
        return list;
    }

    public List<Internship> findByEmployer(int employerId) {
        List<Internship> list = new ArrayList<>();
        String sql = "SELECT i.*, e.company_name FROM internships i " +
                     "LEFT JOIN employers e ON i.employer_id = e.employer_id WHERE i.employer_id = ? ORDER BY i.internship_id DESC";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("InternshipDAO.findByEmployer failed for employer ID {}: {}", employerId, e.getMessage(), e);
        }
        return list;
    }

    public List<Internship> findActive() {
        List<Internship> list = new ArrayList<>();
        String sql = "SELECT i.*, e.company_name FROM internships i " +
                     "LEFT JOIN employers e ON i.employer_id = e.employer_id " +
                     "WHERE i.deadline >= CURDATE() ORDER BY i.deadline ASC";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            logger.error("InternshipDAO.findActive failed: {}", e.getMessage(), e);
        }
        return list;
    }

    public boolean update(Internship internship) {
        String sql = "UPDATE internships SET title=?, description=?, requirements=?, deadline=?, employer_id=? WHERE internship_id=?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, internship.getTitle());
            ps.setString(2, internship.getDescription());
            ps.setString(3, internship.getRequirements());
            ps.setDate(4, internship.getDeadline() != null ? Date.valueOf(internship.getDeadline()) : null);
            if (internship.getEmployerId() > 0) {
                ps.setInt(5, internship.getEmployerId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setInt(6, internship.getInternshipId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("InternshipDAO.update failed for internship ID {}: {}", internship.getInternshipId(), e.getMessage(), e);
        }
        return false;
    }

    public boolean delete(int internshipId) {
        String sql = "DELETE FROM internships WHERE internship_id=?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, internshipId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("InternshipDAO.delete failed for internship ID {}: {}", internshipId, e.getMessage(), e);
        }
        return false;
    }

    public List<Internship> search(String keyword) {
        List<Internship> list = new ArrayList<>();
        String sql = "SELECT i.*, e.company_name FROM internships i " +
                     "LEFT JOIN employers e ON i.employer_id = e.employer_id " +
                     "WHERE i.title LIKE ? OR i.description LIKE ? OR i.requirements LIKE ? ORDER BY i.internship_id DESC";
        String pattern = "%" + keyword + "%";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("InternshipDAO.search failed for keyword {}: {}", keyword, e.getMessage(), e);
        }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM internships";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("InternshipDAO.count failed: {}", e.getMessage(), e);
        }
        return 0;
    }

    private Internship mapRow(ResultSet rs) throws SQLException {
        Internship i = new Internship();
        i.setInternshipId(rs.getInt("internship_id"));
        i.setTitle(rs.getString("title"));
        i.setDescription(rs.getString("description"));
        i.setRequirements(rs.getString("requirements"));
        Date d = rs.getDate("deadline");
        if (d != null) i.setDeadline(d.toLocalDate());
        i.setEmployerId(rs.getInt("employer_id"));
        try { i.setCompanyName(rs.getString("company_name")); } catch (SQLException ignored) {}
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) i.setCreatedAt(ts.toLocalDateTime());
        return i;
    }
}
