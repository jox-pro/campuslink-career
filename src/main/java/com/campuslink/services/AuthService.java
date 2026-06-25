package com.campuslink.services;

import com.campuslink.dao.UserDAO;
import com.campuslink.models.User;
import com.campuslink.utils.PasswordUtil;
import com.campuslink.utils.SessionManager;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null) return null;
        User user = userDAO.findByUsername(username.trim());
        if (user != null && PasswordUtil.checkPassword(password, user.getPassword())) {
            SessionManager.getInstance().setCurrentUser(user);
            return user;
        }
        return null;
    }

    public User register(String username, String password, String role) {
        if (userDAO.findByUsername(username) != null) return null; // already exists
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(PasswordUtil.hashPassword(password));
        user.setRole(role);
        if (userDAO.create(user)) return user;
        return null;
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }
}
