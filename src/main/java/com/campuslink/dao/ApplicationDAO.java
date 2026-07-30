package com.campuslink.dao;

import com.campuslink.models.Application;
import com.campuslink.utils.AppDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationDAO.class);
    private final DataSource dataSource;

    public ApplicationDAO() {
        this(AppDataSource.getInstance());
    }

    public ApplicationDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConn() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }

    public boolean create(Application app) {
        String sql = "INSERT INTO applications (student_id, opportunity_type, opportunity_id, application_date, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, app.getStudentId());
            ps.setString(2, app.getOpportunityType());
            ps.setInt(3, app.getOpportunityId());
            ps.setDate(4, Date.valueOf(app.getApplicationDate()));
            ps.setString(5, app.getStatus());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) app.setApplicationId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("ApplicationDAO.create failed: {}", e.getMessage(), e);
        }
        return false;
    }

    public Application findById(int applicationId) {
        String sql = "SELECT * FROM applications WHERE application_id = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            logger.error("ApplicationDAO.findById failed for ID {}: {}", applicationId, e.getMessage(), e);
        }
        return null;
    }

    public List<Application> findByStudent(int studentId) {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT a.*, s.full_name as student_name, " +
                     "COALESCE(j.title, i.title) as opportunity_title, " +
                     "COALESCE(ej.company_name, ei.company_name) as company_name " +
                     "FROM applications a " +
                     "JOIN students s ON a.student_id = s.student_id " +
                     "LEFT JOIN jobs j ON a.opportunity_type = 'JOB' AND a.opportunity_id = j.job_id " +
                     "LEFT JOIN employers ej ON j.employer_id = ej.employer_id " +
                     "LEFT JOIN internships i ON a.opportunity_type = 'INTERNSHIP' AND a.opportunity_id = i.internship_id " +
                     "LEFT JOIN employers ei ON i.employer_id = ei.employer_id " +
                     "WHERE a.student_id = ? ORDER BY a.application_date DESC";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Application app = mapRow(rs);
                    app.setStudentName(rs.getString("student_name"));
                    app.setOpportunityTitle(rs.getString("opportunity_title"));
                    app.setCompanyName(rs.getString("company_name"));
                    list.add(app);
                }
            }
        } catch (SQLException e) {
            logger.error("ApplicationDAO.findByStudent failed for student ID {}: {}", studentId, e.getMessage(), e);
        }
        return list;
    }

    public List<Application> findByOpportunity(String type, int opportunityId) {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT a.*, s.full_name as student_name, " +
                     "COALESCE(j.title, i.title) as opportunity_title, " +
                     "COALESCE(ej.company_name, ei.company_name) as company_name " +
                     "FROM applications a " +
                     "JOIN students s ON a.student_id = s.student_id " +
                     "LEFT JOIN jobs j ON a.opportunity_type = 'JOB' AND a.opportunity_id = j.job_id " +
                     "LEFT JOIN employers ej ON j.employer_id = ej.employer_id " +
                     "LEFT JOIN internships i ON a.opportunity_type = 'INTERNSHIP' AND a.opportunity_id = i.internship_id " +
                     "LEFT JOIN employers ei ON i.employer_id = ei.employer_id " +
                     "WHERE a.opportunity_type = ? AND a.opportunity_id = ? ORDER BY a.application_date DESC";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setInt(2, opportunityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Application app = mapRow(rs);
                    app.setStudentName(rs.getString("student_name"));
                    app.setOpportunityTitle(rs.getString("opportunity_title"));
                    app.setCompanyName(rs.getString("company_name"));
                    list.add(app);
                }
            }
        } catch (SQLException e) {
            logger.error("ApplicationDAO.findByOpportunity failed for type {} and ID {}: {}", type, opportunityId, e.getMessage(), e);
        }
        return list;
    }

    public List<Application> findAll() {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT a.*, s.full_name as student_name, " +
                     "COALESCE(j.title, i.title) as opportunity_title, " +
                     "COALESCE(ej.company_name, ei.company_name) as company_name " +
                     "FROM applications a " +
                     "JOIN students s ON a.student_id = s.student_id " +
                     "LEFT JOIN jobs j ON a.opportunity_type = 'JOB' AND a.opportunity_id = j.job_id " +
                     "LEFT JOIN employers ej ON j.employer_id = ej.employer_id " +
                     "LEFT JOIN internships i ON a.opportunity_type = 'INTERNSHIP' AND a.opportunity_id = i.internship_id " +
                     "LEFT JOIN employers ei ON i.employer_id = ei.employer_id " +
                     "ORDER BY a.application_date DESC";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Application app = mapRow(rs);
                app.setStudentName(rs.getString("student_name"));
                app.setOpportunityTitle(rs.getString("opportunity_title"));
                app.setCompanyName(rs.getString("company_name"));
                list.add(app);
            }
        } catch (SQLException e) {
            logger.error("ApplicationDAO.findAll failed: {}", e.getMessage(), e);
        }
        return list;
    }

    public boolean update(Application app) {
        String sql = "UPDATE applications SET student_id=?, opportunity_type=?, opportunity_id=?, " +
                     "application_date=?, status=? WHERE application_id=?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, app.getStudentId());
            ps.setString(2, app.getOpportunityType());
            ps.setInt(3, app.getOpportunityId());
            ps.setDate(4, Date.valueOf(app.getApplicationDate()));
            ps.setString(5, app.getStatus());
            ps.setInt(6, app.getApplicationId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("ApplicationDAO.update failed for application ID {}: {}", app.getApplicationId(), e.getMessage(), e);
        }
        return false;
    }

    public boolean delete(int applicationId) {
        String sql = "DELETE FROM applications WHERE application_id=?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("ApplicationDAO.delete failed for application ID {}: {}", applicationId, e.getMessage(), e);
        }
        return false;
    }

    public boolean updateStatus(int applicationId, String status) {
        String sql = "UPDATE applications SET status=? WHERE application_id=?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, applicationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("ApplicationDAO.updateStatus failed for application ID {}: {}", applicationId, e.getMessage(), e);
        }
        return false;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM applications WHERE status = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("ApplicationDAO.countByStatus failed for status {}: {}", status, e.getMessage(), e);
        }
        return 0;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM applications";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("ApplicationDAO.count failed: {}", e.getMessage(), e);
        }
        return 0;
    }

    private Application mapRow(ResultSet rs) throws SQLException {
        Application a = new Application();
        a.setApplicationId(rs.getInt("application_id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setOpportunityType(rs.getString("opportunity_type"));
        a.setOpportunityId(rs.getInt("opportunity_id"));
        Date d = rs.getDate("application_date");
        if (d != null) a.setApplicationDate(d.toLocalDate());
        a.setStatus(rs.getString("status"));
        return a;
    }
}
