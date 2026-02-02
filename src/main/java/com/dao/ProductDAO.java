package com.dao;

import com.config.DatabaseConnection;
import com.models.Product;
import com.util.PerformanceMonitor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Products with in-memory caching
 * All logging is silent - no UI exposure
 */
public class ProductDAO {
    private static final ProductDAO instance = new ProductDAO();
    private Connection connection;
    private final PerformanceMonitor monitor = PerformanceMonitor.getInstance();
    
    private static List<Product> productCache = null;
    private static long cacheTimestamp = 0;
    private static final long CACHE_TTL_MS = 300000;
    private static int cacheHits = 0;
    private static int cacheMisses = 0;

    private ProductDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    public static ProductDAO getInstance() {
        return instance;
    }
    
    private boolean isCacheValid() {
        return productCache != null && 
               (System.currentTimeMillis() - cacheTimestamp) < CACHE_TTL_MS;
    }
    
    public void invalidateCache() {
        productCache = null;
        cacheTimestamp = 0;
    }
    
    public static String getCacheStats() {
        int total = cacheHits + cacheMisses;
        double hitRate = total > 0 ? (cacheHits * 100.0 / total) : 0;
        return String.format("[CACHE] Hits: %d, Misses: %d, Hit Rate: %.1f%%", 
                             cacheHits, cacheMisses, hitRate);
    }

    public List<Product> getProductsByCategory(String category) {
        long startTime = monitor.startTimer();
        
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name, COALESCE(i.quantity_available, 0) as quantity " +
                "FROM Products p " +
                "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
                "WHERE c.category_name = ? ORDER BY p.name";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (SQLException e) {
            // Silent
        }
        
        monitor.recordQueryTime("getProductsByCategory", startTime);
        return products;
    }

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
                invalidateCache();
                return true;
            }
        } catch (SQLException e) {
            // Silent
        }
        return false;
    }

    private void createInventoryEntry(int productId) {
        String sql = "INSERT INTO Inventory (product_id, quantity_available) VALUES (?, 0)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // Silent
        }
    }

    public List<Product> getAllProducts() {
        long perfStartTime = monitor.startTimer();
        
        if (isCacheValid()) {
            cacheHits++;
            monitor.recordQueryTime("getAllProducts", perfStartTime);
            return new ArrayList<>(productCache);
        }
        
        cacheMisses++;
        
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name, COALESCE(i.quantity_available, 0) as quantity " +
                "FROM Products p " +
                "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
                "ORDER BY p.product_id DESC";

        this.connection = DatabaseConnection.getInstance().getConnection();
        
        if (connection == null) {
            return products;
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
            
            productCache = new ArrayList<>(products);
            cacheTimestamp = System.currentTimeMillis();
                             
        } catch (SQLException e) {
            // Silent
        }
        
        monitor.recordQueryTime("getAllProducts", perfStartTime);
        return products;
    }

    public Product getProductById(int id) {
        long startTime = monitor.startTimer();
        
        this.connection = DatabaseConnection.getInstance().getConnection();
        
        String sql = "SELECT p.*, c.category_name, COALESCE(i.quantity_available, 0) as quantity " +
                "FROM Products p " +
                "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
                "WHERE p.product_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Product product = extractProduct(rs);
                monitor.recordQueryTime("getProductById", startTime);
                return product;
            }
        } catch (SQLException e) {
            // Silent
        }
        
        monitor.recordQueryTime("getProductById", startTime);
        return null;
    }

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
                invalidateCache();
            }
            return updated;
        } catch (SQLException e) {
            // Silent
        }
        return false;
    }

    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM Products WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            boolean deleted = pstmt.executeUpdate() > 0;
            if (deleted) {
                invalidateCache();
            }
            return deleted;
        } catch (SQLException e) {
            // Silent
        }
        return false;
    }

    public List<Product> searchProducts(String term) {
        long startTime = monitor.startTimer();
        
        this.connection = DatabaseConnection.getInstance().getConnection();
        
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name, COALESCE(i.quantity_available, 0) as quantity " +
                "FROM Products p " +
                "LEFT JOIN Categories c ON p.category_id = c.category_id " +
                "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
                "WHERE p.name LIKE ? OR p.description LIKE ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String pattern = "%" + term + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (SQLException e) {
            // Silent
        }
        
        monitor.recordQueryTime("searchProducts", startTime);
        return products;
    }

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
