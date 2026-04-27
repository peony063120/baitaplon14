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
                "[ELECTRONICS] %s | Hãng: %s | Model: %s | Bảo hành: %d tháng | Tân trang: %s | Giá KĐ: %.0f VNĐ",
                getName(), brand, model, warrantyMonths, refurbished ? "Có" : "Không", getStartingPrice()
        );
//        System.out.println("====== Electronics ======");
//        System.out.println("Tên      : " + getName());
//        System.out.println("Hãng     : " + brand);
//        System.out.println("Model    : " + model);
//        System.out.println("Bảo hành : " + warrantyMonths + " tháng");
//        System.out.println("Tân trang: " + (refurbished ? "Có" : "Không"));
//        System.out.printf ("Giá KĐ   : %.0f VNĐ%n", getStartingPrice());
//        System.out.println("Mô tả    : " + getDescription());
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
