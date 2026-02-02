package com.dao;

import com.models.Order;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class OrderDAoTest {
    private OrderDAO orderDAO;

    @BeforeEach
    void setUp() {
        orderDAO = new OrderDAO();
    }

    @Test
    void testAddOrder() {
        Order order = new Order(1, "Pending", new BigDecimal("150.00"));
        boolean result = orderDAO.addOrder(order);
        assertTrue(result);
        assertTrue(order.getOrderId() > 0);
    }

    @Test
    void testGetOrderById() {
        Order order = new Order(1, "Pending", new BigDecimal("200.00"));
        orderDAO.addOrder(order);
        Order retrieved = orderDAO.getOrderById(order.getOrderId());
        assertNotNull(retrieved);
        assertEquals(order.getOrderId(), retrieved.getOrderId());
    }

    @Test
    void testGetOrdersByUserId() {
        orderDAO.addOrder(new Order(2, "Pending", new BigDecimal("100.00")));
        orderDAO.addOrder(new Order(2, "Shipped", new BigDecimal("250.00")));
        var orders = orderDAO.getOrdersByUserId(2);
        assertTrue(orders.size() >= 2);
    }

    @Test
    void testUpdateOrderStatus() {
        Order order = new Order(1, "Pending", new BigDecimal("300.00"));
        orderDAO.addOrder(order);
        boolean updated = orderDAO.updateOrderStatus(order.getOrderId(), "Shipped");
        assertTrue(updated);
        Order retrieved = orderDAO.getOrderById(order.getOrderId());
        assertEquals("Shipped", retrieved.getStatus());
    }

    @Test
    void testGetAllOrders() {
        var orders = orderDAO.getAllOrders();
        assertNotNull(orders);
        assertTrue(orders.size() >= 0);
    }

    @Test
    void testDeleteOrder() {
        Order order = new Order(1, "Pending", new BigDecimal("50.00"));
        orderDAO.addOrder(order);
        boolean deleted = orderDAO.deleteOrder(order.getOrderId());
        assertTrue(deleted);
        assertNull(orderDAO.getOrderById(order.getOrderId()));
    }
}
