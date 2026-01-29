package com.dao;

import com.config.MongoDBConnection;
import com.models.Review;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB DAO for Review - Handles unstructured review data storage
 * Uses MongoDB for flexible schema to store customer feedback with various metadata

 */
public class ReviewMongoDAO {
    
    private MongoCollection<Document> reviewsCollection;
    
    public ReviewMongoDAO() {
        this.reviewsCollection = MongoDBConnection.getInstance()
                .getCollection(MongoDBConnection.REVIEWS_COLLECTION);
    }
    
    /**
     * Create a new review in MongoDB with flexible schema
     * Stores additional metadata that SQL schema might not support
     */
    public void createReview(Review review) {
        try {
            Document reviewDoc = new Document()
                    .append("_id", new ObjectId())
                    .append("reviewId", review.getReviewId())
                    .append("productId", review.getProductId())
                    .append("userId", review.getUserId())
                    .append("rating", review.getRating())
                    .append("comment", review.getComment())
                    .append("createdAt", System.currentTimeMillis())
                    .append("updatedAt", System.currentTimeMillis())
                    // Flexible fields that SQL doesn't easily support
                    .append("helpful_count", 0)
                    .append("unhelpful_count", 0)
                    .append("verified_purchase", true)
                    .append("reviewer_name", "Customer")
                    .append("tags", new ArrayList<>())  // Dynamic tags array
                    .append("media", new ArrayList<>()) // Photos/videos array
                    .append("replies", new ArrayList<>()) // Nested replies
                    .append("sentiment", "neutral") // AI-generated sentiment
                    .append("status", "approved");
            
            reviewsCollection.insertOne(reviewDoc);
            System.out.println("✓ Review created in MongoDB: " + review.getReviewId());
            
        } catch (Exception e) {
            System.err.println("✗ Error creating review: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Read review by ID from MongoDB
     */
    public Review getReviewById(int reviewId) {
        try {
            Document doc = reviewsCollection.find(Filters.eq("reviewId", reviewId)).first();
            
            if (doc != null) {
                return convertDocumentToReview(doc);
            }
        } catch (Exception e) {
            System.err.println("✗ Error retrieving review: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get all reviews for a specific product
     * Demonstrates MongoDB's excellent query performance for unstructured data
     */
    public List<Review> getReviewsByProductId(int productId) {
        List<Review> reviews = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = reviewsCollection
                    .find(Filters.eq("productId", productId))
                    .sort(new Document("createdAt", -1)); // Sort by newest first
            
            for (Document doc : documents) {
                reviews.add(convertDocumentToReview(doc));
            }
            
            System.out.println("✓ Retrieved " + reviews.size() + " reviews for product " + productId);
            
        } catch (Exception e) {
            System.err.println("✗ Error retrieving product reviews: " + e.getMessage());
        }
        
        return reviews;
    }
    
    /**
     * Get all reviews for a specific user
     */
    public List<Review> getReviewsByUserId(int userId) {
        List<Review> reviews = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = reviewsCollection
                    .find(Filters.eq("userId", userId));
            
            for (Document doc : documents) {
                reviews.add(convertDocumentToReview(doc));
            }
        } catch (Exception e) {
            System.err.println("✗ Error retrieving user reviews: " + e.getMessage());
        }
        
        return reviews;
    }
    
    /**
     * Get reviews with minimum rating (useful for filtering)
     */
    public List<Review> getReviewsByMinRating(int minRating) {
        List<Review> reviews = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = reviewsCollection
                    .find(Filters.gte("rating", minRating));
            
            for (Document doc : documents) {
                reviews.add(convertDocumentToReview(doc));
            }
        } catch (Exception e) {
            System.err.println("✗ Error retrieving reviews by rating: " + e.getMessage());
        }
        
        return reviews;
    }
    
    /**
     * Update review in MongoDB - demonstrates flexible updates
     */
    public void updateReview(Review review) {
        try {
            reviewsCollection.updateOne(
                    Filters.eq("reviewId", review.getReviewId()),
                    Updates.combine(
                            Updates.set("rating", review.getRating()),
                            Updates.set("comment", review.getComment()),
                            Updates.set("updatedAt", System.currentTimeMillis())
                    )
            );
            System.out.println("✓ Review updated in MongoDB: " + review.getReviewId());
            
        } catch (Exception e) {
            System.err.println("✗ Error updating review: " + e.getMessage());
        }
    }
    
    /**
     * Update helpful count (flexible field that's easy to add/modify in MongoDB)
     */
    public void incrementHelpfulCount(int reviewId) {
        try {
            reviewsCollection.updateOne(
                    Filters.eq("reviewId", reviewId),
                    Updates.inc("helpful_count", 1)
            );
        } catch (Exception e) {
            System.err.println("✗ Error updating helpful count: " + e.getMessage());
        }
    }
    
    /**
     * Add tag to review (demonstrates flexible array operations)
     */
    public void addTagToReview(int reviewId, String tag) {
        try {
            reviewsCollection.updateOne(
                    Filters.eq("reviewId", reviewId),
                    Updates.addToSet("tags", tag) // Only add if not already present
            );
        } catch (Exception e) {
            System.err.println("✗ Error adding tag: " + e.getMessage());
        }
    }
    
    /**
     * Delete review from MongoDB
     */
    public void deleteReview(int reviewId) {
        try {
            reviewsCollection.deleteOne(Filters.eq("reviewId", reviewId));
            System.out.println("✓ Review deleted from MongoDB: " + reviewId);
            
        } catch (Exception e) {
            System.err.println("✗ Error deleting review: " + e.getMessage());
        }
    }
    
    /**
     * Get all reviews (useful for analytics/reporting)
     */
    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        
        try {
            reviewsCollection.find().into(reviews.stream()
                    .map(r -> null)
                    .toList());
            
            // Alternative approach
            for (Document doc : reviewsCollection.find()) {
                reviews.add(convertDocumentToReview(doc));
            }
        } catch (Exception e) {
            System.err.println("✗ Error retrieving all reviews: " + e.getMessage());
        }
        
        return reviews;
    }
    
    /**
     * Text search on reviews - MongoDB advantage for unstructured data
     * Searches comment field for keywords
     */
    public List<Review> searchReviewsByKeyword(String keyword) {
        List<Review> reviews = new ArrayList<>();
        
        try {
            // MongoDB supports text search on comment field
            FindIterable<Document> documents = reviewsCollection
                    .find(Filters.regex("comment", keyword, "i")); // Case-insensitive regex search
            
            for (Document doc : documents) {
                reviews.add(convertDocumentToReview(doc));
            }
            
            System.out.println("✓ Found " + reviews.size() + " reviews matching: " + keyword);
            
        } catch (Exception e) {
            System.err.println("✗ Error searching reviews: " + e.getMessage());
        }
        
        return reviews;
    }
    
    /**
     * Get reviews with unverified purchase status (demonstrates flexible filtering)
     */
    public List<Review> getUnverifiedReviews() {
        List<Review> reviews = new ArrayList<>();
        
        try {
            FindIterable<Document> documents = reviewsCollection
                    .find(Filters.eq("verified_purchase", false));
            
            for (Document doc : documents) {
                reviews.add(convertDocumentToReview(doc));
            }
        } catch (Exception e) {
            System.err.println("✗ Error retrieving unverified reviews: " + e.getMessage());
        }
        
        return reviews;
    }
    
    /**
     * Convert MongoDB Document to Review object
     */
    private Review convertDocumentToReview(Document doc) {
        Review review = new Review();
        review.setReviewId(doc.getInteger("reviewId", 0));
        review.setProductId(doc.getInteger("productId", 0));
        review.setUserId(doc.getInteger("userId", 0));
        review.setRating(doc.getInteger("rating", 0));
        review.setComment(doc.getString("comment"));
        return review;
    }
}
