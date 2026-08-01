package com.campuslink.services;

import com.campuslink.dao.*;
import com.campuslink.models.Application;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

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
        stats.put("rejectedApplications", applicationDAO.countByStatus("REJECTED"));
        stats.put("reviewedApplications", applicationDAO.countByStatus("REVIEWED"));
        return stats;
    }

    public void exportApplicationsToCSV(File file, LocalDate start, LocalDate end) throws IOException {
        List<Application> apps = applicationDAO.findAll();
        // filter by date
        if (start != null) apps = apps.stream().filter(a -> !a.getApplicationDate().isBefore(start)).toList();
        if (end != null) apps = apps.stream().filter(a -> !a.getApplicationDate().isAfter(end)).toList();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("ID", "Student", "Opportunity", "Type", "Company", "Date", "Status")
                .build();

        try (FileWriter out = new FileWriter(file);
             CSVPrinter printer = new CSVPrinter(out, format)) {
            for (Application app : apps) {
                printer.printRecord(
                    app.getApplicationId(),
                    app.getStudentName(),
                    app.getOpportunityTitle(),
                    app.getOpportunityType(),
                    app.getCompanyName(),
                    app.getApplicationDate(),
                    app.getStatus()
                );
            }
        }
    }

    public boolean exportApplicationsToPDF(File file, LocalDate start, LocalDate end) {
        try {
            List<Application> apps = applicationDAO.findAll();
            if (start != null) apps = apps.stream().filter(a -> !a.getApplicationDate().isBefore(start)).toList();
            if (end != null) apps = apps.stream().filter(a -> !a.getApplicationDate().isAfter(end)).toList();

            if (apps.isEmpty()) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("No applications found for the selected period.");
                }
                return true;
            }

            StringBuilder content = new StringBuilder();
            content.append("CampusLink Career Applications Report\n");
            content.append("===================================\n");
            content.append("Generated: ").append(LocalDate.now()).append("\n\n");
            for (Application app : apps) {
                content.append(app.getApplicationId()).append(" | ")
                        .append(app.getStudentName()).append(" | ")
                        .append(app.getOpportunityTitle()).append(" | ")
                        .append(app.getOpportunityType()).append(" | ")
                        .append(app.getCompanyName()).append(" | ")
                        .append(app.getApplicationDate()).append(" | ")
                        .append(app.getStatus()).append("\n");
            }

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content.toString());
            }
            return true;
        } catch (IOException e) {
            logger.error("PDF export failed", e);
            return false;
        }
    }
}
