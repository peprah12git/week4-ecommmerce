package com.util;

import com.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the database schema and sample data on first run
 */
public class DatabaseInitializer {

    public static void initializeDatabase() {
        System.out.println(" Checking database initialization...");
        
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            
            // Check if Products table has data
            if (!hasData(conn, "Products")) {
                System.out.println("📝 Sample data not found. Initializing database...");
                executeSqlScript(conn);
                System.out.println("✓ Database initialized with sample data!");
            } else {
                System.out.println("✓ Database already initialized with sample data");
            }
        } catch (Exception e) {
            System.err.println("⚠️  Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean hasData(Connection conn, String tableName) {
        try {
            String sql = "SELECT COUNT(*) as count FROM " + tableName;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking table: " + e.getMessage());
        }
        return false;
    }

    private static void executeSqlScript(Connection conn) throws SQLException {
        String[] sqlStatements = {
            // Insert Sample Users
            "INSERT INTO Users (name, email, password, phone, address) VALUES " +
            "('John Doe', 'john.doe@email.com', 'hashed_password_123', '555-1001', '123 Main St, City A'), " +
            "('Jane Smith', 'jane.smith@email.com', 'hashed_password_456', '555-1002', '456 Oak Ave, City B'), " +
            "('Bob Johnson', 'bob.j@email.com', 'hashed_password_789', '555-1003', '789 Pine Rd, City C'), " +
            "('Alice Williams', 'alice.w@email.com', 'hashed_password_101', '555-1004', '321 Elm St, City D'), " +
            "('Charlie Brown', 'charlie.b@email.com', 'hashed_password_202', '555-1005', '654 Maple Dr, City E')",
            
            // Insert Sample Categories
            "INSERT INTO Categories (category_name, description) VALUES " +
            "('Electronics', 'Electronic devices and accessories'), " +
            "('Clothing', 'Apparel and fashion items'), " +
            "('Home & Garden', 'Home improvement and garden products'), " +
            "('Sports & Outdoors', 'Sports equipment and outdoor gear'), " +
            "('Books & Media', 'Books, movies, and digital media')",
            
            // Insert Sample Products
            "INSERT INTO Products (name, description, price, category_id) VALUES " +
            "('Laptop Pro 15', 'High-performance laptop with Intel i7 processor', 1299.99, 1), " +
            "('Wireless Headphones', 'Noise-cancelling Bluetooth headphones', 199.99, 1), " +
            "('USB-C Cable 2M', 'Fast charging USB-C cable', 29.99, 1), " +
            "('Running Shoes', 'Professional grade running shoes', 149.99, 2), " +
            "('Winter Jacket', 'Warm waterproof winter jacket', 299.99, 2), " +
            "('Office Chair', 'Ergonomic office chair with lumbar support', 399.99, 3), " +
            "('LED Desk Lamp', 'Adjustable LED desk lamp with USB charging', 79.99, 3), " +
            "('Yoga Mat', 'Non-slip exercise yoga mat', 49.99, 4), " +
            "('Tennis Racket', 'Professional tennis racket', 189.99, 4), " +
            "('The Art of Programming', 'Comprehensive programming guide', 59.99, 5), " +
            "('Smart Watch', 'Fitness tracking smartwatch with heart rate monitor', 249.99, 1), " +
            "('Coffee Maker', 'Programmable coffee maker with timer', 89.99, 3)",
            
            // Insert Sample Inventory
            "INSERT INTO Inventory (product_id, quantity_available) VALUES " +
            "(1, 15), (2, 45), (3, 200), (4, 30), (5, 25), (6, 12), " +
            "(7, 50), (8, 85), (9, 20), (10, 40), (11, 35), (12, 60)",
            
            // Insert Sample Orders
            "INSERT INTO Orders (user_id, status, total_amount) VALUES " +
            "(1, 'completed', 1499.97), " +
            "(2, 'pending', 449.98), " +
            "(3, 'completed', 879.97), " +
            "(4, 'shipped', 299.99), " +
            "(1, 'completed', 189.99)",
            
            // Insert Sample Order Items
            "INSERT INTO OrderItems (order_id, product_id, quantity, unit_price) VALUES " +
            "(1, 1, 1, 1299.99), " +
            "(1, 2, 1, 199.99), " +
            "(2, 4, 3, 149.99), " +
            "(3, 11, 1, 249.99), " +
            "(3, 7, 1, 79.99), " +
            "(3, 10, 1, 149.99), " +
            "(4, 5, 1, 299.99), " +
            "(5, 9, 1, 189.99)",
            
            // Insert Sample Reviews
            "INSERT INTO Reviews (user_id, product_id, rating, comment) VALUES " +
            "(1, 1, 5, 'Excellent laptop! Very fast and reliable.'), " +
            "(2, 2, 4, 'Great sound quality, comfortable fit.'), " +
            "(3, 4, 5, 'Perfect running shoes, very comfortable!'), " +
            "(4, 5, 4, 'Good quality jacket, keeps me warm.'), " +
            "(1, 11, 5, 'Amazing smartwatch, love the battery life!'), " +
            "(2, 7, 4, 'Bright and adjustable lamp, works great.'), " +
            "(3, 9, 5, 'Professional quality racket, highly recommend.'), " +
            "(4, 10, 4, 'Informative and well-written book.'), " +
            "(5, 3, 5, 'Fast charging cable, great quality.'), " +
            "(1, 6, 4, 'Very comfortable office chair, good support.')"
        };
        
        for (String sql : sqlStatements) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("  ✓ Executed: " + sql.substring(0, Math.min(50, sql.length())) + "...");
            } catch (SQLException e) {
                System.err.println("  ⚠️  Statement failed (may already exist): " + e.getMessage());
            }
        }
    }
}
