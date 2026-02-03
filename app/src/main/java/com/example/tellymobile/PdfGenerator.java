package com.example.tellymobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfGenerator {

    private Context context;

    public PdfGenerator(Context context) {
        this.context = context;
    }

    private void showToast(final String message) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }

    public void generateAndOpenPdf(Invoice invoice) {
        try {
            // Inflate layout
            View view = LayoutInflater.from(context).inflate(R.layout.print_invoice_layout, null);

            // Populate Data
            TextView tvCustomer = view.findViewById(R.id.tvCustomerName);
            TextView tvInvoiceNo = view.findViewById(R.id.tvInvoiceNo);
            TextView tvDate = view.findViewById(R.id.tvDate);
            TextView tvSellerPan = view.findViewById(R.id.tvSellerPan); // Added
            
            // New Fields Binding
            TextView tvBuyerAddress = view.findViewById(R.id.tvBuyerAddress);
            TextView tvBuyerGst = view.findViewById(R.id.tvBuyerGst);
            TextView tvBuyerState = view.findViewById(R.id.tvBuyerState);
            
            TextView tvConsigneeName = view.findViewById(R.id.tvConsigneeName);
            TextView tvConsigneeAddress = view.findViewById(R.id.tvConsigneeAddress);
            TextView tvConsigneeGst = view.findViewById(R.id.tvConsigneeGst);
            TextView tvConsigneeState = view.findViewById(R.id.tvConsigneeState);
            
            TextView tvDeliveryNote = view.findViewById(R.id.tvDeliveryNote);
            TextView tvModePayment = view.findViewById(R.id.tvModePayment);
            TextView tvRefNo = view.findViewById(R.id.tvRefNo);
            TextView tvOtherRef = view.findViewById(R.id.tvOtherRef);
            TextView tvBuyerOrderNo = view.findViewById(R.id.tvBuyerOrderNo);
            TextView tvOrderDate = view.findViewById(R.id.tvOrderDate);
            TextView tvDispatchDocNo = view.findViewById(R.id.tvDispatchDocNo);
            TextView tvDispatchThrough = view.findViewById(R.id.tvDispatchThrough);
            TextView tvDestination = view.findViewById(R.id.tvDestination);
            TextView tvTermsDelivery = view.findViewById(R.id.tvTermsDelivery);

            TextView tvPdfSubtotal = view.findViewById(R.id.tvPdfSubtotal);
            TextView tvPdfTotalTax = view.findViewById(R.id.tvPdfTotalTax);
            TextView tvPdfDelivery = view.findViewById(R.id.tvPdfDelivery);
            TextView tvRoundOff = view.findViewById(R.id.tvRoundOff); // Added
            TextView tvTotal = view.findViewById(R.id.tvTotal);
            
            TextView tvAmountWords = view.findViewById(R.id.tvAmountWords);
            TextView tvTaxWords = view.findViewById(R.id.tvTaxWords); // Added
            LinearLayout itemsContainer = view.findViewById(R.id.llItemsContainer);
            
            if (invoice == null) throw new RuntimeException("Invoice object is null");

            if (tvCustomer != null) tvCustomer.setText(invoice.getCustomerName());
            if (tvInvoiceNo != null) tvInvoiceNo.setText(invoice.getInvoiceNumber());
            if (tvDate != null) tvDate.setText(invoice.getDate());
            
            // Populate New Fields
            if (tvBuyerAddress != null) tvBuyerAddress.setText(checkNull(invoice.getBuyerAddress()));
            if (tvBuyerGst != null) tvBuyerGst.setText("GSTIN: " + checkNull(invoice.getBuyerGst()));
            if (tvBuyerState != null) tvBuyerState.setText("State: " + checkNull(invoice.getBuyerState()));
            
            if (tvConsigneeName != null) tvConsigneeName.setText(checkNull(invoice.getConsigneeName()));
            if (tvConsigneeAddress != null) tvConsigneeAddress.setText(checkNull(invoice.getConsigneeAddress()));
            if (tvConsigneeGst != null) tvConsigneeGst.setText("GSTIN: " + checkNull(invoice.getConsigneeGst()));
            if (tvConsigneeState != null) tvConsigneeState.setText("State: " + checkNull(invoice.getConsigneeState()));
            
            if (tvDeliveryNote != null) tvDeliveryNote.setText(checkNull(invoice.getDeliveryNote()));
            if (tvModePayment != null) tvModePayment.setText(checkNull(invoice.getModeOfPayment()));
            if (tvRefNo != null) tvRefNo.setText(checkNull(invoice.getReferenceNo()));
            if (tvOtherRef != null) tvOtherRef.setText(checkNull(invoice.getOtherReferences()));
            if (tvBuyerOrderNo != null) tvBuyerOrderNo.setText(checkNull(invoice.getBuyersOrderNo()));
            if (tvOrderDate != null) tvOrderDate.setText(checkNull(invoice.getDeliveryNoteDate())); // Mapping Delivery Note Date here
            if (tvDispatchDocNo != null) tvDispatchDocNo.setText(checkNull(invoice.getDispatchDocNo()));
            if (tvDispatchThrough != null) tvDispatchThrough.setText(checkNull(invoice.getDispatchThrough()));
            if (tvDestination != null) tvDestination.setText(checkNull(invoice.getDestination()));
            if (tvTermsDelivery != null) tvTermsDelivery.setText(checkNull(invoice.getTermsOfDelivery()));
            
            // Footer Totals
            if (tvPdfSubtotal != null) tvPdfSubtotal.setText("₹" + String.format("%.2f", invoice.getTotalAmount())); 
            double delivery = invoice.getDeliveryCharges();
            double totalTax = invoice.getTotalTaxAmount();
            if (tvPdfTotalTax != null) tvPdfTotalTax.setText("₹" + String.format("%.2f", totalTax));
            if (tvPdfDelivery != null) tvPdfDelivery.setText("₹" + String.format("%.2f", delivery));
            
            double grandTotal = invoice.getGrandTotal();
            long roundedTotal = Math.round(grandTotal);
            double roundOff = roundedTotal - grandTotal;
            
            if (tvRoundOff != null) tvRoundOff.setText("₹" + String.format("%.2f", roundOff));
            if (tvTotal != null) tvTotal.setText("₹" + String.format("%.2f", (double)roundedTotal)); // Display rounded total

            // Number to Words
            if (tvAmountWords != null) tvAmountWords.setText("INR " + convertToWords(roundedTotal) + " Only");
            if (tvTaxWords != null) tvTaxWords.setText("INR " + convertToWords((long)Math.round(totalTax)) + " Only"); 
            
            // Add Items
            if (invoice != null && invoice.getItems() != null) {
                int slNo = 1;
                for (InvoiceItem item : invoice.getItems()) {
                    View itemView = LayoutInflater.from(context).inflate(R.layout.item_invoice_print_row, null);
                    TextView tvSlNo = itemView.findViewById(R.id.tvPrintSlNo);
                    TextView name = itemView.findViewById(R.id.tvPrintName);
                    TextView rate = itemView.findViewById(R.id.tvPrintRate);
                    TextView qty = itemView.findViewById(R.id.tvPrintQty);
                    TextView unit = itemView.findViewById(R.id.tvPrintUnit);
                    TextView amount = itemView.findViewById(R.id.tvPrintAmount);
                    TextView cgst = itemView.findViewById(R.id.tvPrintCgst);
                    TextView sgst = itemView.findViewById(R.id.tvPrintSgst);

                    if (tvSlNo != null) tvSlNo.setText(String.valueOf(slNo++));
                    if (name != null) name.setText(item.getItemName());
                    if (rate != null) rate.setText(String.format("%.2f", item.getRate()));
                    if (qty != null) qty.setText(String.valueOf(item.getQuantity()));
                    if (unit != null) unit.setText(checkNull(item.getUnit()));
                    if (amount != null) amount.setText(String.format("%.2f", item.getAmount()));
                    if (cgst != null) cgst.setText(String.format("%.2f", item.getCgstAmount()));
                    if (sgst != null) sgst.setText(String.format("%.2f", item.getSgstAmount()));
                    
                    itemsContainer.addView(itemView);
                }
            }

            // Measure & Layout with Scaling
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            float density = displayMetrics.density;
            
            // A4 dimensions in "points" (1/72 inch)
            int pdfWidthPoints = 595;
            int pdfHeightPoints = 842;
            
            // Scale to pixels for measuring the view (so it looks good on high-res)
            int measuredWidth = (int) (pdfWidthPoints * density);
            int measuredHeight = (int) (pdfHeightPoints * density);

            view.measure(View.MeasureSpec.makeMeasureSpec(measuredWidth, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(measuredHeight, View.MeasureSpec.EXACTLY));
            view.layout(0, 0, measuredWidth, measuredHeight);
            
            // Create PDF
            PdfDocument document = new PdfDocument();
            // The page info uses the POINTS dimensions (A4 standard)
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pdfWidthPoints, pdfHeightPoints, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            
            // Scale the canvas DOWN so the large view fits on the point-sized page
            canvas.scale(1 / density, 1 / density);
            
            view.draw(canvas);
            document.finishPage(page);
            
            // Save File
            File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Invoices");
            if (!directory.exists()) {
                boolean dirCreated = directory.mkdirs();
                if (!dirCreated) {
                     throw new IOException("Failed to create directory");
                }
            }
            File file = new File(directory, "Invoice_" + invoice.getInvoiceNumber() + ".pdf");

            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();
            
            showToast("PDF Saved: " + file.getAbsolutePath());
            android.util.Log.d("PdfGenerator", "PDF Saved at: " + file.getAbsolutePath());
            openPdf(file);
        
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("PdfGenerator", "Error in PDF Generation", e);
            String cause = e.getCause() != null ? e.getCause().toString() : "";
            showToast("Error: " + e.getMessage() + "\nCause: " + cause);
        }
    }

    private void openPdf(File file) {
        showToast("Opening PDF...");
        try {
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            android.util.Log.d("PdfGenerator", "File URI: " + uri.toString());
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // Check if there is an app handling this
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                  // Try staring anyway, resolveActivity can sometimes return null on Android 11+ due to package visibility
                   try {
                       context.startActivity(intent);
                   } catch (android.content.ActivityNotFoundException ex) {
                       showToast("No PDF Viewer installed found to open this file.");
                   }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error opening PDF: " + e.getMessage());
            android.util.Log.e("PdfGenerator", "Error opening PDF", e);
        }
    }

    private String checkNull(String s) {
        return s == null ? "" : s;
    }
    
    private String convertToWords(long n) {
        if (n == 0) return "Zero";
        
        String[] units = { "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen" };
        String[] tens = { "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety" };
  
        if (n < 0) return "Minus " + convertToWords(-n);
        if (n < 20) return units[(int)n];
        if (n < 100) return tens[(int)(n/10)] + ((n % 10 != 0) ? " " : "") + units[(int)(n % 10)];
        if (n < 1000) return units[(int)(n/100)] + " Hundred" + ((n % 100 != 0) ? " " : "") + convertToWords(n % 100);
        if (n < 100000) return convertToWords(n / 1000) + " Thousand" + ((n % 1000 != 0) ? " " : "") + convertToWords(n % 1000);
        if (n < 10000000) return convertToWords(n / 100000) + " Lakh" + ((n % 100000 != 0) ? " " : "") + convertToWords(n % 100000);
        return convertToWords(n / 10000000) + " Crore" + ((n % 10000000 != 0) ? " " : "") + convertToWords(n % 10000000);
    }
}
