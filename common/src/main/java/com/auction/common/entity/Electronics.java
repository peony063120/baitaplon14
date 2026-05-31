package com.auction.common.entity;

/*
- Electronics - sp điện tử
- Kế thừa Item, thêm: brand, model, warrantyMonths, refurbished
 */

public class Electronics extends Item {


    private String brand;
    private String model;
    private int warrantyMonths;
    private boolean refurbished;

    public Electronics() {
        super();
    }

    public Electronics(String name, String description, double startingPrice,
                       String sellerId, String brand, String model,
                       int warrantyMonths, boolean refurbished) {
        super(name, description, startingPrice, sellerId);
        this.brand = brand;
        this.model = model;
        this.warrantyMonths = warrantyMonths;
        this.refurbished = refurbished;
    }

    @Override
    public String getItemType() {
        return "ELECTRONICS";
    }

    @Override
    public String displayInfo() {
        return String.format(
                "[ELECTRONICS] %s | Brand: %s | Model: %s | Warranty: %d months | Refurbished: %s | Starting Price: $%.0f",
                getName(), brand, model, warrantyMonths, refurbished ? "Yes" : "No", getStartingPrice()
        );
    }

    // Getters & Setters
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }

    public boolean isRefurbished() { return refurbished; }
    public void setRefurbished(boolean refurbished) { this.refurbished = refurbished; }
}
