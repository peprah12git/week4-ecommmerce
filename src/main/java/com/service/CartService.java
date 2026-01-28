package com.service;

import com.ecommerce.models.CartItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;

public class CartService {
    private static final CartService instance = new CartService();
    private ObservableList<CartItem> cartItems;

    private CartService() {
        this.cartItems = FXCollections.observableArrayList();
    }

    public static CartService getInstance() {
        return instance;
    }

    public void addItem(CartItem item) {
        // Check if item already exists in cart
        for (CartItem existing : cartItems) {
            if (existing.getProductId() == item.getProductId()) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                return;
            }
        }
        cartItems.add(item);
    }

    public void removeItem(CartItem item) {
        cartItems.remove(item);
    }

    public void removeItem(int productId) {
        cartItems.removeIf(item -> item.getProductId() == productId);
    }

    public void updateQuantity(int productId, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getProductId() == productId) {
                if (quantity <= 0) {
                    cartItems.remove(item);
                } else {
                    item.setQuantity(quantity);
                }
                return;
            }
        }
    }

    public ObservableList<CartItem> getCartItems() {
        return cartItems;
    }

    public BigDecimal getTotalPrice() {
        return cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getItemCount() {
        return cartItems.size();
    }

    public void clearCart() {
        cartItems.clear();
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }
}
