package com.auction.common.factory;

import com.auction.common.entity.Art;
import com.auction.common.entity.Electronics;
import com.auction.common.entity.Item;
import com.auction.common.entity.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ItemFactory Tests")
class ItemFactoryTest {

    // ======== createElectronics ========

    @Test
    @DisplayName("createElectronics: tra ve Electronics voi dung thuoc tinh")
    void testCreateElectronics() {
        Item item = ItemFactory.createElectronics(
                "Laptop", "Gaming laptop", "seller-1",
                20000000, "Asus", "ROG", 12, false
        );

        assertInstanceOf(Electronics.class, item);
        assertEquals("ELECTRONICS", item.getItemType());
        assertEquals("Laptop", item.getName());
        assertEquals(20000000, item.getStartingPrice());

        Electronics e = (Electronics) item;
        assertEquals("Asus", e.getBrand());
        assertEquals("ROG", e.getModel());
        assertEquals(12, e.getWarrantyMonths());
    }

    // ======== createArt ========

    @Test
    @DisplayName("createArt: tra ve Art voi dung thuoc tinh")
    void testCreateArt() {
        Item item = ItemFactory.createArt(
                "Sunflower", "Oil painting", "seller-2",
                5000000, "Van Gogh", 1888, "Oil"
        );

        assertInstanceOf(Art.class, item);
        assertEquals("ART", item.getItemType());

        Art a = (Art) item;
        assertEquals("Van Gogh", a.getArtist());
        assertEquals(1888, a.getYearCreated());
        assertEquals("Oil", a.getMedium());
    }

    // ======== createVehicle ========

    @Test
    @DisplayName("createVehicle: tra ve Vehicle voi dung thuoc tinh")
    void testCreateVehicle() {
        Item item = ItemFactory.createVehicle(
                "Camry 2020", "Sedan", "seller-3",
                800000000, "Toyota", "Camry", 2020, "51G-12345"
        );

        assertInstanceOf(Vehicle.class, item);
        assertEquals("VEHICLE", item.getItemType());

        Vehicle v = (Vehicle) item;
        assertEquals("Toyota", v.getMake());
        assertEquals("Camry", v.getModel());
        assertEquals(2020, v.getYear());
    }

    // ======== createItem (generic method) ========

    @Test
    @DisplayName("createItem ELECTRONICS: tra ve Electronics")
    void testCreateItem_electronics() {
        Item item = ItemFactory.createItem(
                "ELECTRONICS", "Phone", "Smartphone", "seller-1", 10000000,
                "Samsung", "Galaxy S24", 24, false
        );
        assertInstanceOf(Electronics.class, item);
        assertEquals("ELECTRONICS", item.getItemType());
    }

    @Test
    @DisplayName("createItem ART: tra ve Art")
    void testCreateItem_art() {
        Item item = ItemFactory.createItem(
                "ART", "Painting", "Abstract", "seller-2", 3000000,
                "Picasso", 1920, "Watercolor"
        );
        assertInstanceOf(Art.class, item);
        assertEquals("ART", item.getItemType());
    }

    @Test
    @DisplayName("createItem VEHICLE: tra ve Vehicle")
    void testCreateItem_vehicle() {
        Item item = ItemFactory.createItem(
                "VEHICLE", "Honda Civic", "Compact car", "seller-3", 500000000,
                "Honda", "Civic", 2019, "30A-99999"
        );
        assertInstanceOf(Vehicle.class, item);
        assertEquals("VEHICLE", item.getItemType());
    }

    @Test
    @DisplayName("createItem category khong hop le -> throw IllegalArgumentException")
    void testCreateItem_unknownCategory() {
        assertThrows(IllegalArgumentException.class, () ->
                ItemFactory.createItem("UNKNOWN", "name", "desc", "seller-1", 1000)
        );
    }
}