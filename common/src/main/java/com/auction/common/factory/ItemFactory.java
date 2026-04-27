package com.auction.common.factory;

import com.auction.common.entity.Art;
import com.auction.common.entity.Electronics;
import com.auction.common.entity.Item;
import com.auction.common.entity.Vehicle;

public class ItemFactory {

    // Electronics: (name, description, startingPrice, sellerId, brand, model, warrantyMonths, refurbished)
    public static Item createElectronics(String name, String description, String sellerId,
                                         double startingPrice, String brand, String model,
                                         int warrantyMonths, boolean refurbished) {
        return new Electronics(name, description, startingPrice, sellerId,
                brand, model, warrantyMonths, refurbished);
    }

    // Art: (name, description, startingPrice, sellerId, artist, year, medium)
    public static Item createArt(String name, String description, String sellerId,
                                 double startingPrice, String artist, int year, String medium) {
        return new Art(name, description, startingPrice, sellerId,
                artist, year, medium);
    }

    // Vehicle: (name, description, startingPrice, sellerId, make, model, year, vin)
    public static Item createVehicle(String name, String description, String sellerId,
                                     double startingPrice, String make, String model,
                                     int year, String vin) {
        return new Vehicle(name, description, startingPrice, sellerId,
                make, model, year, vin);
    }

    // Utility method to create item based on category string
    public static Item createItem(String category, String name, String description,
                                  String sellerId, double startingPrice,
                                  Object... specificParams) {
        switch (category.toUpperCase()) {
            case "ELECTRONICS":
                if (specificParams.length >= 4) {
                    // specificParams: brand, model, warrantyMonths, refurbished
                    return createElectronics(name, description, sellerId, startingPrice,
                            (String) specificParams[0],
                            (String) specificParams[1],
                            (int) specificParams[2],
                            (boolean) specificParams[3]);
                }
                break;
            case "ART":
                if (specificParams.length >= 3) {
                    // specificParams: artist, year, medium
                    return createArt(name, description, sellerId, startingPrice,
                            (String) specificParams[0],
                            (int) specificParams[1],
                            (String) specificParams[2]);
                }
                break;
            case "VEHICLE":
                if (specificParams.length >= 4) {
                    // specificParams: make, model, year, vin
                    return createVehicle(name, description, sellerId, startingPrice,
                            (String) specificParams[0],
                            (String) specificParams[1],
                            (int) specificParams[2],
                            (String) specificParams[3]);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown category: " + category);
        }
        throw new IllegalArgumentException("Invalid parameters for category: " + category);
    }
}