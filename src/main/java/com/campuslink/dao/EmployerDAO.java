package com.campuslink.dao;

import com.campuslink.models.Employer;
import com.campuslink.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployerDAO {
    private Connection getConn() { return DBConnection.getInstance().getConnection(); }

    public boolean create(Employer employer) {
        String sql = "INSERT INTO employers (user_id, company_name, contact_person, email, phone, address) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, employer.getUserId());
            ps.setString(2, employer.getCompanyName());
            ps.setString(3, employer.getContactPerson());
            ps.setString(4, employer.getEmail());
            ps.setString(5, employer.getPhone());
            ps.setString(6, employer.getAddress());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) employer.setEmployerId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) { System.err.println("EmployerDAO.create: " + e.getMessage()); }
        return false;
    }

    public Employer findById(int employerId) {
        String sql = "SELECT * FROM employers WHERE employer_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, employerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { System.err.println("EmployerDAO.findById: " + e.getMessage()); }
        return null;
    }

    public Employer findByUserId(int userId) {
        String sql = "SELECT * FROM employers WHERE user_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { System.err.println("EmployerDAO.findByUserId: " + e.getMessage()); }
        return null;
    }

    public List<Employer> findAll() {
        List<Employer> list = new ArrayList<>();
        String sql = "SELECT * FROM employers ORDER BY employer_id";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("EmployerDAO.findAll: " + e.getMessage()); }
        return list;
    }

    public boolean update(Employer employer) {
        String sql = "UPDATE employers SET company_name=?, contact_person=?, email=?, phone=?, address=? " +
                     "WHERE employer_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, employer.getCompanyName());
            ps.setString(2, employer.getContactPerson());
            ps.setString(3, employer.getEmail());
            ps.setString(4, employer.getPhone());
            ps.setString(5, employer.getAddress());
            ps.setInt(6, employer.getEmployerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("EmployerDAO.update: " + e.getMessage()); }
        return false;
    }

    public boolean delete(int employerId) {
        String sql = "DELETE FROM employers WHERE employer_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, employerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("EmployerDAO.delete: " + e.getMessage()); }
        return false;
    }

    public List<Employer> search(String keyword) {
        List<Employer> list = new ArrayList<>();
        String sql = "SELECT * FROM employers WHERE company_name LIKE ? OR contact_person LIKE ? ORDER BY employer_id";
        String pattern = "%" + keyword + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("EmployerDAO.search: " + e.getMessage()); }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM employers";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("EmployerDAO.count: " + e.getMessage()); }
        return 0;
    }

    private Employer mapRow(ResultSet rs) throws SQLException {
        Employer e = new Employer();
        e.setEmployerId(rs.getInt("employer_id"));
        e.setUserId(rs.getInt("user_id"));
        e.setCompanyName(rs.getString("company_name"));
        e.setContactPerson(rs.getString("contact_person"));
        e.setEmail(rs.getString("email"));
        e.setPhone(rs.getString("phone"));
        e.setAddress(rs.getString("address"));
        return e;
    }
}
