package com.example.cafeflow;

public class OrderItem {
    private String itemId;
    private String itemName;
    private int quantity;
    private double price;

    public OrderItem() {}

    public OrderItem(String itemId, String itemName, int quantity, double price) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}