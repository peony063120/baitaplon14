package com.auction.common.factory;

import com.auction.common.entity.Art;
import com.auction.common.entity.Electronics;
import com.auction.common.entity.Item;
import com.auction.common.entity.Vehicle;

public class ItemFactory {

    public static Item createElectronics(String name, String description, String sellerId,
                                         double startingPrice, String brand, String model,
                                         int warrantyMonths) {
        return new Electronics(name, description, sellerId, startingPrice,
                brand, model, warrantyMonths);
    }

    public static Item createArt(String name, String description, String sellerId,
                                 double startingPrice, String artist, int year, String medium) {
        return new Art(name, description, sellerId, startingPrice,
                artist, year, medium);
    }

    public static Item createVehicle(String name, String description, String sellerId,
                                     double startingPrice, String make, String model,
                                     int year, String vin) {
        return new Vehicle(name, description, sellerId, startingPrice,
                make, model, year, vin);
    }

    // Phương thức tiện ích: tạo item dựa trên category string
    public static Item createItem(String category, String name, String description,
                                  String sellerId, double startingPrice,
                                  Object... specificParams) {
        switch (category.toUpperCase()) {
            case "ELECTRONICS":
                if (specificParams.length >= 3) {
                    return createElectronics(name, description, sellerId, startingPrice,
                            (String) specificParams[0], (String) specificParams[1], (int) specificParams[2]);
                }
                break;
            case "ART":
                if (specificParams.length >= 3) {
                    return createArt(name, description, sellerId, startingPrice,
                            (String) specificParams[0], (int) specificParams[1], (String) specificParams[2]);
                }
                break;
            case "VEHICLE":
                if (specificParams.length >= 4) {
                    return createVehicle(name, description, sellerId, startingPrice,
                            (String) specificParams[0], (String) specificParams[1], (int) specificParams[2], (String) specificParams[3]);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown category: " + category);
        }
        throw new IllegalArgumentException("Invalid parameters for category: " + category);
    }
}