package com.campuslink.models;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private boolean mustChangePassword;
    private LocalDateTime createdAt;

    public User() {}

    public User(int id, String username, String password, String role) {
        this(id, username, password, role, true);
    }

    public User(int id, String username, String password, String role, boolean mustChangePassword) {
        this.id = id; this.username = username;
        this.password = password; this.role = role;
        this.mustChangePassword = mustChangePassword;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() { return "User{id=" + id + ", username='" + username + "', role='" + role + "'}"; }
}
