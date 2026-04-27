package com.auction.common.enums;

public enum ItemType {
    ELECTRONICS("Đồ điện tử"),
    ART("Nghệ thuật"),
    VEHICLE("Phương tiện");

    private final String label;
    ItemType(String label){
        this.label = label;
    }
    public String getLabel(){
        return label;
    }
}
