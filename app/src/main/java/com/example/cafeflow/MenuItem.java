package com.example.cafeflow;

public class MenuItem {
    private String id;
    private String name;
    private double price;
    private int stockQuantity;
    private String imageUrl; // Using String for URL
    private String category;

    public MenuItem() {}

    public MenuItem(String id, String name, double price, int stockQuantity, String imageUrl, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }

    // toString helps the ListView display data easily
    @Override
    public String toString() {
        return name + " - $" + price + " (" + stockQuantity + " left)";
    }
}