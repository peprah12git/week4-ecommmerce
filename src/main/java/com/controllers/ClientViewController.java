package com.controllers;

import com.ecommerce.Main;
import com.ecommerce.service.CartService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientViewController {
    @FXML private StackPane mainContent;
    @FXML private Button cartButton;
    @FXML private Button ordersButton;
    @FXML private Button logoutButton;
    @FXML private Label userLabel;

    private int currentUserId;
    private String currentUserName;

    @FXML
    public void initialize() {
        currentUserId = UserSession.getCurrentUserId();
        currentUserName = UserSession.getCurrentUserName();
        userLabel.setText("Welcome, " + currentUserName);
        loadProductBrowser();
        updateCartButton();
    }

    public void setUser(int userId, String userName) {
        this.currentUserId = userId;
        this.currentUserName = userName;
        userLabel.setText("Welcome, " + userName);
    }

    private void loadProductBrowser() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/ecommerce/product-browser.fxml"));
            Parent root = loader.load();
            ProductBrowserController controller = loader.getController();
            controller.setClientViewController(this);
            controller.setCurrentUserId(currentUserId);
            mainContent.getChildren().setAll(root);
            System.out.println("✓ Product browser loaded successfully");
        } catch (IOException e) {
            System.err.println("Error loading product browser: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showCart() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/ecommerce/cart-view.fxml"));
            Parent root = loader.load();
            CartViewController controller = loader.getController();
            controller.setClientViewController(this);
            mainContent.getChildren().setAll(root);
        } catch (IOException e) {
            System.err.println("Error loading cart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/ecommerce/order-history.fxml"));
            Parent root = loader.load();
            OrderHistoryController controller = loader.getController();
            controller.setClientViewController(this);
            controller.setCurrentUserId(currentUserId);
            controller.loadOrders();
            mainContent.getChildren().setAll(root);
        } catch (IOException e) {
            System.err.println("Error loading orders: " + e.getMessage());
        }
    }

    @FXML
    private void logout() {
        try {
            Main main = new Main();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            main.start(stage);
        } catch (Exception e) {
            System.err.println("Error logging out: " + e.getMessage());
        }
    }

    public void updateCartButton() {
        int itemCount = CartService.getInstance().getItemCount();
        cartButton.setText("🛒 Cart (" + itemCount + ")");
    }

    public void backToProducts() {
        loadProductBrowser();
    }
}
