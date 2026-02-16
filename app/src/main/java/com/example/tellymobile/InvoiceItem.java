package com.example.tellymobile;

public class InvoiceItem {
    private String itemName;
    private double quantity;
    private double rate;
    private double amount; // Taxable Value
    private double gstRate;
    private double cgstAmount;
    private double sgstAmount;
    private double igstAmount; // Optional for future use
    private String hsn; // Added
    private String unit;

    public InvoiceItem(String itemName, double quantity, double rate, double amount, double gstRate, double cgstAmount, double sgstAmount, String unit, String hsn) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.rate = rate;
        this.amount = amount;
        this.gstRate = gstRate;
        this.cgstAmount = cgstAmount;
        this.sgstAmount = sgstAmount;
        this.unit = unit;
        this.hsn = hsn;
    }
    
    // Constructor chaining for backward compatibility if needed, or update callers
    // Constructor chaining
    public InvoiceItem(String itemName, double quantity, double rate, double amount, double gstRate, double cgstAmount, double sgstAmount, String unit) {
        this(itemName, quantity, rate, amount, gstRate, cgstAmount, sgstAmount, unit, "");
    }
    
    // Legacy
    public InvoiceItem(String itemName, double quantity, double rate, double amount, double gstRate, double cgstAmount, double sgstAmount) {
         this(itemName, quantity, rate, amount, gstRate, cgstAmount, sgstAmount, "", "");
    }

    public String getItemName() { return itemName; }
    public double getQuantity() { return quantity; }
    public double getRate() { return rate; }
    public double getAmount() { return amount; } // This is taxable value
    public double getGstRate() { return gstRate; }
    public double getCgstAmount() { return cgstAmount; }
    public double getSgstAmount() { return sgstAmount; }
    public double getIgstAmount() { return igstAmount; }
    public String getUnit() { return unit; }
    public String getHsn() { return hsn; }
}
