package com.campuslink.utils;

import com.campuslink.models.User;

public class SessionManager {
    private static volatile SessionManager instance;
    private volatile User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        SessionManager local = instance;
        if (local == null) {
            synchronized (SessionManager.class) {
                local = instance;
                if (local == null) {
                    local = new SessionManager();
                    instance = local;
                }
            }
        }
        return local;
    }

    public synchronized User getCurrentUser() { return currentUser; }
    public synchronized void setCurrentUser(User user) { this.currentUser = user; }
    public synchronized boolean isLoggedIn() { return currentUser != null; }
    public synchronized String getRole() { return currentUser != null ? currentUser.getRole() : null; }

    public synchronized void logout() {
        currentUser = null;
        DBConnection.getInstance().closeConnection();
    }
}
