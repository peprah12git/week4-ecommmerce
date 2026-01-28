package com.dao;

import com.config.DatabaseConnection;
import com.models.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private Connection connection;

    public OrderDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean addOrder(Order order) {
        String sql = "INSERT INTO Orders (user_id, status, total_amount) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, order.getUserId());
            pstmt.setString(2, order.getStatus());
            pstmt.setBigDecimal(3, order.getTotalAmount());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    order.setOrderId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding order: " + e.getMessage());
        }
        return false;
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.name as user_name FROM Orders o " +
                "LEFT JOIN Users u ON o.user_id = u.user_id " +
                "ORDER BY o.order_date DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(extractOrder(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders: " + e.getMessage());
        }
        return orders;
    }

    public Order getOrderById(int id) {
        String sql = "SELECT o.*, u.name as user_name FROM Orders o " +
                "LEFT JOIN Users u ON o.user_id = u.user_id " +
                "WHERE o.order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractOrder(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order: " + e.getMessage());
        }
        return null;
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.name as user_name FROM Orders o " +
                "LEFT JOIN Users u ON o.user_id = u.user_id " +
                "WHERE o.user_id = ? ORDER BY o.order_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                orders.add(extractOrder(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user orders: " + e.getMessage());
        }
        return orders;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE Orders SET status = ? WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order status: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteOrder(int id) {
        String sql = "DELETE FROM Orders WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting order: " + e.getMessage());
        }
        return false;
    }

    private Order extractOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setUserId(rs.getInt("user_id"));
        order.setUserName(rs.getString("user_name"));
        order.setOrderDate(rs.getTimestamp("order_date"));
        order.setStatus(rs.getString("status"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        return order;
    }
}