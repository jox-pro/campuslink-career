package com.campuslink.services;

import com.campuslink.dao.ApplicationDAO;
import com.campuslink.dao.EmployerDAO;
import com.campuslink.dao.InternshipDAO;
import com.campuslink.dao.JobDAO;
import com.campuslink.models.Application;
import com.campuslink.models.Employer;
import com.campuslink.models.Internship;
import com.campuslink.models.Job;
import com.campuslink.utils.ValidationUtil;

import java.util.List;

public class EmployerService {
    private final EmployerDAO employerDAO = new EmployerDAO();
    private final JobDAO jobDAO = new JobDAO();
    private final InternshipDAO internshipDAO = new InternshipDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();

    public Employer getProfile(int userId) {
        return employerDAO.findByUserId(userId);
    }

    public Employer getById(int employerId) {
        return employerDAO.findById(employerId);
    }

    public boolean updateProfile(Employer employer) {
        if (ValidationUtil.isNullOrEmpty(employer.getCompanyName())) return false;
        if (!ValidationUtil.isValidEmail(employer.getEmail())) return false;
        return employerDAO.update(employer);
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
        if (ValidationUtil.isNullOrEmpty(job.getTitle())) return false;
        return jobDAO.update(job);
    }

    public boolean deleteJob(int jobId) {
        return jobDAO.delete(jobId);
    }

    public boolean postInternship(Internship internship) {
        if (ValidationUtil.isNullOrEmpty(internship.getTitle())) return false;
        if (internship.getDeadline() != null && !ValidationUtil.isValidDeadline(internship.getDeadline())) return false;
        return internshipDAO.create(internship);
    }

    public boolean updateInternship(Internship internship) {
        if (ValidationUtil.isNullOrEmpty(internship.getTitle())) return false;
        return internshipDAO.update(internship);
    }

    public boolean deleteInternship(int internshipId) {
        return internshipDAO.delete(internshipId);
    }

    public List<Application> getApplicants(String type, int opportunityId) {
        return applicationDAO.findByOpportunity(type, opportunityId);
    }

    public boolean updateApplicationStatus(int appId, String status) {
        return applicationDAO.updateStatus(appId, status);
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
