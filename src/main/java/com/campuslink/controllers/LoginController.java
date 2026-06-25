package com.campuslink.controllers;

import com.campuslink.models.User;
import com.campuslink.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginBtn;
    @FXML private Hyperlink registerLink;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.trim().isEmpty()) {
            showError("Please enter your username.");
            return;
        }
        if (password == null || password.isEmpty()) {
            showError("Please enter your password.");
            return;
        }

        try {
            User user = authService.login(username, password);
            if (user == null) {
                showError("Invalid username or password. Please try again.");
                passwordField.clear();
                return;
            }

            Stage stage = (Stage) loginBtn.getScene().getWindow();
            switch (user.getRole()) {
                case "ADMIN":
                    navigateTo(stage, "/fxml/AdminDashboard.fxml", 1100, 700);
                    break;
                case "STUDENT":
                    navigateTo(stage, "/fxml/StudentDashboard.fxml", 1100, 700);
                    break;
                case "EMPLOYER":
                    navigateTo(stage, "/fxml/EmployerDashboard.fxml", 1100, 700);
                    break;
                default:
                    showError("Unknown user role: " + user.getRole());
            }
        } catch (Exception e) {
            showError("Login failed: " + e.getMessage());
            System.err.println("Login error: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        try {
            Stage stage = (Stage) registerLink.getScene().getWindow();
            navigateTo(stage, "/fxml/Register.fxml", 480, 680);
        } catch (Exception e) {
            showError("Cannot open registration: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    public void navigateTo(Stage stage, String fxmlPath, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource(fxmlPath)
        );
        Scene scene = new Scene(loader.load(), width, height);
        scene.getStylesheets().add(
            Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm()
        );
        stage.setScene(scene);
        stage.setResizable(true);
        stage.centerOnScreen();
    }
}
