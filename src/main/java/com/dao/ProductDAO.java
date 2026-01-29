package com.dao;

import com.config.DatabaseConnection;
import com.models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Products with in-memory caching
 * Implements caching strategy to reduce database load
 */
public class ProductDAO {
    private Connection connection;
    
    // ============ CACHING IMPLEMENTATION ============
    // Static cache shared across all ProductDAO instances
    private static List<Product> productCache = null;
    private static long cacheTimestamp = 0;
    private static final long CACHE_TTL_MS = 300000; // 5 minutes cache TTL
    private static int cacheHits = 0;
    private static int cacheMisses = 0;

    public ProductDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    /**
     * Check if cache is valid (exists and not expired)
     */
    private boolean isCacheValid() {
        return productCache != null && 
               (System.currentTimeMillis() - cacheTimestamp) < CACHE_TTL_MS;
    }
    
    /**
     * Invalidate the cache - call after any write operation
     */
    public void invalidateCache() {
        productCache = null;
        cacheTimestamp = 0;
        System.out.println("[CACHE] Product cache invalidated");
    }
    
    /**
     * Get cache statistics for performance monitoring
     */
    public static String getCacheStats() {
        int total = cacheHits + cacheMisses;
        double hitRate = total > 0 ? (cacheHits * 100.0 / total) : 0;
        return String.format("[CACHE] Hits: %d, Misses: %d, Hit Rate: %.1f%%", 
                             cacheHits, cacheMisses, hitRate);
    }
//adds a new product to the database
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO Products (name, description, price, category_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDescription());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setInt(4, product.getCategoryId());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    product.setProductId(rs.getInt(1));
                    createInventoryEntry(product.getProductId());
                }
                invalidateCache(); // Invalidate cache after insert
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
        }
        return false;
    }
//creates an inventory entry for a new product
    private void createInventoryEntry(int productId) {
        String sql = "INSERT INTO Inventory (product_id, quantity_available) VALUES (?, 0)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error creating inventory: " + e.getMessage());
        }
    }
        //fetches all products from the database with caching
    public List<Product> getAllProducts() {
        // Check cache first
        if (isCacheValid()) {
            cacheHits++;
            System.out.println("[CACHE] Product cache HIT - returning " + productCache.size() + " cached products");
            return new ArrayList<>(productCache); // Return copy to prevent modification
        }
        
        // Cache miss - query database
        cacheMisses++;
        long startTime = System.currentTimeMillis();
        
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name, COALESCE(i.quantity_available, 0) as quantity " +
                "FROM Products p " +
                "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
                "ORDER BY p.product_id DESC";

        // Re-fetch connection to ensure it's valid
        this.connection = DatabaseConnection.getInstance().getConnection();
        
        if (connection == null) {
            System.err.println(" Database connection is NULL! Cannot fetch products.");
            return products;
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println(" Executing SQL query for products...");
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
            
            // Update cache
            productCache = new ArrayList<>(products);
            cacheTimestamp = System.currentTimeMillis();
            
            long queryTime = System.currentTimeMillis() - startTime;
            System.out.println("[CACHE] Product cache MISS - loaded " + products.size() + 
                             " products from DB in " + queryTime + "ms");
                             
        } catch (SQLException e) {
            System.err.println(" Error fetching products: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }
//fetches one product using its product ID
    public Product getProductById(int id) {
        String sql = "SELECT p.*, c.category_name, COALESCE(i.quantity_available, 0) as quantity " +
                "FROM Products p " +
                "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
                "WHERE p.product_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return extractProduct(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product: " + e.getMessage());
        }
        return null;
    }
//updates product details in the database
    public boolean updateProduct(Product product) {
        String sql = "UPDATE Products SET name = ?, description = ?, price = ?, category_id = ? WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDescription());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setInt(4, product.getCategoryId());
            pstmt.setInt(5, product.getProductId());
            boolean updated = pstmt.executeUpdate() > 0;
            if (updated) {
                invalidateCache(); // Invalidate cache after update
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("Error updating product: " + e.getMessage());
        }
        return false;
    }
//deletes a product from the database
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM Products WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            boolean deleted = pstmt.executeUpdate() > 0;
            if (deleted) {
                invalidateCache(); // Invalidate cache after delete
            }
            return deleted;
        } catch (SQLException e) {
            System.err.println("Error deleting product: " + e.getMessage());
        }
        return false;
    }
//searches products by name with a LIKE query
    public List<Product> searchProductsByName(String term) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name, COALESCE(i.quantity_available, 0) as quantity " +
                "FROM Products p " +
                "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
                "WHERE p.name LIKE ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + term + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching: " + e.getMessage());
        }
        return products;
    }
//extracts product data from ResultSet
    private Product extractProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setProductName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setQuantityAvailable(rs.getInt("quantity"));
        return p;
    }
}