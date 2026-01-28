package com.service;

import com.ecommerce.dao.InventoryDAO;
import com.ecommerce.models.Inventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryService {
    private InventoryDAO inventoryDAO;
    private List<Inventory> inventoryCache;
    private long lastCacheUpdate;
    private static final long CACHE_VALIDITY = 120000; // 2 minutes (inventory changes frequently)

    public InventoryService() {
        this.inventoryDAO = new InventoryDAO();
        this.inventoryCache = new ArrayList<>();
        this.lastCacheUpdate = 0;
    }

    public boolean updateInventory(int productId, int quantity) {
        boolean success = inventoryDAO.updateInventory(productId, quantity);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public Inventory getInventoryByProductId(int productId) {
        return inventoryDAO.getInventoryByProductId(productId);
    }

    public List<Inventory> getAllInventory() {
        long now = System.currentTimeMillis();

        if (!inventoryCache.isEmpty() && (now - lastCacheUpdate) < CACHE_VALIDITY) {
            System.out.println("✓ Inventory from cache");
            return new ArrayList<>(inventoryCache);
        }

        System.out.println("✗ Fetching inventory from database");
        inventoryCache = inventoryDAO.getAllInventory();
        lastCacheUpdate = now;
        return new ArrayList<>(inventoryCache);
    }

    public List<Inventory> getLowStockItems(int threshold) {
        return inventoryDAO.getLowStockItems(threshold);
    }

    // Business logic: Check if product is in stock
    public boolean isInStock(int productId) {
        Inventory inv = getInventoryByProductId(productId);
        return inv != null && inv.getQuantityAvailable() > 0;
    }

    // Business logic: Check if sufficient quantity available
    public boolean hasEnoughStock(int productId, int requestedQuantity) {
        Inventory inv = getInventoryByProductId(productId);
        return inv != null && inv.getQuantityAvailable() >= requestedQuantity;
    }

    // Business logic: Reduce stock (for order processing)
    public boolean reduceStock(int productId, int quantity) {
        Inventory inv = getInventoryByProductId(productId);
        if (inv != null && inv.getQuantityAvailable() >= quantity) {
            int newQuantity = inv.getQuantityAvailable() - quantity;
            return updateInventory(productId, newQuantity);
        }
        return false;
    }

    // Business logic: Restock
    public boolean addStock(int productId, int quantity) {
        Inventory inv = getInventoryByProductId(productId);
        if (inv != null) {
            int newQuantity = inv.getQuantityAvailable() + quantity;
            return updateInventory(productId, newQuantity);
        }
        return false;
    }

    // Filtering: Out of stock items
    public List<Inventory> getOutOfStockItems() {
        return getAllInventory().stream()
                .filter(i -> i.getQuantityAvailable() == 0)
                .collect(Collectors.toList());
    }

    // Sorting by quantity
    public List<Inventory> sortByQuantity(boolean ascending) {
        List<Inventory> items = getAllInventory();
        if (ascending) {
            items.sort(Comparator.comparing(Inventory::getQuantityAvailable));
        } else {
            items.sort(Comparator.comparing(Inventory::getQuantityAvailable).reversed());
        }
        return items;
    }

    /**
     * Update stock for a product (alias for updateInventory)
     */
    public boolean updateStock(int productId, int quantity) {
        return updateInventory(productId, quantity);
    }

    private void invalidateCache() {
        inventoryCache.clear();
        lastCacheUpdate = 0;
    }
}