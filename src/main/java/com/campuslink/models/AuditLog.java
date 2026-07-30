package com.campuslink.models;

import java.time.LocalDateTime;

public class AuditLog {
    private int auditId;
    private LocalDateTime eventTime;
    private String event;
    private String username;
    private String outcome;
    private String details;

    public int getAuditId() { return auditId; }
    public void setAuditId(int auditId) { this.auditId = auditId; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
