package com.controllers;

import com.models.CartItem;
import com.models.Product;
import com.models.Review;
import com.service.CartService;
import com.service.ProductService;
import com.service.ReviewService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for Product Detail view
 * Uses ProductService and ReviewService for business logic
 * 
 * Pattern: Controller -> Service -> DAO
 */
public class ProductDetailController {
    @FXML private Label productNameLabel;
    @FXML private Label productPriceLabel;
    @FXML private Label productDescLabel;
    @FXML private Label productStockLabel;
    @FXML private Label avgRatingLabel;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button addToCartButton;
    @FXML private VBox reviewsContainer;
    
    @FXML private TextArea reviewTextArea;
    @FXML private Spinner<Integer> ratingSpinner;
    @FXML private Button submitReviewButton;
    
    private Product product;
    // Services (not DAOs)
    private ProductService productService;
    private ReviewService reviewService;
    private ClientViewController clientViewController;
    private int currentUserId;

    @FXML
    public void initialize() {
        // Use services instead of DAOs
        productService = ProductService.getInstance();
        reviewService = new ReviewService();
    }

    public void setProduct(int productId) {
        // Use ProductService to get product
        this.product = productService.getProductById(productId);
        if (product != null) {
            displayProductDetails();
            loadReviews();
        }
    }

    public void setClientViewController(ClientViewController controller) {
        this.clientViewController = controller;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    private void displayProductDetails() {
        productNameLabel.setText(product.getProductName());
        productPriceLabel.setText(String.format("$%.2f", product.getPrice()));
        
        // Display description or placeholder if empty
        String description = product.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            productDescLabel.setText(description);
        } else {
            productDescLabel.setText("No description available for this product.");
            productDescLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 10; -fx-background-color: #f9f9f9; -fx-background-radius: 5;");
        }
        
        productStockLabel.setText("In Stock: " + product.getQuantityAvailable());
        
        // Setup quantity spinner
        quantitySpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, product.getQuantityAvailable(), 1)
        );

        // Load average rating using ReviewService
        double avgRating = reviewService.getAverageRating(product.getProductId());
        avgRatingLabel.setText(String.format("⭐ %.1f / 5.0", avgRating));
    }

    private void loadReviews() {
        reviewsContainer.getChildren().clear();
        // Use ReviewService to get reviews
        List<Review> reviews = reviewService.getReviewsByProductId(product.getProductId());
        
        if (reviews.isEmpty()) {
            Label noReviews = new Label("No reviews yet. Be the first to review!");
            noReviews.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            reviewsContainer.getChildren().add(noReviews);
        } else {
            for (Review review : reviews) {
                reviewsContainer.getChildren().add(createReviewCard(review));
            }
        }
    }

    private VBox createReviewCard(Review review) {
        VBox card = new VBox(8);
        card.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-color: #f9f9f9; -fx-padding: 12;");
        card.setPadding(new Insets(12));

        // User and rating header
        HBox header = new HBox(10);
        Label userLabel = new Label(review.getUserName() != null ? review.getUserName() : "Anonymous");
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        
        String stars = "⭐".repeat(review.getRating());
        Label ratingLabel = new Label(stars);
        ratingLabel.setStyle("-fx-font-size: 13;");
        
        header.getChildren().addAll(userLabel, ratingLabel);

        // Review text
        Label reviewText = new Label(review.getComment());
        reviewText.setWrapText(true);
        reviewText.setStyle("-fx-font-size: 12;");

        // Date
        Label dateLabel = new Label(review.getReviewDate() != null ? 
            review.getReviewDate().toLocalDateTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "");
        dateLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #7f8c8d;");

        card.getChildren().addAll(header, reviewText, dateLabel);
        return card;
    }

    @FXML
    private void handleAddToCart() {
        if (product == null) {
            showAlert("Error", "No product selected.");
            return;
        }
        int quantity = quantitySpinner.getValue();
        if (quantity <= 0) {
            showAlert("Invalid Quantity", "Please select a valid quantity.");
            return;
        }
        // Add to cart
        CartItem item = new CartItem(product.getProductId(), product.getProductName(), product.getPrice(), quantity, product.getDescription());
        CartService.getInstance().addItem(item);
        if (clientViewController != null) {
            clientViewController.updateCartButton();
        }
        showAlert("Added to Cart", String.format("%s x%d added to your cart.", product.getProductName(), quantity));
    }

    @FXML
    private void handleSubmitReview() {
        if (currentUserId == 0) {
            showAlert(Alert.AlertType.WARNING, "Login Required", "Please login to submit a review");
            return;
        }

        String reviewText = reviewTextArea.getText().trim();
        int rating = ratingSpinner.getValue();

        if (reviewText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please write a review");
            return;
        }

        Review review = new Review();
        review.setProductId(product.getProductId());
        review.setUserId(currentUserId);
        review.setRating(rating);
        review.setComment(reviewText);

        // Use ReviewService to add review
        if (reviewService.addReview(review)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Review submitted successfully!");
            reviewTextArea.clear();
            ratingSpinner.getValueFactory().setValue(5);
            loadReviews();
            displayProductDetails(); // Refresh average rating
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to submit review");
        }
    }

    @FXML
    private void handleBackToProducts() {
        clientViewController.backToProducts();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }
}
