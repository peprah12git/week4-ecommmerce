package com.controllers;

import com.models.Order;
import com.service.OrderService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Controller for Order History view
 * Uses OrderService for order retrieval
 * 
 * Pattern: Controller -> Service -> DAO
 */
public class OrderHistoryController {
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> dateColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> totalColumn;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Label emptyLabel;

    // Service (not DAOs)
    private OrderService orderService;
    private ClientViewController clientViewController;
    private int currentUserId;

    @FXML
    public void initialize() {
        // Use service instead of DAOs
        orderService = OrderService.getInstance();
        setupTable();
        setupFilters();
    }

    public void setClientViewController(ClientViewController controller) {
        this.clientViewController = controller;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    private void setupTable() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        dateColumn.setCellValueFactory(cellData -> {
            String dateStr = cellData.getValue().getOrderDate() != null ?
                    cellData.getValue().getOrderDate().toString() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(dateStr);
        });
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalColumn.setCellValueFactory(cellData -> {
            String total = "$" + cellData.getValue().getTotalAmount();
            return new javafx.beans.property.SimpleStringProperty(total);
        });
    }

    private void setupFilters() {
        // Use OrderService to get status options
        statusFilter.getItems().add("All");
        statusFilter.getItems().addAll(orderService.getStatusOptions());
        statusFilter.setValue("All");
    }

    public void loadOrders() {
        // Use OrderService to get orders
        List<Order> orders = orderService.getOrdersByUserId(currentUserId);
        
        if (orders.isEmpty()) {
            ordersTable.setVisible(false);
            emptyLabel.setVisible(true);
        } else {
            ordersTable.setVisible(true);
            emptyLabel.setVisible(false);
            ordersTable.getItems().setAll(orders);
        }
    }

    @FXML
    private void refreshOrders() {
        loadOrders();
    }

    @FXML
    private void backToProducts() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                com.Main.class.getResource("/design-application/views/client-modern.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) ordersTable.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
