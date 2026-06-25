package com.campuslink.controllers;

import com.campuslink.models.Employer;
import com.campuslink.models.Internship;
import com.campuslink.models.Student;
import com.campuslink.services.EmployerService;
import com.campuslink.services.StudentService;
import com.campuslink.utils.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InternshipController {

    // ===== List view =====
    @FXML private TableView<Internship> tableView;
    @FXML private TableColumn<Internship, Integer> colId;
    @FXML private TableColumn<Internship, String> colTitle;
    @FXML private TableColumn<Internship, String> colCompany;
    @FXML private TableColumn<Internship, String> colDeadline;
    @FXML private TableColumn<Internship, String> colDescription;
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
    private Internship editingInternship = null;

    @FXML
    public void initialize() {
        String role = SessionManager.getInstance().getRole();
        if (tableView != null) {
            initTableColumns();
            configureForRole(role);
            loadInternships(role);
        } else if (fieldTitle != null) {
            if (formMessage != null) { formMessage.setVisible(false); formMessage.setManaged(false); }
        }
    }

    private void initTableColumns() {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getInternshipId()).asObject());
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
                if (pageTitle != null) pageTitle.setText("Available Internships");
                if (btnApply != null) btnApply.setVisible(true);
                if (btnAdd != null) { btnAdd.setVisible(false); btnAdd.setManaged(false); }
                if (btnEdit != null) { btnEdit.setVisible(false); btnEdit.setManaged(false); }
                if (btnDelete != null) { btnDelete.setVisible(false); btnDelete.setManaged(false); }
                break;
            case "EMPLOYER":
                if (pageTitle != null) pageTitle.setText("My Internship Listings");
                if (btnApply != null) { btnApply.setVisible(false); btnApply.setManaged(false); }
                break;
            case "ADMIN":
                if (pageTitle != null) pageTitle.setText("All Internships");
                if (btnApply != null) { btnApply.setVisible(false); btnApply.setManaged(false); }
                break;
        }
    }

    private void loadInternships(String role) {
        List<Internship> internships;
        try {
            if ("EMPLOYER".equals(role)) {
                Employer emp = employerService.getProfile(SessionManager.getInstance().getCurrentUser().getId());
                internships = (emp != null) ? employerService.getEmployerInternships(emp.getEmployerId()) : List.of();
            } else if ("STUDENT".equals(role)) {
                internships = employerService.getActiveInternships();
            } else {
                internships = employerService.getAllInternships();
            }
            tableView.setItems(FXCollections.observableArrayList(internships));
            if (statusLabel != null) statusLabel.setText("Total: " + internships.size() + " internships");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load internships: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        if (searchField == null) return;
        String keyword = searchField.getText();
        List<Internship> results = employerService.searchInternships(keyword);
        tableView.setItems(FXCollections.observableArrayList(results));
        if (statusLabel != null) statusLabel.setText("Found: " + results.size() + " internships");
    }

    @FXML
    private void handleClearSearch() {
        if (searchField != null) searchField.clear();
        loadInternships(SessionManager.getInstance().getRole());
    }

    @FXML
    private void handleApply() {
        Internship selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an internship to apply for."); return; }

        Student student = studentService.getProfile(SessionManager.getInstance().getCurrentUser().getId());
        if (student == null) { showAlert(Alert.AlertType.WARNING, "No Profile", "Please complete your profile before applying."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Apply for: " + selected.getTitle() + "?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            boolean applied = studentService.applyForOpportunity(student.getStudentId(), "INTERNSHIP", selected.getInternshipId());
            if (applied) {
                showAlert(Alert.AlertType.INFORMATION, "Applied!", "Application submitted. Track it under 'My Applications'.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Already Applied", "You have already applied for this internship.");
            }
        }
    }

    @FXML
    private void handleAdd() {
        editingInternship = null;
        clearForm();
        if (formTitle != null) formTitle.setText("Post a New Internship");
    }

    @FXML
    private void handleEdit() {
        if (tableView == null) return;
        Internship selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an internship to edit."); return; }
        editingInternship = selected;
        populateForm(selected);
        if (formTitle != null) formTitle.setText("Edit Internship");
    }

    @FXML
    private void handleDelete() {
        Internship selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an internship to delete."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete internship: " + selected.getTitle() + "?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (employerService.deleteInternship(selected.getInternshipId())) {
                loadInternships(SessionManager.getInstance().getRole());
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Internship deleted.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete internship.");
            }
        }
    }

    @FXML
    private void handleRefresh() {
        if (searchField != null) searchField.clear();
        loadInternships(SessionManager.getInstance().getRole());
    }

    @FXML
    private void handleSave() {
        if (fieldTitle == null) return;
        String title = fieldTitle.getText();
        String description = fieldDescription != null ? fieldDescription.getText() : "";
        String requirements = fieldRequirements != null ? fieldRequirements.getText() : "";
        LocalDate deadline = fieldDeadline != null ? fieldDeadline.getValue() : null;

        if (title == null || title.trim().isEmpty()) { showFormMessage("Title is required.", true); return; }

        Employer emp = employerService.getProfile(SessionManager.getInstance().getCurrentUser().getId());
        int employerId = (emp != null) ? emp.getEmployerId() : 0;

        if (editingInternship == null) {
            Internship internship = new Internship();
            internship.setTitle(title.trim());
            internship.setDescription(description);
            internship.setRequirements(requirements);
            internship.setDeadline(deadline);
            internship.setEmployerId(employerId);
            if (employerService.postInternship(internship)) {
                showFormMessage("Internship posted successfully!", false);
                clearForm();
            } else {
                showFormMessage("Failed to post internship.", true);
            }
        } else {
            editingInternship.setTitle(title.trim());
            editingInternship.setDescription(description);
            editingInternship.setRequirements(requirements);
            editingInternship.setDeadline(deadline);
            if (employerService.updateInternship(editingInternship)) {
                showFormMessage("Internship updated successfully!", false);
            } else {
                showFormMessage("Failed to update internship.", true);
            }
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
        editingInternship = null;
    }

    private void populateForm(Internship internship) {
        if (fieldTitle != null) fieldTitle.setText(internship.getTitle() != null ? internship.getTitle() : "");
        if (fieldDescription != null) fieldDescription.setText(internship.getDescription() != null ? internship.getDescription() : "");
        if (fieldRequirements != null) fieldRequirements.setText(internship.getRequirements() != null ? internship.getRequirements() : "");
        if (fieldDeadline != null) fieldDeadline.setValue(internship.getDeadline());
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
