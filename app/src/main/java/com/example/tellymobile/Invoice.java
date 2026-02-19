package com.example.tellymobile;

import java.util.List;

public class Invoice {
    private String invoiceNumber;
    private String date;
    private String customerName;
    private List<InvoiceItem> items;
    private double totalAmount; // Subtotal (Taxable)
    private double deliveryCharges;
    private double totalTaxAmount;
    private double grandTotal;

    private String deliveryNote;
    private String modeOfPayment;
    private String referenceNo;
    private String otherReferences;
    private String buyersOrderNo;
    private String buyersOrderDate;
    private String dispatchDocNo;
    private String deliveryNoteDate;
    private String dispatchThrough;
    private String destination;
    private String termsOfDelivery;
    
    private String billOfLading; // Bill of Lading/LR-RR No.
    private String motorVehicleNo;
    
    private String consigneeName;
    private String consigneeAddress;
    private String consigneeGst;
    private String consigneeState;
    private String consigneeEmail;
    private String consigneeMobile;
    
    private String buyerAddress;
    private String buyerGst;
    private String buyerState; // Used to determine IGST vs CGST/SGST
    private String buyerEmail;
    private String buyerMobile;

    private String supplierInvDate;
    private String supplierInvNo; // New
    private String supplierCst;
    private String supplierTin;
    private String buyerVatTin;

    public Invoice(String invoiceNumber, String date, String customerName, List<InvoiceItem> items, double totalAmount, double deliveryCharges, double totalTaxAmount, double grandTotal) {
        this.invoiceNumber = invoiceNumber;
        this.date = date;
        this.customerName = customerName;
        this.items = items;
        this.totalAmount = totalAmount;
        this.deliveryCharges = deliveryCharges;
        this.totalTaxAmount = totalTaxAmount;
        this.grandTotal = grandTotal;
    }

    // Setters for new fields (Builder style or standard)
    public void setDispatchDetails(String deliveryNote, String modeOfPayment, String referenceNo, String otherReferences, String buyersOrderNo, String buyersOrderDate, String dispatchDocNo, String deliveryNoteDate, String dispatchThrough, String destination, String termsOfDelivery, String billOfLading, String motorVehicleNo) {
        this.deliveryNote = deliveryNote; this.modeOfPayment = modeOfPayment; this.referenceNo = referenceNo; this.otherReferences = otherReferences;
        this.buyersOrderNo = buyersOrderNo; this.buyersOrderDate = buyersOrderDate; this.dispatchDocNo = dispatchDocNo; this.deliveryNoteDate = deliveryNoteDate;
        this.dispatchThrough = dispatchThrough; this.destination = destination; this.termsOfDelivery = termsOfDelivery;
        this.billOfLading = billOfLading; this.motorVehicleNo = motorVehicleNo;
    }
    
    public void setBuyersOrderNo(String val) { this.buyersOrderNo = val; }
    public void setDispatchThrough(String val) { this.dispatchThrough = val; }
    public void setDeliveryNote(String val) { this.deliveryNote = val; }
    public void setModeOfPayment(String val) { this.modeOfPayment = val; }

    public void setConsigneeDetails(String name, String address, String gst, String state, String email, String mobile) {
        this.consigneeName = name; this.consigneeAddress = address; this.consigneeGst = gst; this.consigneeState = state;
        this.consigneeEmail = email; this.consigneeMobile = mobile;
    }

    public void setBuyerDetails(String address, String gst, String state, String email, String mobile) {
        this.buyerAddress = address; this.buyerGst = gst; this.buyerState = state;
        this.buyerEmail = email; this.buyerMobile = mobile;
    }

    public String getInvoiceNumber() { return invoiceNumber; }
    public String getDate() { return date; }
    public String getCustomerName() { return customerName; }
    public List<InvoiceItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    public double getDeliveryCharges() { return deliveryCharges; }
    public double getTotalTaxAmount() { return totalTaxAmount; }
    public double getGrandTotal() { return grandTotal; }
    
    // New Getters
    public String getDeliveryNote() { return deliveryNote; }
    public String getModeOfPayment() { return modeOfPayment; }
    public String getReferenceNo() { return referenceNo; }
    public String getOtherReferences() { return otherReferences; }
    public String getBuyersOrderNo() { return buyersOrderNo; }
    public String getBuyersOrderDate() { return buyersOrderDate; }
    public String getDispatchDocNo() { return dispatchDocNo; }
    public String getDeliveryNoteDate() { return deliveryNoteDate; }
    public String getDispatchThrough() { return dispatchThrough; }
    public String getDestination() { return destination; }
    public String getTermsOfDelivery() { return termsOfDelivery; }
    public String getBillOfLading() { return billOfLading; }
    public String getMotorVehicleNo() { return motorVehicleNo; }
    
    public String getConsigneeName() { return consigneeName; }
    public String getConsigneeAddress() { return consigneeAddress; }
    public String getConsigneeGst() { return consigneeGst; }
    public String getConsigneeState() { return consigneeState; }
    public String getConsigneeEmail() { return consigneeEmail; }
    public String getConsigneeMobile() { return consigneeMobile; }
    
    public String getBuyerAddress() { return buyerAddress; }
    public String getBuyerGst() { return buyerGst; }
    public String getBuyerState() { return buyerState; }
    public String getBuyerEmail() { return buyerEmail; }
    public String getBuyerMobile() { return buyerMobile; }

    public String getSupplierInvDate() { return supplierInvDate; }
    public void setSupplierInvDate(String supplierInvDate) { this.supplierInvDate = supplierInvDate; }
    public String getSupplierInvNo() { return supplierInvNo; }
    public void setSupplierInvNo(String supplierInvNo) { this.supplierInvNo = supplierInvNo; }
    public String getSupplierCst() { return supplierCst; }
    public void setSupplierCst(String supplierCst) { this.supplierCst = supplierCst; }
    public String getSupplierTin() { return supplierTin; }
    public void setSupplierTin(String supplierTin) { this.supplierTin = supplierTin; }
    public String getBuyerVatTin() { return buyerVatTin; }
    public void setBuyerVatTin(String buyerVatTin) { this.buyerVatTin = buyerVatTin; }
    
    private int bankLedgerId = -1;
    public void setBankLedgerId(int id) { this.bankLedgerId = id; }
    public int getBankLedgerId() { return bankLedgerId; }

    private List<InvoiceCharge> extraCharges;
    public void setExtraCharges(List<InvoiceCharge> extraCharges) { this.extraCharges = extraCharges; }
    public List<InvoiceCharge> getExtraCharges() { return extraCharges; }
}
