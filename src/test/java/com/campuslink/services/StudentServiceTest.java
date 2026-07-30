package com.campuslink.services;

import com.campuslink.dao.ApplicationDAO;
import com.campuslink.dao.AuditLogDAO;
import com.campuslink.dao.JobDAO;
import com.campuslink.dao.InternshipDAO;
import com.campuslink.dao.StudentDAO;
import com.campuslink.models.Application;
import com.campuslink.models.Job;
import com.campuslink.models.Student;
import com.campuslink.models.User;
import com.campuslink.utils.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentDAO studentDAO;
    @Mock private ApplicationDAO applicationDAO;
    @Mock private JobDAO jobDAO;
    @Mock private InternshipDAO internshipDAO;
    @Mock private AuditLogDAO auditLogDAO;

    private StudentService studentService;
    private User currentUser;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentDAO, applicationDAO, jobDAO, internshipDAO, auditLogDAO);
        currentUser = new User(101, "testuser", "pass", "STUDENT");
        SessionManager.getInstance().setCurrentUser(currentUser);
    }

    private Student validStudent() {
        Student s = new Student();
        s.setUserId(101);
        s.setFullName("Jane Doe");
        s.setEmail("jane.doe@example.ac.tz");
        s.setPhone("+255700000000");
        s.setCourse("BSc Computer Science");
        return s;
    }

    @Test
    void createProfile_withValidData_delegatesToDao() {
        when(studentDAO.create(any(Student.class))).thenReturn(true);

        assertTrue(studentService.createProfile(validStudent()));
        verify(studentDAO).create(any(Student.class));
    }

    @Test
    void updateProfile_asOwner_succeeds() {
        Student s = validStudent();
        s.setStudentId(1);
        when(studentDAO.update(any(Student.class))).thenReturn(true);

        assertTrue(studentService.updateProfile(s));
        verify(studentDAO).update(any(Student.class));
    }

    @Test
    void updateProfile_asWrongUser_throwsSecurityException() {
        Student s = validStudent();
        s.setUserId(999); // different from currentUser.getId() (101)

        assertThrows(SecurityException.class, () -> studentService.updateProfile(s));
        verifyNoInteractions(studentDAO);
    }

    @Test
    void applyForOpportunity_duplicateApplication_rejectedWithoutInserting() {
        Student s = new Student();
        s.setStudentId(7);
        s.setUserId(101);
        when(studentDAO.findById(7)).thenReturn(s);
        
        Job j = new Job();
        j.setJobId(42);
        j.setDeadline(LocalDate.now().plusDays(1));
        when(jobDAO.findById(42)).thenReturn(j);

        Application existing = new Application();
        existing.setStudentId(7);
        existing.setOpportunityType("JOB");
        existing.setOpportunityId(42);
        when(applicationDAO.findByStudent(7)).thenReturn(List.of(existing));

        assertFalse(studentService.applyForOpportunity(7, "JOB", 42));
        verify(applicationDAO, never()).create(any());
    }

    @Test
    void applyForOpportunity_expiredListing_rejected() {
        Student s = new Student();
        s.setStudentId(7);
        s.setUserId(101);
        when(studentDAO.findById(7)).thenReturn(s);
        
        Job j = new Job();
        j.setJobId(42);
        j.setDeadline(LocalDate.now().minusDays(1)); // Expired
        when(jobDAO.findById(42)).thenReturn(j);

        assertFalse(studentService.applyForOpportunity(7, "JOB", 42));
        verify(applicationDAO, never()).create(any());
    }

    @Test
    void applyForOpportunity_sameOpportunityDifferentStudent_succeeds() {
        Student s = new Student();
        s.setStudentId(7);
        s.setUserId(101);
        when(studentDAO.findById(7)).thenReturn(s);
        
        Job j = new Job();
        j.setJobId(42);
        j.setDeadline(LocalDate.now().plusDays(1));
        when(jobDAO.findById(42)).thenReturn(j);

        Application existing = new Application();
        existing.setStudentId(99); // a different student already applied
        existing.setOpportunityType("JOB");
        existing.setOpportunityId(42);
        when(applicationDAO.findByStudent(7)).thenReturn(List.of()); // No applications for student 7
        when(applicationDAO.create(any(Application.class))).thenReturn(true);

        assertTrue(studentService.applyForOpportunity(7, "JOB", 42));
    }
}
