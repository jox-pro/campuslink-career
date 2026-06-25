package com.campuslink.services;

import com.campuslink.dao.ApplicationDAO;
import com.campuslink.dao.StudentDAO;
import com.campuslink.models.Application;
import com.campuslink.models.Student;
import com.campuslink.utils.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

public class StudentService {
    private final StudentDAO studentDAO = new StudentDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();

    public boolean createProfile(Student student) {
        if (ValidationUtil.isNullOrEmpty(student.getFullName())) return false;
        if (!ValidationUtil.isValidEmail(student.getEmail())) return false;
        if (!ValidationUtil.isValidPhone(student.getPhone())) return false;
        return studentDAO.create(student);
    }

    public boolean updateProfile(Student student) {
        if (ValidationUtil.isNullOrEmpty(student.getFullName())) return false;
        if (!ValidationUtil.isValidEmail(student.getEmail())) return false;
        if (!ValidationUtil.isValidPhone(student.getPhone())) return false;
        return studentDAO.update(student);
    }

    public Student getProfile(int userId) {
        return studentDAO.findByUserId(userId);
    }

    public Student getById(int studentId) {
        return studentDAO.findById(studentId);
    }

    public List<Student> getAllStudents() {
        return studentDAO.findAll();
    }

    public List<Student> searchStudents(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return studentDAO.findAll();
        return studentDAO.search(keyword.trim());
    }

    public boolean deleteStudent(int studentId) {
        return studentDAO.delete(studentId);
    }

    public boolean applyForOpportunity(int studentId, String type, int opportunityId) {
        // Check for duplicate application
        List<Application> existing = applicationDAO.findByOpportunity(type, opportunityId);
        for (Application a : existing) {
            if (a.getStudentId() == studentId) return false; // already applied
        }
        Application app = new Application();
        app.setStudentId(studentId);
        app.setOpportunityType(type.toUpperCase());
        app.setOpportunityId(opportunityId);
        app.setApplicationDate(LocalDate.now());
        app.setStatus("PENDING");
        return applicationDAO.create(app);
    }

    public boolean withdrawApplication(int applicationId) {
        return applicationDAO.delete(applicationId);
    }

    public List<Application> getStudentApplications(int studentId) {
        return applicationDAO.findByStudent(studentId);
    }
}
