package com.models;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private int userId;
    private String userName; // For joined queries
    private Timestamp orderDate;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItem> orderItems; // ← ADD THIS FIELD

    public Order() {
        this.orderItems = new ArrayList<>(); // Initialize the list
    }

    public Order(int userId, String status, BigDecimal totalAmount) {
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.orderItems = new ArrayList<>(); // Initialize the list
    }

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    // ← ADD THESE METHODS
    public List<OrderItem> getOrderItems() { 
        return orderItems; 
    }

    public void setOrderItems(List<OrderItem> orderItems) { 
        this.orderItems = orderItems; 
    }

    @Override
    public String toString() {
        return "Order{id=" + orderId + ", userId=" + userId + ", total=" + totalAmount + ", status='" + status + "'}";
    }
}