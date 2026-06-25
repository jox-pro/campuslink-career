package com.campuslink.controllers;

import com.campuslink.models.Application;
import com.campuslink.models.Employer;
import com.campuslink.models.Student;
import com.campuslink.services.EmployerService;
import com.campuslink.services.StudentService;
import com.campuslink.utils.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;

public class ApplicationController {

    @FXML private TableView<Application> tableView;
    @FXML private TableColumn<Application, Integer> colId;
    @FXML private TableColumn<Application, String> colStudent;
    @FXML private TableColumn<Application, String> colOpportunity;
    @FXML private TableColumn<Application, String> colType;
    @FXML private TableColumn<Application, String> colDate;
    @FXML private TableColumn<Application, String> colStatus;
    @FXML private TableColumn<Application, String> colCompany;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label statusLabel;
    @FXML private Label pageTitle;
    @FXML private Button btnWithdraw;

    private final StudentService studentService = new StudentService();
    private final EmployerService employerService = new EmployerService();

    private static final String[] STATUSES = {"PENDING", "REVIEWED", "SHORTLISTED", "REJECTED", "ACCEPTED"};

    @FXML
    public void initialize() {
        initTableColumns();
        setupStatusCombo();
        configureForRole();
        loadApplications();
    }

    private void initTableColumns() {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getApplicationId()).asObject());
        colStudent.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStudentName() != null ? d.getValue().getStudentName() : ""));
        colOpportunity.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOpportunityTitle() != null ? d.getValue().getOpportunityTitle() : ""));
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOpportunityType() != null ? d.getValue().getOpportunityType() : ""));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApplicationDate() != null ? d.getValue().getApplicationDate().toString() : ""));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus() != null ? d.getValue().getStatus() : ""));
        colCompany.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCompanyName() != null ? d.getValue().getCompanyName() : ""));
    }

    private void setupStatusCombo() {
        if (statusCombo != null) {
            statusCombo.setItems(FXCollections.observableArrayList(STATUSES));
        }
    }

    private void configureForRole() {
        String role = SessionManager.getInstance().getRole();
        if ("STUDENT".equals(role)) {
            if (pageTitle != null) pageTitle.setText("My Applications");
            if (btnWithdraw != null) btnWithdraw.setVisible(true);
            if (statusCombo != null) { statusCombo.setVisible(false); statusCombo.setManaged(false); }
        } else if ("EMPLOYER".equals(role)) {
            if (pageTitle != null) pageTitle.setText("Applicants");
            if (btnWithdraw != null) { btnWithdraw.setVisible(false); btnWithdraw.setManaged(false); }
            if (statusCombo != null) statusCombo.setVisible(true);
        } else {
            if (pageTitle != null) pageTitle.setText("All Applications");
            if (btnWithdraw != null) { btnWithdraw.setVisible(false); btnWithdraw.setManaged(false); }
        }
    }

    private void loadApplications() {
        String role = SessionManager.getInstance().getRole();
        List<Application> apps;
        try {
            if ("STUDENT".equals(role)) {
                Student student = studentService.getProfile(SessionManager.getInstance().getCurrentUser().getId());
                if (student != null) {
                    apps = studentService.getStudentApplications(student.getStudentId());
                } else {
                    apps = List.of();
                }
            } else if ("EMPLOYER".equals(role)) {
                Employer emp = employerService.getProfile(SessionManager.getInstance().getCurrentUser().getId());
                if (emp != null) {
                    // Merge job and internship applicants
                    List<Application> jobApps = new java.util.ArrayList<>();
                    employerService.getEmployerJobs(emp.getEmployerId()).forEach(j ->
                        jobApps.addAll(employerService.getApplicants("JOB", j.getJobId()))
                    );
                    employerService.getEmployerInternships(emp.getEmployerId()).forEach(i ->
                        jobApps.addAll(employerService.getApplicants("INTERNSHIP", i.getInternshipId()))
                    );
                    apps = jobApps;
                } else {
                    apps = List.of();
                }
            } else {
                // Admin: all applications
                apps = new com.campuslink.dao.ApplicationDAO().findAll();
            }
            tableView.setItems(FXCollections.observableArrayList(apps));
            if (statusLabel != null) statusLabel.setText("Total: " + apps.size() + " applications");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load applications: " + e.getMessage());
            System.err.println("ApplicationController.loadApplications: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateStatus() {
        Application selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an application."); return; }
        if (statusCombo == null || statusCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Status", "Please select a new status."); return;
        }

        String newStatus = statusCombo.getValue();
        boolean updated = employerService.updateApplicationStatus(selected.getApplicationId(), newStatus);
        if (updated) {
            loadApplications();
            showAlert(Alert.AlertType.INFORMATION, "Updated", "Application status updated to: " + newStatus);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status.");
        }
    }

    @FXML
    private void handleWithdraw() {
        Application selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an application to withdraw."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Withdraw application for: " + selected.getOpportunityTitle() + "?",
            ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (studentService.withdrawApplication(selected.getApplicationId())) {
                loadApplications();
                showAlert(Alert.AlertType.INFORMATION, "Withdrawn", "Application withdrawn.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to withdraw application.");
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadApplications();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
