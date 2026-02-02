package services;

import com.models.Product;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceTest {
    private com.service.ProductService productService;

    @BeforeEach
    void setUp() {
        productService = com.service.ProductService.getInstance();
    }

    @Test
    void testGetAllProducts() {
        var products = productService.getAllProducts();
        assertNotNull(products);
    }

    @Test
    void testGetProductById() {
        var products = productService.getAllProducts();
        if (!products.isEmpty()) {
            Product product = productService.getProductById(products.get(0).getProductId());
            assertNotNull(product);
        }
    }

    @Test
    void testSearchProductsByName() {
        var results = productService.searchProductsByName("Laptop");
        assertNotNull(results);
    }

    @Test
    void testSortByPrice() {
        var sorted = productService.sortByPrice(true);
        assertNotNull(sorted);
        if (sorted.size() > 1) {
            assertTrue(sorted.get(0).getPrice().compareTo(sorted.get(1).getPrice()) <= 0);
        }
    }

    @Test
    void testFilterByPriceRange() {
        var filtered = productService.filterByPriceRange(100.0, 1000.0);
        assertNotNull(filtered);
        for (Product p : filtered) {
            assertTrue(p.getPrice().doubleValue() >= 100.0 && p.getPrice().doubleValue() <= 1000.0);
        }
    }

    @Test
    void testSearchAndFilter() {
        var results = productService.searchAndFilter("Laptop", "All");
        assertNotNull(results);
    }

    @Test
    void testGetCacheStats() {
        var stats = productService.getCacheStats();
        assertNotNull(stats);
        assertTrue(stats.containsKey("cachedProducts"));
    }
}
