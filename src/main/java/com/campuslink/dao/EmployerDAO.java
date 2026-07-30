package com.campuslink.dao;

import com.campuslink.models.Employer;
import com.campuslink.utils.AppDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployerDAO {
    private static final Logger logger = LoggerFactory.getLogger(EmployerDAO.class);
    private final DataSource dataSource;

    public EmployerDAO() {
        this(AppDataSource.getInstance());
    }

    public EmployerDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConn() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Database connection failed: " + e.getMessage(), e);
        }
    }

    public boolean create(Employer employer) {
        try (Connection conn = getConn()) {
            return create(employer, conn);
        } catch (SQLException e) {
            logger.error("EmployerDAO.create failed for employer {}: {}", employer.getCompanyName(), e.getMessage(), e);
            return false;
        }
    }

    public boolean create(Employer employer, Connection conn) throws SQLException {
        String sql = "INSERT INTO employers (user_id, company_name, contact_person, email, phone, address) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
        }
        return false;
    }

    public Employer findById(int employerId) {
        String sql = "SELECT * FROM employers WHERE employer_id = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            logger.error("EmployerDAO.findById failed for ID {}: {}", employerId, e.getMessage(), e);
        }
        return null;
    }

    public Employer findByUserId(int userId) {
        String sql = "SELECT * FROM employers WHERE user_id = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            logger.error("EmployerDAO.findByUserId failed for user ID {}: {}", userId, e.getMessage(), e);
        }
        return null;
    }

    public List<Employer> findAll() {
        List<Employer> list = new ArrayList<>();
        String sql = "SELECT * FROM employers ORDER BY employer_id";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            logger.error("EmployerDAO.findAll failed: {}", e.getMessage(), e);
        }
        return list;
    }

    public boolean update(Employer employer) {
        String sql = "UPDATE employers SET company_name=?, contact_person=?, email=?, phone=?, address=? " +
                     "WHERE employer_id=?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employer.getCompanyName());
            ps.setString(2, employer.getContactPerson());
            ps.setString(3, employer.getEmail());
            ps.setString(4, employer.getPhone());
            ps.setString(5, employer.getAddress());
            ps.setInt(6, employer.getEmployerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("EmployerDAO.update failed for employer ID {}: {}", employer.getEmployerId(), e.getMessage(), e);
        }
        return false;
    }

    public boolean delete(int employerId) {
        String sql = "DELETE FROM employers WHERE employer_id=?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("EmployerDAO.delete failed for employer ID {}: {}", employerId, e.getMessage(), e);
        }
        return false;
    }

    public List<Employer> search(String keyword) {
        List<Employer> list = new ArrayList<>();
        String sql = "SELECT * FROM employers WHERE company_name LIKE ? OR contact_person LIKE ? ORDER BY employer_id";
        String pattern = "%" + keyword + "%";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("EmployerDAO.search failed for keyword {}: {}", keyword, e.getMessage(), e);
        }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM employers";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("EmployerDAO.count failed: {}", e.getMessage(), e);
        }
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
