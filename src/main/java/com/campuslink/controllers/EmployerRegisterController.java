package com.campuslink.controllers;

import com.campuslink.models.Employer;
import com.campuslink.models.User;
import com.campuslink.services.AuthService;
import com.campuslink.utils.ValidationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class EmployerRegisterController {
    private static final Logger logger = LoggerFactory.getLogger(EmployerRegisterController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField companyNameField;
    @FXML private TextField contactPersonField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressArea;
    @FXML private Label messageLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String companyName = companyNameField.getText();
        String email = emailField.getText();
        String contactPerson = contactPersonField.getText();
        String phone = phoneField.getText();
        String address = addressArea.getText();

        if (ValidationUtil.isNullOrEmpty(username)) { showMessage("Username is required.", true); return; }
        if (ValidationUtil.isNullOrEmpty(password)) { showMessage("Password is required.", true); return; }
        if (!ValidationUtil.isPasswordMatch(password, confirmPassword)) {
            showMessage("Passwords do not match or are too short (min 6 chars).", true); return;
        }
        if (ValidationUtil.isNullOrEmpty(companyName)) { showMessage("Company name is required.", true); return; }
        if (!ValidationUtil.isValidEmail(email)) { showMessage("Please enter a valid email address.", true); return; }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password);

        Employer employer = new Employer();
        employer.setCompanyName(companyName.trim());
        employer.setContactPerson(contactPerson.trim());
        employer.setEmail(email.trim());
        employer.setPhone(phone.trim());
        employer.setAddress(address.trim());

        if (authService.registerEmployer(user, employer)) {
            showMessage("Registration successful! Redirecting...", false);
            try {
                Stage stage = (Stage) usernameField.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EmployerDashboard.fxml"));
                Scene scene = new Scene(loader.load(), 1100, 700);
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
                stage.setScene(scene);
                stage.setResizable(true);
                stage.centerOnScreen();
            } catch (IOException e) {
                showMessage("Registration successful! Please login.", false);
                logger.error("Navigation error after employer registration", e);
            }
        } else {
            showMessage("Registration failed. Username might already exist or invalid data.", true);
        }
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(loader.load(), 480, 600);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            logger.error("Navigation error back to login", e);
        }
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.setStyle(isError ? "-fx-text-fill: #C62828;" : "-fx-text-fill: #2E7D32;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
