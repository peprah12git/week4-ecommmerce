package com.controllers;

import com.models.Category;
import com.models.Product;
import com.models.Review;
import com.service.CategoryService;
import com.service.ProductService;
import com.service.ReviewService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.List;

public class ClientController {

    // Services
    private ProductService productService;
    private CategoryService categoryService;
    private ReviewService reviewService;

    // ===== CART TAB =====
    @FXML private ComboBox<Product> cmbCartProduct;
    @FXML private TextField txtCartQuantity;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colCartProduct;
    @FXML private TableColumn<CartItem, String> colCartDescription;
    @FXML private TableColumn<CartItem, Integer> colCartQty;
    @FXML private TableColumn<CartItem, BigDecimal> colCartPrice;
    @FXML private TableColumn<CartItem, BigDecimal> colCartSubtotal;
    @FXML private Label lblCartTotal;
    @FXML private Label lblCartStatus;

    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    // ===== PRODUCTS TAB =====
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colProductId;
    @FXML private TableColumn<Product, String> colProductName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, BigDecimal> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<Category> cmbFilterCategory;
    @FXML private ComboBox<String> cmbSortBy;
    @FXML private Label lblStatus;

    // ===== PRODUCT DETAILS PANEL =====
    @FXML private javafx.scene.layout.VBox productDetailsPanel;
    @FXML private Label lblDetailName;
    @FXML private Label lblDetailDescription;
    @FXML private Label lblDetailPrice;
    @FXML private Label lblDetailStock;
    @FXML private TextField txtDetailQuantity;
    @FXML private TableView<Review> productReviewTable;
    @FXML private TableColumn<Review, String> colDetailReviewUser;
    @FXML private TableColumn<Review, Integer> colDetailRating;
    @FXML private TableColumn<Review, String> colDetailComment;

    @FXML private TableView<CartItem> cartPreviewTable;
    @FXML private TableColumn<CartItem, String> colPreviewProduct;
    @FXML private TableColumn<CartItem, Integer> colPreviewQty;
    @FXML private TableColumn<CartItem, BigDecimal> colPreviewPrice;
    @FXML private TableColumn<CartItem, BigDecimal> colPreviewSubtotal;
    @FXML private Label lblPreviewTotal;

    private Product selectedProduct;

    // ===== REVIEWS TAB =====
    @FXML private TableView<Review> reviewTable;
    @FXML private TableColumn<Review, Integer> colReviewId;
    @FXML private TableColumn<Review, String> colReviewProduct;
    @FXML private TableColumn<Review, String> colReviewUser;
    @FXML private TableColumn<Review, Integer> colRating;
    @FXML private TableColumn<Review, String> colComment;

    @FXML private TextArea txtReviewDetails;

    @FXML
    public void initialize() {
        productService = new ProductService();
        categoryService = new CategoryService();
        reviewService = new ReviewService();

        setupProductsTab();
        setupCartTab();
        setupReviewsTab();

        showStatus("Ready", false);
    }

    // ==================== CART TAB ====================

    private void setupCartTab() {
        if (cartTable == null) return;

        colCartProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCartDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colCartSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        cartTable.setItems(cartItems);
        configureCartProductCombo();
        refreshCartProducts(productService.getAllProducts());
        updateCartTotal();
    }

    @FXML
    public void handleAddToCart() {
        if (cmbCartProduct == null || txtCartQuantity == null) return;

        Product product = cmbCartProduct.getValue();
        if (product == null) {
            showCartStatus("Select a product first", true);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(txtCartQuantity.getText().trim());
        } catch (NumberFormatException e) {
            showCartStatus("Quantity must be a number", true);
            return;
        }

        if (quantity <= 0) {
            showCartStatus("Quantity must be greater than zero", true);
            return;
        }

        addProductToCart(product, quantity);
    }

    @FXML
    public void handleRemoveCartItem() {
        if (cartTable == null) return;
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showCartStatus("Select an item to remove", true);
            return;
        }
        cartItems.remove(selected);
        updateCartTotal();
        showCartStatus("Item removed", false);
    }

    @FXML
    public void handleCheckout() {
        if (cartItems.isEmpty()) {
            showCartStatus("Cart is empty", true);
            return;
        }
        
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                com.ecommerce.Main.class.getResource("/com/ecommerce/checkout.fxml"));
            javafx.scene.Parent root = loader.load();
            
            CheckoutController controller = loader.getController();
            // Pass cart items to checkout
            List<com.ecommerce.models.CartItem> checkoutItems = new java.util.ArrayList<>();
            for (CartItem item : cartItems) {
                com.ecommerce.models.CartItem cartItem = new com.ecommerce.models.CartItem(
                    item.getProductId(), item.getProductName(), item.getPrice(), item.getQuantity(), "");
                checkoutItems.add(cartItem);
            }
            controller.setCartItems(checkoutItems);
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) cartTable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showCartStatus("Error loading checkout: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private CartItem findCartItem(int productId) {
        for (CartItem item : cartItems) {
            if (item.getProductId() == productId) {
                return item;
            }
        }
        return null;
    }

    private void addProductToCart(Product product, int quantity) {
        if (product == null || quantity <= 0) return;

        System.out.println("[DEBUG] Before add - Cart size: " + cartItems.size());
        
        CartItem existing = findCartItem(product.getProductId());
        if (existing != null) {
            existing.addQuantity(quantity);
            System.out.println("[DEBUG] Updated existing item: " + existing.getProductName() + " - New qty: " + existing.getQuantity());
        } else {
            CartItem newItem = new CartItem(product, quantity);
            cartItems.add(newItem);
            System.out.println("[DEBUG] Added new item: " + newItem.getProductName() + " - Qty: " + newItem.getQuantity());
        }

        System.out.println("[DEBUG] After add - Cart size: " + cartItems.size());

        if (cartTable != null) {
            cartTable.refresh();
        }

        updateCartTotal();
        updateCartPreview();
        showCartStatus(String.format("✓ %s added to cart", product.getProductName()), false);
        
        System.out.println("[CART] Added: " + product.getProductName() + " x" + quantity + " (Total items: " + cartItems.size() + ")");
    }

    private void updateCartTotal() {
        if (lblCartTotal == null) return;
        BigDecimal total = cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblCartTotal.setText(String.format("$%.2f", total));
    }

    private void updateCartPreview() {
        if (cartPreviewTable != null) {
            cartPreviewTable.setItems(cartItems);
            cartPreviewTable.refresh();
            System.out.println("[CART PREVIEW] Updated with " + cartItems.size() + " items");
        }
        if (lblPreviewTotal != null) {
            BigDecimal total = cartItems.stream()
                    .map(CartItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            lblPreviewTotal.setText(String.format("$%.2f", total));
            System.out.println("[CART PREVIEW] Total: $" + total);
        }
    }

    private void showCartStatus(String message, boolean isError) {
        if (lblCartStatus == null) return;
        lblCartStatus.setText(message);
        lblCartStatus.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }

    // ==================== PRODUCTS TAB ====================

    private void setupProductsTab() {
        if (productTable == null) return;

        colProductId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("quantityAvailable"));

        colPrice.setCellFactory(col -> new TableCell<Product, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("$%.2f", price));
            }
        });

        loadCategories();
        loadAllProducts();

        productTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        selectedProduct = newSelection;
                        displayProductDetails(newSelection);
                    } else {
                        hideProductDetails();
                    }
                }
        );

        setupProductDetailsPanel();

        if (cmbSortBy != null) {
            cmbSortBy.setItems(FXCollections.observableArrayList(
                    "Name (A-Z)", "Name (Z-A)", "Price (Low-High)", "Price (High-Low)"
            ));
        }
    }

    private void loadCategories() {
        List<Category> categories = categoryService.getAllCategories();
        if (cmbFilterCategory != null) {
            ObservableList<Category> filterCategories = FXCollections.observableArrayList(categories);
            Category allCategory = new Category();
            allCategory.setCategoryId(0);
            allCategory.setCategoryName("All Categories");
            filterCategories.add(0, allCategory);
            cmbFilterCategory.setItems(filterCategories);
            cmbFilterCategory.getSelectionModel().selectFirst();
        }
    }

    private void loadAllProducts() {
        List<Product> products = productService.getAllProducts();
        productTable.setItems(FXCollections.observableArrayList(products));
        refreshCartProducts(products);
    }

    private void setupProductDetailsPanel() {
        if (productReviewTable == null) return;
        
        colDetailReviewUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colDetailRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
        colDetailComment.setCellValueFactory(new PropertyValueFactory<>("comment"));

        if (cartPreviewTable != null) {
            colPreviewProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
            colPreviewQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            colPreviewPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
            colPreviewSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
            
            colPreviewPrice.setCellFactory(col -> new TableCell<CartItem, BigDecimal>() {
                @Override
                protected void updateItem(BigDecimal price, boolean empty) {
                    super.updateItem(price, empty);
                    setText(empty || price == null ? null : String.format("$%.2f", price));
                }
            });
            
            colPreviewSubtotal.setCellFactory(col -> new TableCell<CartItem, BigDecimal>() {
                @Override
                protected void updateItem(BigDecimal subtotal, boolean empty) {
                    super.updateItem(subtotal, empty);
                    setText(empty || subtotal == null ? null : String.format("$%.2f", subtotal));
                }
            });
            
            cartPreviewTable.setItems(cartItems);
            updateCartPreview();
        }
    }

    private void displayProductDetails(Product product) {
        if (productDetailsPanel == null) return;
        
        productDetailsPanel.setVisible(true);
        productDetailsPanel.setManaged(true);
        
        lblDetailName.setText(product.getProductName());
        lblDetailDescription.setText(product.getDescription() != null ? product.getDescription() : "No description");
        lblDetailPrice.setText(String.format("$%.2f", product.getPrice()));
        lblDetailStock.setText(String.valueOf(product.getQuantityAvailable()));
        txtDetailQuantity.setText("1");
        
        // Load reviews for this product
        List<Review> productReviews = reviewService.getAllReviews().stream()
                .filter(r -> r.getProductId() == product.getProductId())
                .collect(java.util.stream.Collectors.toList());
        
        productReviewTable.setItems(FXCollections.observableArrayList(productReviews));
        
        // Refresh cart preview when panel is shown
        updateCartPreview();
    }

    private void hideProductDetails() {
        if (productDetailsPanel == null) return;
        productDetailsPanel.setVisible(false);
        productDetailsPanel.setManaged(false);
    }

    @FXML
    public void handleAddToCartFromDetails() {
        if (selectedProduct == null) {
            showCartStatus("No product selected", true);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(txtDetailQuantity.getText().trim());
        } catch (NumberFormatException e) {
            showCartStatus("Quantity must be a number", true);
            return;
        }

        if (quantity <= 0) {
            showCartStatus("Quantity must be greater than zero", true);
            return;
        }

        addProductToCart(selectedProduct, quantity);
        txtDetailQuantity.setText("1");
    }

    private void promptAddToCart(Product product) {
        if (product == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Add to Cart");
        confirm.setHeaderText(product.getProductName());
        confirm.setContentText("Add this product to cart?");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                TextInputDialog quantityDialog = new TextInputDialog("1");
                quantityDialog.setTitle("Quantity");
                quantityDialog.setHeaderText("Enter quantity for " + product.getProductName());
                quantityDialog.setContentText("Quantity:");

                quantityDialog.showAndWait().ifPresent(qtyStr -> {
                    try {
                        int qty = Integer.parseInt(qtyStr.trim());
                        if (qty > 0) {
                            addProductToCart(product, qty);
                        } else {
                            showCartStatus("Quantity must be greater than zero", true);
                        }
                    } catch (NumberFormatException e) {
                        showCartStatus("Quantity must be a number", true);
                    }
                });
            }
        });
    }

    private void refreshCartProducts(List<Product> products) {
        if (cmbCartProduct != null) {
            Product current = cmbCartProduct.getValue();
            cmbCartProduct.setItems(FXCollections.observableArrayList(products));

            if (current != null) {
                products.stream()
                        .filter(p -> p.getProductId() == current.getProductId())
                        .findFirst()
                        .ifPresent(cmbCartProduct::setValue);
            }
        }
    }

    private void configureCartProductCombo() {
        if (cmbCartProduct == null) return;

        cmbCartProduct.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getProductName());
            }
        });

        cmbCartProduct.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getProductName());
            }
        });

        cmbCartProduct.setConverter(new StringConverter<>() {
            @Override
            public String toString(Product product) {
                return product != null ? product.getProductName() : "";
            }

            @Override
            public Product fromString(String name) {
                if (name == null) return null;
                return cmbCartProduct.getItems().stream()
                        .filter(p -> name.equals(p.getProductName()))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    @FXML
    public void handleSearch() {
        if (txtSearch == null) return;
        String searchTerm = txtSearch.getText().trim();

        if (searchTerm.isEmpty()) {
            loadAllProducts();
            return;
        }

        List<Product> results = productService.searchProductsByName(searchTerm);
        productTable.setItems(FXCollections.observableArrayList(results));
        showStatus("Found " + results.size() + " product(s)", false);
    }

    @FXML
    public void handleFilter() {
        if (cmbFilterCategory == null) return;
        Category selected = cmbFilterCategory.getValue();

        if (selected == null || selected.getCategoryId() == 0) {
            loadAllProducts();
            return;
        }

        List<Product> filtered = productService.filterByCategory(selected.getCategoryId());
        productTable.setItems(FXCollections.observableArrayList(filtered));
        showStatus("Filtered: " + filtered.size() + " product(s)", false);
    }

    @FXML
    public void handleSort() {
        if (cmbSortBy == null) return;
        String sortOption = cmbSortBy.getValue();
        if (sortOption == null) return;

        List<Product> sorted;
        switch (sortOption) {
            case "Name (A-Z)": sorted = productService.sortByName(true); break;
            case "Name (Z-A)": sorted = productService.sortByName(false); break;
            case "Price (Low-High)": sorted = productService.sortByPrice(true); break;
            case "Price (High-Low)": sorted = productService.sortByPrice(false); break;
            default: sorted = productService.getAllProducts();
        }

        productTable.setItems(FXCollections.observableArrayList(sorted));
        showStatus("Sorted by: " + sortOption, false);
    }

    @FXML
    public void handleRefresh() {
        loadAllProducts();
        if (txtSearch != null) txtSearch.clear();
        if (cmbFilterCategory != null) cmbFilterCategory.getSelectionModel().selectFirst();
        if (cmbSortBy != null) cmbSortBy.getSelectionModel().clearSelection();
        showStatus("Data refreshed", false);
    }

    // ==================== REVIEWS TAB ====================

    private void setupReviewsTab() {
        if (reviewTable == null) return;

        colReviewId.setCellValueFactory(new PropertyValueFactory<>("reviewId"));
        colReviewProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colReviewUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
        colComment.setCellValueFactory(new PropertyValueFactory<>("comment"));

        loadAllReviews();

        reviewTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (txtReviewDetails == null) return;
            if (newVal == null) {
                txtReviewDetails.clear();
                return;
            }
            StringBuilder details = new StringBuilder();
            details.append("Product: ").append(newVal.getProductName()).append("\n");
            details.append("Customer: ").append(newVal.getUserName()).append("\n");
            details.append("Rating: ").append(newVal.getRating()).append("/5\n\n");
            details.append(newVal.getComment());
            txtReviewDetails.setText(details.toString());
        });
    }

    private void loadAllReviews() {
        if (reviewTable == null) return;
        List<Review> reviews = reviewService.getAllReviews();
        reviewTable.setItems(FXCollections.observableArrayList(reviews));
    }

    // ==================== UTILITY METHODS ====================

    private void showStatus(String message, boolean isError) {
        if (lblStatus == null) return;
        lblStatus.setText(message);
        lblStatus.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }

    public static class CartItem {
        private final Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public String getProductName() {
            return product.getProductName();
        }

        public String getDescription() {
            return product.getDescription();
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getPrice() {
            return product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        }

        public BigDecimal getSubtotal() {
            return getPrice().multiply(new BigDecimal(quantity));
        }

        public int getProductId() {
            return product.getProductId();
        }

        public void addQuantity(int delta) {
            this.quantity += delta;
        }
    }
}
