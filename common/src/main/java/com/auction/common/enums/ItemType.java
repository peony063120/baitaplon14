package com.auction.common.enums;

public enum ItemType {
    ELECTRONICS("Electronics"),
    ART("Art"),
    VEHICLE("Vehicle");

    private final String label;
    ItemType(String label){
        this.label = label;
    }
    public String getLabel(){
        return label;
    }
}
