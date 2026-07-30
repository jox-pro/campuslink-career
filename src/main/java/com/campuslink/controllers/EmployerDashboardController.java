package com.campuslink.controllers;

import com.campuslink.utils.SessionManager;
import com.campuslink.utils.ViewNavigator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class EmployerDashboardController implements ViewNavigator {
    private static final Logger logger = LoggerFactory.getLogger(EmployerDashboardController.class);

    @FXML private StackPane contentArea;
    @FXML private Label lblCurrentUser;
    @FXML private Button btnPostJob;
    @FXML private Button btnPostInternship;
    @FXML private Button btnMyJobs;
    @FXML private Button btnApplicants;
    @FXML private Button btnProfile;

    private Button activeBtn;

    @FXML
    public void initialize() {
        SessionManager.getInstance().setViewNavigator(this);
        if (lblCurrentUser != null && SessionManager.getInstance().getCurrentUser() != null) {
            lblCurrentUser.setText("Logged in as: " + SessionManager.getInstance().getCurrentUser().getUsername());
        }
        loadView("/fxml/JobList.fxml");
        setActive(btnMyJobs);
    }

    @FXML private void handlePostJob()         { loadView("/fxml/JobForm.fxml");         setActive(btnPostJob); }
    @FXML private void handlePostInternship()  { loadView("/fxml/InternshipForm.fxml");  setActive(btnPostInternship); }
    @FXML private void handleMyJobs()          { loadView("/fxml/JobList.fxml");         setActive(btnMyJobs); }
    @FXML private void handleApplicants()      { loadView("/fxml/ApplicationList.fxml"); setActive(btnApplicants); }
    @FXML private void handleProfile()         { loadView("/fxml/EmployerList.fxml");    setActive(btnProfile); }

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
            logger.error("Logout navigation error: {}", e.getMessage(), e);
        }
    }

    @Override
    public <T> T loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();
            contentArea.getChildren().setAll(node);
            return loader.getController();
        } catch (IOException e) {
            logger.error("Failed to load view {}: {}", fxmlPath, e.getMessage(), e);
            Label errorLabel = new Label("Failed to load view: " + fxmlPath);
            errorLabel.setStyle("-fx-text-fill: red;");
            contentArea.getChildren().setAll(errorLabel);
            return null;
        }
    }

    private void setActive(Button btn) {
        if (activeBtn != null) activeBtn.getStyleClass().remove("sidebar-btn-active");
        activeBtn = btn;
        if (btn != null) btn.getStyleClass().add("sidebar-btn-active");
    }
}
