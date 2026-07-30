package com.campuslink.controllers;

import com.campuslink.dao.AuditLogDAO;
import com.campuslink.models.AuditLog;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;

public class AuditLogController {

    @FXML private TableView<AuditLog> tableView;
    @FXML private TableColumn<AuditLog, Integer> colId;
    @FXML private TableColumn<AuditLog, String> colTime;
    @FXML private TableColumn<AuditLog, String> colEvent;
    @FXML private TableColumn<AuditLog, String> colUser;
    @FXML private TableColumn<AuditLog, String> colOutcome;
    @FXML private TableColumn<AuditLog, String> colDetails;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @FXML
    public void initialize() {
        initTableColumns();
        loadAuditLogs();
    }

    private void initTableColumns() {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getAuditId()).asObject());
        colTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEventTime() != null ? d.getValue().getEventTime().toString().substring(0, 19).replace('T', ' ') : ""));
        colEvent.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEvent()));
        colUser.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername() != null ? d.getValue().getUsername() : ""));
        colOutcome.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOutcome()));
        colDetails.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDetails() != null ? d.getValue().getDetails() : ""));
    }

    private void loadAuditLogs() {
        List<AuditLog> logs = auditLogDAO.findAll();
        tableView.setItems(FXCollections.observableArrayList(logs));
        if (statusLabel != null) statusLabel.setText("Total: " + logs.size() + " events");
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        List<AuditLog> all = auditLogDAO.findAll();
        if (keyword == null || keyword.trim().isEmpty()) {
            tableView.setItems(FXCollections.observableArrayList(all));
        } else {
            String k = keyword.toLowerCase();
            List<AuditLog> filtered = all.stream().filter(l -> 
                l.getEvent().toLowerCase().contains(k) || 
                (l.getUsername() != null && l.getUsername().toLowerCase().contains(k)) ||
                (l.getDetails() != null && l.getDetails().toLowerCase().contains(k))
            ).toList();
            tableView.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    @FXML
    private void handleRefresh() {
        if (searchField != null) searchField.clear();
        loadAuditLogs();
    }
}
