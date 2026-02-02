package services;

import com.models.CartItem;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest {
    private com.service.OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = com.service.OrderService.getInstance();
    }

    @Test
    void testGetAllOrders() {
        var orders = orderService.getAllOrders();
        assertNotNull(orders);
    }

    @Test
    void testGetOrdersByStatus() {
        var orders = orderService.getOrdersByStatus("pending");
        assertNotNull(orders);
    }

    @Test
    void testCalculateTotalRevenue() {
        double revenue = orderService.calculateTotalRevenue();
        assertTrue(revenue >= 0);
    }

    @Test
    void testCalculateSubtotal() {
        ArrayList<CartItem> items = new ArrayList<>();
        items.add(new CartItem(1, "Item1", new BigDecimal("100.00"), 2, "Desc"));
        items.add(new CartItem(2, "Item2", new BigDecimal("50.00"), 1, "Desc"));
        BigDecimal subtotal = orderService.calculateSubtotal(items);
        assertEquals(new BigDecimal("250.00"), subtotal);
    }

    @Test
    void testCreateOrderWithEmptyCart() {
        var result = orderService.createOrder(1, new ArrayList<>());
        assertFalse(result.isSuccess());
        assertEquals("Cart is empty", result.getMessage());
    }

    @Test
    void testSortByAmount() {
        var sorted = orderService.sortByAmount(true);
        assertNotNull(sorted);
    }
}
