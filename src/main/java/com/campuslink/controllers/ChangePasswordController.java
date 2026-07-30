package com.campuslink.controllers;

import com.campuslink.models.User;
import com.campuslink.services.AuthService;
import com.campuslink.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class ChangePasswordController {
    private static final Logger logger = LoggerFactory.getLogger(ChangePasswordController.class);

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Button submitBtn;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    @FXML
    private void handleChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("New passwords do not match.");
            return;
        }

        if (newPassword.length() < 8) {
            showError("New password must be at least 8 characters long.");
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showError("Session expired. Please login again.");
            return;
        }

        try {
            if (authService.changePassword(currentUser, currentPassword, newPassword)) {
                Stage stage = (Stage) submitBtn.getScene().getWindow();
                navigateToDashboard(stage, currentUser);
            } else {
                showError("Current password is incorrect.");
            }
        } catch (Exception e) {
            showError("Error changing password: " + e.getMessage());
            logger.error("Password change error", e);
        }
    }

    @FXML
    private void handleCancel() {
        authService.logout();
        try {
            Stage stage = (Stage) submitBtn.getScene().getWindow();
            navigateTo(stage, "/fxml/Login.fxml", 480, 580);
        } catch (IOException e) {
            logger.error("Navigation error", e);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void navigateToDashboard(Stage stage, User user) throws IOException {
        String fxmlPath;
        switch (user.getRole()) {
            case "ADMIN": fxmlPath = "/fxml/AdminDashboard.fxml"; break;
            case "STUDENT": fxmlPath = "/fxml/StudentDashboard.fxml"; break;
            case "EMPLOYER": fxmlPath = "/fxml/EmployerDashboard.fxml"; break;
            default: throw new IllegalStateException("Unknown role: " + user.getRole());
        }
        navigateTo(stage, fxmlPath, 1100, 700);
    }

    private void navigateTo(Stage stage, String fxmlPath, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Scene scene = new Scene(loader.load(), width, height);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
        stage.setScene(scene);
        stage.centerOnScreen();
    }
}
