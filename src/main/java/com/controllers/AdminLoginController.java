package com.controllers;

import com.models.User;
import com.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for Admin Login view
 * Uses UserService for admin authentication
 * 
 * Pattern: Controller -> Service -> DAO
 */
public class AdminLoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    // Service (not DAO)
    private UserService userService;
    private AdminLoginHost host;

    @FXML
    public void initialize() {
        // Use service instead of DAO
        userService = UserService.getInstance();
    }

    public void setHost(AdminLoginHost host) {
        this.host = host;
    }

    @FXML
    private void handleAdminLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        // Use UserService for admin authentication
        User user = userService.authenticateAdmin(email, password);
        
        if (user == null) {
            showError("Invalid credentials or access denied");
            return;
        }

        // Admin authenticated successfully
        UserSession.setCurrentUser(user);
        if (host != null) {
            host.onAdminAuthenticated();
        }
    }

    @FXML
    private void handleSwitchToUserLogin() {
        if (host != null) {
            host.onSwitchToUserLogin();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    public interface AdminLoginHost {
        void onAdminAuthenticated();
        void onSwitchToUserLogin();
    }
}
