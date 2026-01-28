package com.controllers;

import com.ecommerce.models.CartItem;
import com.ecommerce.models.Product;
import com.ecommerce.service.CartService;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controller for Product Browser view
 * Uses ProductService and CategoryService for business logic
 * 
 * Pattern: Controller -> Service -> DAO
 */
public class ProductBrowserController {
    @FXML private GridPane productsGrid;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;

    // Services (not DAOs)
    private ProductService productService;
    private CategoryService categoryService;
    
    private ClientViewController clientViewController;
    private int currentUserId;
    private List<Product> allProducts;

    @FXML
    public void initialize() {
        // Use services instead of DAOs
        productService = ProductService.getInstance();
        categoryService = CategoryService.getInstance();
        
        System.out.println("✓ ProductBrowserController initialized");
        loadProducts();
        setupCategoryFilter();
    }

    public void setClientViewController(ClientViewController controller) {
        this.clientViewController = controller;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    private void loadProducts() {
        System.out.println("📦 Loading products...");
        allProducts = productService.getAllProducts();
        System.out.println("📦 Total products fetched: " + (allProducts != null ? allProducts.size() : 0));
        displayProducts(allProducts);
    }

    private void setupCategoryFilter() {
        // Use CategoryService to get category names dynamically
        categoryFilter.getItems().clear();
        categoryFilter.getItems().addAll(categoryService.getCategoryNamesWithAll());
        categoryFilter.setValue("All Categories");
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText();
        String category = categoryFilter.getValue();

        // Use ProductService for search and filter
        List<Product> filtered = productService.searchAndFilter(searchTerm, category);
        displayProducts(filtered);
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        categoryFilter.setValue("All Categories");
        displayProducts(allProducts);
    }

    private void displayProducts(List<Product> products) {
        System.out.println("🎨 Displaying products. Count: " + (products != null ? products.size() : 0));
        productsGrid.getChildren().clear();

        if (products == null || products.isEmpty()) {
            System.out.println("⚠️  No products to display!");
            return;
        }

        int row = 0, col = 0;
        for (Product product : products) {
            System.out.println("  - Adding product: " + product.getProductName());
            VBox productCard = createProductCard(product);
            productsGrid.add(productCard, col, row);

            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: #ddd; -fx-padding: 10; -fx-border-radius: 5;");
        card.setPrefSize(200, 280);

        Label nameLabel = new Label(product.getProductName());
        nameLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-alignment: center; -fx-wrap-text: true;");

        Label descLabel = new Label(product.getDescription());
        descLabel.setStyle("-fx-font-size: 10; -fx-text-alignment: center; -fx-wrap-text: true;");
        descLabel.setWrapText(true);

        Label priceLabel = new Label("$" + product.getPrice());
        priceLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Label stockLabel = new Label("Stock: " + product.getQuantityAvailable());
        stockLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #7f8c8d;");

        Button addButton = new Button("Add to Cart");
        addButton.setPrefWidth(150);
        addButton.setStyle("-fx-font-size: 11; -fx-padding: 8;");
        
        // Disable add button if out of stock
        if (product.getQuantityAvailable() <= 0) {
            addButton.setDisable(true);
            addButton.setText("Out of Stock");
            stockLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        }
        
        addButton.setOnAction(e -> addToCart(product, 1));

        Button viewButton = new Button("View Details");
        viewButton.setPrefWidth(150);
        viewButton.setStyle("-fx-font-size: 11; -fx-padding: 8; -fx-background-color: #3498db; -fx-text-fill: white;");
        viewButton.setOnAction(e -> viewProductDetails(product));

        card.getChildren().addAll(nameLabel, descLabel, priceLabel, stockLabel, addButton, viewButton);
        return card;
    }

    private void viewProductDetails(Product product) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    com.ecommerce.Main.class.getResource("/com/ecommerce/product-detail.fxml"));
            javafx.scene.Parent root = loader.load();
            ProductDetailController controller = loader.getController();
            controller.setProduct(product.getProductId());
            controller.setClientViewController(clientViewController);
            controller.setCurrentUserId(currentUserId);

            javafx.scene.layout.StackPane mainContent = 
                    (javafx.scene.layout.StackPane) productsGrid.getScene().lookup("#mainContent");
            if (mainContent != null) {
                mainContent.getChildren().setAll(root);
            }
        } catch (Exception e) {
            System.err.println("Error loading product details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addToCart(Product product, int quantity) {
        if (quantity <= 0) {
            showAlert("Invalid Quantity", "Please select a quantity greater than 0");
            return;
        }

        CartItem item = new CartItem(
                product.getProductId(),
                product.getProductName(),
                product.getPrice(),
                quantity,
                product.getDescription()
        );

        CartService.getInstance().addItem(item);
        clientViewController.updateCartButton();
        showAlert("Success", product.getProductName() + " added to cart!");
    }

    private String getCategoryName(int categoryId) {
        // Use CategoryService for category name lookup
        return categoryService.mapCategoryIdToName(categoryId);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
