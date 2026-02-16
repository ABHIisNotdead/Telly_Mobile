package com.example.tellymobile;

import java.util.List;

public class PaymentVoucher {
    private int id;
    private String voucherNo;
    private String date;
    private String throughLedger; // Bank/Cash Ledger Name
    private double totalAmount;
    private String narration;
    private int companyId;
    
    // Particulars (using VoucherCharge as it has Ledger Name & Amount)
    private List<VoucherCharge> particulars;

    public PaymentVoucher(int id, String voucherNo, String date, String throughLedger, double totalAmount, String narration, int companyId, List<VoucherCharge> particulars) {
        this.id = id;
        this.voucherNo = voucherNo;
        this.date = date;
        this.throughLedger = throughLedger;
        this.totalAmount = totalAmount;
        this.narration = narration;
        this.companyId = companyId;
        this.particulars = particulars;
    }

    public int getId() { return id; }
    public String getVoucherNo() { return voucherNo; }
    public String getDate() { return date; }
    public String getThroughLedger() { return throughLedger; }
    public double getTotalAmount() { return totalAmount; }
    public String getNarration() { return narration; }
    public int getCompanyId() { return companyId; }
    public List<VoucherCharge> getParticulars() { return particulars; }
}
