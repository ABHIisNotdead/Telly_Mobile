package com.example.tellymobile;

public class VoucherCharge {
    public int ledgerId;
    public String ledgerName;
    public double amount;
    public boolean isPercentage;
    public double rate;
    public boolean isDebit;
    public String paymentMode; // New
    
    public VoucherCharge(int ledgerId, String ledgerName, double amount, boolean isPercentage, double rate, boolean isDebit, String paymentMode) {
        this.ledgerId = ledgerId; 
        this.ledgerName = ledgerName; 
        this.amount = amount; 
        this.isPercentage = isPercentage; 
        this.rate = rate;
        this.isDebit = isDebit;
        this.paymentMode = paymentMode;
    }

    public VoucherCharge(int ledgerId, String ledgerName, double amount, boolean isPercentage, double rate, boolean isDebit) {
        this(ledgerId, ledgerName, amount, isPercentage, rate, isDebit, "None");
    }
    
    // For backward compatibility
    public VoucherCharge(int ledgerId, String ledgerName, double amount, boolean isPercentage, double rate) {
        this(ledgerId, ledgerName, amount, isPercentage, rate, false, "None");
    }

    public String getPaymentMode() {
        return paymentMode != null ? paymentMode : "None";
    }
}
