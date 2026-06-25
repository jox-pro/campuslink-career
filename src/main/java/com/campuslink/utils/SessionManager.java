package com.campuslink.utils;

import com.campuslink.models.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User user) { this.currentUser = user; }
    public boolean isLoggedIn() { return currentUser != null; }
    public String getRole() { return currentUser != null ? currentUser.getRole() : null; }

    public void logout() {
        currentUser = null;
        DBConnection.getInstance().closeConnection();
    }
}
