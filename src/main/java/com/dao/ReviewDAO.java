package com.dao;

import com.ecommerce.config.DatabaseConnection;
import com.ecommerce.models.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {
    private Connection connection;

    public ReviewDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public boolean addReview(Review review) {
        String sql = "INSERT INTO Reviews (user_id, product_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, review.getUserId());
            pstmt.setInt(2, review.getProductId());
            pstmt.setInt(3, review.getRating());
            pstmt.setString(4, review.getComment());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    review.setReviewId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding review: " + e.getMessage());
        }
        return false;
    }

    public List<Review> getReviewsByProductId(int productId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.name as user_name, p.name as product_name " +
                "FROM Reviews r " +
                "LEFT JOIN Users u ON r.user_id = u.user_id " +
                "LEFT JOIN Products p ON r.product_id = p.product_id " +
                "WHERE r.product_id = ? ORDER BY r.review_date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                reviews.add(extractReview(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reviews: " + e.getMessage());
        }
        return reviews;
    }

    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.name as user_name, p.name as product_name " +
                "FROM Reviews r " +
                "LEFT JOIN Users u ON r.user_id = u.user_id " +
                "LEFT JOIN Products p ON r.product_id = p.product_id " +
                "ORDER BY r.review_date DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reviews.add(extractReview(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all reviews: " + e.getMessage());
        }
        return reviews;
    }

    public boolean deleteReview(int id) {
        String sql = "DELETE FROM Reviews WHERE review_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting review: " + e.getMessage());
        }
        return false;
    }

    public double getAverageRating(int productId) {
        String sql = "SELECT AVG(rating) as avg_rating FROM Reviews WHERE product_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
        }
        return 0.0;
    }

    private Review extractReview(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getInt("review_id"));
        review.setUserId(rs.getInt("user_id"));
        review.setProductId(rs.getInt("product_id"));
        review.setUserName(rs.getString("user_name"));
        review.setProductName(rs.getString("product_name"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        review.setReviewDate(rs.getTimestamp("review_date"));
        return review;
    }
}