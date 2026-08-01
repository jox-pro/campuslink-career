package com.campuslink.services;

import com.campuslink.dao.AuditLogDAO;
import com.campuslink.dao.EmployerDAO;
import com.campuslink.dao.StudentDAO;
import com.campuslink.dao.UserDAO;
import com.campuslink.models.Employer;
import com.campuslink.models.Student;
import com.campuslink.models.User;
import com.campuslink.utils.AppDataSource;
import com.campuslink.utils.PasswordUtil;
import com.campuslink.utils.SessionManager;
import com.campuslink.utils.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDAO userDAO;
    private final StudentDAO studentDAO;
    private final EmployerDAO employerDAO;
    private final AuditLogDAO auditLogDAO;

    public AuthService() {
        this(new UserDAO(), new StudentDAO(), new EmployerDAO(), new AuditLogDAO());
    }

    public AuthService(UserDAO userDAO, StudentDAO studentDAO, EmployerDAO employerDAO, AuditLogDAO auditLogDAO) {
        this.userDAO = userDAO;
        this.studentDAO = studentDAO;
        this.employerDAO = employerDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null) return null;
        String normalizedUsername = username.trim();
        User user = userDAO.findByUsername(normalizedUsername);
        if (user != null && PasswordUtil.checkPassword(password, user.getPassword())) {
            SessionManager.getInstance().setCurrentUser(user);
            auditLogDAO.insert("login", normalizedUsername, "success", "authenticated");
            return user;
        }
        auditLogDAO.insert("login", normalizedUsername, "failure", "invalid credentials");
        return null;
    }

    public User register(String username, String password, String role) {
        if (username == null || username.trim().isEmpty() || password == null || password.isBlank()) {
            return null;
        }
        if (!ValidationUtil.isStrongPassword(password)) {
            auditLogDAO.insert("register", username.trim(), "failure", "password-too-weak");
            return null;
        }
        if (!role.equals("ADMIN") && !role.equals("STUDENT") && !role.equals("EMPLOYER")) {
            return null;
        }
        String normalizedUsername = username.trim();
        if (userDAO.findByUsername(normalizedUsername) != null) return null; // already exists
        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(PasswordUtil.hashPassword(password));
        user.setRole(role);
        user.setMustChangePassword(role.equals("ADMIN")); // Admins created via app must change password
        if (userDAO.create(user)) {
            SessionManager.getInstance().setCurrentUser(user);
            auditLogDAO.insert("register", normalizedUsername, "success", "account-created");
            return user;
        }
        auditLogDAO.insert("register", normalizedUsername, "failure", "account-creation-failed");
        return null;
    }

    public boolean registerStudent(User user, Student student) {
        Connection conn = null;
        try {
            conn = AppDataSource.getInstance().getConnection();
            conn.setAutoCommit(false);

            user.setRole("STUDENT");
            user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
            user.setMustChangePassword(false);
            if (!userDAO.create(user, conn)) {
                conn.rollback();
                return false;
            }

            student.setUserId(user.getId());
            if (!studentDAO.create(student, conn)) {
                conn.rollback();
                return false;
            }

            conn.commit();
            SessionManager.getInstance().setCurrentUser(user);
            auditLogDAO.insert("register-student", user.getUsername(), "success", "account and profile created");
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { logger.error("Rollback failed", ex); }
            logger.error("Registration student transaction failed", e);
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) { logger.error("Connection close failed", e); }
        }
    }

    public boolean registerEmployer(User user, Employer employer) {
        Connection conn = null;
        try {
            conn = AppDataSource.getInstance().getConnection();
            conn.setAutoCommit(false);

            user.setRole("EMPLOYER");
            user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
            user.setMustChangePassword(false);
            if (!userDAO.create(user, conn)) {
                conn.rollback();
                return false;
            }

            employer.setUserId(user.getId());
            if (!employerDAO.create(employer, conn)) {
                conn.rollback();
                return false;
            }

            conn.commit();
            SessionManager.getInstance().setCurrentUser(user);
            auditLogDAO.insert("register-employer", user.getUsername(), "success", "account and profile created");
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { logger.error("Rollback failed", ex); }
            logger.error("Registration employer transaction failed", e);
            return false;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) { logger.error("Connection close failed", e); }
        }
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }

    public boolean changePassword(User user, String currentPassword, String newPassword) {
        if (user == null || currentPassword == null || newPassword == null || newPassword.isBlank()) return false;
        if (!PasswordUtil.checkPassword(currentPassword, user.getPassword())) return false;

        user.setPassword(PasswordUtil.hashPassword(newPassword));
        user.setMustChangePassword(false);
        if (userDAO.update(user)) {
            auditLogDAO.insert("password-change", user.getUsername(), "success", "password updated");
            return true;
        }
        return false;
    }
}
