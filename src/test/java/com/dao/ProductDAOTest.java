package com.dao;

import com.models.Product;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class ProductDAOTest {
    private com.dao.ProductDAO productDAO;

    @BeforeEach
    void setUp() {
        productDAO = com.dao.ProductDAO.getInstance();
    }

    @Test
    void testAddProduct() {
        Product product = new Product("Test Product", "Test Description", new BigDecimal("99.99"), 1);
        boolean result = productDAO.addProduct(product);
        assertTrue(result);
        assertTrue(product.getProductId() > 0);
    }

    @Test
    void testGetProductById() {
        Product product = new Product("Laptop", "Gaming Laptop", new BigDecimal("1200.00"), 1);
        productDAO.addProduct(product);
        Product retrieved = productDAO.getProductById(product.getProductId());
        assertNotNull(retrieved);
        assertEquals(product.getProductName(), retrieved.getProductName());
    }

    @Test
    void testGetAllProducts() {
        var products = productDAO.getAllProducts();
        assertNotNull(products);
        assertTrue(products.size() >= 0);
    }

    @Test
    void testUpdateProduct() {
        Product product = new Product("Phone", "Smartphone", new BigDecimal("500.00"), 1);
        productDAO.addProduct(product);
        product.setPrice(new BigDecimal("450.00"));
        boolean updated = productDAO.updateProduct(product);
        assertTrue(updated);
        Product retrieved = productDAO.getProductById(product.getProductId());
        assertEquals(new BigDecimal("450.00"), retrieved.getPrice());
    }

    @Test
    void testSearchProducts() {
        var products = productDAO.searchProducts("Laptop");
        assertNotNull(products);
    }

    @Test
    void testDeleteProduct() {
        Product product = new Product("Temp", "Temporary", new BigDecimal("10.00"), 1);
        productDAO.addProduct(product);
        boolean deleted = productDAO.deleteProduct(product.getProductId());
        assertTrue(deleted);
        assertNull(productDAO.getProductById(product.getProductId()));
    }
}
