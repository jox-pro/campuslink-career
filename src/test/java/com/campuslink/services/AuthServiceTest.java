package com.campuslink.services;

import com.campuslink.dao.AuditLogDAO;
import com.campuslink.dao.EmployerDAO;
import com.campuslink.dao.StudentDAO;
import com.campuslink.dao.UserDAO;
import com.campuslink.models.User;
import com.campuslink.utils.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Exercises AuthService entirely through mocked DAOs — no database, no classpath
 * db.properties required. This is the payoff of the Phase 1 DI refactor: these tests
 * run anywhere, including CI, without a MySQL instance.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserDAO userDAO;
    @Mock private StudentDAO studentDAO;
    @Mock private EmployerDAO employerDAO;
    @Mock private AuditLogDAO auditLogDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userDAO, studentDAO, employerDAO, auditLogDAO);
    }

    @Test
    void login_withCorrectPassword_setsUserAndLogsSuccess() {
        User stored = new User(1, "jstudent", PasswordUtil.hashPassword("correct-horse"), "STUDENT");
        stored.setMustChangePassword(false);
        when(userDAO.findByUsername("jstudent")).thenReturn(stored);

        User result = authService.login("jstudent", "correct-horse");

        assertNotNull(result);
        assertEquals("jstudent", result.getUsername());
        verify(auditLogDAO).insert(eq("login"), eq("jstudent"), eq("success"), anyString());
    }

    @Test
    void login_adminWithMustChangePassword_succeedsButFlagIsTrue() {
        User stored = new User(1, "admin", PasswordUtil.hashPassword("Admin@123"), "ADMIN", true);
        when(userDAO.findByUsername("admin")).thenReturn(stored);

        User result = authService.login("admin", "Admin@123");

        assertNotNull(result);
        assertTrue(result.isMustChangePassword());
    }

    @Test
    void changePassword_withCorrectCurrentPassword_updatesAndReturnsTrue() {
        User user = new User(1, "user1", PasswordUtil.hashPassword("old-pass"), "STUDENT", true);
        when(userDAO.update(any(User.class))).thenReturn(true);

        boolean success = authService.changePassword(user, "old-pass", "new-strong-pass");

        assertTrue(success);
        assertFalse(user.isMustChangePassword());
        assertTrue(PasswordUtil.checkPassword("new-strong-pass", user.getPassword()));
        verify(userDAO).update(user);
    }

    @Test
    void changePassword_withWrongCurrentPassword_returnsFalse() {
        User user = new User(1, "user1", PasswordUtil.hashPassword("old-pass"), "STUDENT", true);

        boolean success = authService.changePassword(user, "wrong-pass", "new-strong-pass");

        assertFalse(success);
        assertTrue(user.isMustChangePassword());
        verify(userDAO, never()).update(any());
    }

    @Test
    void login_withWrongPassword_returnsNullAndLogsFailure() {
        User stored = new User(1, "jstudent", PasswordUtil.hashPassword("correct-horse"), "STUDENT");
        when(userDAO.findByUsername("jstudent")).thenReturn(stored);

        User result = authService.login("jstudent", "wrong-password");

        assertNull(result);
        verify(auditLogDAO).insert(eq("login"), eq("jstudent"), eq("failure"), anyString());
    }

    @Test
    void login_withUnknownUsername_returnsNullWithoutThrowing() {
        when(userDAO.findByUsername("ghost")).thenReturn(null);

        User result = authService.login("ghost", "anything");

        assertNull(result);
        verify(auditLogDAO).insert(eq("login"), eq("ghost"), eq("failure"), anyString());
    }

    @Test
    void login_withBlankUsername_shortCircuitsBeforeTouchingDaosOrLogging() {
        User result = authService.login("   ", "anything");

        assertNull(result);
        verifyNoInteractions(userDAO);
        verifyNoInteractions(auditLogDAO);
    }

    @Test
    void register_withNewUsername_hashesPasswordAndPersists() {
        when(userDAO.findByUsername("newstudent")).thenReturn(null);
        when(userDAO.create(any(User.class))).thenReturn(true);

        User result = authService.register("newstudent", "PlaintextPw1", "STUDENT");

        assertNotNull(result);
        assertNotEquals("PlaintextPw1", result.getPassword(), "password must be hashed, never stored plain");
        assertTrue(PasswordUtil.checkPassword("PlaintextPw1", result.getPassword()));
        verify(auditLogDAO).insert(eq("register"), eq("newstudent"), eq("success"), anyString());
    }

    @Test
    void register_withExistingUsername_returnsNullWithoutCreating() {
        when(userDAO.findByUsername("existing")).thenReturn(new User(1, "existing", "hash", "STUDENT"));

        User result = authService.register("existing", "StrongPass1", "STUDENT");

        assertNull(result);
        verify(userDAO, never()).create(any());
    }

    @Test
    void register_withPasswordShorterThanEight_returnsNull() {
        User result = authService.register("newstudent", "short", "STUDENT");

        assertNull(result);
        verify(userDAO, never()).create(any());
        verify(auditLogDAO).insert(eq("register"), eq("newstudent"), eq("failure"), anyString());
    }
}
