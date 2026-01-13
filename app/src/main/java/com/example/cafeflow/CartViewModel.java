package com.example.cafeflow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class CartViewModel extends ViewModel {
    private final MutableLiveData<List<OrderItem>> cartItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> totalAmount = new MutableLiveData<>(0.0);

    public LiveData<List<OrderItem>> getCartItems() {
        return cartItems;
    }

    public LiveData<Double> getTotalAmount() {
        return totalAmount;
    }

    public void addToCart(MenuItem menuItem) {
        List<OrderItem> currentItems = cartItems.getValue();
        if (currentItems == null) {
            currentItems = new ArrayList<>();
        }
        // Check if item is already in cart
        for (OrderItem item : currentItems) {
            if (item.getItemId().equals(menuItem.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                cartItems.setValue(new ArrayList<>(currentItems));
                calculateTotal();
                return;
            }
        }
        // Add item if not in cart
        currentItems.add(new OrderItem(menuItem.getId(), menuItem.getName(), 1, menuItem.getPrice()));
        cartItems.setValue(new ArrayList<>(currentItems));
        calculateTotal();
    }

    public void removeFromCart(OrderItem orderItem) {
        List<OrderItem> currentItems = cartItems.getValue();
        if (currentItems != null) {
            currentItems.remove(orderItem);
            cartItems.setValue(new ArrayList<>(currentItems));
            calculateTotal();
        }
    }

    public void clearCart() {
        cartItems.setValue(new ArrayList<>());
        totalAmount.setValue(0.0);
    }

    private void calculateTotal() {
        double total = 0;
        List<OrderItem> currentItems = cartItems.getValue();
        if (currentItems != null) {
            for (OrderItem item : currentItems) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        totalAmount.setValue(total);
    }
}
