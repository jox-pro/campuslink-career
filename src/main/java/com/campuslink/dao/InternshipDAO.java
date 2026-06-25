package com.campuslink.dao;

import com.campuslink.models.Internship;
import com.campuslink.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InternshipDAO {
    private Connection getConn() { return DBConnection.getInstance().getConnection(); }

    public boolean create(Internship internship) {
        String sql = "INSERT INTO internships (title, description, requirements, deadline, employer_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, internship.getTitle());
            ps.setString(2, internship.getDescription());
            ps.setString(3, internship.getRequirements());
            ps.setDate(4, internship.getDeadline() != null ? Date.valueOf(internship.getDeadline()) : null);
            ps.setInt(5, internship.getEmployerId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) internship.setInternshipId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) { System.err.println("InternshipDAO.create: " + e.getMessage()); }
        return false;
    }

    public Internship findById(int internshipId) {
        String sql = "SELECT i.*, e.company_name FROM internships i " +
                     "LEFT JOIN employers e ON i.employer_id = e.employer_id WHERE i.internship_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, internshipId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { System.err.println("InternshipDAO.findById: " + e.getMessage()); }
        return null;
    }

    public List<Internship> findAll() {
        List<Internship> list = new ArrayList<>();
        String sql = "SELECT i.*, e.company_name FROM internships i " +
                     "LEFT JOIN employers e ON i.employer_id = e.employer_id ORDER BY i.internship_id DESC";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("InternshipDAO.findAll: " + e.getMessage()); }
        return list;
    }

    public List<Internship> findByEmployer(int employerId) {
        List<Internship> list = new ArrayList<>();
        String sql = "SELECT i.*, e.company_name FROM internships i " +
                     "LEFT JOIN employers e ON i.employer_id = e.employer_id WHERE i.employer_id = ? ORDER BY i.internship_id DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, employerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("InternshipDAO.findByEmployer: " + e.getMessage()); }
        return list;
    }

    public List<Internship> findActive() {
        List<Internship> list = new ArrayList<>();
        String sql = "SELECT i.*, e.company_name FROM internships i " +
                     "LEFT JOIN employers e ON i.employer_id = e.employer_id " +
                     "WHERE i.deadline >= CURDATE() ORDER BY i.deadline ASC";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("InternshipDAO.findActive: " + e.getMessage()); }
        return list;
    }

    public boolean update(Internship internship) {
        String sql = "UPDATE internships SET title=?, description=?, requirements=?, deadline=?, employer_id=? WHERE internship_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, internship.getTitle());
            ps.setString(2, internship.getDescription());
            ps.setString(3, internship.getRequirements());
            ps.setDate(4, internship.getDeadline() != null ? Date.valueOf(internship.getDeadline()) : null);
            ps.setInt(5, internship.getEmployerId());
            ps.setInt(6, internship.getInternshipId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("InternshipDAO.update: " + e.getMessage()); }
        return false;
    }

    public boolean delete(int internshipId) {
        String sql = "DELETE FROM internships WHERE internship_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, internshipId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("InternshipDAO.delete: " + e.getMessage()); }
        return false;
    }

    public List<Internship> search(String keyword) {
        List<Internship> list = new ArrayList<>();
        String sql = "SELECT i.*, e.company_name FROM internships i " +
                     "LEFT JOIN employers e ON i.employer_id = e.employer_id " +
                     "WHERE i.title LIKE ? OR i.description LIKE ? OR i.requirements LIKE ? ORDER BY i.internship_id DESC";
        String pattern = "%" + keyword + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("InternshipDAO.search: " + e.getMessage()); }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM internships";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("InternshipDAO.count: " + e.getMessage()); }
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
