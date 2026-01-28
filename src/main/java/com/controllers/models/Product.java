package com.controllers.models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Product {
    private int productId;
    private String productName;
    private String description;
    private BigDecimal price;
    private int categoryId;
    private String categoryName;
    private Timestamp createdAt;
    private int quantityAvailable;

    public Product() {}

    public Product(String productName, String description, BigDecimal price, int categoryId) {
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.categoryId = categoryId;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(int quantityAvailable) { this.quantityAvailable = quantityAvailable; }

    // For AdminDashboardController compatibility
    public int getQuantity() {
        return getQuantityAvailable();
    }

    public void setQuantity(int quantity) {
        setQuantityAvailable(quantity);
    }

    @Override
    public String toString() {
        return "Product{id=" + productId + ", name='" + productName + "', price=" + price + "}";
    }
}
