package com.example.tellymobile;

public class InvoiceCharge {
    private String chargeName;
    private double amount;
    private double rate;
    private boolean isPercentage;
    private boolean isDebit;
    private String paymentMode; // New

    public InvoiceCharge(String chargeName, double amount, double rate, boolean isPercentage) {
        this(chargeName, amount, rate, isPercentage, false, "None");
    }

    public InvoiceCharge(String chargeName, double amount, double rate, boolean isPercentage, boolean isDebit, String paymentMode) {
        this.chargeName = chargeName;
        this.amount = amount;
        this.rate = rate;
        this.isPercentage = isPercentage;
        this.isDebit = isDebit;
        this.paymentMode = paymentMode;
    }

    public String getChargeName() { return chargeName; }
    public double getAmount() { return amount; }
    public double getRate() { return rate; }
    public boolean isPercentage() { return isPercentage; }
    public boolean isDebit() { return isDebit; }
    public String getPaymentMode() { return paymentMode; }
}
