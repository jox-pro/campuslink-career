package com.campuslink.services;

import com.campuslink.models.User;
import com.campuslink.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationServiceTest {

    @BeforeEach
    void setUp() {
        SessionManager.getInstance().logout();
    }

    @Test
    void checkRole_withCorrectRole_doesNotThrow() {
        User user = new User(1, "admin", "pass", "ADMIN");
        SessionManager.getInstance().setCurrentUser(user);
        
        assertDoesNotThrow(() -> AuthorizationService.checkRole("ADMIN"));
        assertDoesNotThrow(() -> AuthorizationService.checkRole("ADMIN", "STUDENT"));
    }

    @Test
    void checkRole_withWrongRole_throwsSecurityException() {
        User user = new User(1, "student", "pass", "STUDENT");
        SessionManager.getInstance().setCurrentUser(user);
        
        assertThrows(SecurityException.class, () -> AuthorizationService.checkRole("ADMIN"));
    }

    @Test
    void checkRole_whenNoUserLoggedIn_throwsSecurityException() {
        assertThrows(SecurityException.class, () -> AuthorizationService.checkRole("ADMIN"));
    }

    @Test
    void checkOwnership_asOwner_doesNotThrow() {
        User user = new User(10, "user", "pass", "STUDENT");
        SessionManager.getInstance().setCurrentUser(user);
        
        assertDoesNotThrow(() -> AuthorizationService.checkOwnership(10));
    }

    @Test
    void checkOwnership_asAdmin_doesNotThrow() {
        User user = new User(1, "admin", "pass", "ADMIN");
        SessionManager.getInstance().setCurrentUser(user);
        
        assertDoesNotThrow(() -> AuthorizationService.checkOwnership(99)); // Admin owns everything
    }

    @Test
    void checkOwnership_asWrongUser_throwsSecurityException() {
        User user = new User(10, "user", "pass", "STUDENT");
        SessionManager.getInstance().setCurrentUser(user);
        
        assertThrows(SecurityException.class, () -> AuthorizationService.checkOwnership(99));
    }
}
