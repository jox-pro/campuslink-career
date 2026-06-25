package com.campuslink.dao;

import com.campuslink.models.Job;
import com.campuslink.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JobDAO {
    private Connection getConn() { return DBConnection.getInstance().getConnection(); }

    public boolean create(Job job) {
        String sql = "INSERT INTO jobs (title, description, requirements, deadline, employer_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, job.getTitle());
            ps.setString(2, job.getDescription());
            ps.setString(3, job.getRequirements());
            ps.setDate(4, job.getDeadline() != null ? Date.valueOf(job.getDeadline()) : null);
            ps.setInt(5, job.getEmployerId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) job.setJobId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) { System.err.println("JobDAO.create: " + e.getMessage()); }
        return false;
    }

    public Job findById(int jobId) {
        String sql = "SELECT j.*, e.company_name FROM jobs j " +
                     "LEFT JOIN employers e ON j.employer_id = e.employer_id WHERE j.job_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { System.err.println("JobDAO.findById: " + e.getMessage()); }
        return null;
    }

    public List<Job> findAll() {
        List<Job> list = new ArrayList<>();
        String sql = "SELECT j.*, e.company_name FROM jobs j " +
                     "LEFT JOIN employers e ON j.employer_id = e.employer_id ORDER BY j.job_id DESC";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("JobDAO.findAll: " + e.getMessage()); }
        return list;
    }

    public List<Job> findByEmployer(int employerId) {
        List<Job> list = new ArrayList<>();
        String sql = "SELECT j.*, e.company_name FROM jobs j " +
                     "LEFT JOIN employers e ON j.employer_id = e.employer_id WHERE j.employer_id = ? ORDER BY j.job_id DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, employerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("JobDAO.findByEmployer: " + e.getMessage()); }
        return list;
    }

    public List<Job> findActive() {
        List<Job> list = new ArrayList<>();
        String sql = "SELECT j.*, e.company_name FROM jobs j " +
                     "LEFT JOIN employers e ON j.employer_id = e.employer_id " +
                     "WHERE j.deadline >= CURDATE() ORDER BY j.deadline ASC";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("JobDAO.findActive: " + e.getMessage()); }
        return list;
    }

    public boolean update(Job job) {
        String sql = "UPDATE jobs SET title=?, description=?, requirements=?, deadline=?, employer_id=? WHERE job_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, job.getTitle());
            ps.setString(2, job.getDescription());
            ps.setString(3, job.getRequirements());
            ps.setDate(4, job.getDeadline() != null ? Date.valueOf(job.getDeadline()) : null);
            ps.setInt(5, job.getEmployerId());
            ps.setInt(6, job.getJobId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("JobDAO.update: " + e.getMessage()); }
        return false;
    }

    public boolean delete(int jobId) {
        String sql = "DELETE FROM jobs WHERE job_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, jobId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("JobDAO.delete: " + e.getMessage()); }
        return false;
    }

    public List<Job> search(String keyword) {
        List<Job> list = new ArrayList<>();
        String sql = "SELECT j.*, e.company_name FROM jobs j " +
                     "LEFT JOIN employers e ON j.employer_id = e.employer_id " +
                     "WHERE j.title LIKE ? OR j.description LIKE ? OR j.requirements LIKE ? ORDER BY j.job_id DESC";
        String pattern = "%" + keyword + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("JobDAO.search: " + e.getMessage()); }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM jobs";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("JobDAO.count: " + e.getMessage()); }
        return 0;
    }

    private Job mapRow(ResultSet rs) throws SQLException {
        Job j = new Job();
        j.setJobId(rs.getInt("job_id"));
        j.setTitle(rs.getString("title"));
        j.setDescription(rs.getString("description"));
        j.setRequirements(rs.getString("requirements"));
        Date d = rs.getDate("deadline");
        if (d != null) j.setDeadline(d.toLocalDate());
        j.setEmployerId(rs.getInt("employer_id"));
        try { j.setCompanyName(rs.getString("company_name")); } catch (SQLException ignored) {}
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) j.setCreatedAt(ts.toLocalDateTime());
        return j;
    }
}
