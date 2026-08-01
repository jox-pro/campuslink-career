package com.campuslink.services;

import com.campuslink.dao.ApplicationDAO;
import com.campuslink.dao.AuditLogDAO;
import com.campuslink.dao.JobDAO;
import com.campuslink.dao.InternshipDAO;
import com.campuslink.dao.StudentDAO;
import com.campuslink.models.Application;
import com.campuslink.models.Student;
import com.campuslink.utils.SessionManager;
import com.campuslink.utils.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

public class StudentService {
    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);
    private final StudentDAO studentDAO;
    private final ApplicationDAO applicationDAO;
    private final JobDAO jobDAO;
    private final InternshipDAO internshipDAO;
    private final AuditLogDAO auditLogDAO;

    public StudentService() {
        this(new StudentDAO(), new ApplicationDAO(), new JobDAO(), new InternshipDAO(), new AuditLogDAO());
    }

    public StudentService(StudentDAO studentDAO, ApplicationDAO applicationDAO, JobDAO jobDAO, InternshipDAO internshipDAO, AuditLogDAO auditLogDAO) {
        this.studentDAO = studentDAO;
        this.applicationDAO = applicationDAO;
        this.jobDAO = jobDAO;
        this.internshipDAO = internshipDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public boolean createProfile(Student student) {
        if (ValidationUtil.isNullOrEmpty(student.getFullName())) return false;
        if (!ValidationUtil.isValidEmail(student.getEmail())) return false;
        if (!ValidationUtil.isValidPhone(student.getPhone())) return false;
        boolean created = studentDAO.create(student);
        if (created) {
            auditLogDAO.insert("profile-create", SessionManager.getInstance().getCurrentUser().getUsername(), "success", "student profile created");
        }
        return created;
    }

    public boolean updateProfile(Student student) {
        AuthorizationService.checkOwnership(student.getUserId());
        if (ValidationUtil.isNullOrEmpty(student.getFullName())) return false;
        if (!ValidationUtil.isValidEmail(student.getEmail())) return false;
        if (!ValidationUtil.isValidPhone(student.getPhone())) return false;
        boolean updated = studentDAO.update(student);
        if (updated) {
            auditLogDAO.insert("profile-update", SessionManager.getInstance().getCurrentUser().getUsername(), "success", "student profile updated");
        }
        return updated;
    }

    public Student getProfile(int userId) {
        AuthorizationService.checkOwnership(userId);
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
        Student student = studentDAO.findById(studentId);
        if (student == null) return false;
        AuthorizationService.checkOwnership(student.getUserId());

        // Check if opportunity exists and is not expired
        LocalDate deadline = null;
        if (type.equalsIgnoreCase("JOB")) {
            com.campuslink.models.Job job = jobDAO.findById(opportunityId);
            if (job == null) return false;
            deadline = job.getDeadline();
        } else if (type.equalsIgnoreCase("INTERNSHIP")) {
            com.campuslink.models.Internship internship = internshipDAO.findById(opportunityId);
            if (internship == null) return false;
            deadline = internship.getDeadline();
        } else {
            return false;
        }

        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            return false; // Expired
        }

        // Check for duplicate application
        List<Application> existing = applicationDAO.findByStudent(studentId);
        for (Application a : existing) {
            if (a.getOpportunityType().equalsIgnoreCase(type) && a.getOpportunityId() == opportunityId) {
                return false; // already applied
            }
        }
        Application app = new Application();
        app.setStudentId(studentId);
        app.setOpportunityType(type.toUpperCase());
        app.setOpportunityId(opportunityId);
        app.setApplicationDate(LocalDate.now());
        app.setStatus("PENDING");
        boolean created = applicationDAO.create(app);
        if (created) {
            auditLogDAO.insert("apply", SessionManager.getInstance().getCurrentUser().getUsername(), "success", type + " ID: " + opportunityId);
        }
        return created;
    }

    public boolean withdrawApplication(int applicationId) {
        Application app = applicationDAO.findById(applicationId);
        if (app == null) return false;
        Student student = studentDAO.findById(app.getStudentId());
        if (student == null) return false;
        AuthorizationService.checkOwnership(student.getUserId());

        boolean deleted = applicationDAO.delete(applicationId);
        if (deleted) {
            auditLogDAO.insert("withdraw", SessionManager.getInstance().getCurrentUser().getUsername(), "success", "Application ID: " + applicationId);
        }
        return deleted;
    }

    public List<Application> getStudentApplications(int studentId) {
        return applicationDAO.findByStudent(studentId);
    }

    public boolean uploadCV(int studentId, java.io.File file) {
        Student student = studentDAO.findById(studentId);
        if (student == null || file == null || !file.isFile()) return false;
        AuthorizationService.checkOwnership(student.getUserId());

        // Validation: max 5MB, PDF only
        if (file.length() > 5 * 1024 * 1024) return false;
        if (!file.getName().toLowerCase().endsWith(".pdf")) return false;

        Path storageDir = Path.of(System.getProperty("user.home"), ".campuslink-career", "storage", "cvs").toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageDir);
        } catch (java.io.IOException e) {
            logger.error("Unable to create CV storage directory", e);
            return false;
        }

        String fileName = "cv_" + studentId + "_" + System.currentTimeMillis() + ".pdf";
        Path dest = storageDir.resolve(fileName);

        try {
            Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            // Delete old CV if exists
            if (student.getCvPath() != null) {
                try {
                    Path oldFile = Path.of(student.getCvPath());
                    Files.deleteIfExists(oldFile);
                } catch (java.lang.Exception ignored) {
                    // ignore cleanup errors
                }
            }

            student.setCvPath(dest.toString());
            boolean updated = studentDAO.update(student);
            if (updated) {
                auditLogDAO.insert("cv-upload", SessionManager.getInstance().getCurrentUser().getUsername(), "success", "CV uploaded: " + fileName);
            }
            return updated;
        } catch (java.io.IOException e) {
            logger.error("CV upload failed for student ID {}: {}", studentId, e.getMessage(), e);
            return false;
        }
    }
}
