package com.campuslink.services;

import com.campuslink.models.User;
import com.campuslink.utils.SessionManager;

public class AuthorizationService {

    public static void checkRole(String... allowedRoles) {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            throw new SecurityException("Unauthorized: No user logged in.");
        }
        boolean allowed = false;
        for (String role : allowedRoles) {
            if (user.getRole().equals(role)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new SecurityException("Forbidden: User does not have required role.");
        }
    }

    public static void checkOwnership(int ownerUserId) {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            throw new SecurityException("Unauthorized: No user logged in.");
        }
        if (!user.getRole().equals("ADMIN") && user.getId() != ownerUserId) {
            throw new SecurityException("Forbidden: User does not own this resource.");
        }
    }

    public static boolean isAdmin() {
        User user = SessionManager.getInstance().getCurrentUser();
        return user != null && "ADMIN".equals(user.getRole());
    }

    public static boolean isStudent() {
        User user = SessionManager.getInstance().getCurrentUser();
        return user != null && "STUDENT".equals(user.getRole());
    }

    public static boolean isEmployer() {
        User user = SessionManager.getInstance().getCurrentUser();
        return user != null && "EMPLOYER".equals(user.getRole());
    }
}
