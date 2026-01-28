package com.controllers;

import com.ecommerce.Main;
import com.ecommerce.models.Category;
import com.ecommerce.models.Product;
import com.ecommerce.service.CartService;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class ClientModernController {

    @FXML private Label lblWelcome;
    @FXML private Button btnCart;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<Category> cmbCategory;
    @FXML private ComboBox<String> cmbSort;
    @FXML private Label lblStatus;
    @FXML private FlowPane productCardsContainer;

    private ProductService productService;
    private CategoryService categoryService;
    private CartService cartService;

    @FXML
    public void initialize() {
        productService = ProductService.getInstance();
        categoryService = CategoryService.getInstance();
        cartService = CartService.getInstance();

        setupUI();
        loadProducts();
        updateCartButton();
    }

    private void setupUI() {
        if (com.ecommerce.service.UserService.getInstance().getCurrentUser() != null) {
            lblWelcome.setText("Welcome, " + com.ecommerce.service.UserService.getInstance().getCurrentUser().getName());
        }

        // Setup category filter
        List<Category> categories = categoryService.getAllCategories();
        Category allCategory = new Category();
        allCategory.setCategoryId(0);
        allCategory.setCategoryName("All Categories");
        categories.add(0, allCategory);
        cmbCategory.setItems(FXCollections.observableArrayList(categories));
        cmbCategory.getSelectionModel().selectFirst();

        // Setup sort options
        cmbSort.setItems(FXCollections.observableArrayList(
            "Name (A-Z)", "Name (Z-A)", "Price (Low-High)", "Price (High-Low)"
        ));
    }

    private void loadProducts() {
        List<Product> products = productService.getAllProducts();
        displayProductCards(products);
        lblStatus.setText(products.size() + " products available");
    }

    private void displayProductCards(List<Product> products) {
        productCardsContainer.getChildren().clear();
        
        for (Product product : products) {
            VBox card = createProductCard(product);
            productCardsContainer.getChildren().add(card);
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3); " +
            "-fx-cursor: hand;"
        );

        // Product image placeholder
        Label imgPlaceholder = new Label("📦");
        imgPlaceholder.setStyle("-fx-font-size: 60px; -fx-padding: 20;");

        // Product name
        Label name = new Label(product.getProductName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        name.setWrapText(true);
        name.setMaxWidth(250);

        // Product description
        Label desc = new Label(product.getDescription() != null ? 
            (product.getDescription().length() > 80 ? 
                product.getDescription().substring(0, 80) + "..." : 
                product.getDescription()) : 
            "No description");
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        desc.setWrapText(true);
        desc.setMaxWidth(250);

        // Price and stock
        Label price = new Label(String.format("$%.2f", product.getPrice()));
        price.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Label stock = new Label(product.getQuantityAvailable() > 0 ? 
            "In Stock (" + product.getQuantityAvailable() + ")" : 
            "Out of Stock");
        stock.setStyle("-fx-font-size: 11px; -fx-text-fill: " + 
            (product.getQuantityAvailable() > 0 ? "#27ae60" : "#e74c3c") + ";");

        // Add to cart button
        Button addBtn = new Button("🛒 Add to Cart");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand;"
        );
        addBtn.setDisable(product.getQuantityAvailable() <= 0);
        addBtn.setOnAction(e -> addToCart(product));

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); " +
            "-fx-cursor: hand; " +
            "-fx-scale-x: 1.02; " +
            "-fx-scale-y: 1.02;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3); " +
            "-fx-cursor: hand;"
        ));

        card.getChildren().addAll(imgPlaceholder, name, desc, price, stock, addBtn);
        return card;
    }

    private void addToCart(Product product) {
        com.ecommerce.models.CartItem cartItem = new com.ecommerce.models.CartItem(
            product.getProductId(), 
            product.getProductName(), 
            product.getPrice(), 
            1, 
            product.getDescription()
        );
        cartService.addItem(cartItem);
        updateCartButton();
        lblStatus.setText("✓ " + product.getProductName() + " added to cart!");
        lblStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    @FXML
    public void handleSearch() {
        String searchTerm = txtSearch.getText().trim();
        List<Product> products = searchTerm.isEmpty() ? 
            productService.getAllProducts() : 
            productService.searchProductsByName(searchTerm);
        displayProductCards(products);
        lblStatus.setText("Found " + products.size() + " products");
    }

    @FXML
    public void handleFilter() {
        Category selected = cmbCategory.getValue();
        if (selected == null || selected.getCategoryId() == 0) {
            loadProducts();
            return;
        }
        List<Product> products = productService.filterByCategory(selected.getCategoryId());
        displayProductCards(products);
        lblStatus.setText("Filtered: " + products.size() + " products");
    }

    @FXML
    public void handleSort() {
        String sortOption = cmbSort.getValue();
        if (sortOption == null) return;

        List<Product> products;
        switch (sortOption) {
            case "Name (A-Z)": products = productService.sortByName(true); break;
            case "Name (Z-A)": products = productService.sortByName(false); break;
            case "Price (Low-High)": products = productService.sortByPrice(true); break;
            case "Price (High-Low)": products = productService.sortByPrice(false); break;
            default: products = productService.getAllProducts();
        }
        displayProductCards(products);
        lblStatus.setText("Sorted by: " + sortOption);
    }

    @FXML
    public void handleRefresh() {
        txtSearch.clear();
        cmbCategory.getSelectionModel().selectFirst();
        cmbSort.getSelectionModel().clearSelection();
        loadProducts();
    }

    @FXML
    public void showCart() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                Main.class.getResource("/com/ecommerce/cart-view.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            Stage stage = (Stage) btnCart.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showOrders() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                Main.class.getResource("/com/ecommerce/order-history.fxml"));
            javafx.scene.Parent root = loader.load();
            OrderHistoryController controller = loader.getController();
            int userId = com.ecommerce.service.UserService.getInstance().getCurrentUser() != null ? 
                com.ecommerce.service.UserService.getInstance().getCurrentUser().getUserId() : 0;
            controller.setCurrentUserId(userId);
            controller.loadOrders();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            Stage stage = (Stage) btnCart.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void logout() {
        try {
            Main main = new Main();
            Stage stage = (Stage) btnCart.getScene().getWindow();
            main.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateCartButton() {
        int itemCount = cartService.getItemCount();
        btnCart.setText("🛒 Cart (" + itemCount + ")");
    }
}
