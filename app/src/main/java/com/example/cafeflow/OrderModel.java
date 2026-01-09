package com.example.cafeflow;

import java.util.Date;
import java.util.List;

public class OrderModel {
    private String id;
    private String customerId;
    private String customerName;
    private List<OrderItem> items;
    private double totalAmount;
    private OrderStatus status;
    private Date timestamp;

    public OrderModel() {}

    public OrderModel(String id, String customerId, String customerName, List<OrderItem> items, double totalAmount, OrderStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.timestamp = new Date();
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public List<OrderItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public Date getTimestamp() { return timestamp; }
}