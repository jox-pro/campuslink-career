package com.campuslink.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Internship {
    private int internshipId;
    private String title;
    private String description;
    private String requirements;
    private LocalDate deadline;
    private int employerId;
    private String companyName;
    private LocalDateTime createdAt;

    public Internship() {}

    public Internship(int internshipId, String title, String description, String requirements,
                      LocalDate deadline, int employerId) {
        this.internshipId = internshipId; this.title = title;
        this.description = description; this.requirements = requirements;
        this.deadline = deadline; this.employerId = employerId;
    }

    public int getInternshipId() { return internshipId; }
    public void setInternshipId(int internshipId) { this.internshipId = internshipId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public int getEmployerId() { return employerId; }
    public void setEmployerId(int employerId) { this.employerId = employerId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() { return "Internship{id=" + internshipId + ", title='" + title + "'}"; }
}
