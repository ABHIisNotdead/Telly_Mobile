package com.example.tellymobile;

public class Item {
    private String name;
    private double rate;
    private String unit;

    public Item(String name, double rate, String unit) {
        this.name = name;
        this.rate = rate;
        this.unit = unit;
    }

    public String getName() { return name; }
    public double getRate() { return rate; }
    public String getUnit() { return unit; }
}
