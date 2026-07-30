package com.campuslink.services;

import com.campuslink.dao.ApplicationDAO;
import com.campuslink.dao.AuditLogDAO;
import com.campuslink.dao.EmployerDAO;
import com.campuslink.dao.InternshipDAO;
import com.campuslink.dao.JobDAO;
import com.campuslink.models.Application;
import com.campuslink.models.Employer;
import com.campuslink.models.Internship;
import com.campuslink.models.Job;
import com.campuslink.utils.SessionManager;
import com.campuslink.utils.ValidationUtil;

import java.util.List;

public class EmployerService {
    private final EmployerDAO employerDAO;
    private final JobDAO jobDAO;
    private final InternshipDAO internshipDAO;
    private final ApplicationDAO applicationDAO;
    private final AuditLogDAO auditLogDAO;

    public EmployerService() {
        this(new EmployerDAO(), new JobDAO(), new InternshipDAO(), new ApplicationDAO(), new AuditLogDAO());
    }

    public EmployerService(EmployerDAO employerDAO, JobDAO jobDAO, InternshipDAO internshipDAO, ApplicationDAO applicationDAO, AuditLogDAO auditLogDAO) {
        this.employerDAO = employerDAO;
        this.jobDAO = jobDAO;
        this.internshipDAO = internshipDAO;
        this.applicationDAO = applicationDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public Employer getProfile(int userId) {
        AuthorizationService.checkOwnership(userId);
        return employerDAO.findByUserId(userId);
    }

    public Employer getById(int employerId) {
        return employerDAO.findById(employerId);
    }

    public boolean updateProfile(Employer employer) {
        AuthorizationService.checkOwnership(employer.getUserId());
        if (ValidationUtil.isNullOrEmpty(employer.getCompanyName())) return false;
        if (!ValidationUtil.isValidEmail(employer.getEmail())) return false;
        boolean updated = employerDAO.update(employer);
        if (updated) {
            auditLogDAO.insert("profile-update", SessionManager.getInstance().getCurrentUser().getUsername(), "success", "employer profile updated");
        }
        return updated;
    }

    public List<Employer> getAllEmployers() {
        return employerDAO.findAll();
    }

    public List<Employer> searchEmployers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return employerDAO.findAll();
        return employerDAO.search(keyword.trim());
    }

    public boolean deleteEmployer(int employerId) {
        return employerDAO.delete(employerId);
    }

    public boolean createEmployerProfile(Employer employer) {
        if (ValidationUtil.isNullOrEmpty(employer.getCompanyName())) return false;
        if (!ValidationUtil.isValidEmail(employer.getEmail())) return false;
        return employerDAO.create(employer);
    }

    public boolean postJob(Job job) {
        if (ValidationUtil.isNullOrEmpty(job.getTitle())) return false;
        if (job.getDeadline() != null && !ValidationUtil.isValidDeadline(job.getDeadline())) return false;
        return jobDAO.create(job);
    }

    public boolean updateJob(Job job) {
        Job existing = jobDAO.findById(job.getJobId());
        if (existing == null) return false;
        Employer employer = employerDAO.findById(existing.getEmployerId());
        if (employer == null) return false;
        AuthorizationService.checkOwnership(employer.getUserId());

        if (ValidationUtil.isNullOrEmpty(job.getTitle())) return false;
        boolean updated = jobDAO.update(job);
        if (updated) {
            auditLogDAO.insert("job-update", SessionManager.getInstance().getCurrentUser().getUsername(), "success", "Job ID: " + job.getJobId());
        }
        return updated;
    }

    public boolean deleteJob(int jobId) {
        Job existing = jobDAO.findById(jobId);
        if (existing == null) return false;
        Employer employer = employerDAO.findById(existing.getEmployerId());
        if (employer == null) return false;
        AuthorizationService.checkOwnership(employer.getUserId());

        boolean deleted = jobDAO.delete(jobId);
        if (deleted) {
            auditLogDAO.insert("job-delete", SessionManager.getInstance().getCurrentUser().getUsername(), "success", "Job ID: " + jobId);
        }
        return deleted;
    }

    public boolean postInternship(Internship internship) {
        if (ValidationUtil.isNullOrEmpty(internship.getTitle())) return false;
        if (internship.getDeadline() != null && !ValidationUtil.isValidDeadline(internship.getDeadline())) return false;
        boolean created = internshipDAO.create(internship);
        if (created) {
            auditLogDAO.insert("internship-post", SessionManager.getInstance().getCurrentUser().getUsername(), "success", "Internship ID: " + internship.getInternshipId());
        }
        return created;
    }

    public boolean updateInternship(Internship internship) {
        Internship existing = internshipDAO.findById(internship.getInternshipId());
        if (existing == null) return false;
        Employer employer = employerDAO.findById(existing.getEmployerId());
        if (employer == null) return false;
        AuthorizationService.checkOwnership(employer.getUserId());

        if (ValidationUtil.isNullOrEmpty(internship.getTitle())) return false;
        boolean updated = internshipDAO.update(internship);
        if (updated) {
            auditLogDAO.insert("internship-update", SessionManager.getInstance().getCurrentUser().getUsername(), "success", "Internship ID: " + internship.getInternshipId());
        }
        return updated;
    }

    public boolean deleteInternship(int internshipId) {
        Internship existing = internshipDAO.findById(internshipId);
        if (existing == null) return false;
        Employer employer = employerDAO.findById(existing.getEmployerId());
        if (employer == null) return false;
        AuthorizationService.checkOwnership(employer.getUserId());

        boolean deleted = internshipDAO.delete(internshipId);
        if (deleted) {
            auditLogDAO.insert("internship-delete", SessionManager.getInstance().getCurrentUser().getUsername(), "success", "Internship ID: " + internshipId);
        }
        return deleted;
    }

    public List<Application> getApplicants(String type, int opportunityId) {
        // Ownership check for the listing
        if (type.equalsIgnoreCase("JOB")) {
            Job job = jobDAO.findById(opportunityId);
            if (job != null) {
                Employer employer = employerDAO.findById(job.getEmployerId());
                if (employer != null) AuthorizationService.checkOwnership(employer.getUserId());
            }
        } else {
            Internship internship = internshipDAO.findById(opportunityId);
            if (internship != null) {
                Employer employer = employerDAO.findById(internship.getEmployerId());
                if (employer != null) AuthorizationService.checkOwnership(employer.getUserId());
            }
        }
        return applicationDAO.findByOpportunity(type, opportunityId);
    }

    public boolean updateApplicationStatus(int appId, String status) {
        Application app = applicationDAO.findById(appId);
        if (app == null) return false;

        // Check ownership of the listing
        if (app.getOpportunityType().equalsIgnoreCase("JOB")) {
            Job job = jobDAO.findById(app.getOpportunityId());
            if (job == null) return false;
            Employer employer = employerDAO.findById(job.getEmployerId());
            if (employer == null) return false;
            AuthorizationService.checkOwnership(employer.getUserId());
        } else {
            Internship internship = internshipDAO.findById(app.getOpportunityId());
            if (internship == null) return false;
            Employer employer = employerDAO.findById(internship.getEmployerId());
            if (employer == null) return false;
            AuthorizationService.checkOwnership(employer.getUserId());
        }

        boolean updated = applicationDAO.updateStatus(appId, status);
        if (updated) {
            auditLogDAO.insert("application-status", SessionManager.getInstance().getCurrentUser().getUsername(), "success", "App ID: " + appId + " to " + status);
        }
        return updated;
    }

    public List<Job> getEmployerJobs(int employerId) {
        return jobDAO.findByEmployer(employerId);
    }

    public List<Internship> getEmployerInternships(int employerId) {
        return internshipDAO.findByEmployer(employerId);
    }

    public List<Job> getAllJobs() {
        return jobDAO.findAll();
    }

    public List<Internship> getAllInternships() {
        return internshipDAO.findAll();
    }

    public List<Job> getActiveJobs() {
        return jobDAO.findActive();
    }

    public List<Internship> getActiveInternships() {
        return internshipDAO.findActive();
    }

    public List<Job> searchJobs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return jobDAO.findAll();
        return jobDAO.search(keyword.trim());
    }

    public List<Internship> searchInternships(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return internshipDAO.findAll();
        return internshipDAO.search(keyword.trim());
    }
}
