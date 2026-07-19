package controllers;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.User;

import java.io.IOException;
import java.sql.SQLException;

public class LoginRegisterController {

    @FXML private Label formTitleLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    @FXML private Button primaryActionButton;
    @FXML private Button toggleModeButton;

    private final UserDAO userDAO = new UserDAO();
    private boolean registerMode = false;

    @FXML
    private void handleToggleMode() {
        registerMode = !registerMode;
        messageLabel.setText("");
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();

        if (registerMode) {
            formTitleLabel.setText("Create a Customer Account");
            primaryActionButton.setText("Register");
            toggleModeButton.setText("Already have an account? Login");
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
        } else {
            formTitleLabel.setText("Login");
            primaryActionButton.setText("Login");
            toggleModeButton.setText("Don't have an account? Register");
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
        }
    }

    @FXML
    private void handlePrimaryAction() {
        if (registerMode) {
            handleRegister();
        } else {
            handleLogin();
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            return;
        }

        try {
            User user = userDAO.login(username, password);
            if (user == null) {
                messageLabel.setText("Invalid username or password.");
                return;
            }
            if (user.isAdmin()) {
                openDashboard("/views/AdminDashboard.fxml", user, "Admin Dashboard");
            } else {
                openDashboard("/views/CustomerDashboard.fxml", user, "Customer Dashboard");
            }
        } catch (SQLException e) {
            messageLabel.setText("Database error: " + e.getMessage());
        } catch (IOException e) {
            messageLabel.setText("Unable to load dashboard: " + e.getMessage());
        }
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password.");
            return;
        }
        if (username.length() < 3) {
            messageLabel.setText("Username must be at least 3 characters.");
            return;
        }
        if (password.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        try {
            if (!userDAO.isUsernameAvailable(username)) {
                messageLabel.setText("That username is already taken.");
                return;
            }
            boolean success = userDAO.register(username, password);
            if (success) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Account created! You can now log in.");
                handleToggleMode(); // flip back to login mode
            } else {
                messageLabel.setText("Registration failed. Please try again.");
            }
        } catch (SQLException e) {
            messageLabel.setText("Database error: " + e.getMessage());
        }
    }

    private void openDashboard(String fxmlPath, User user, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Pass the logged-in user into whichever controller was loaded.
        Object controller = loader.getController();
        if (controller instanceof AdminDashboardController) {
            ((AdminDashboardController) controller).setCurrentUser(user);
        } else if (controller instanceof CustomerDashboardController) {
            ((CustomerDashboardController) controller).setCurrentUser(user);
        }

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.centerOnScreen();
    }
}
