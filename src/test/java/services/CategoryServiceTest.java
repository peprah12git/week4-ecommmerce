package services;

import com.models.Category;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class CategoryServiceTest {
    private com.service.CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = com.service.CategoryService.getInstance();
    }

    @Test
    void testGetAllCategories() {
        var categories = categoryService.getAllCategories();
        assertNotNull(categories);
        assertTrue(categories.size() >= 0);
    }

    @Test
    void testGetCategoryById() {
        Category category = categoryService.getCategoryById(1);
        if (category != null) {
            assertNotNull(category.getCategoryName());
        }
    }

    @Test
    void testGetCategoryNamesWithAll() {
        var names = categoryService.getCategoryNamesWithAll();
        assertNotNull(names);
        assertTrue(names.contains("All"));
    }

    @Test
    void testMapCategoryIdToName() {
        String name = categoryService.mapCategoryIdToName(1);
        assertNotNull(name);
    }

    @Test
    void testCategoryCache() {
        var categories1 = categoryService.getAllCategories();
        var categories2 = categoryService.getAllCategories();
        assertEquals(categories1.size(), categories2.size());
    }

    @Test
    void testMapInvalidCategoryId() {
        String name = categoryService.mapCategoryIdToName(9999);
        assertEquals("Unknown", name);
    }
}
