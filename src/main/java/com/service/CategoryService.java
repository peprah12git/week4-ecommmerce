package com.service;

import com.ecommerce.dao.CategoryDAO;
import com.ecommerce.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryService {
    private static CategoryService instance;
    private CategoryDAO categoryDAO;
    private List<Category> categoryCache;
    private long lastCacheUpdate;
    private static final long CACHE_VALIDITY = 600000; // 10 minutes

    public static CategoryService getInstance() {
        if (instance == null) {
            instance = new CategoryService();
        }
        return instance;
    }

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
        this.categoryCache = new ArrayList<>();
        this.lastCacheUpdate = 0;
    }

    public List<Category> getAllCategories() {
        long now = System.currentTimeMillis();

        if (!categoryCache.isEmpty() && (now - lastCacheUpdate) < CACHE_VALIDITY) {
            return new ArrayList<>(categoryCache);
        }

        categoryCache = categoryDAO.getAllCategories();
        lastCacheUpdate = now;
        return new ArrayList<>(categoryCache);
    }

    public Category getCategoryById(int id) {
        return categoryDAO.getCategoryById(id);
    }

    public List<String> getCategoryNamesWithAll() {
        List<String> names = new ArrayList<>();
        names.add("All");
        for (Category c : getAllCategories()) {
            names.add(c.getName());
        }
        return names;
    }

    public String mapCategoryIdToName(int categoryId) {
        Category category = getCategoryById(categoryId);
        return category != null ? category.getCategoryName() : "Unknown";
    }
}
