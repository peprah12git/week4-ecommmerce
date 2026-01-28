package com.controllers.models;

import java.sql.Timestamp;

public class Inventory {
    private int inventoryId;
    private int productId;
    private String productName;
    private int quantityAvailable;
    private Timestamp lastUpdated;

    // Constructors
    public Inventory() {
    }

    public Inventory(int inventoryId, int productId, int quantityAvailable) {
        this.inventoryId = inventoryId;
        this.productId = productId;
        this.quantityAvailable = quantityAvailable;
    }

    // Getters and Setters
    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isLowStock(int threshold) {
        return quantityAvailable < threshold;
    }
}
