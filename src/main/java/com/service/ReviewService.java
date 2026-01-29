package com.service;

import com.dao.ReviewDAO;
import com.models.Review;

import java.util.*;
import java.util.stream.Collectors;

public class ReviewService {
    private ReviewDAO reviewDAO;
    private Map<Integer, List<Review>> productReviewsCache; // Key: productId
    private long lastCacheUpdate;
    private static final long CACHE_VALIDITY = 300000; // 5 minutes

    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
        this.productReviewsCache = new HashMap<>();
        this.lastCacheUpdate = 0;
    }

    public boolean addReview(Review review) {
        boolean success = reviewDAO.addReview(review);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public List<Review> getReviewsByProductId(int productId) {
        long now = System.currentTimeMillis();

        if (productReviewsCache.containsKey(productId) &&
                (now - lastCacheUpdate) < CACHE_VALIDITY) {
            System.out.println("✓ Reviews from cache for product " + productId);
            return new ArrayList<>(productReviewsCache.get(productId));
        }

        List<Review> reviews = reviewDAO.getReviewsByProductId(productId);
        productReviewsCache.put(productId, reviews);
        lastCacheUpdate = now;
        return reviews;
    }

    public List<Review> getAllReviews() {
        return reviewDAO.getAllReviews();
    }

    public boolean deleteReview(int id) {
        boolean success = reviewDAO.deleteReview(id);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public double getAverageRating(int productId) {
        return reviewDAO.getAverageRating(productId);
    }

    // Business logic: Get high-rated reviews
    public List<Review> getHighRatedReviews(int productId, int minRating) {
        return getReviewsByProductId(productId).stream()
                .filter(r -> r.getRating() >= minRating)
                .collect(Collectors.toList());
    }

    // Business logic: Get recent reviews for product
    public List<Review> getRecentReviews(int productId, int limit) {
        return getReviewsByProductId(productId).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Sorting by rating
    public List<Review> sortByRating(int productId, boolean ascending) {
        List<Review> reviews = getReviewsByProductId(productId);
        if (ascending) {
            reviews.sort(Comparator.comparing(Review::getRating));
        } else {
            reviews.sort(Comparator.comparing(Review::getRating).reversed());
        }
        return reviews;
    }

    private void invalidateCache() {
        productReviewsCache.clear();
        lastCacheUpdate = 0;
    }
}