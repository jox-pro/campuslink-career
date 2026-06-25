package com.campuslink.controllers;

import com.campuslink.models.Employer;
import com.campuslink.models.Job;
import com.campuslink.models.Student;
import com.campuslink.services.EmployerService;
import com.campuslink.services.StudentService;
import com.campuslink.utils.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class JobController {

    // ===== List view =====
    @FXML private TableView<Job> tableView;
    @FXML private TableColumn<Job, Integer> colId;
    @FXML private TableColumn<Job, String> colTitle;
    @FXML private TableColumn<Job, String> colCompany;
    @FXML private TableColumn<Job, String> colDeadline;
    @FXML private TableColumn<Job, String> colDescription;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Label pageTitle;
    @FXML private Button btnApply;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    // ===== Form view =====
    @FXML private TextField fieldTitle;
    @FXML private TextArea fieldDescription;
    @FXML private TextArea fieldRequirements;
    @FXML private DatePicker fieldDeadline;
    @FXML private Label formTitle;
    @FXML private Label formMessage;
    @FXML private Button btnSave;

    private final EmployerService employerService = new EmployerService();
    private final StudentService studentService = new StudentService();
    private Job editingJob = null;

    @FXML
    public void initialize() {
        String role = SessionManager.getInstance().getRole();
        if (tableView != null) {
            initTableColumns();
            configureForRole(role);
            loadJobs(role);
        } else if (fieldTitle != null) {
            configureForm(role);
        }
    }

    private void initTableColumns() {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getJobId()).asObject());
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        colCompany.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCompanyName() != null ? d.getValue().getCompanyName() : ""));
        colDeadline.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDeadline() != null ? d.getValue().getDeadline().toString() : ""));
        colDescription.setCellValueFactory(d -> {
            String desc = d.getValue().getDescription();
            return new SimpleStringProperty(desc != null && desc.length() > 80 ? desc.substring(0, 80) + "..." : (desc != null ? desc : ""));
        });
    }

    private void configureForRole(String role) {
        if (role == null) return;
        switch (role) {
            case "STUDENT":
                if (pageTitle != null) pageTitle.setText("Available Jobs");
                if (btnApply != null) btnApply.setVisible(true);
                if (btnAdd != null) { btnAdd.setVisible(false); btnAdd.setManaged(false); }
                if (btnEdit != null) { btnEdit.setVisible(false); btnEdit.setManaged(false); }
                if (btnDelete != null) { btnDelete.setVisible(false); btnDelete.setManaged(false); }
                break;
            case "EMPLOYER":
                if (pageTitle != null) pageTitle.setText("My Job Listings");
                if (btnApply != null) { btnApply.setVisible(false); btnApply.setManaged(false); }
                if (btnAdd != null) btnAdd.setVisible(true);
                if (btnEdit != null) btnEdit.setVisible(true);
                if (btnDelete != null) btnDelete.setVisible(true);
                break;
            case "ADMIN":
                if (pageTitle != null) pageTitle.setText("All Jobs");
                if (btnApply != null) { btnApply.setVisible(false); btnApply.setManaged(false); }
                if (btnAdd != null) btnAdd.setVisible(true);
                if (btnEdit != null) btnEdit.setVisible(true);
                if (btnDelete != null) btnDelete.setVisible(true);
                break;
        }
    }

    private void loadJobs(String role) {
        List<Job> jobs;
        try {
            if ("EMPLOYER".equals(role)) {
                Employer emp = employerService.getProfile(SessionManager.getInstance().getCurrentUser().getId());
                if (emp != null) {
                    jobs = employerService.getEmployerJobs(emp.getEmployerId());
                } else {
                    jobs = List.of();
                }
            } else if ("STUDENT".equals(role)) {
                jobs = employerService.getActiveJobs();
            } else {
                jobs = employerService.getAllJobs();
            }
            tableView.setItems(FXCollections.observableArrayList(jobs));
            if (statusLabel != null) statusLabel.setText("Total: " + jobs.size() + " jobs");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load jobs: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        if (searchField == null) return;
        String keyword = searchField.getText();
        List<Job> results = employerService.searchJobs(keyword);
        tableView.setItems(FXCollections.observableArrayList(results));
        if (statusLabel != null) statusLabel.setText("Found: " + results.size() + " jobs");
    }

    @FXML
    private void handleClearSearch() {
        if (searchField != null) searchField.clear();
        loadJobs(SessionManager.getInstance().getRole());
    }

    @FXML
    private void handleApply() {
        Job selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a job to apply for."); return; }

        Student student = studentService.getProfile(SessionManager.getInstance().getCurrentUser().getId());
        if (student == null) { showAlert(Alert.AlertType.WARNING, "No Profile", "Please complete your profile before applying."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Apply for: " + selected.getTitle() + " at " + selected.getCompanyName() + "?",
            ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            boolean applied = studentService.applyForOpportunity(student.getStudentId(), "JOB", selected.getJobId());
            if (applied) {
                showAlert(Alert.AlertType.INFORMATION, "Applied!", "Application submitted successfully. You can track it under 'My Applications'.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Already Applied", "You have already applied for this position.");
            }
        }
    }

    @FXML
    private void handleAdd() {
        editingJob = null;
        clearForm();
        if (formTitle != null) formTitle.setText("Post a New Job");
        if (btnSave != null) btnSave.setText("Post Job");
    }

    @FXML
    private void handleEdit() {
        if (tableView == null) return;
        Job selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a job to edit."); return; }
        editingJob = selected;
        populateForm(selected);
        if (formTitle != null) formTitle.setText("Edit Job");
        if (btnSave != null) btnSave.setText("Update Job");
    }

    @FXML
    private void handleDelete() {
        Job selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a job to delete."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete job: " + selected.getTitle() + "?",
            ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (employerService.deleteJob(selected.getJobId())) {
                loadJobs(SessionManager.getInstance().getRole());
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Job deleted successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete job.");
            }
        }
    }

    @FXML
    private void handleRefresh() {
        if (searchField != null) searchField.clear();
        loadJobs(SessionManager.getInstance().getRole());
    }

    // ===== Form handlers =====

    private void configureForm(String role) {
        if (formMessage != null) { formMessage.setVisible(false); formMessage.setManaged(false); }
    }

    @FXML
    private void handleSave() {
        if (fieldTitle == null) return;
        String title = fieldTitle.getText();
        String description = fieldDescription.getText();
        String requirements = fieldRequirements.getText();
        LocalDate deadline = fieldDeadline != null ? fieldDeadline.getValue() : null;

        if (title == null || title.trim().isEmpty()) {
            showFormMessage("Job title is required.", true); return;
        }

        Employer emp = employerService.getProfile(SessionManager.getInstance().getCurrentUser().getId());
        int employerId = (emp != null) ? emp.getEmployerId() : 0;

        if (editingJob == null) {
            Job job = new Job();
            job.setTitle(title.trim());
            job.setDescription(description);
            job.setRequirements(requirements);
            job.setDeadline(deadline);
            job.setEmployerId(employerId);
            if (employerService.postJob(job)) {
                showFormMessage("Job posted successfully!", false);
                clearForm();
                editingJob = null;
            } else {
                showFormMessage("Failed to post job.", true);
            }
        } else {
            editingJob.setTitle(title.trim());
            editingJob.setDescription(description);
            editingJob.setRequirements(requirements);
            editingJob.setDeadline(deadline);
            if (employerService.updateJob(editingJob)) {
                showFormMessage("Job updated successfully!", false);
            } else {
                showFormMessage("Failed to update job.", true);
            }
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
        editingJob = null;
    }

    private void populateForm(Job job) {
        if (fieldTitle != null) fieldTitle.setText(job.getTitle() != null ? job.getTitle() : "");
        if (fieldDescription != null) fieldDescription.setText(job.getDescription() != null ? job.getDescription() : "");
        if (fieldRequirements != null) fieldRequirements.setText(job.getRequirements() != null ? job.getRequirements() : "");
        if (fieldDeadline != null) fieldDeadline.setValue(job.getDeadline());
    }

    private void clearForm() {
        if (fieldTitle != null) fieldTitle.clear();
        if (fieldDescription != null) fieldDescription.clear();
        if (fieldRequirements != null) fieldRequirements.clear();
        if (fieldDeadline != null) fieldDeadline.setValue(null);
        if (formMessage != null) { formMessage.setVisible(false); formMessage.setManaged(false); }
    }

    private void showFormMessage(String text, boolean isError) {
        if (formMessage == null) return;
        formMessage.setText(text);
        formMessage.setStyle(isError ? "-fx-text-fill: #C62828;" : "-fx-text-fill: #2E7D32;");
        formMessage.setVisible(true);
        formMessage.setManaged(true);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
