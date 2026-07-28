package com.campuslink.services;

import com.campuslink.dao.*;

import java.util.HashMap;
import java.util.Map;

public class ReportService {
    private final StudentDAO studentDAO;
    private final EmployerDAO employerDAO;
    private final JobDAO jobDAO;
    private final InternshipDAO internshipDAO;
    private final ApplicationDAO applicationDAO;

    public ReportService() {
        this(new StudentDAO(), new EmployerDAO(), new JobDAO(), new InternshipDAO(), new ApplicationDAO());
    }

    public ReportService(StudentDAO studentDAO, EmployerDAO employerDAO, JobDAO jobDAO,
                          InternshipDAO internshipDAO, ApplicationDAO applicationDAO) {
        this.studentDAO = studentDAO;
        this.employerDAO = employerDAO;
        this.jobDAO = jobDAO;
        this.internshipDAO = internshipDAO;
        this.applicationDAO = applicationDAO;
    }

    public Map<String, Integer> getDashboardStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalStudents", studentDAO.count());
        stats.put("totalEmployers", employerDAO.count());
        stats.put("totalJobs", jobDAO.count());
        stats.put("totalInternships", internshipDAO.count());
        stats.put("totalApplications", applicationDAO.count());
        stats.put("pendingApplications", applicationDAO.countByStatus("PENDING"));
        stats.put("acceptedApplications", applicationDAO.countByStatus("ACCEPTED"));
        return stats;
    }
}
