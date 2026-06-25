package com.campuslink.controllers;

import com.campuslink.models.Student;
import com.campuslink.services.StudentService;
import com.campuslink.utils.SessionManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class StudentController {

    // ========== StudentList view fields ==========
    @FXML private TableView<Student> tableView;
    @FXML private TableColumn<Student, Integer> colId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colEmail;
    @FXML private TableColumn<Student, String> colCourse;
    @FXML private TableColumn<Student, Integer> colYear;
    @FXML private TableColumn<Student, String> colPhone;
    @FXML private TableColumn<Student, String> colSkills;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    // ========== StudentProfile view fields ==========
    @FXML private TextField fieldFullName;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldPhone;
    @FXML private TextField fieldCourse;
    @FXML private TextField fieldYear;
    @FXML private TextArea fieldSkills;
    @FXML private TextField fieldCvPath;
    @FXML private Label profileMessage;

    private final StudentService studentService = new StudentService();
    private Student currentStudent;

    @FXML
    public void initialize() {
        if (tableView != null) {
            initTableColumns();
            loadAllStudents();
        } else if (fieldFullName != null) {
            loadCurrentStudentProfile();
        }
    }

    // ===== LIST VIEW =====

    private void initTableColumns() {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getStudentId()).asObject());
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colCourse.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCourse()));
        colYear.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getYearOfStudy()).asObject());
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        colSkills.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSkills()));
    }

    private void loadAllStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            tableView.setItems(FXCollections.observableArrayList(students));
            if (statusLabel != null) statusLabel.setText("Total: " + students.size() + " students");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load students: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        if (searchField == null) return;
        String keyword = searchField.getText();
        List<Student> results = studentService.searchStudents(keyword);
        tableView.setItems(FXCollections.observableArrayList(results));
        if (statusLabel != null) statusLabel.setText("Found: " + results.size() + " students");
    }

    @FXML
    private void handleClearSearch() {
        if (searchField != null) searchField.clear();
        loadAllStudents();
    }

    @FXML
    private void handleAdd() {
        // Open a simple dialog to add a student (admin use)
        showAlert(Alert.AlertType.INFORMATION, "Info",
            "To add a student, ask them to register via the Student Registration page.");
    }

    @FXML
    private void handleEdit() {
        Student selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to edit."); return; }

        // Open a quick edit dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Student");
        dialog.setHeaderText("Edit: " + selected.getFullName());

        TextField nameF = new TextField(selected.getFullName());
        TextField emailF = new TextField(selected.getEmail());
        TextField phoneF = new TextField(selected.getPhone() != null ? selected.getPhone() : "");
        TextField courseF = new TextField(selected.getCourse() != null ? selected.getCourse() : "");
        TextField yearF = new TextField(String.valueOf(selected.getYearOfStudy()));

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Full Name:"), nameF);
        grid.addRow(1, new Label("Email:"), emailF);
        grid.addRow(2, new Label("Phone:"), phoneF);
        grid.addRow(3, new Label("Course:"), courseF);
        grid.addRow(4, new Label("Year:"), yearF);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            selected.setFullName(nameF.getText());
            selected.setEmail(emailF.getText());
            selected.setPhone(phoneF.getText());
            selected.setCourse(courseF.getText());
            try { selected.setYearOfStudy(Integer.parseInt(yearF.getText())); } catch (NumberFormatException ignored) {}
            if (studentService.updateProfile(selected)) {
                loadAllStudents();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Student updated successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update student.");
            }
        }
    }

    @FXML
    private void handleDelete() {
        Student selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a student to delete."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Delete student: " + selected.getFullName() + "?\nThis will also delete their user account.",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            if (studentService.deleteStudent(selected.getStudentId())) {
                loadAllStudents();
                showAlert(Alert.AlertType.INFORMATION, "Deleted", "Student deleted successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete student.");
            }
        }
    }

    @FXML
    private void handleRefresh() {
        if (searchField != null) searchField.clear();
        loadAllStudents();
    }

    // ===== PROFILE VIEW =====

    private void loadCurrentStudentProfile() {
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        currentStudent = studentService.getProfile(userId);
        if (currentStudent != null) {
            fieldFullName.setText(currentStudent.getFullName() != null ? currentStudent.getFullName() : "");
            fieldEmail.setText(currentStudent.getEmail() != null ? currentStudent.getEmail() : "");
            fieldPhone.setText(currentStudent.getPhone() != null ? currentStudent.getPhone() : "");
            fieldCourse.setText(currentStudent.getCourse() != null ? currentStudent.getCourse() : "");
            fieldYear.setText(currentStudent.getYearOfStudy() > 0 ? String.valueOf(currentStudent.getYearOfStudy()) : "");
            fieldSkills.setText(currentStudent.getSkills() != null ? currentStudent.getSkills() : "");
            fieldCvPath.setText(currentStudent.getCvPath() != null ? currentStudent.getCvPath() : "");
        }
        if (profileMessage != null) { profileMessage.setVisible(false); profileMessage.setManaged(false); }
    }

    @FXML
    private void handleSave() {
        if (currentStudent == null) {
            // New profile creation
            currentStudent = new Student();
            currentStudent.setUserId(SessionManager.getInstance().getCurrentUser().getId());
        }

        String fullName = fieldFullName.getText();
        String email = fieldEmail.getText();
        String phone = fieldPhone.getText();
        String course = fieldCourse.getText();
        String yearStr = fieldYear.getText();
        String skills = fieldSkills.getText();
        String cvPath = fieldCvPath.getText();

        if (fullName == null || fullName.trim().isEmpty()) {
            showProfileMessage("Full name is required.", true); return;
        }

        int year = 1;
        if (yearStr != null && !yearStr.trim().isEmpty()) {
            try { year = Integer.parseInt(yearStr.trim()); } catch (NumberFormatException e) {
                showProfileMessage("Year of study must be a number.", true); return;
            }
        }

        currentStudent.setFullName(fullName.trim());
        currentStudent.setEmail(email.trim());
        currentStudent.setPhone(phone.trim());
        currentStudent.setCourse(course.trim());
        currentStudent.setYearOfStudy(year);
        currentStudent.setSkills(skills);
        currentStudent.setCvPath(cvPath);

        boolean success;
        if (currentStudent.getStudentId() == 0) {
            success = studentService.createProfile(currentStudent);
        } else {
            success = studentService.updateProfile(currentStudent);
        }

        if (success) {
            showProfileMessage("Profile saved successfully!", false);
        } else {
            showProfileMessage("Failed to save profile. Please check your details.", true);
        }
    }

    @FXML
    private void handleCancel() {
        loadCurrentStudentProfile();
    }

    @FXML
    private void handleBrowseCV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select CV File");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        Stage stage = (Stage) fieldCvPath.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            fieldCvPath.setText(file.getAbsolutePath());
        }
    }

    private void showProfileMessage(String text, boolean isError) {
        if (profileMessage == null) return;
        profileMessage.setText(text);
        profileMessage.setStyle(isError ? "-fx-text-fill: #C62828;" : "-fx-text-fill: #2E7D32;");
        profileMessage.setVisible(true);
        profileMessage.setManaged(true);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
