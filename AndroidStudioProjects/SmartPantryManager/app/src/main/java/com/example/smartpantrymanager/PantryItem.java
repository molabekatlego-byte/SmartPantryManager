package com.example.smartpantrymanager;

/** One ingredient the user currently has at home. */
public class PantryItem {
    public int id;            // 0 means "not saved yet"
    public String name;
    public double quantity;
    public String unit;
    public String expiry;     // "yyyy-MM-dd" or null when there is no expiry date

    public PantryItem(int id, String name, double quantity, String unit, String expiry) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiry = expiry;
    }
}
