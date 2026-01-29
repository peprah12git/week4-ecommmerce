package com.service;

import com.dao.OrderDAO;
import com.dao.OrderItemDAO;
import com.models.CartItem;
import com.models.Order;
import com.models.OrderItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OrderService {
    private static OrderService instance;
    private OrderDAO orderDAO;
    private OrderItemDAO orderItemDAO;
    private List<Order> ordersCache;
    private long lastCacheUpdate;
    private static final long CACHE_VALIDITY = 180000; // 3 minutes

    public static OrderService getInstance() {
        if (instance == null) {
            instance = new OrderService();
        }
        return instance;
    }

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.orderItemDAO = new OrderItemDAO();
        this.ordersCache = new ArrayList<>();
        this.lastCacheUpdate = 0;
    }

    public boolean createOrder(Order order) {
        boolean success = orderDAO.addOrder(order);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public boolean addOrderItem(OrderItem item) {
        boolean success = orderItemDAO.addOrderItem(item);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public List<Order> getAllOrders() {
        long now = System.currentTimeMillis();

        if (!ordersCache.isEmpty() && (now - lastCacheUpdate) < CACHE_VALIDITY) {
            System.out.println("✓ Orders from cache");
            return new ArrayList<>(ordersCache);
        }

        System.out.println("✗ Fetching orders from database");
        ordersCache = orderDAO.getAllOrders();
        lastCacheUpdate = now;
        return new ArrayList<>(ordersCache);
    }

    public Order getOrderById(int id) {
        return orderDAO.getOrderById(id);
    }

    public List<Order> getOrdersByUserId(int userId) {
        return orderDAO.getOrdersByUserId(userId);
    }

    public List<OrderItem> getOrderItems(int orderId) {
        return orderItemDAO.getOrderItemsByOrderId(orderId);
    }

    public boolean updateOrderStatus(int orderId, String status) {
        boolean success = orderDAO.updateOrderStatus(orderId, status);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public boolean deleteOrder(int id) {
        boolean success = orderDAO.deleteOrder(id);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    // Business logic: Filter orders by status
    public List<Order> getOrdersByStatus(String status) {
        return getAllOrders().stream()
                .filter(o -> o.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    // Business logic: Get recent orders
    public List<Order> getRecentOrders(int limit) {
        List<Order> orders = getAllOrders();
        return orders.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Business logic: Calculate total revenue
    public double calculateTotalRevenue() {
        return getAllOrders().stream()
                .filter(o -> !o.getStatus().equals("cancelled"))
                .mapToDouble(o -> o.getTotalAmount().doubleValue())
                .sum();
    }

    // Sorting
    public List<Order> sortByDate(boolean ascending) {
        List<Order> orders = getAllOrders();
        if (ascending) {
            orders.sort(Comparator.comparing(Order::getOrderDate));
        } else {
            orders.sort(Comparator.comparing(Order::getOrderDate).reversed());
        }
        return orders;
    }

    public List<Order> sortByAmount(boolean ascending) {
        List<Order> orders = getAllOrders();
        if (ascending) {
            orders.sort(Comparator.comparing(Order::getTotalAmount));
        } else {
            orders.sort(Comparator.comparing(Order::getTotalAmount).reversed());
        }
        return orders;
    }

    public List<String> getStatusOptions() {
        List<String> statuses = new ArrayList<>();
        statuses.add("pending");
        statuses.add("processing");
        statuses.add("shipped");
        statuses.add("delivered");
        statuses.add("cancelled");
        return statuses;
    }

    public BigDecimal calculateSubtotal(ArrayList<CartItem> items) {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTax(BigDecimal subtotal) {
        return subtotal.multiply(new BigDecimal("0.10"));
    }

    public static class OrderResult {
        private final boolean success;
        private final String message;
        private final Order order;

        public OrderResult(boolean success, String message, Order order) {
            this.success = success;
            this.message = message;
            this.order = order;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Order getOrder() { return order; }
    }

    public OrderResult createOrder(int userId, ArrayList<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            return new OrderResult(false, "Cart is empty", null);
        }

        BigDecimal subtotal = calculateSubtotal(cartItems);
        BigDecimal tax = calculateTax(subtotal);
        BigDecimal total = subtotal.add(tax);

        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus("pending");

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(item.getPrice());
            orderItems.add(orderItem);
        }

        boolean success = createOrderWithItems(order, orderItems);
        if (success) {
            return new OrderResult(true, "Order placed successfully", order);
        } else {
            return new OrderResult(false, "Failed to create order", null);
        }
    }

    private void invalidateCache() {
        ordersCache.clear();
        lastCacheUpdate = 0;
    }

    /**
     * Create an order with items in a single database transaction.
     * Ensures consistency: either all inserts succeed or none do.
     */
    public boolean createOrderWithItems(Order order, List<OrderItem> items) {
        java.sql.Connection conn = com.config.DatabaseConnection.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);

            // Create order
            boolean orderCreated = orderDAO.addOrder(order);
            if (!orderCreated) {
                conn.rollback();
                return false;
            }

            // Add items
            for (OrderItem item : items) {
                item.setOrderId(order.getOrderId());
                boolean itemCreated = orderItemDAO.addOrderItem(item);
                if (!itemCreated) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            invalidateCache();
            return true;
        } catch (Exception e) {
            try { conn.rollback(); } catch (Exception ignored) {}
            System.err.println("✗ Transaction failed: " + e.getMessage());
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (Exception ignored) {}
        }
    }
}
