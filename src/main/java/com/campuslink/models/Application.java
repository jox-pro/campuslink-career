package com.campuslink.models;

import java.time.LocalDate;

public class Application {
    private int applicationId;
    private int studentId;
    private String opportunityType; // "JOB" or "INTERNSHIP"
    private int opportunityId;
    private LocalDate applicationDate;
    private String status; // PENDING, REVIEWED, SHORTLISTED, REJECTED, ACCEPTED
    // Display fields
    private String studentName;
    private String opportunityTitle;
    private String companyName;

    public Application() {}

    public Application(int applicationId, int studentId, String opportunityType,
                       int opportunityId, LocalDate applicationDate, String status) {
        this.applicationId = applicationId; this.studentId = studentId;
        this.opportunityType = opportunityType; this.opportunityId = opportunityId;
        this.applicationDate = applicationDate; this.status = status;
    }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getOpportunityType() { return opportunityType; }
    public void setOpportunityType(String opportunityType) { this.opportunityType = opportunityType; }
    public int getOpportunityId() { return opportunityId; }
    public void setOpportunityId(int opportunityId) { this.opportunityId = opportunityId; }
    public LocalDate getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getOpportunityTitle() { return opportunityTitle; }
    public void setOpportunityTitle(String opportunityTitle) { this.opportunityTitle = opportunityTitle; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    @Override
    public String toString() { return "Application{id=" + applicationId + ", status='" + status + "'}"; }
}
