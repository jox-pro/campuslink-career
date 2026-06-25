package com.campuslink.controllers;

import com.campuslink.models.Student;
import com.campuslink.models.User;
import com.campuslink.services.AuthService;
import com.campuslink.services.StudentService;
import com.campuslink.utils.ValidationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField courseField;
    @FXML private TextField yearField;
    @FXML private Label messageLabel;

    private final AuthService authService = new AuthService();
    private final StudentService studentService = new StudentService();

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
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String course = courseField.getText();
        String yearStr = yearField.getText();

        // Validation
        if (ValidationUtil.isNullOrEmpty(username)) { showMessage("Username is required.", true); return; }
        if (ValidationUtil.isNullOrEmpty(password)) { showMessage("Password is required.", true); return; }
        if (!ValidationUtil.isPasswordMatch(password, confirmPassword)) {
            showMessage("Passwords do not match or are too short (min 6 chars).", true); return;
        }
        if (ValidationUtil.isNullOrEmpty(fullName)) { showMessage("Full name is required.", true); return; }
        if (!ValidationUtil.isValidEmail(email)) { showMessage("Please enter a valid email address.", true); return; }
        if (!ValidationUtil.isValidPhone(phone)) { showMessage("Phone number format is invalid.", true); return; }
        if (ValidationUtil.isNullOrEmpty(course)) { showMessage("Course/programme is required.", true); return; }

        int year;
        try {
            year = Integer.parseInt(yearStr.trim());
            if (!ValidationUtil.isValidYearOfStudy(year)) {
                showMessage("Year of study must be between 1 and 7.", true); return;
            }
        } catch (NumberFormatException e) {
            showMessage("Year of study must be a number.", true); return;
        }

        // Register user account
        User user = authService.register(username.trim(), password, "STUDENT");
        if (user == null) {
            showMessage("Username already exists. Please choose a different username.", true);
            return;
        }

        // Create student profile
        Student student = new Student();
        student.setUserId(user.getId());
        student.setFullName(fullName.trim());
        student.setEmail(email.trim());
        student.setPhone(phone.trim());
        student.setCourse(course.trim());
        student.setYearOfStudy(year);

        boolean profileCreated = studentService.createProfile(student);
        if (!profileCreated) {
            showMessage("Account created but profile setup failed. Please update your profile after login.", false);
        } else {
            showMessage("Registration successful! Redirecting...", false);
        }

        // Navigate to StudentDashboard
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StudentDashboard.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 700);
            scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm()
            );
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            showMessage("Registration successful! Please login.", false);
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(loader.load(), 480, 600);
            scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm()
            );
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Navigation error: " + e.getMessage());
        }
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.setStyle(isError ? "-fx-text-fill: #C62828;" : "-fx-text-fill: #2E7D32;");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
