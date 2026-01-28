package com.dao;

import com.config.DatabaseConnection;
import com.models.Inventory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    public List<Inventory> getAllInventory() {
        List<Inventory> inventories = new ArrayList<>();
        String sql = "SELECT i.*, p.name as product_name FROM Inventory i " +
                "LEFT JOIN Products p ON i.product_id = p.product_id " +
                "ORDER BY i.quantity_available ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                inventories.add(extractInventory(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all inventory: " + e.getMessage());
        }
        return inventories;
    }
    public List<Inventory> getLowStockItems(int threshold) {
        List<Inventory> items = new ArrayList<>();
        String sql = "SELECT i.*, p.name as product_name FROM Inventory i " +
                "LEFT JOIN Products p ON i.product_id = p.product_id " +
                "WHERE i.quantity_available < ? " +
                "ORDER BY i.quantity_available ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, threshold);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(extractInventory(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching low stock items: " + e.getMessage());
        }
        return items;
    }
    private Inventory extractInventory(ResultSet rs) throws SQLException {
        Inventory inv = new Inventory();
        inv.setInventoryId(rs.getInt("inventory_id"));
        inv.setProductId(rs.getInt("product_id"));
        inv.setProductName(rs.getString("product_name"));
        inv.setQuantityAvailable(rs.getInt("quantity_available"));
        inv.setLastUpdated(rs.getTimestamp("last_updated"));
        return inv;
    }
}