package com.campuslink.models;

import java.time.LocalDateTime;

public class Resource {
    private int resourceId;
    private String title;
    private String description;
    private String filePath;
    private LocalDateTime uploadedAt;

    public Resource() {}

    public Resource(int resourceId, String title, String description, String filePath) {
        this.resourceId = resourceId; this.title = title;
        this.description = description; this.filePath = filePath;
    }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    @Override
    public String toString() { return "Resource{id=" + resourceId + ", title='" + title + "'}"; }
}
