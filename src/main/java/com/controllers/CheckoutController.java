package com.controllers;

import com.ecommerce.models.CartItem;
import com.ecommerce.models.Order;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Controller for Checkout view
 * Uses OrderService for order processing
 * 
 * Pattern: Controller -> Service -> DAO
 */
public class CheckoutController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private TextField cityField;
    @FXML private TextField postalCodeField;
    @FXML private TextArea orderNotesField;
    @FXML private TableView<CartItem> summaryTable;
    @FXML private TableColumn<CartItem, String> productColumn;
    @FXML private TableColumn<CartItem, Integer> qtyColumn;
    @FXML private TableColumn<CartItem, BigDecimal> priceColumn;
    @FXML private Label checkoutSubtotal;
    @FXML private Label checkoutTax;
    @FXML private Label checkoutTotal;

    private ClientViewController clientViewController;
    private java.util.List<CartItem> cartItems;
    
    // Service (not DAOs)
    private OrderService orderService;

    public void setCartItems(java.util.List<CartItem> items) {
        this.cartItems = items;
        if (summaryTable != null) {
            summaryTable.setItems(javafx.collections.FXCollections.observableArrayList(items));
            updateSummary();
        }
    }

    @FXML
    public void initialize() {
        // Use service instead of DAOs
        orderService = OrderService.getInstance();
        setupTableColumns();
        displayOrderSummary();
        prefillUserInfo();
    }

    public void setClientViewController(ClientViewController controller) {
        this.clientViewController = controller;
    }

    private void setupTableColumns() {
        productColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("productName"));
        qtyColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("subtotal"));
    }

    private void prefillUserInfo() {
        // Pre-fill user information if logged in
        if (com.ecommerce.service.UserService.getInstance().getCurrentUser() != null) {
            nameField.setText(com.ecommerce.service.UserService.getInstance().getCurrentUser().getName());
            emailField.setText(com.ecommerce.service.UserService.getInstance().getCurrentUser().getEmail());
            if (com.ecommerce.service.UserService.getInstance().getCurrentUser().getPhone() != null) {
                phoneField.setText(com.ecommerce.service.UserService.getInstance().getCurrentUser().getPhone());
            }
            if (com.ecommerce.service.UserService.getInstance().getCurrentUser().getAddress() != null) {
                addressField.setText(com.ecommerce.service.UserService.getInstance().getCurrentUser().getAddress());
            }
        }
    }

    private void displayOrderSummary() {
        if (cartItems != null) {
            summaryTable.setItems(javafx.collections.FXCollections.observableArrayList(cartItems));
        } else {
            summaryTable.setItems(CartService.getInstance().getCartItems());
        }
        updateSummary();
    }

    private void updateSummary() {
        java.util.List<CartItem> items = cartItems != null ? cartItems : 
            new ArrayList<>(CartService.getInstance().getCartItems());
        
        BigDecimal subtotal = items.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.10"));
        BigDecimal total = subtotal.add(tax);

        checkoutSubtotal.setText(String.format("$%.2f", subtotal));
        checkoutTax.setText(String.format("$%.2f", tax));
        checkoutTotal.setText(String.format("$%.2f", total));
    }

    @FXML
    public void placeOrder() {
        if (nameField.getText().isEmpty() || emailField.getText().isEmpty() ||
            phoneField.getText().isEmpty() || addressField.getText().isEmpty() ||
            cityField.getText().isEmpty() || postalCodeField.getText().isEmpty()) {
            showAlert("Validation Error", "Please fill in all required fields");
            return;
        }

        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Validation Error", "Please enter a valid email address");
            return;
        }

        java.util.List<CartItem> items = cartItems != null ? cartItems : 
            new ArrayList<>(CartService.getInstance().getCartItems());
        
        if (items.isEmpty()) {
            showAlert("Empty Cart", "Your cart is empty");
            return;
        }

        try {
            int userId = com.ecommerce.service.UserService.getInstance().getCurrentUser() != null ? 
                        com.ecommerce.service.UserService.getInstance().getCurrentUser().getUserId() : 0;
            
            String shippingAddress = String.format("%s, %s %s",
                    addressField.getText().trim(),
                    cityField.getText().trim(),
                    postalCodeField.getText().trim());
            
            System.out.println("Creating order for user: " + userId);
            System.out.println("Cart items: " + items.size());
            
            OrderService.OrderResult result = orderService.createOrder(userId, new ArrayList<>(items));

            System.out.println("Order result: " + result.isSuccess() + ", Message: " + result.getMessage());
            
            if (result.isSuccess()) {
                CartService.getInstance().clearCart();
                if (clientViewController != null) {
                    clientViewController.updateCartButton();
                }

                showOrderConfirmation(result.getOrder(), shippingAddress);
                backToMain();
            } else {
                showAlert("Error", "Failed to create order: " + result.getMessage());
            }
        } catch (Exception e) {
            showAlert("Error", "Error placing order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void backToMain() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                com.ecommerce.Main.class.getResource("/com/ecommerce/client-modern.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) nameField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showOrderConfirmation(Order order, String shippingAddress) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Confirmed!");
        alert.setHeaderText("Thank you for your order!");
        alert.setContentText(String.format(
                "Order ID: %d\n" +
                "Total: $%.2f\n\n" +
                "Shipping to:\n%s\n%s\n\n" +
                "You will receive a confirmation email shortly.",
                order.getOrderId(),
                order.getTotalAmount(),
                nameField.getText(),
                shippingAddress
        ));
        alert.showAndWait();
    }

    @FXML
    public void backToCart() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    com.ecommerce.Main.class.getResource("/com/ecommerce/cart-view.fxml"));
            javafx.scene.Parent root = loader.load();
            CartViewController controller = loader.getController();
            controller.setClientViewController(clientViewController);

            javafx.scene.layout.StackPane mainContent = 
                    (javafx.scene.layout.StackPane) nameField.getScene().lookup("#mainContent");
            if (mainContent != null) {
                mainContent.getChildren().setAll(root);
            }
        } catch (Exception e) {
            System.err.println("Error loading cart: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
