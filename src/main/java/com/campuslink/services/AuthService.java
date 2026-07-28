package com.campuslink.services;

import com.campuslink.dao.AuditLogDAO;
import com.campuslink.dao.UserDAO;
import com.campuslink.models.User;
import com.campuslink.utils.PasswordUtil;
import com.campuslink.utils.SessionManager;

public class AuthService {
    private final UserDAO userDAO;
    private final AuditLogDAO auditLogDAO;

    public AuthService() {
        this(new UserDAO(), new AuditLogDAO());
    }

    public AuthService(UserDAO userDAO, AuditLogDAO auditLogDAO) {
        this.userDAO = userDAO;
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
        if (userDAO.findByUsername(username) != null) return null; // already exists
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(PasswordUtil.hashPassword(password));
        user.setRole(role);
        if (userDAO.create(user)) {
            auditLogDAO.insert("register", username.trim(), "success", "account-created");
            return user;
        }
        auditLogDAO.insert("register", username != null ? username.trim() : "", "failure", "account-creation-failed");
        return null;
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }
}
