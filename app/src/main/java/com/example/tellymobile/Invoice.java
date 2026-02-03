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
    private String dispatchDocNo;
    private String deliveryNoteDate;
    private String dispatchThrough;
    private String destination;
    private String termsOfDelivery;
    
    private String consigneeName;
    private String consigneeAddress;
    private String consigneeGst;
    private String consigneeState;
    
    private String buyerAddress;
    private String buyerGst;
    private String buyerState; // Used to determine IGST vs CGST/SGST

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
    public void setDispatchDetails(String deliveryNote, String modeOfPayment, String referenceNo, String otherReferences, String buyersOrderNo, String dispatchDocNo, String deliveryNoteDate, String dispatchThrough, String destination, String termsOfDelivery) {
        this.deliveryNote = deliveryNote; this.modeOfPayment = modeOfPayment; this.referenceNo = referenceNo; this.otherReferences = otherReferences;
        this.buyersOrderNo = buyersOrderNo; this.dispatchDocNo = dispatchDocNo; this.deliveryNoteDate = deliveryNoteDate;
        this.dispatchThrough = dispatchThrough; this.destination = destination; this.termsOfDelivery = termsOfDelivery;
    }

    public void setConsigneeDetails(String name, String address, String gst, String state) {
        this.consigneeName = name; this.consigneeAddress = address; this.consigneeGst = gst; this.consigneeState = state;
    }

    public void setBuyerDetails(String address, String gst, String state) {
        this.buyerAddress = address; this.buyerGst = gst; this.buyerState = state;
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
    public String getDispatchDocNo() { return dispatchDocNo; }
    public String getDeliveryNoteDate() { return deliveryNoteDate; }
    public String getDispatchThrough() { return dispatchThrough; }
    public String getDestination() { return destination; }
    public String getTermsOfDelivery() { return termsOfDelivery; }
    
    public String getConsigneeName() { return consigneeName; }
    public String getConsigneeAddress() { return consigneeAddress; }
    public String getConsigneeGst() { return consigneeGst; }
    public String getConsigneeState() { return consigneeState; }
    
    public String getBuyerAddress() { return buyerAddress; }
    public String getBuyerGst() { return buyerGst; }
    public String getBuyerState() { return buyerState; }
}
