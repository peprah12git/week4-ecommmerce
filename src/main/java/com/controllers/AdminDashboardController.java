package com.controllers;

import com.ecommerce.Main;
import com.ecommerce.models.Category;
import com.ecommerce.models.Order;
import com.ecommerce.models.Product;
import com.ecommerce.models.User;
import com.ecommerce.service.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Admin Dashboard
 * Provides CRUD operations for Products, Orders, Users, and Inventory
 * 
 * Pattern: Controller -> Service -> DAO
 */
public class AdminDashboardController {
    
    // FXML injected components
    @FXML private Label adminNameLabel;
    @FXML private Label statusLabel;
    @FXML private StackPane contentArea;
    @FXML private Button logoutButton;
    
    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button productsBtn;
    @FXML private Button ordersBtn;
    @FXML private Button usersBtn;
    @FXML private Button inventoryBtn;
    @FXML private Button performanceBtn;
    
    // Services
    private ProductService productService;
    private OrderService orderService;
    private UserService userService;
    private InventoryService inventoryService;
    private CategoryService categoryService;
    
    // Current data
    private ObservableList<Product> productList;
    private ObservableList<Order> orderList;
    private ObservableList<User> userList;
    
    @FXML
    public void initialize() {
        // Initialize services
        productService = ProductService.getInstance();
        orderService = OrderService.getInstance();
        userService = UserService.getInstance();
        inventoryService = new InventoryService();
        categoryService = CategoryService.getInstance();
        
        // Set admin name
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            adminNameLabel.setText("Welcome, " + currentUser.getName());
        } else {
            adminNameLabel.setText("Welcome, Admin");
        }
        
        // Show dashboard by default
        showDashboard();
    }
    
    // ============ NAVIGATION METHODS ============
    
    @FXML
    private void showDashboard() {
        setActiveButton(dashboardBtn);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createDashboardView());
        updateStatus("Dashboard loaded");
    }
    
    @FXML
    private void showProducts() {
        setActiveButton(productsBtn);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createProductsView());
        updateStatus("Products management loaded");
    }
    
    @FXML
    private void showOrders() {
        setActiveButton(ordersBtn);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createOrdersView());
        updateStatus("Orders management loaded");
    }
    
    @FXML
    private void showUsers() {
        setActiveButton(usersBtn);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createUsersView());
        updateStatus("Users management loaded");
    }
    
    @FXML
    private void showInventory() {
        setActiveButton(inventoryBtn);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(createInventoryView());
        updateStatus("Inventory management loaded");
    }

    @FXML
    private void showPerformanceReport() {
        setActiveButton(performanceBtn);
        contentArea.getChildren().clear();
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                Main.class.getResource("/com/ecommerce/views/PerformanceReport.fxml"));
            javafx.scene.Parent performanceView = loader.load();
            contentArea.getChildren().add(performanceView);
            updateStatus("Performance report loaded");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load performance report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLogout() {
        userService.setCurrentUser(null);
        try {
            Main main = new Main();
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            main.start(stage);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
        }
    }
    
    // ============ DASHBOARD VIEW ============
    
    private VBox createDashboardView() {
        VBox dashboard = new VBox(20);
        dashboard.setPadding(new Insets(20));
        dashboard.setStyle("-fx-background-color: #f5f6fa;");
        
        // Title
        Label title = new Label("📊 Admin Dashboard");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Stats cards
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);
        
        List<Product> products = productService.getAllProducts();
        List<Order> orders = orderService.getAllOrders();
        List<User> users = userService.getAllUsers();
        
        // Calculate statistics
        long pendingOrders = orders.stream()
                .filter(o -> "Pending".equalsIgnoreCase(o.getStatus()))
                .count();
        
        BigDecimal totalRevenue = orders.stream()
                .filter(o -> "Completed".equalsIgnoreCase(o.getStatus()) || "Delivered".equalsIgnoreCase(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        statsRow.getChildren().addAll(
            createStatCard("📦 Products", String.valueOf(products.size()), "#3498db"),
            createStatCard("🛒 Orders", String.valueOf(orders.size()), "#9b59b6"),
            createStatCard("👥 Users", String.valueOf(users.size()), "#27ae60"),
            createStatCard("⏳ Pending", String.valueOf(pendingOrders), "#e67e22"),
            createStatCard("💰 Revenue", String.format("$%.2f", totalRevenue), "#1abc9c")
        );
        
        // Recent orders table
        Label recentLabel = new Label("📋 Recent Orders");
        recentLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        TableView<Order> recentOrdersTable = createRecentOrdersTable(orders);
        VBox.setVgrow(recentOrdersTable, Priority.ALWAYS);
        
        dashboard.getChildren().addAll(title, statsRow, recentLabel, recentOrdersTable);
        return dashboard;
    }
    
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(180);
        card.setPrefHeight(120);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }
    
    private TableView<Order> createRecentOrdersTable(List<Order> orders) {
        TableView<Order> table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        TableColumn<Order, Integer> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        idCol.setPrefWidth(80);
        
        TableColumn<Order, String> userCol = new TableColumn<>("Customer");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userCol.setPrefWidth(150);
        
        TableColumn<Order, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getOrderDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getOrderDate().toString());
            }
            return new SimpleStringProperty("N/A");
        });
        dateCol.setPrefWidth(180);
        
        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        TableColumn<Order, BigDecimal> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalCol.setPrefWidth(100);
        
        table.getColumns().addAll(idCol, userCol, dateCol, statusCol, totalCol);
        
        // Show only 10 most recent orders
        List<Order> recentOrders = orders.stream()
                .limit(10)
                .toList();
        table.setItems(FXCollections.observableArrayList(recentOrders));
        
        return table;
    }
    
    // ============ PRODUCTS VIEW ============
    
    private VBox createProductsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f6fa;");
        
        // Header with title and add button
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("📦 Product Management");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addBtn = new Button("+ Add Product");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; " +
                       "-fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        addBtn.setOnAction(e -> showAddProductDialog());
        
        header.getChildren().addAll(title, spacer, addBtn);
        
        // Product table
        TableView<Product> productTable = createProductTable();
        VBox.setVgrow(productTable, Priority.ALWAYS);
        
        container.getChildren().addAll(header, productTable);
        return container;
    }
    
    private TableView<Product> createProductTable() {
        TableView<Product> table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        idCol.setPrefWidth(60);
        
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        nameCol.setPrefWidth(200);
        
        TableColumn<Product, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(250);
        
        TableColumn<Product, BigDecimal> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);
        
        TableColumn<Product, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryCol.setPrefWidth(120);
        
        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        stockCol.setPrefWidth(80);
        
        TableColumn<Product, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(180);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);
            
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                buttons.setAlignment(Pos.CENTER);
                
                editBtn.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showEditProductDialog(product);
                });
                
                deleteBtn.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleDeleteProduct(product, getTableView());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, descCol, priceCol, categoryCol, stockCol, actionsCol);
        
        // Load products
        productList = FXCollections.observableArrayList(productService.getAllProducts());
        table.setItems(productList);
        
        return table;
    }
    
    private void showAddProductDialog() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Add New Product");
        dialog.setHeaderText("Enter product details");
        
        // Buttons
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");
        
        TextArea descField = new TextArea();
        descField.setPromptText("Description");
        descField.setPrefRowCount(3);
        
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        
        ComboBox<Category> categoryBox = new ComboBox<>();
        categoryBox.setPromptText("Select Category");
        categoryBox.setItems(FXCollections.observableArrayList(categoryService.getAllCategories()));
        
        TextField stockField = new TextField();
        stockField.setPromptText("Initial Stock");
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryBox, 1, 3);
        grid.add(new Label("Stock:"), 0, 4);
        grid.add(stockField, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    Product product = new Product();
                    product.setProductName(nameField.getText());
                    product.setDescription(descField.getText());
                    product.setPrice(new BigDecimal(priceField.getText()));
                    if (categoryBox.getValue() != null) {
                        product.setCategoryId(categoryBox.getValue().getCategoryId());
                    }
                    return product;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid price");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(product -> {
            if (productService.addProduct(product)) {
                // Update stock if specified
                try {
                    int stock = Integer.parseInt(stockField.getText());
                    if (stock > 0) {
                        inventoryService.updateStock(product.getProductId(), stock);
                    }
                } catch (NumberFormatException ignored) {}
                
                updateStatus("Product added successfully");
                showProducts(); // Refresh view
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add product");
            }
        });
    }
    
    private void showEditProductDialog(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        dialog.setHeaderText("Update product details");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField(product.getProductName());
        TextArea descField = new TextArea(product.getDescription());
        descField.setPrefRowCount(3);
        TextField priceField = new TextField(product.getPrice().toString());
        
        ComboBox<Category> categoryBox = new ComboBox<>();
        categoryBox.setItems(FXCollections.observableArrayList(categoryService.getAllCategories()));
        // Select current category
        for (Category cat : categoryBox.getItems()) {
            if (cat.getCategoryId() == product.getCategoryId()) {
                categoryBox.setValue(cat);
                break;
            }
        }
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Price:"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryBox, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    product.setProductName(nameField.getText());
                    product.setDescription(descField.getText());
                    product.setPrice(new BigDecimal(priceField.getText()));
                    if (categoryBox.getValue() != null) {
                        product.setCategoryId(categoryBox.getValue().getCategoryId());
                    }
                    return product;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid price");
                    return null;
                }
            }
            return null;
        });
        
        Optional<Product> result = dialog.showAndWait();
        result.ifPresent(updatedProduct -> {
            if (productService.updateProduct(updatedProduct)) {
                updateStatus("Product updated successfully");
                showProducts();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update product");
            }
        });
    }
    
    private void handleDeleteProduct(Product product, TableView<Product> table) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Product");
        confirm.setContentText("Are you sure you want to delete: " + product.getProductName() + "?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (productService.deleteProduct(product.getProductId())) {
                productList.remove(product);
                updateStatus("Product deleted successfully");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete product. It may have associated orders.");
            }
        }
    }
    
    // ============ ORDERS VIEW ============
    
    private VBox createOrdersView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f6fa;");
        
        Label title = new Label("🛒 Order Management");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Filter row
        HBox filterRow = new HBox(15);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Pending", "Processing", "Shipped", "Delivered", "Cancelled");
        statusFilter.setValue("All");
        statusFilter.setStyle("-fx-pref-width: 150;");
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        
        filterRow.getChildren().addAll(new Label("Filter by Status:"), statusFilter, refreshBtn);
        
        // Orders table
        TableView<Order> orderTable = createOrderTable();
        VBox.setVgrow(orderTable, Priority.ALWAYS);
        
        // Filter functionality
        statusFilter.setOnAction(e -> {
            String selected = statusFilter.getValue();
            if ("All".equals(selected)) {
                orderTable.setItems(FXCollections.observableArrayList(orderService.getAllOrders()));
            } else {
                List<Order> filtered = orderService.getAllOrders().stream()
                        .filter(o -> selected.equalsIgnoreCase(o.getStatus()))
                        .toList();
                orderTable.setItems(FXCollections.observableArrayList(filtered));
            }
        });
        
        refreshBtn.setOnAction(e -> {
            statusFilter.setValue("All");
            orderTable.setItems(FXCollections.observableArrayList(orderService.getAllOrders()));
            updateStatus("Orders refreshed");
        });
        
        container.getChildren().addAll(title, filterRow, orderTable);
        return container;
    }
    
    private TableView<Order> createOrderTable() {
        TableView<Order> table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        TableColumn<Order, Integer> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        idCol.setPrefWidth(80);
        
        TableColumn<Order, String> userCol = new TableColumn<>("Customer");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userCol.setPrefWidth(150);
        
        TableColumn<Order, String> dateCol = new TableColumn<>("Order Date");
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getOrderDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getOrderDate().toString());
            }
            return new SimpleStringProperty("N/A");
        });
        dateCol.setPrefWidth(180);
        
        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    String color = switch (status.toLowerCase()) {
                        case "pending" -> "#e67e22";
                        case "processing" -> "#3498db";
                        case "shipped" -> "#9b59b6";
                        case "delivered" -> "#27ae60";
                        case "cancelled" -> "#e74c3c";
                        default -> "#7f8c8d";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
        
        TableColumn<Order, BigDecimal> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        totalCol.setPrefWidth(100);
        
        TableColumn<Order, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> statusBox = new ComboBox<>();
            private final Button updateBtn = new Button("Update");
            private final HBox container = new HBox(5, statusBox, updateBtn);
            
            {
                statusBox.getItems().addAll("Pending", "Processing", "Shipped", "Delivered", "Cancelled");
                statusBox.setStyle("-fx-pref-width: 100;");
                updateBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                container.setAlignment(Pos.CENTER);
                
                updateBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    String newStatus = statusBox.getValue();
                    if (newStatus != null) {
                        if (orderService.updateOrderStatus(order.getOrderId(), newStatus)) {
                            order.setStatus(newStatus);
                            getTableView().refresh();
                            updateStatus("Order #" + order.getOrderId() + " status updated to " + newStatus);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update order status");
                        }
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    statusBox.setValue(order.getStatus());
                    setGraphic(container);
                }
            }
        });
        
        table.getColumns().addAll(idCol, userCol, dateCol, statusCol, totalCol, actionsCol);
        
        orderList = FXCollections.observableArrayList(orderService.getAllOrders());
        table.setItems(orderList);
        
        return table;
    }
    
    // ============ USERS VIEW ============
    
    private VBox createUsersView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f6fa;");
        
        Label title = new Label("👥 User Management");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // User table
        TableView<User> userTable = createUserTable();
        VBox.setVgrow(userTable, Priority.ALWAYS);
        
        container.getChildren().addAll(title, userTable);
        return container;
    }
    
    private TableView<User> createUserTable() {
        TableView<User> table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        idCol.setPrefWidth(60);
        
        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);
        
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);
        
        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(120);
        
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(80);
        roleCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role);
                    String color = "admin".equalsIgnoreCase(role) ? "#e74c3c" : "#3498db";
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
        
        TableColumn<User, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setPrefWidth(250);
        
        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, roleCol, addressCol);
        
        userList = FXCollections.observableArrayList(userService.getAllUsers());
        table.setItems(userList);
        
        return table;
    }
    
    // ============ INVENTORY VIEW ============
    
    private VBox createInventoryView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #f5f6fa;");
        
        Label title = new Label("📋 Inventory Management");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Filter for low stock
        HBox filterRow = new HBox(15);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        
        CheckBox lowStockOnly = new CheckBox("Show Low Stock Only (< 10 items)");
        lowStockOnly.setStyle("-fx-font-size: 14px;");
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        
        filterRow.getChildren().addAll(lowStockOnly, refreshBtn);
        
        // Inventory table
        TableView<Product> inventoryTable = createInventoryTable();
        VBox.setVgrow(inventoryTable, Priority.ALWAYS);
        
        // Filter functionality
        lowStockOnly.setOnAction(e -> {
            if (lowStockOnly.isSelected()) {
                List<Product> lowStock = productService.getAllProducts().stream()
                        .filter(p -> p.getQuantity() < 10)
                        .toList();
                inventoryTable.setItems(FXCollections.observableArrayList(lowStock));
            } else {
                inventoryTable.setItems(FXCollections.observableArrayList(productService.getAllProducts()));
            }
        });
        
        refreshBtn.setOnAction(e -> {
            lowStockOnly.setSelected(false);
            inventoryTable.setItems(FXCollections.observableArrayList(productService.getAllProducts()));
            updateStatus("Inventory refreshed");
        });
        
        container.getChildren().addAll(title, filterRow, inventoryTable);
        return container;
    }
    
    private TableView<Product> createInventoryTable() {
        TableView<Product> table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        TableColumn<Product, Integer> idCol = new TableColumn<>("Product ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        idCol.setPrefWidth(100);
        
        TableColumn<Product, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        nameCol.setPrefWidth(250);
        
        TableColumn<Product, Integer> stockCol = new TableColumn<>("Current Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        stockCol.setPrefWidth(120);
        stockCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(stock.toString());
                    String color = stock < 10 ? "#e74c3c" : (stock < 50 ? "#e67e22" : "#27ae60");
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
        
        TableColumn<Product, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(120);
        statusCol.setCellValueFactory(cellData -> {
            int qty = cellData.getValue().getQuantity();
            if (qty == 0) return new SimpleStringProperty("Out of Stock");
            if (qty < 10) return new SimpleStringProperty("Low Stock");
            if (qty < 50) return new SimpleStringProperty("Medium");
            return new SimpleStringProperty("In Stock");
        });
        
        TableColumn<Product, Void> actionsCol = new TableColumn<>("Update Stock");
        actionsCol.setPrefWidth(250);
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final TextField stockField = new TextField();
            private final Button updateBtn = new Button("Update");
            private final Button addBtn = new Button("+10");
            private final HBox container = new HBox(5, stockField, updateBtn, addBtn);
            
            {
                stockField.setPrefWidth(80);
                stockField.setPromptText("Qty");
                updateBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                addBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                container.setAlignment(Pos.CENTER);
                
                updateBtn.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    try {
                        int newStock = Integer.parseInt(stockField.getText());
                        if (inventoryService.updateStock(product.getProductId(), newStock)) {
                            product.setQuantity(newStock);
                            getTableView().refresh();
                            updateStatus("Stock updated for " + product.getProductName());
                            stockField.clear();
                        }
                    } catch (NumberFormatException ex) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number");
                    }
                });
                
                addBtn.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    int newStock = product.getQuantity() + 10;
                    if (inventoryService.updateStock(product.getProductId(), newStock)) {
                        product.setQuantity(newStock);
                        getTableView().refresh();
                        updateStatus("Added 10 items to " + product.getProductName());
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, stockCol, statusCol, actionsCol);
        table.setItems(FXCollections.observableArrayList(productService.getAllProducts()));
        
        return table;
    }
    
    // ============ HELPER METHODS ============
    
    private void setActiveButton(Button activeBtn) {
        Button[] buttons = {dashboardBtn, productsBtn, ordersBtn, usersBtn, inventoryBtn, performanceBtn};
        for (Button btn : buttons) {
            if (btn != null) {
                if (btn == activeBtn) {
                    btn.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; " +
                                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 20; " +
                                "-fx-background-radius: 5; -fx-cursor: hand;");
                } else {
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                                "-fx-font-size: 14px; -fx-padding: 12 20; " +
                                "-fx-background-radius: 5; -fx-cursor: hand;");
                }
            }
        }
    }
    
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText("✓ " + message);
        }
        System.out.println("[Admin] " + message);
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
