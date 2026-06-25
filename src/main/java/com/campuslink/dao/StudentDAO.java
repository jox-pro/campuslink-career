package com.campuslink.dao;

import com.campuslink.models.Student;
import com.campuslink.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    private Connection getConn() { return DBConnection.getInstance().getConnection(); }

    public boolean create(Student student) {
        String sql = "INSERT INTO students (user_id, full_name, email, phone, course, year_of_study, skills, cv_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, student.getUserId());
            ps.setString(2, student.getFullName());
            ps.setString(3, student.getEmail());
            ps.setString(4, student.getPhone());
            ps.setString(5, student.getCourse());
            ps.setInt(6, student.getYearOfStudy());
            ps.setString(7, student.getSkills());
            ps.setString(8, student.getCvPath());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) student.setStudentId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) { System.err.println("StudentDAO.create: " + e.getMessage()); }
        return false;
    }

    public Student findById(int studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { System.err.println("StudentDAO.findById: " + e.getMessage()); }
        return null;
    }

    public Student findByUserId(int userId) {
        String sql = "SELECT * FROM students WHERE user_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { System.err.println("StudentDAO.findByUserId: " + e.getMessage()); }
        return null;
    }

    public List<Student> findAll() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY student_id";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.err.println("StudentDAO.findAll: " + e.getMessage()); }
        return list;
    }

    public boolean update(Student student) {
        String sql = "UPDATE students SET full_name=?, email=?, phone=?, course=?, " +
                     "year_of_study=?, skills=?, cv_path=? WHERE student_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, student.getFullName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getPhone());
            ps.setString(4, student.getCourse());
            ps.setInt(5, student.getYearOfStudy());
            ps.setString(6, student.getSkills());
            ps.setString(7, student.getCvPath());
            ps.setInt(8, student.getStudentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("StudentDAO.update: " + e.getMessage()); }
        return false;
    }

    public boolean delete(int studentId) {
        String sql = "DELETE FROM students WHERE student_id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("StudentDAO.delete: " + e.getMessage()); }
        return false;
    }

    public List<Student> search(String keyword) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students WHERE full_name LIKE ? OR course LIKE ? ORDER BY student_id";
        String pattern = "%" + keyword + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) { System.err.println("StudentDAO.search: " + e.getMessage()); }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("StudentDAO.count: " + e.getMessage()); }
        return 0;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("student_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setFullName(rs.getString("full_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setCourse(rs.getString("course"));
        s.setYearOfStudy(rs.getInt("year_of_study"));
        s.setSkills(rs.getString("skills"));
        s.setCvPath(rs.getString("cv_path"));
        return s;
    }
}
