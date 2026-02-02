package services;

import com.models.CartItem;
import com.service.CartService;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class CartServicesTest {
    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = CartService.getInstance();
        cartService.clearCart();
    }

    @Test
    void testAddItem() {
        CartItem item = new CartItem(1, "Laptop", new BigDecimal("1000.00"), 1, "Gaming Laptop");
        cartService.addItem(item);
        assertEquals(1, cartService.getItemCount());
    }

    @Test
    void testAddDuplicateItem() {
        CartItem item1 = new CartItem(1, "Phone", new BigDecimal("500.00"), 2, "Smartphone");
        CartItem item2 = new CartItem(1, "Phone", new BigDecimal("500.00"), 3, "Smartphone");
        cartService.addItem(item1);
        cartService.addItem(item2);
        assertEquals(1, cartService.getItemCount());
        assertEquals(5, cartService.getCartItems().get(0).getQuantity());
    }

    @Test
    void testRemoveItem() {
        CartItem item = new CartItem(1, "Tablet", new BigDecimal("300.00"), 1, "Tablet");
        cartService.addItem(item);
        cartService.removeItem(1);
        assertTrue(cartService.isEmpty());
    }

    @Test
    void testUpdateQuantity() {
        CartItem item = new CartItem(1, "Mouse", new BigDecimal("25.00"), 1, "Wireless Mouse");
        cartService.addItem(item);
        cartService.updateQuantity(1, 5);
        assertEquals(5, cartService.getCartItems().get(0).getQuantity());
    }

    @Test
    void testGetTotalPrice() {
        cartService.addItem(new CartItem(1, "Item1", new BigDecimal("100.00"), 2, "Desc1"));
        cartService.addItem(new CartItem(2, "Item2", new BigDecimal("50.00"), 3, "Desc2"));
        assertEquals(new BigDecimal("350.00"), cartService.getTotalPrice());
    }

    @Test
    void testClearCart() {
        cartService.addItem(new CartItem(1, "Item", new BigDecimal("10.00"), 1, "Desc"));
        cartService.clearCart();
        assertTrue(cartService.isEmpty());
    }
}
