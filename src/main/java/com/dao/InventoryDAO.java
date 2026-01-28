package com.dao;

import com.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public  class InventoryDAO {
    private Connection connection;

    public InventoryDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    public boolean updateInventory(int productId, int quantity) {
        String sql = "UPDATE Inventory SET quantity_available = ? WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating inventory: " + e.getMessage());
        }
        return false;
    }

    public Inventory getInventoryByProductId(int productId) {
        String sql = "SELECT i.*, p.name as product_name FROM Inventory i " +
                "LEFT JOIN Products p ON i.product_id = p.product_id " +
                "WHERE i.product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractInventory(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching inventory: " + e.getMessage());
        }
        return null;
    }
}