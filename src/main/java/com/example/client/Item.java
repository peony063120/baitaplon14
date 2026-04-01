package com.example.client;

import java.time.LocalDateTime;

// Abstract class đảm bảo tính Abstraction
public abstract class Item {
    private String id;
    private String name;
    private double startingPrice;
    private double currentMaxPrice;
    private LocalDateTime endTime;

    // Constructor, Getter/Setter (Encapsulation)
    public Item(String name, double startingPrice, LocalDateTime endTime) {
        this.name = name;
        this.startingPrice = startingPrice;
        this.currentMaxPrice = startingPrice;
        this.endTime = endTime;
    }

    // Phương thức trừu tượng để các lớp con override (Polymorphism)
    public abstract void displaySpecialInfo();
}