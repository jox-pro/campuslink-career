package com.campuslink.controllers;

import com.campuslink.services.ReportService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ReportController {

    @FXML private Label lblStudents;
    @FXML private Label lblEmployers;
    @FXML private Label lblJobs;
    @FXML private Label lblInternships;
    @FXML private Label lblApplications;
    @FXML private Label lblPending;
    @FXML private Label lblAccepted;
    @FXML private Label exportStatus;

    private final ReportService reportService = new ReportService();
    private Map<String, Integer> currentStats;

    @FXML
    public void initialize() {
        loadStats();
    }

    private void loadStats() {
        try {
            currentStats = reportService.getDashboardStats();
            if (lblStudents != null)     lblStudents.setText(String.valueOf(currentStats.getOrDefault("totalStudents", 0)));
            if (lblEmployers != null)    lblEmployers.setText(String.valueOf(currentStats.getOrDefault("totalEmployers", 0)));
            if (lblJobs != null)         lblJobs.setText(String.valueOf(currentStats.getOrDefault("totalJobs", 0)));
            if (lblInternships != null)  lblInternships.setText(String.valueOf(currentStats.getOrDefault("totalInternships", 0)));
            if (lblApplications != null) lblApplications.setText(String.valueOf(currentStats.getOrDefault("totalApplications", 0)));
            if (lblPending != null)      lblPending.setText(String.valueOf(currentStats.getOrDefault("pendingApplications", 0)));
            if (lblAccepted != null)     lblAccepted.setText(String.valueOf(currentStats.getOrDefault("acceptedApplications", 0)));
        } catch (Exception e) {
            System.err.println("ReportController.loadStats: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadStats();
        if (exportStatus != null) exportStatus.setText("Stats refreshed.");
    }

    @FXML
    private void handleExport() {
        if (currentStats == null) { loadStats(); }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.setInitialFileName("campuslink_report_" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        Stage stage = (Stage) lblStudents.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("=================================================\n");
            writer.write("        CAMPUSLINK CAREER - SYSTEM REPORT        \n");
            writer.write("=================================================\n");
            writer.write("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
            writer.write("SUMMARY STATISTICS\n");
            writer.write("------------------\n");
            writer.write(String.format("%-30s %d%n", "Total Students:", currentStats.getOrDefault("totalStudents", 0)));
            writer.write(String.format("%-30s %d%n", "Total Employers:", currentStats.getOrDefault("totalEmployers", 0)));
            writer.write(String.format("%-30s %d%n", "Total Job Listings:", currentStats.getOrDefault("totalJobs", 0)));
            writer.write(String.format("%-30s %d%n", "Total Internships:", currentStats.getOrDefault("totalInternships", 0)));
            writer.write(String.format("%-30s %d%n", "Total Applications:", currentStats.getOrDefault("totalApplications", 0)));
            writer.write("\nAPPLICATION STATUS BREAKDOWN\n");
            writer.write("----------------------------\n");
            writer.write(String.format("%-30s %d%n", "Pending:", currentStats.getOrDefault("pendingApplications", 0)));
            writer.write(String.format("%-30s %d%n", "Accepted:", currentStats.getOrDefault("acceptedApplications", 0)));
            writer.write("\n=================================================\n");
            writer.write("          END OF REPORT\n");
            writer.write("=================================================\n");

            if (exportStatus != null) exportStatus.setText("Report exported to: " + file.getAbsolutePath());
            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Report saved to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not save report: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
