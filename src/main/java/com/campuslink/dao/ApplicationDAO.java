package com.campuslink.dao;

import com.campuslink.models.Application;
import com.campuslink.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {
    private Connection getConn() { return DBConnection.getInstance().getConnection(); }

    public boolean create(Application app) {
        String sql = "INSERT INTO applications (student_id, opportunity_type, opportunity_id, application_date, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        } catch (SQLException e) { System.err.println("ApplicationDAO.create: " + e.getMessage()); }
        return false;
    }

    public Application findById(int applicationId) {
        String sql = "SELECT * FROM applications WHERE application_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { System.err.println("ApplicationDAO.findById: " + e.getMessage()); }
        return null;
    }

    public List<Application> findByStudent(int studentId) {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT a.*, s.full_name as student_name FROM applications a " +
                     "JOIN students s ON a.student_id = s.student_id " +
                     "WHERE a.student_id = ? ORDER BY a.application_date DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Application app = mapRow(rs);
                    app.setStudentName(rs.getString("student_name"));
                    // Fetch opportunity title
                    enrichOpportunityTitle(app);
                    list.add(app);
                }
            }
        } catch (SQLException e) { System.err.println("ApplicationDAO.findByStudent: " + e.getMessage()); }
        return list;
    }

    public List<Application> findByOpportunity(String type, int opportunityId) {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT a.*, s.full_name as student_name FROM applications a " +
                     "JOIN students s ON a.student_id = s.student_id " +
                     "WHERE a.opportunity_type = ? AND a.opportunity_id = ? ORDER BY a.application_date DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setInt(2, opportunityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Application app = mapRow(rs);
                    app.setStudentName(rs.getString("student_name"));
                    enrichOpportunityTitle(app);
                    list.add(app);
                }
            }
        } catch (SQLException e) { System.err.println("ApplicationDAO.findByOpportunity: " + e.getMessage()); }
        return list;
    }

    public List<Application> findAll() {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT a.*, s.full_name as student_name FROM applications a " +
                     "JOIN students s ON a.student_id = s.student_id " +
                     "ORDER BY a.application_date DESC";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Application app = mapRow(rs);
                app.setStudentName(rs.getString("student_name"));
                enrichOpportunityTitle(app);
                list.add(app);
            }
        } catch (SQLException e) { System.err.println("ApplicationDAO.findAll: " + e.getMessage()); }
        return list;
    }

    public boolean update(Application app) {
        String sql = "UPDATE applications SET student_id=?, opportunity_type=?, opportunity_id=?, " +
                     "application_date=?, status=? WHERE application_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, app.getStudentId());
            ps.setString(2, app.getOpportunityType());
            ps.setInt(3, app.getOpportunityId());
            ps.setDate(4, Date.valueOf(app.getApplicationDate()));
            ps.setString(5, app.getStatus());
            ps.setInt(6, app.getApplicationId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("ApplicationDAO.update: " + e.getMessage()); }
        return false;
    }

    public boolean delete(int applicationId) {
        String sql = "DELETE FROM applications WHERE application_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("ApplicationDAO.delete: " + e.getMessage()); }
        return false;
    }

    public boolean updateStatus(int applicationId, String status) {
        String sql = "UPDATE applications SET status=? WHERE application_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, applicationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("ApplicationDAO.updateStatus: " + e.getMessage()); }
        return false;
    }

    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM applications WHERE status = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { System.err.println("ApplicationDAO.countByStatus: " + e.getMessage()); }
        return 0;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM applications";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("ApplicationDAO.count: " + e.getMessage()); }
        return 0;
    }

    private void enrichOpportunityTitle(Application app) {
        try {
            if ("JOB".equalsIgnoreCase(app.getOpportunityType())) {
                String sql = "SELECT j.title, e.company_name FROM jobs j LEFT JOIN employers e ON j.employer_id = e.employer_id WHERE j.job_id = ?";
                try (PreparedStatement ps = getConn().prepareStatement(sql)) {
                    ps.setInt(1, app.getOpportunityId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            app.setOpportunityTitle(rs.getString("title"));
                            app.setCompanyName(rs.getString("company_name"));
                        }
                    }
                }
            } else if ("INTERNSHIP".equalsIgnoreCase(app.getOpportunityType())) {
                String sql = "SELECT i.title, e.company_name FROM internships i LEFT JOIN employers e ON i.employer_id = e.employer_id WHERE i.internship_id = ?";
                try (PreparedStatement ps = getConn().prepareStatement(sql)) {
                    ps.setInt(1, app.getOpportunityId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            app.setOpportunityTitle(rs.getString("title"));
                            app.setCompanyName(rs.getString("company_name"));
                        }
                    }
                }
            }
        } catch (SQLException e) { System.err.println("ApplicationDAO.enrichOpportunityTitle: " + e.getMessage()); }
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
