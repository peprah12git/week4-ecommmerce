package com.service;

import com.dao.ProductDAO;
import com.models.Product;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Layer with CACHING, SORTING, SEARCHING (DSA Concepts)
 */
public class ProductService {
    private static ProductService instance;
        public static ProductService getInstance() {
            if (instance == null) {
                instance = new ProductService();
            }
            return instance;
        }
        // Combined search and filter for controller
        public List<Product> searchAndFilter(String searchTerm, String category) {
            List<Product> filtered = getAllProducts();
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String lower = searchTerm.toLowerCase();
                filtered = filtered.stream()
                    .filter(p -> p.getProductName().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
            }
            if (category != null && !category.equalsIgnoreCase("All")) {
                filtered = filtered.stream()
                    .filter(p -> category.equalsIgnoreCase(p.getCategoryName()))
                    .collect(Collectors.toList());
            }
            return filtered;
        }
    private final ProductDAO productDAO;

    // CACHING - HashMap for O(1) lookup
    private final Map<Integer, Product> productCache;
    private List<Product> allProductsCache;
    private long lastCacheUpdate;
    private static final long CACHE_VALIDITY = 300000; // 5 minutes

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.productCache = new HashMap<>();
        this.allProductsCache = new ArrayList<>();
        this.lastCacheUpdate = 0;
    }

    public boolean addProduct(Product product) {
        boolean success = productDAO.addProduct(product);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public List<Product> getAllProducts() {
        long now = System.currentTimeMillis();

        // Check cache validity
        if (!allProductsCache.isEmpty() && (now - lastCacheUpdate) < CACHE_VALIDITY) {
            System.out.println("✓ CACHE HIT - Returning from memory");
            return new ArrayList<>(allProductsCache);
        }

        // Cache miss - fetch from DB with timing instrumentation
        System.out.println("✗ CACHE MISS - Fetching from database");
        long start = System.currentTimeMillis();
        allProductsCache = productDAO.getAllProducts();
        long duration = System.currentTimeMillis() - start;
        System.out.println("⏱ getAllProducts() query time: " + duration + " ms | rows: " + allProductsCache.size());

        lastCacheUpdate = now;

        // Update individual cache
        for (Product p : allProductsCache) {
            productCache.put(p.getProductId(), p);
        }

        return new ArrayList<>(allProductsCache);
    }

    public Product getProductById(int id) {
        if (productCache.containsKey(id)) {
            System.out.println("✓ Product found in cache");
            return productCache.get(id);
        }

        Product p = productDAO.getProductById(id);
        if (p != null) {
            productCache.put(id, p);
        }
        return p;
    }

    public boolean updateProduct(Product product) {
        boolean success = productDAO.updateProduct(product);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public boolean deleteProduct(int id) {
        boolean success = productDAO.deleteProduct(id);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    // SEARCH with in-memory optimization
    public List<Product> searchProductsByName(String term) {
        if (term == null || term.trim().isEmpty()) {
            return getAllProducts();
        }

        // Use cache if available
        if (!allProductsCache.isEmpty()) {
            String lower = term.toLowerCase();
            return allProductsCache.stream()
                    .filter(p -> p.getProductName().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
        }

        long start = System.currentTimeMillis();
        List<Product> results = productDAO.searchProductsByName(term);
        long duration = System.currentTimeMillis() - start;
        System.out.println("⏱ searchProductsByName('" + term + "') time: " + duration + " ms | rows: " + results.size());
        return results;
    }

    // SORTING algorithms
    public List<Product> sortByPrice(boolean ascending) {
        List<Product> products = getAllProducts();
        if (ascending) {
            products.sort(Comparator.comparing(Product::getPrice));
        } else {
            products.sort(Comparator.comparing(Product::getPrice).reversed());
        }
        return products;
    }

    public List<Product> sortByName(boolean ascending) {
        List<Product> products = getAllProducts();
        if (ascending) {
            products.sort(Comparator.comparing(Product::getProductName));
        } else {
            products.sort(Comparator.comparing(Product::getProductName).reversed());
        }
        return products;
    }

    // FILTERING
    public List<Product> filterByCategory(int categoryId) {
        return getAllProducts().stream()
                .filter(p -> p.getCategoryId() == categoryId)
                .collect(Collectors.toList());
    }

    public List<Product> filterByPriceRange(double min, double max) {
        return getAllProducts().stream()
                .filter(p -> p.getPrice().doubleValue() >= min && p.getPrice().doubleValue() <= max)
                .collect(Collectors.toList());
    }

    public List<Product> getLowStockProducts(int threshold) {
        return getAllProducts().stream()
                .filter(p -> p.getQuantityAvailable() < threshold)
                .sorted(Comparator.comparingInt(Product::getQuantityAvailable))
                .collect(Collectors.toList());
    }

    private void invalidateCache() {
        productCache.clear();
        allProductsCache.clear();
        lastCacheUpdate = 0;
        System.out.println("✓ Cache invalidated");
    }

    // Performance testing
    public long measureQueryTime(boolean useCache) {
        if (!useCache) {
            invalidateCache();
        }

        long start = System.nanoTime();
        getAllProducts();
        long end = System.nanoTime();

        return (end - start) / 1_000_000; // milliseconds
    }

    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cachedProducts", productCache.size());
        stats.put("allProductsSize", allProductsCache.size());
        stats.put("cacheAge", System.currentTimeMillis() - lastCacheUpdate);
        stats.put("cacheValid", (System.currentTimeMillis() - lastCacheUpdate) < CACHE_VALIDITY);
        return stats;
    }
}
