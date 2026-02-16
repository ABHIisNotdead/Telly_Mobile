package com.example.tellymobile;

public class InvoiceCharge {
    private String chargeName;
    private double amount;
    private double rate;
    private boolean isPercentage;

    public InvoiceCharge(String chargeName, double amount, double rate, boolean isPercentage) {
        this.chargeName = chargeName;
        this.amount = amount;
        this.rate = rate;
        this.isPercentage = isPercentage;
    }

    public String getChargeName() { return chargeName; }
    public double getAmount() { return amount; }
    public double getRate() { return rate; }
    public boolean isPercentage() { return isPercentage; }
}
