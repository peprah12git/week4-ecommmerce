package models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Order {
    private int orderId;
    private int userId;
    private String userName; // For joined queries
    private Timestamp orderDate;
    private String status;
    private BigDecimal totalAmount;

    public Order() {}

    public Order(int userId, String status, BigDecimal totalAmount) {
        this.userId = userId;
        this.status = status;
        this.totalAmount = totalAmount;
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

    @Override
    public String toString() {
        return "Order{id=" + orderId + ", userId=" + userId + ", total=" + totalAmount + ", status='" + status + "'}";
    }
}
