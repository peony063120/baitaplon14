package com.auction.common.entity;

// Art - tác phẩm nghệ thuật
// Kế thừa Item, thêm: artist, yearCreated, medium, height, width


public class Art extends Item {

    private String artist;
    private int yearCreated;
    private String medium;  // Sơn dầu, Màu nước, Điêu khắc, Kỹ thuật số,...
    private double height;  // cm
    private double width;   // cm

    public Art(String name, String description, double startingPrice, String sellerId, String artist, int year, String medium) {
        // Gán các thuộc tính chung vào lớp cha Item
        super(name, description, startingPrice, sellerId);
        // Gán các thuộc tính riêng của Art
        this.artist = artist;
        this.yearCreated = year;
        this.medium = medium;
    }

    public Art(String name, String description, double startingPrice,
               String sellerId, String artist, int yearCreated,
               String medium, double height, double width) {
        super(name, description, startingPrice, sellerId);
        this.artist = artist;
        this.yearCreated = yearCreated;
        this.medium = medium;
        this.height = height;
        this.width = width;
    }

    @Override
    public String getItemType() { return "ART"; }

    @Override
    public String displayInfo() {
        return String.format(
                "[ART] %s | Nghệ sĩ: %s | Năm: %d | Chất liệu: %s | Kích thước: %.1fx%.1f cm | Giá KĐ: %.0f VNĐ",
                getName(), artist, yearCreated, medium, height, width, getStartingPrice()
        );
//        System.out.println("====== Art ======");
//        System.out.println("Tên         : " + getName());
//        System.out.println("Nghệ sĩ     : " + artist);
//        System.out.println("Năm sáng tác: " + yearCreated);
//        System.out.println("Chất liệu   : " + medium);
//        System.out.printf ("Kích thước  : %.1f x %.1f cm%n", height, width);
//        System.out.printf ("Giá KĐ      : %.0f VNĐ%n", getStartingPrice());
//        System.out.println("Mô tả       : " + getDescription());
    }

    // Getters & Setters
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public int getYearCreated() { return yearCreated; }
    public void setYearCreated(int yearCreated) { this.yearCreated = yearCreated; }

    public String getMedium() { return medium; }
    public void setMedium(String medium) { this.medium = medium; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
}
