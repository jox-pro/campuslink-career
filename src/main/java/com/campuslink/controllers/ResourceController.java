package com.campuslink.controllers;

import com.campuslink.dao.ResourceDAO;
import com.campuslink.models.Resource;
import com.campuslink.utils.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class ResourceController {

    @FXML private TableView<Resource> tableView;
    @FXML private TableColumn<Resource, Integer> colId;
    @FXML private TableColumn<Resource, String> colTitle;
    @FXML private TableColumn<Resource, String> colDescription;
    @FXML private TableColumn<Resource, String> colFilePath;
    @FXML private TableColumn<Resource, String> colUploaded;
    @FXML private Label statusLabel;
    @FXML private Button btnAdd;
    @FXML private Button btnDelete;

    private final ResourceDAO resourceDAO = new ResourceDAO();

    @FXML
    public void initialize() {
        initTableColumns();
        configureForRole();
        loadResources();
    }

    private void initTableColumns() {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getResourceId()).asObject());
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        colDescription.setCellValueFactory(d -> {
            String desc = d.getValue().getDescription();
            return new SimpleStringProperty(desc != null && desc.length() > 80 ? desc.substring(0, 80) + "..." : (desc != null ? desc : ""));
        });
        colFilePath.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFilePath() != null ? d.getValue().getFilePath() : ""));
        colUploaded.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getUploadedAt() != null ? d.getValue().getUploadedAt().toString().substring(0, 16) : ""
        ));
    }

    private void configureForRole() {
        String role = SessionManager.getInstance().getRole();
        boolean isAdmin = "ADMIN".equals(role);
        if (btnAdd != null) { btnAdd.setVisible(isAdmin); btnAdd.setManaged(isAdmin); }
        if (btnDelete != null) { btnDelete.setVisible(isAdmin); btnDelete.setManaged(isAdmin); }
    }

    private void loadResources() {
        try {
            List<Resource> resources = resourceDAO.findAll();
            tableView.setItems(FXCollections.observableArrayList(resources));
            if (statusLabel != null) statusLabel.setText("Total: " + resources.size() + " resources");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load resources: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Resource");
        dialog.setHeaderText("Add a new career resource");

        TextField titleF = new TextField();
        titleF.setPromptText("Resource title");
        TextArea descF = new TextArea();
        descF.setPromptText("Description");
        descF.setPrefRowCount(3);
        TextField pathF = new TextField();
        pathF.setPromptText("/path/to/file.pdf");
        Button browseBtn = new Button("Browse...");
        browseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Resource File");
            Stage s = (Stage) tableView.getScene().getWindow();
            File f = fc.showOpenDialog(s);
            if (f != null) pathF.setText(f.getAbsolutePath());
        });

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPrefWidth(400);
        grid.addRow(0, new Label("Title *:"), titleF);
        grid.addRow(1, new Label("Description:"), descF);
        grid.addRow(2, new Label("File Path:"), pathF);
        grid.addRow(3, new Label(), browseBtn);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String title = titleF.getText();
            if (title == null || title.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Resource title is required.");
                return;
            }
            Resource resource = new Resource();
            resource.setTitle(title.trim());
            resource.setDescription(descF.getText());
            resource.setFilePath(pathF.getText());
            if (resourceDAO.create(resource)) {
                loadResources();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Resource added successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add resource.");
            }
        }
    }

    @FXML
    private void handleDelete() {
        Resource selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a resource to delete."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete resource: " + selected.getTitle() + "?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (resourceDAO.delete(selected.getResourceId())) {
                loadResources();
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Resource deleted.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete resource.");
            }
        }
    }

    @FXML
    private void handleOpen() {
        Resource selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a resource to open."); return; }

        String path = selected.getFilePath();
        if (path == null || path.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No File", "This resource has no file path."); return;
        }

        Path baseDir = Paths.get(System.getProperty("user.home"), ".campuslink-career", "resources");
        Path resolvedPath = Paths.get(path).normalize();
        Path allowedBase = baseDir.toAbsolutePath().normalize();
        if (!resolvedPath.startsWith(allowedBase)) {
            showAlert(Alert.AlertType.WARNING, "Access Denied",
                "Only files under the configured resource directory can be opened.");
            return;
        }

        if (!Files.exists(resolvedPath) || !Files.isRegularFile(resolvedPath)) {
            showAlert(Alert.AlertType.WARNING, "File Not Found",
                "File not found at: " + path + "\nPlease ensure the file exists."); return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(resolvedPath.toFile());
            } else {
                showAlert(Alert.AlertType.INFORMATION, "File Path", "File path: " + resolvedPath.toAbsolutePath());
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open file: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadResources();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
