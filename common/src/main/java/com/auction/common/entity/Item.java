package com.auction.common.entity;

/*
- Abstract Item - sản phẩm đấu giá
- Áp dụng: inheritance, abstraction, polymorphism (OOP)
- Subclasses: Electronics, Art, Vehicle
- Created by: ItemFactory (Factory Pattern)
 */

public abstract class Item extends Entity {

    private String name;
    private String description;
    private double startingPrice;
    private String sellerId;
    private String category;

    protected Item() {
        super();
    }

    protected Item(String name, String description, double startingPrice, String sellerId) {
        super();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.sellerId = sellerId;
        this.category = getItemType();
    }

    // Abstract methods — Polymorphism
    public abstract String getItemType();  // Moi subclass tra ve loai item cua minh
    public abstract void displayInfo();    // In thong tin chi tiet ra console

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name)  {
        this.name = name;
        updateTimestamp();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        updateTimestamp();
    }

    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
        updateTimestamp();
    }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) {
        this.category = category;
    }
}