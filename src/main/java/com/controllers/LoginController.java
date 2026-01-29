package com.controllers;

import com.models.User;
import com.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller for Login view
 * Uses UserService for authentication and registration
 * 
 * Pattern: Controller -> Service -> DAO
 */
public class LoginController {
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblStatus;
    @FXML private VBox loginBox;
    @FXML private VBox registerBox;
    
    @FXML private TextField txtRegName;
    @FXML private TextField txtRegEmail;
    @FXML private PasswordField txtRegPassword;
    @FXML private TextField txtRegPhone;
    @FXML private TextField txtRegAddress;
    @FXML private Label lblRegStatus;

    // Service (not DAO)
    private UserService userService;
    private MainHost mainHost;

    @FXML
    public void initialize() {
        // Use service instead of DAO
        userService = UserService.getInstance();
    }

    public void setHost(MainHost host) {
        this.mainHost = host;
    }
    @FXML
    private void handleLogin() {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password");
            return;
        }

        User user = userService.authenticate(email, password);
        if (user != null) {
            userService.setCurrentUser(user);
            if ("admin".equalsIgnoreCase(user.getRole())) {
                mainHost.onAdminAuthenticated();
            } else {
                mainHost.onUserAuthenticated();
            }
        } else {
            showError("Invalid email or password");
        }
    }

    private void showError(String message) {
        lblStatus.setText(message);
    }

    @FXML
    private void handleGuest() {
        userService.setCurrentUser(new User(0, "Guest", "guest@example.com", "", "", "", "guest"));
        mainHost.onGuest();
    }

    @FXML
    private void handleAdminLogin() {
        mainHost.onSwitchToAdminLogin();
    }

    @FXML
    public void handleRegister() {
        String name = txtRegName.getText().trim();
        String email = txtRegEmail.getText().trim();
        String password = txtRegPassword.getText().trim();
        String phone = txtRegPhone.getText().trim();
        String address = txtRegAddress.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showRegError("Name, email, and password are required");
            return;
        }

        UserService.RegisterResult result = userService.registerUser(name, email, password, phone, address);

        if (result.isSuccess()) {
            User user = userService.authenticate(email, password);
            if (user != null) {
                userService.setCurrentUser(user);
                mainHost.onUserAuthenticated();
            }
        } else {
            showRegError(result.getMessage());
        }
    }

    @FXML
    public void showRegisterForm() {
        if (loginBox != null && registerBox != null) {
            loginBox.setVisible(false);
            loginBox.setManaged(false);
            registerBox.setVisible(true);
            registerBox.setManaged(true);
            lblStatus.setText("");
            lblRegStatus.setText("");
        }
    }

    @FXML
    public void showLoginForm() {
        if (loginBox != null && registerBox != null) {
            registerBox.setVisible(false);
            registerBox.setManaged(false);
            loginBox.setVisible(true);
            loginBox.setManaged(true);
            lblStatus.setText("");
            lblRegStatus.setText("");
        }
    }

    private void clearRegisterForm() {
        if (txtRegName != null) txtRegName.clear();
        if (txtRegEmail != null) txtRegEmail.clear();
        if (txtRegPassword != null) txtRegPassword.clear();
        if (txtRegPhone != null) txtRegPhone.clear();
        if (txtRegAddress != null) txtRegAddress.clear();
    }

    private void showRegError(String message) {
        if (lblRegStatus != null) {
            lblRegStatus.setText(message);
            lblRegStatus.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        }
    }

    public interface MainHost {
        void onAdminAuthenticated();
        void onUserAuthenticated();
        void onGuest();
        void onSwitchToAdminLogin();
    }
}
