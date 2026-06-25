package com.campuslink.controllers;

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
import java.util.Objects;

public class StudentDashboardController {

    @FXML private StackPane contentArea;
    @FXML private Label lblCurrentUser;
    @FXML private Button btnProfile;
    @FXML private Button btnJobs;
    @FXML private Button btnInternships;
    @FXML private Button btnApplications;
    @FXML private Button btnResources;

    private Button activeBtn;

    @FXML
    public void initialize() {
        if (lblCurrentUser != null && SessionManager.getInstance().getCurrentUser() != null) {
            lblCurrentUser.setText("Logged in as: " + SessionManager.getInstance().getCurrentUser().getUsername());
        }
        loadView("/fxml/StudentProfile.fxml");
        setActive(btnProfile);
    }

    @FXML private void handleProfile()      { loadView("/fxml/StudentProfile.fxml");  setActive(btnProfile); }
    @FXML private void handleJobs()         { loadView("/fxml/JobList.fxml");          setActive(btnJobs); }
    @FXML private void handleInternships()  { loadView("/fxml/InternshipList.fxml");   setActive(btnInternships); }
    @FXML private void handleApplications() { loadView("/fxml/ApplicationList.fxml");  setActive(btnApplications); }
    @FXML private void handleResources()    { loadView("/fxml/ResourceList.fxml");     setActive(btnResources); }

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
            Label errorLabel = new Label("Failed to load view: " + fxmlPath);
            errorLabel.setStyle("-fx-text-fill: red;");
            contentArea.getChildren().setAll(errorLabel);
        }
    }

    private void setActive(Button btn) {
        if (activeBtn != null) activeBtn.getStyleClass().remove("sidebar-btn-active");
        activeBtn = btn;
        if (btn != null) btn.getStyleClass().add("sidebar-btn-active");
    }
}
