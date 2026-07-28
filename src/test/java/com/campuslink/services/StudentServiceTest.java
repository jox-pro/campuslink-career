package com.campuslink.services;

import com.campuslink.dao.ApplicationDAO;
import com.campuslink.dao.StudentDAO;
import com.campuslink.models.Application;
import com.campuslink.models.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentDAO studentDAO;
    @Mock private ApplicationDAO applicationDAO;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentDAO, applicationDAO);
    }

    private Student validStudent() {
        Student s = new Student();
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
    void createProfile_withInvalidEmail_rejectsBeforeTouchingDao() {
        Student s = validStudent();
        s.setEmail("not-an-email");

        assertFalse(studentService.createProfile(s));
        verifyNoInteractions(studentDAO);
    }

    @Test
    void createProfile_withMissingFullName_rejectsBeforeTouchingDao() {
        Student s = validStudent();
        s.setFullName("");

        assertFalse(studentService.createProfile(s));
        verifyNoInteractions(studentDAO);
    }

    @Test
    void applyForOpportunity_firstApplication_succeeds() {
        when(applicationDAO.findByOpportunity("JOB", 42)).thenReturn(List.of());
        when(applicationDAO.create(any(Application.class))).thenReturn(true);

        assertTrue(studentService.applyForOpportunity(7, "JOB", 42));
        verify(applicationDAO).create(any(Application.class));
    }

    @Test
    void applyForOpportunity_duplicateApplication_rejectedWithoutInserting() {
        Application existing = new Application();
        existing.setStudentId(7);
        when(applicationDAO.findByOpportunity("JOB", 42)).thenReturn(List.of(existing));

        assertFalse(studentService.applyForOpportunity(7, "JOB", 42));
        verify(applicationDAO, never()).create(any());
    }

    @Test
    void applyForOpportunity_sameOpportunityDifferentStudent_succeeds() {
        Application existing = new Application();
        existing.setStudentId(99); // a different student already applied
        when(applicationDAO.findByOpportunity("JOB", 42)).thenReturn(List.of(existing));
        when(applicationDAO.create(any(Application.class))).thenReturn(true);

        assertTrue(studentService.applyForOpportunity(7, "JOB", 42));
    }
}
