package com.auction.common.entity;

/*
- Vehicle - xe cộ
- Kế thừa Item, thêm: make, model, year, licensePlate, mileage
 */

public class Vehicle extends Item {

    private String make;          // Hãng sxuat (Toyota, Honda,...)
    private String model;         // Dòng xe (Camry, Civic,...)
    private int year;             // Năm sản xuất
    private String licensePlate;  // Biển số xe
    private double mileage;       // Số km đã đi

    public Vehicle(String name, String description, double startingPrice, String sellerId, String make, String model, int year, String vin) {
        super();
    }

    public Vehicle(String name, String description, double startingPrice,
                   String sellerId, String make, String model,
                   int year, String licensePlate, double mileage) {
        super(name, description, startingPrice, sellerId);
        this.make = make;
        this.model = model;
        this.year = year;
        this.licensePlate = licensePlate;
        this.mileage = mileage;
    }

    @Override
    public String getItemType() {
        return "VEHICLE";
    }

    @Override
    public String displayInfo() {
        return String.format(
                "[VEHICLE] %s | Hãng: %s | Dòng xe: %s | Năm: %d | Biển số: %s | Số km: %.1f | Giá KĐ: %.0f VNĐ",
                getName(), make, model, year, licensePlate, mileage, getStartingPrice()
        );
//        System.out.println("====== Vehicle ======");
//        System.out.println("Tên     : " + getName());
//        System.out.println("Hãng    : " + make);
//        System.out.println("Dòng xe : " + model);
//        System.out.println("Năm SX  : " + year);
//        System.out.println("Biển số : " + licensePlate);
//        System.out.printf ("Số km   : %.1f km%n", mileage);
//        System.out.printf ("Giá KĐ  : %.0f VNĐ%n", getStartingPrice());
//        System.out.println("Mô tả   : " + getDescription());
    }

    // Getters & Setters
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public double getMileage() { return mileage; }
    public void setMileage(double mileage) { this.mileage = mileage; }
}