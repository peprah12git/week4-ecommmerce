package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.config.DatabaseConnection;
import com.models.Product;
import com.util.PerformanceMonitor;

public class ProductionOptimizedDAO {
    private static final ProductionOptimizedDAO instance = new ProductionOptimizedDAO();
    private final Map<Integer, Product> productCache = new ConcurrentHashMap<>();
    private final Map<String, List<Product>> searchCache = new ConcurrentHashMap<>();
    private final PerformanceMonitor monitor = PerformanceMonitor.getInstance();
    
    private static final String GET_ALL_PRODUCTS = """
        SELECT p.product_id, p.name, p.description, p.price, p.category_id, p.created_at,
               c.category_name, COALESCE(i.quantity_available, 0) as quantity
        FROM Products p 
        LEFT JOIN Categories c ON p.category_id = c.category_id 
        LEFT JOIN Inventory i ON p.product_id = i.product_id 
        ORDER BY p.product_id
        """;
    
    private static final String GET_PRODUCT_BY_ID = """
        SELECT p.product_id, p.name, p.description, p.price, p.category_id, p.created_at,
               c.category_name, COALESCE(i.quantity_available, 0) as quantity
        FROM Products p 
        LEFT JOIN Categories c ON p.category_id = c.category_id 
        LEFT JOIN Inventory i ON p.product_id = i.product_id 
        WHERE p.product_id = ?
        """;
    
    private static final String SEARCH_PRODUCTS = """
        SELECT p.product_id, p.name, p.description, p.price, p.category_id, p.created_at,
               c.category_name, COALESCE(i.quantity_available, 0) as quantity
        FROM Products p 
        LEFT JOIN Categories c ON p.category_id = c.category_id 
        LEFT JOIN Inventory i ON p.product_id = i.product_id 
        WHERE p.name LIKE ? OR p.description LIKE ?
        ORDER BY p.name
        LIMIT 50
        """;

    private ProductionOptimizedDAO() {}

    public static ProductionOptimizedDAO getInstance() {
        return instance;
    }

    public List<Product> getAllProducts() {
        long startTime = monitor.startTimer();
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_ALL_PRODUCTS);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Product product = mapResultSetToProduct(rs);
                products.add(product);
                productCache.put(product.getProductId(), product);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        monitor.recordQueryTime("getAllProducts", startTime);
        return products;
    }

    public Product getProductById(int productId) {
        if (productCache.containsKey(productId)) {
            return productCache.get(productId);
        }

        long startTime = monitor.startTimer();
        Product product = null;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_PRODUCT_BY_ID)) {
            
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    product = mapResultSetToProduct(rs);
                    productCache.put(productId, product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        monitor.recordQueryTime("getProductById", startTime);
        return product;
    }

    public List<Product> searchProducts(String searchTerm) {
        String cacheKey = searchTerm.toLowerCase();
        if (searchCache.containsKey(cacheKey)) {
            return new ArrayList<>(searchCache.get(cacheKey));
        }

        long startTime = monitor.startTimer();
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_PRODUCTS)) {
            
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = mapResultSetToProduct(rs);
                    products.add(product);
                    productCache.put(product.getProductId(), product);
                }
            }
            
            searchCache.put(cacheKey, products);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        monitor.recordQueryTime("searchProducts", startTime);
        return products;
    }

    public void clearCache() {
        productCache.clear();
        searchCache.clear();
    }

    public Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("productCache", productCache.size());
        stats.put("searchCache", searchCache.size());
        return stats;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setProductName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setCategoryName(rs.getString("category_name"));
        product.setCreatedAt(rs.getTimestamp("created_at"));
        product.setQuantityAvailable(rs.getInt("quantity"));
        return product;
    }
}