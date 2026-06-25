package com.campuslink.controllers;

import com.campuslink.models.Employer;
import com.campuslink.services.EmployerService;
import com.campuslink.utils.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;

public class EmployerController {

    @FXML private TableView<Employer> tableView;
    @FXML private TableColumn<Employer, Integer> colId;
    @FXML private TableColumn<Employer, String> colCompany;
    @FXML private TableColumn<Employer, String> colContact;
    @FXML private TableColumn<Employer, String> colEmail;
    @FXML private TableColumn<Employer, String> colPhone;
    @FXML private TableColumn<Employer, String> colAddress;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final EmployerService employerService = new EmployerService();

    @FXML
    public void initialize() {
        if (tableView != null) {
            initTableColumns();
            loadAllEmployers();
        }
    }

    private void initTableColumns() {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getEmployerId()).asObject());
        colCompany.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCompanyName()));
        colContact.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getContactPerson()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        colAddress.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAddress()));
    }

    private void loadAllEmployers() {
        try {
            List<Employer> employers = employerService.getAllEmployers();
            tableView.setItems(FXCollections.observableArrayList(employers));
            if (statusLabel != null) statusLabel.setText("Total: " + employers.size() + " employers");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load employers: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField != null ? searchField.getText() : null;
        if (keyword == null || keyword.trim().isEmpty()) { loadAllEmployers(); return; }
        List<Employer> results = employerService.searchEmployers(keyword.trim());
        tableView.setItems(FXCollections.observableArrayList(results));
        if (statusLabel != null) statusLabel.setText("Found: " + results.size() + " employers");
    }

    @FXML
    private void handleClearSearch() {
        if (searchField != null) searchField.clear();
        loadAllEmployers();
    }

    @FXML
    private void handleEdit() {
        Employer selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an employer to edit."); return; }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Employer");
        dialog.setHeaderText("Edit: " + selected.getCompanyName());

        TextField companyF = new TextField(selected.getCompanyName());
        TextField contactF = new TextField(selected.getContactPerson() != null ? selected.getContactPerson() : "");
        TextField emailF = new TextField(selected.getEmail() != null ? selected.getEmail() : "");
        TextField phoneF = new TextField(selected.getPhone() != null ? selected.getPhone() : "");
        TextField addressF = new TextField(selected.getAddress() != null ? selected.getAddress() : "");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Company Name:"), companyF);
        grid.addRow(1, new Label("Contact Person:"), contactF);
        grid.addRow(2, new Label("Email:"), emailF);
        grid.addRow(3, new Label("Phone:"), phoneF);
        grid.addRow(4, new Label("Address:"), addressF);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            selected.setCompanyName(companyF.getText());
            selected.setContactPerson(contactF.getText());
            selected.setEmail(emailF.getText());
            selected.setPhone(phoneF.getText());
            selected.setAddress(addressF.getText());
            if (employerService.updateProfile(selected)) {
                loadAllEmployers();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employer updated successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update employer.");
            }
        }
    }

    @FXML
    private void handleDelete() {
        Employer selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an employer to delete."); return; }

        // Prevent deleting own account
        String role = SessionManager.getInstance().getRole();
        if ("EMPLOYER".equals(role)) {
            Employer myProfile = employerService.getProfile(SessionManager.getInstance().getCurrentUser().getId());
            if (myProfile != null && myProfile.getEmployerId() == selected.getEmployerId()) {
                showAlert(Alert.AlertType.WARNING, "Cannot Delete", "You cannot delete your own account."); return;
            }
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete employer: " + selected.getCompanyName() + "?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (employerService.deleteEmployer(selected.getEmployerId())) {
                loadAllEmployers();
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Employer deleted successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete employer.");
            }
        }
    }

    @FXML
    private void handleRefresh() {
        if (searchField != null) searchField.clear();
        loadAllEmployers();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
