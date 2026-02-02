package com;

import com.util.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private Stage primaryStage;
    
    @Override
    public void start(Stage stage) throws IOException {
        this.primaryStage = stage;
        
        // Initialize database on startup
        System.out.println(" Starting e-Commerce Application...");
        DatabaseInitializer.initializeDatabase();
        
        showLogin(); // Start with user/guest login
    }

    private void showAdminLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/design-application/views/admin-login.fxml"));
        Parent root = loader.load();

        com.controllers.AdminLoginController controller = loader.getController();
        controller.setHost(new com.controllers.AdminLoginController.AdminLoginHost() {
            @Override
            public void onAdminAuthenticated() {
                try {
                    showAdminView();
                } catch (IOException e) {
                    showError("Failed to load admin view", e);
                }
            }

            @Override
            public void onSwitchToUserLogin() {
                try {
                    showLogin();
                } catch (IOException e) {
                    showError("Failed to load user login", e);
                }
            }
        });

        Scene scene = new Scene(root, 450, 400);
        primaryStage.setTitle("Admin Login - E-Commerce System");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void showLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/design-application/views/login-view-new.fxml"));
        Parent root = loader.load();

        com.controllers.LoginController controller = loader.getController();
        controller.setHost(new com.controllers.LoginController.MainHost() {
            @Override
            public void onAdminAuthenticated() {
                try {
                    showAdminView();
                } catch (IOException e) {
                    showError("Failed to load admin view", e);
                }
            }

            @Override
            public void onUserAuthenticated() {
                try {
                    showClientView();
                } catch (IOException e) {
                    showError("Failed to load client view", e);
                }
            }

            @Override
            public void onGuest() {
                try {
                    showClientView();
                } catch (IOException e) {
                    showError("Failed to load client view", e);
                }
            }

            @Override
            public void onSwitchToAdminLogin() {
                try {
                    showAdminLogin();
                } catch (IOException e) {
                    showError("Failed to load admin login", e);
                }
            }
        });

        Scene scene = new Scene(root);
        primaryStage.setTitle("Sign In - Smart E-Commerce System");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void showAdminView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/design-application/views/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1400, 800);

        primaryStage.setTitle("Smart E-Commerce System - Admin");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(700);
        primaryStage.show();

        System.out.println("✓ Admin view loaded");
    }

    private void showClientView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/design-application/views/client-modern.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);

        primaryStage.setTitle("Smart E-Commerce Store");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();

        System.out.println("✓ Modern client view loaded for " + 
            com.controllers.UserSession.getCurrentUserName());
    }

    private void showError(String message, Exception e) {
        System.err.println("✗ " + message + ": " + e.getMessage());
        e.printStackTrace();
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
    
    @Override
    public void stop() {
        System.out.println("✓ Application closing...");
        com.config.DatabaseConnection.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch();
    }
}