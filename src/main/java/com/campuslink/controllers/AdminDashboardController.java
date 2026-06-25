package com.campuslink.controllers;

import com.campuslink.services.ReportService;
import com.campuslink.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class AdminDashboardController {

    @FXML private StackPane contentArea;
    @FXML private Label lblCurrentUser;
    @FXML private Button btnDashboard;
    @FXML private Button btnStudents;
    @FXML private Button btnEmployers;
    @FXML private Button btnJobs;
    @FXML private Button btnInternships;
    @FXML private Button btnResources;
    @FXML private Button btnReports;

    // Stats labels injected when DashboardHome.fxml is loaded
    @FXML private Label lblStudentCount;
    @FXML private Label lblEmployerCount;
    @FXML private Label lblJobCount;
    @FXML private Label lblApplicationCount;
    @FXML private Label lblInternshipCount;
    @FXML private Label lblPendingCount;
    @FXML private Label lblAcceptedCount;

    private final ReportService reportService = new ReportService();
    private Button activeBtn;

    @FXML
    public void initialize() {
        if (lblCurrentUser != null && SessionManager.getInstance().getCurrentUser() != null) {
            lblCurrentUser.setText("Logged in as: " + SessionManager.getInstance().getCurrentUser().getUsername());
        }
        // If this is the main dashboard controller (not the sub-view), load dashboard home
        if (contentArea != null) {
            loadView("/fxml/DashboardHome.fxml");
            setActive(btnDashboard);
        } else {
            // We are in DashboardHome.fxml context - load stats
            loadStats();
        }
    }

    private void loadStats() {
        try {
            Map<String, Integer> stats = reportService.getDashboardStats();
            if (lblStudentCount != null)     lblStudentCount.setText(String.valueOf(stats.getOrDefault("totalStudents", 0)));
            if (lblEmployerCount != null)    lblEmployerCount.setText(String.valueOf(stats.getOrDefault("totalEmployers", 0)));
            if (lblJobCount != null)         lblJobCount.setText(String.valueOf(stats.getOrDefault("totalJobs", 0)));
            if (lblApplicationCount != null) lblApplicationCount.setText(String.valueOf(stats.getOrDefault("totalApplications", 0)));
            if (lblInternshipCount != null)  lblInternshipCount.setText(String.valueOf(stats.getOrDefault("totalInternships", 0)));
            if (lblPendingCount != null)     lblPendingCount.setText(String.valueOf(stats.getOrDefault("pendingApplications", 0)));
            if (lblAcceptedCount != null)    lblAcceptedCount.setText(String.valueOf(stats.getOrDefault("acceptedApplications", 0)));
        } catch (Exception e) {
            System.err.println("Failed to load stats: " + e.getMessage());
        }
    }

    @FXML private void handleDashboard() { loadView("/fxml/DashboardHome.fxml"); setActive(btnDashboard); }
    @FXML private void handleStudents()  { loadView("/fxml/StudentList.fxml");   setActive(btnStudents); }
    @FXML private void handleEmployers() { loadView("/fxml/EmployerList.fxml");  setActive(btnEmployers); }
    @FXML private void handleJobs()      { loadView("/fxml/JobList.fxml");       setActive(btnJobs); }
    @FXML private void handleInternships(){ loadView("/fxml/InternshipList.fxml"); setActive(btnInternships); }
    @FXML private void handleResources() { loadView("/fxml/ResourceList.fxml");  setActive(btnResources); }
    @FXML private void handleReports()   { loadView("/fxml/Reports.fxml");       setActive(btnReports); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(loader.load(), 480, 600);
            scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm()
            );
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Logout navigation error: " + e.getMessage());
        }
    }

    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();
            contentArea.getChildren().setAll(node);
        } catch (IOException e) {
            System.err.println("Failed to load view " + fxmlPath + ": " + e.getMessage());
            Label errorLabel = new Label("Failed to load view: " + fxmlPath + "\n" + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            contentArea.getChildren().setAll(errorLabel);
        }
    }

    private void setActive(Button btn) {
        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("sidebar-btn-active");
        }
        activeBtn = btn;
        if (btn != null) {
            btn.getStyleClass().add("sidebar-btn-active");
        }
    }
}
