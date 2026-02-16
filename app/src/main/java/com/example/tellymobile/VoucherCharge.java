package com.example.tellymobile;

public class VoucherCharge {
    public int ledgerId;
    public String ledgerName;
    public double amount;
    public boolean isPercentage;
    public double rate;
    public boolean isDebit;
    
    public VoucherCharge(int ledgerId, String ledgerName, double amount, boolean isPercentage, double rate, boolean isDebit) {
        this.ledgerId = ledgerId; 
        this.ledgerName = ledgerName; 
        this.amount = amount; 
        this.isPercentage = isPercentage; 
        this.rate = rate;
        this.isDebit = isDebit;
    }
    
    // For backward compatibility
    public VoucherCharge(int ledgerId, String ledgerName, double amount, boolean isPercentage, double rate) {
        this(ledgerId, ledgerName, amount, isPercentage, rate, false);
    }
}
