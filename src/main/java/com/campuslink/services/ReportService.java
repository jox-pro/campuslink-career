package com.campuslink.services;

import com.campuslink.dao.*;

import java.util.HashMap;
import java.util.Map;

public class ReportService {
    private final StudentDAO studentDAO = new StudentDAO();
    private final EmployerDAO employerDAO = new EmployerDAO();
    private final JobDAO jobDAO = new JobDAO();
    private final InternshipDAO internshipDAO = new InternshipDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();

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
