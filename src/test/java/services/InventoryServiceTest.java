package services;

import com.service.InventoryService;
import com.models.Inventory;
import org.junit.jupiter.api.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class InventoryServiceTest {
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService();
    }

    @Test
    void testGetInventoryByProductId() {
        Inventory inventory = inventoryService.getInventoryByProductId(1);
        assertNotNull(inventory);
        assertEquals(1, inventory.getProductId());
    }

    @Test
    void testGetAllInventory() {
        List<Inventory> items = inventoryService.getAllInventory();
        assertNotNull(items);
        assertTrue(items.size() >= 0);
    }

    @Test
    void testUpdateInventory() {
        boolean result = inventoryService.updateInventory(1, 100);
        assertTrue(result);
        Inventory updated = inventoryService.getInventoryByProductId(1);
        assertEquals(100, updated.getQuantityAvailable());
    }

    @Test
    void testIsInStock() {
        inventoryService.updateInventory(1, 10);
        assertTrue(inventoryService.isInStock(1));
        
        inventoryService.updateInventory(1, 0);
        assertFalse(inventoryService.isInStock(1));
    }

    @Test
    void testHasEnoughStock() {
        inventoryService.updateInventory(1, 50);
        assertTrue(inventoryService.hasEnoughStock(1, 30));
        assertFalse(inventoryService.hasEnoughStock(1, 100));
    }

    @Test
    void testReduceStock() {
        inventoryService.updateInventory(1, 100);
        boolean reduced = inventoryService.reduceStock(1, 20);
        assertTrue(reduced);
        Inventory inventory = inventoryService.getInventoryByProductId(1);
        assertEquals(80, inventory.getQuantityAvailable());
    }

    @Test
    void testReduceStockInsufficientQuantity() {
        inventoryService.updateInventory(1, 10);
        boolean reduced = inventoryService.reduceStock(1, 50);
        assertFalse(reduced);
    }

    @Test
    void testAddStock() {
        inventoryService.updateInventory(1, 50);
        boolean added = inventoryService.addStock(1, 30);
        assertTrue(added);
        Inventory inventory = inventoryService.getInventoryByProductId(1);
        assertEquals(80, inventory.getQuantityAvailable());
    }

    @Test
    void testGetLowStockItems() {
        List<Inventory> lowStock = inventoryService.getLowStockItems(10);
        assertNotNull(lowStock);
        lowStock.forEach(item -> assertTrue(item.getQuantityAvailable() < 10));
    }

    @Test
    void testGetOutOfStockItems() {
        List<Inventory> outOfStock = inventoryService.getOutOfStockItems();
        assertNotNull(outOfStock);
        outOfStock.forEach(item -> assertEquals(0, item.getQuantityAvailable()));
    }

    @Test
    void testSortByQuantityAscending() {
        List<Inventory> sorted = inventoryService.sortByQuantity(true);
        assertNotNull(sorted);
        for (int i = 1; i < sorted.size(); i++) {
            assertTrue(sorted.get(i-1).getQuantityAvailable() <= sorted.get(i).getQuantityAvailable());
        }
    }

    @Test
    void testSortByQuantityDescending() {
        List<Inventory> sorted = inventoryService.sortByQuantity(false);
        assertNotNull(sorted);
        for (int i = 1; i < sorted.size(); i++) {
            assertTrue(sorted.get(i-1).getQuantityAvailable() >= sorted.get(i).getQuantityAvailable());
        }
    }
}
