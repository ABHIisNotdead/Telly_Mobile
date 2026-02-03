package com.example.tellymobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class InvoiceActivity extends BaseActivity {

    private EditText etInvoiceNo, etDate, etItemName, etQuantity, etRate, etGstRate, etUnit;
    private android.widget.Spinner spnCustomer;
    // Dispatch Fields
    private EditText etDeliveryNote, etModePayment, etRefNo, etOtherRef, etBuyerOrderNo, etDispatchDocNo, etDeliveryNoteDate, etDispatchThrough, etDestination, etTermsDelivery;
    // Party Fields
    private EditText etBuyerAddress, etBuyerGst, etBuyerState;
    private EditText etConsigneeName, etConsigneeAddress, etConsigneeGst, etConsigneeState;
    private TextView tvSubtotal, tvGrandTotal;
    private Button btnAddItem, btnSaveInvoice, btnPrintShare, btnExcelShare, btnAddCharge;

    private RecyclerView rvInvoiceItems, rvCharges;
    private InvoiceAdapter adapter;
    private ChargesAdapter chargesAdapter;
    private List<InvoiceItem> invoiceItemList;
    private List<VoucherCharge> chargesList;
    private DatabaseHelper databaseHelper;
    private double subtotalAmount = 0;
    private double grandTotalAmount = 0;
    private double totalChargesAmount = 0;
    private long updateId = -1;
    private long savedInvoiceId = -1;
    private String mode = "CREATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        databaseHelper = new DatabaseHelper(this);
        invoiceItemList = new ArrayList<>();
        chargesList = new ArrayList<>();

        initViews();
        setupRecyclerViews();
        setupListeners();
        
        mode = getIntent().getStringExtra("MODE");
         if ("EDIT".equals(mode)) {
            updateId = getIntent().getIntExtra("ID", -1);
            if (updateId != -1) {
                loadInvoiceData(updateId);
                btnSaveInvoice.setText("Update Invoice");
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("Update Invoice");
            }
        }
    }
    
    private void loadInvoiceItems(long id) {
        android.database.Cursor cursor = databaseHelper.getVoucherItems((int)id, "Sales");
        if (cursor != null) {
            invoiceItemList.clear();
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("item_name"));
                double qty = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"));
                double rate = cursor.getDouble(cursor.getColumnIndexOrThrow("rate"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                
                double gstRate = 0;
                int gstIdx = cursor.getColumnIndex("gst_rate");
                if (gstIdx != -1) gstRate = cursor.getDouble(gstIdx);
                
                double cgst = 0;
                int cgstIdx = cursor.getColumnIndex("cgst_amount");
                if (cgstIdx != -1) cgst = cursor.getDouble(cgstIdx);
                
                double sgst = 0;
                int sgstIdx = cursor.getColumnIndex("sgst_amount");
                if (sgstIdx != -1) sgst = cursor.getDouble(sgstIdx);

                String unit = "";
                int unitIdx = cursor.getColumnIndex("unit");
                if (unitIdx != -1) unit = cursor.getString(unitIdx);

                InvoiceItem item = new InvoiceItem(name, qty, rate, amount, gstRate, cgst, sgst, unit);
                invoiceItemList.add(item);
            }
            cursor.close();
            adapter.notifyDataSetChanged();
            updateTotals();
        }
    }
    
    private void loadVoucherCharges(long id) {
        android.database.Cursor cursor = databaseHelper.getVoucherCharges(id, "Sales");
        if (cursor != null) {
            chargesList.clear();
            while (cursor.moveToNext()) {
                int ledgerId = cursor.getInt(cursor.getColumnIndexOrThrow("ledger_id"));
                String ledgerName = cursor.getString(cursor.getColumnIndexOrThrow("ledger_name"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                boolean isPercentage = cursor.getInt(cursor.getColumnIndexOrThrow("is_percentage")) == 1;
                double rate = cursor.getDouble(cursor.getColumnIndexOrThrow("rate"));
                
                chargesList.add(new VoucherCharge(ledgerId, ledgerName, amount, isPercentage, rate));
            }
            cursor.close();
            chargesAdapter.notifyDataSetChanged();
            updateTotals();
        }
    }

    private void loadInvoiceData(long id) {
        android.database.Cursor cursor = databaseHelper.getVoucher((int)id, "Sales");
        if (cursor != null && cursor.moveToFirst()) {
            etInvoiceNo.setText(cursor.getString(cursor.getColumnIndexOrThrow("invoice_number")));
            etDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("date")));
            String custName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name"));
            // Set Spinner Selection
            android.widget.ArrayAdapter<String> adapter = (android.widget.ArrayAdapter<String>) spnCustomer.getAdapter();
            if (adapter != null) {
                int pos = adapter.getPosition(custName);
                if (pos >= 0) spnCustomer.setSelection(pos);
            }
            
            // New Fields
            safeSetText(etDeliveryNote, cursor, "delivery_note");
            safeSetText(etModePayment, cursor, "mode_payment");
            safeSetText(etRefNo, cursor, "reference_no");
            safeSetText(etOtherRef, cursor, "other_references");
            safeSetText(etBuyerOrderNo, cursor, "buyers_order_no");
            safeSetText(etDispatchDocNo, cursor, "dispatch_doc_no");
            safeSetText(etDeliveryNoteDate, cursor, "delivery_note_date");
            safeSetText(etDispatchThrough, cursor, "dispatch_through");
            safeSetText(etDestination, cursor, "destination");
            safeSetText(etTermsDelivery, cursor, "terms_delivery");
            
            safeSetText(etBuyerAddress, cursor, "buyer_address");
            safeSetText(etBuyerGst, cursor, "buyer_gst");
            safeSetText(etBuyerState, cursor, "buyer_state");
            
            safeSetText(etConsigneeName, cursor, "consignee_name");
            safeSetText(etConsigneeAddress, cursor, "consignee_address");
            safeSetText(etConsigneeGst, cursor, "consignee_gst");
            safeSetText(etConsigneeState, cursor, "consignee_state");
            
            cursor.close();
            
            // Load Items
            loadInvoiceItems(id);
            // Load Charges
            loadVoucherCharges(id);
        }
    }
    
    private void safeSetText(EditText et, android.database.Cursor c, String col) {
        int idx = c.getColumnIndex(col);
        if (idx != -1) et.setText(c.getString(idx));
    }

    private void initViews() {
        etInvoiceNo = findViewById(R.id.etInvoiceNo);
        etDate = findViewById(R.id.etDate);
        spnCustomer = findViewById(R.id.spnCustomer); // Changed from EditText
        etItemName = findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etQuantity);
        etRate = findViewById(R.id.etRate);
        etRate = findViewById(R.id.etRate);
        etGstRate = findViewById(R.id.etGstRate);
        etUnit = findViewById(R.id.etUnit); // Added
        
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        // tvTotalTax removed from layout references if not needed or keep if hidden
        
        btnAddItem = findViewById(R.id.btnAddItem);
        btnAddCharge = findViewById(R.id.btnAddCharge);
        btnSaveInvoice = findViewById(R.id.btnSaveInvoice);
        btnPrintShare = findViewById(R.id.btnPrintShare);
        btnExcelShare = findViewById(R.id.btnExcelShare);
        
        // Populate Customer Spinner
        List<String> customers = databaseHelper.getLedgersByGroupList("Sundry Debtors");
        if (customers.isEmpty()) {
            customers.add("Cash"); // Fallback
        }
        android.widget.ArrayAdapter<String> custAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, customers);
        spnCustomer.setAdapter(custAdapter);
        
        // ... (Back logic)
        rvInvoiceItems = findViewById(R.id.rvInvoiceItems);
        rvCharges = findViewById(R.id.rvCharges);
        
        // Dispatch Fields
        etDeliveryNote = findViewById(R.id.etDeliveryNote);
        etModePayment = findViewById(R.id.etModePayment);
        etRefNo = findViewById(R.id.etRefNo);
        etOtherRef = findViewById(R.id.etOtherRef);
        etBuyerOrderNo = findViewById(R.id.etBuyerOrderNo);
        etDispatchDocNo = findViewById(R.id.etDispatchDocNo);
        etDeliveryNoteDate = findViewById(R.id.etDeliveryNoteDate);
        etDispatchThrough = findViewById(R.id.etDispatchThrough);
        etDestination = findViewById(R.id.etDestination);
        etTermsDelivery = findViewById(R.id.etTermsDelivery);
        
        // Party Fields
        etBuyerAddress = findViewById(R.id.etBuyerAddress);
        etBuyerGst = findViewById(R.id.etBuyerGst);
        etBuyerState = findViewById(R.id.etBuyerState);
        etConsigneeName = findViewById(R.id.etConsigneeName);
        etConsigneeAddress = findViewById(R.id.etConsigneeAddress);
        etConsigneeGst = findViewById(R.id.etConsigneeGst);
        etConsigneeState = findViewById(R.id.etConsigneeState);
        
        // Back Button Logic
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerViews() {
        adapter = new InvoiceAdapter(invoiceItemList);
        rvInvoiceItems.setLayoutManager(new LinearLayoutManager(this));
        rvInvoiceItems.setAdapter(adapter);
        
        chargesAdapter = new ChargesAdapter(chargesList, this::removeCharge);
        rvCharges.setLayoutManager(new LinearLayoutManager(this));
        rvCharges.setAdapter(chargesAdapter);
    }
    
    private void removeCharge(int position) {
        chargesList.remove(position);
        chargesAdapter.notifyItemRemoved(position);
        updateTotals();
    }

    private void setupListeners() {
        btnAddItem.setOnClickListener(v -> addItem());
        btnAddCharge.setOnClickListener(v -> showAddChargeDialog());
        btnSaveInvoice.setOnClickListener(v -> saveInvoice());
        btnPrintShare.setOnClickListener(v -> printAndShare());
        btnExcelShare.setOnClickListener(v -> printAndShareExcel());
        
        spnCustomer.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                 String customerName = (String) parent.getItemAtPosition(position);
                 // Fetch details ONLY if creating new or if user hasn't typed manually? 
                 // Simple logic: Always fetch on change.
                 android.database.Cursor c = databaseHelper.getLedgerDetails(customerName);
                 if (c != null && c.moveToFirst()) {
                     // Auto-fill Buyer Details
                     String addr = c.getString(c.getColumnIndexOrThrow("address"));
                     String gst = c.getString(c.getColumnIndexOrThrow("gst"));
                     
                     etBuyerAddress.setText(addr);
                     etBuyerGst.setText(gst);
                     
                     // Try to guess state? Or leave blank.
                     // Also auto-fill consignee with same data as default
                     etConsigneeName.setText(customerName);
                     etConsigneeAddress.setText(addr);
                     etConsigneeGst.setText(gst);
                     c.close();
                 }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }
    
    private void showAddChargeDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add Charge / Tax");
        
        // programmatically build layout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        final android.widget.Spinner spnLedger = new android.widget.Spinner(this);
        // Filter for specific groups relevant to charges/taxes
        List<String> ledgers = new ArrayList<>();
        ledgers.addAll(databaseHelper.getLedgersByGroupList("Duties & Taxes"));
        ledgers.addAll(databaseHelper.getLedgersByGroupList("Indirect Expenses"));
        ledgers.addAll(databaseHelper.getLedgersByGroupList("Direct Expenses"));
        ledgers.addAll(databaseHelper.getLedgersByGroupList("Indirect Incomes")); 
        
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ledgers);
        spnLedger.setAdapter(adapter);
        layout.addView(spnLedger);
        
        final EditText etValue = new EditText(this);
        etValue.setHint("Amount or Rate (%)");
        etValue.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etValue);
        
        final android.widget.CheckBox cbIsPercentage = new android.widget.CheckBox(this);
        cbIsPercentage.setText("Is Percentage (%)");
        layout.addView(cbIsPercentage);
        
        spnLedger.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedName = ledgers.get(position);
                android.database.Cursor c = databaseHelper.getLedgerDetails(selectedName);
                if (c != null && c.moveToFirst()) {
                    int rateIdx = c.getColumnIndex("tax_rate");
                    int pctIdx = c.getColumnIndex("is_percentage");
                    
                    if (rateIdx != -1) {
                        double rate = c.getDouble(rateIdx);
                        if (rate > 0) etValue.setText(String.valueOf(rate));
                        else etValue.setText("");
                    }
                    if (pctIdx != -1) {
                         cbIsPercentage.setChecked(c.getInt(pctIdx) == 1);
                    }
                    c.close();
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        builder.setView(layout);
        
        builder.setPositiveButton("Add", (dialog, which) -> {
            String ledgerName = (String) spnLedger.getSelectedItem();
            String valStr = etValue.getText().toString();
            if (ledgerName != null && !valStr.isEmpty()) {
                double val = Double.parseDouble(valStr);
                boolean isPercent = cbIsPercentage.isChecked();
                
                // Calculate amount initially
                double amount = isPercent ? (subtotalAmount * val / 100) : val;
                
                // Ledger ID - fetch based on name (Using helper or assuming name is unique enough for this UI)
                // We need ID for DB. Simplification: fetch ID from name query
                int ledgerId = 0; 
                android.database.Cursor c = databaseHelper.getLedgerDetails(ledgerName);
                if(c!=null && c.moveToFirst()) {
                    ledgerId = c.getInt(c.getColumnIndexOrThrow("_id"));
                    c.close();
                }
                
                chargesList.add(new VoucherCharge(ledgerId, ledgerName, amount, isPercent, val));
                chargesAdapter.notifyDataSetChanged();
                updateTotals();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addItem() {
        String name = etItemName.getText().toString();
        String qtyStr = etQuantity.getText().toString();
        String rateStr = etRate.getText().toString();
        String unit = etUnit.getText().toString();

        if (name.isEmpty() || qtyStr.isEmpty() || rateStr.isEmpty()) {
            Toast.makeText(this, "Please fill all item details", Toast.LENGTH_SHORT).show();
            return;
        }

        double qty = Double.parseDouble(qtyStr);
        double rate = Double.parseDouble(rateStr);
        String gstStr = etGstRate.getText().toString();
        double gstRate = gstStr.isEmpty() ? 0 : Double.parseDouble(gstStr);
        
        // Calculate Amounts
        double taxableValue = qty * rate;
        
        // Tax Calculation (Item level)
        double taxAmount = taxableValue * (gstRate / 100);
        double cgst = taxAmount / 2;
        double sgst = taxAmount / 2;

        InvoiceItem item = new InvoiceItem(name, qty, rate, taxableValue, gstRate, cgst, sgst, unit);
        invoiceItemList.add(item);
        this.adapter.notifyDataSetChanged();

        updateTotals();

        // Clear item fields
        etItemName.setText("");
        etQuantity.setText("");
        etRate.setText("");
        etUnit.setText(""); // Added
        etGstRate.setText("0");
        etItemName.requestFocus();
    }
    
    private void updateTotals() {
        subtotalAmount = 0;
        double itemTaxTotal = 0;
        
        for (InvoiceItem item : invoiceItemList) {
            subtotalAmount += item.getAmount(); // Taxable
            itemTaxTotal += (item.getCgstAmount() + item.getSgstAmount());
        }
        
        // Recalculate Charges (especially percentages based on new subtotal)
        totalChargesAmount = 0;
        for (VoucherCharge charge : chargesList) {
            if (charge.isPercentage) {
                charge.amount = subtotalAmount * (charge.rate / 100);
            }
            totalChargesAmount += charge.amount;
        }
        if (chargesAdapter != null) chargesAdapter.notifyDataSetChanged(); // Refresh amounts in UI
        
        grandTotalAmount = subtotalAmount + itemTaxTotal + totalChargesAmount;
        
        tvSubtotal.setText(String.format("₹%.2f", subtotalAmount));
        // tvTotalTax removed
        tvGrandTotal.setText(String.format("Total: ₹%.2f", grandTotalAmount));
    }

    private boolean saveInvoice() {
        String invoiceNo = etInvoiceNo.getText().toString();
        String date = etDate.getText().toString();
        String customer = spnCustomer.getSelectedItem() != null ? spnCustomer.getSelectedItem().toString() : "";

        if (invoiceNo.isEmpty() || date.isEmpty() || customer.isEmpty() || invoiceItemList.isEmpty()) {
            Toast.makeText(this, "Please fill invoice details and add items", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // We aren't storing each item tax in invoice total tax column separately from charges, so let's sum them up or store properly.
        // Legacy column 'total_tax' can store Item Tax. 'delivery_charges' is deprecated, we use 'grand_total'.
        // Let's store Item Tax in 'total_tax' and Charges will be in separate table. 
        // We can put totalChargesAmount in 'delivery_charges' column or just rely on the separate table.
        // For compatibility, let's put totalChargesAmount into delivery_charges column for now (as "Other Charges").
        
        double itemTaxTotal = 0;
        for (InvoiceItem item : invoiceItemList) {
            itemTaxTotal += (item.getCgstAmount() + item.getSgstAmount());
        }

        if ("EDIT".equals(mode) && updateId != -1) {
            // Update
            Invoice inv = createInvoiceObject();
            
            databaseHelper.updateInvoice(updateId, inv);
            
            // Revert & Save Items
            databaseHelper.deleteInvoiceItems(updateId); 
            for (InvoiceItem item : invoiceItemList) {
                databaseHelper.addInvoiceItem(updateId, item.getItemName(), item.getQuantity(), item.getRate(), item.getAmount(), item.getGstRate(), item.getCgstAmount(), item.getSgstAmount(), item.getUnit());
            }
            
            // Revert & Save Charges
            databaseHelper.deleteVoucherCharges(updateId, "Sales");
            for (VoucherCharge charge : chargesList) {
                databaseHelper.addVoucherCharge(updateId, "Sales", charge.ledgerId, charge.ledgerName, charge.amount, charge.isPercentage, charge.rate);
            }
            
            Toast.makeText(this, "Invoice Updated Successfully!", Toast.LENGTH_SHORT).show();
            savedInvoiceId = updateId;
        } else {
            // Create New
            Invoice inv = createInvoiceObject();

            savedInvoiceId = databaseHelper.addInvoiceObject(inv);
            
            if (savedInvoiceId != -1) {
                for (InvoiceItem item : invoiceItemList) {
                    databaseHelper.addInvoiceItem(savedInvoiceId, item.getItemName(), item.getQuantity(), item.getRate(), item.getAmount(), item.getGstRate(), item.getCgstAmount(), item.getSgstAmount(), item.getUnit());
                }
                // Save Charges
                for (VoucherCharge charge : chargesList) {
                    databaseHelper.addVoucherCharge(savedInvoiceId, "Sales", charge.ledgerId, charge.ledgerName, charge.amount, charge.isPercentage, charge.rate);
                }
                
                Toast.makeText(this, "Invoice Saved Successfully!", Toast.LENGTH_SHORT).show();
            } else {
                 Toast.makeText(this, "Failed to save invoice", Toast.LENGTH_SHORT).show();
                 return false;
            }
        }
        
        btnSaveInvoice.setEnabled(false); // Prevent double save
        return true;
    }

    private Invoice createInvoiceObject() {
        Invoice invoice = new Invoice(
            etInvoiceNo.getText().toString(),
            etDate.getText().toString(),
            spnCustomer.getSelectedItem() != null ? spnCustomer.getSelectedItem().toString() : "",
            invoiceItemList,
            subtotalAmount,
            totalChargesAmount, 
            grandTotalAmount - subtotalAmount - totalChargesAmount, 
            grandTotalAmount
        );
        invoice.setDispatchDetails(etDeliveryNote.getText().toString(), etModePayment.getText().toString(), etRefNo.getText().toString(),
                etOtherRef.getText().toString(), etBuyerOrderNo.getText().toString(), etDispatchDocNo.getText().toString(),
                etDeliveryNoteDate.getText().toString(), etDispatchThrough.getText().toString(), etDestination.getText().toString(),
                etTermsDelivery.getText().toString());
        invoice.setBuyerDetails(etBuyerAddress.getText().toString(), etBuyerGst.getText().toString(), etBuyerState.getText().toString());
        invoice.setConsigneeDetails(etConsigneeName.getText().toString(), etConsigneeAddress.getText().toString(), etConsigneeGst.getText().toString(), etConsigneeState.getText().toString());
        return invoice;
    }

    private void printAndShare() {
        if ( ("CREATE".equals(mode) && savedInvoiceId == -1) || ("EDIT".equals(mode) && btnSaveInvoice.isEnabled()) ) {
             if (!saveInvoice()) {
                 return;
             }
        }
        
        PdfGenerator pdfGenerator = new PdfGenerator(this);
        pdfGenerator.generateAndOpenPdf(createInvoiceObject());
    }
    
    private void printAndShareExcel() {
        if ( ("CREATE".equals(mode) && savedInvoiceId == -1) || ("EDIT".equals(mode) && btnSaveInvoice.isEnabled()) ) {
             if (!saveInvoice()) {
                 return;
             }
        }
        
        ExcelGenerator excelGenerator = new ExcelGenerator(this);
        excelGenerator.generateAndOpenExcel(createInvoiceObject());
    }
    
    // Inner Classes
    
    private static class VoucherCharge {
        int ledgerId;
        String ledgerName;
        double amount;
        boolean isPercentage;
        double rate;
        
        VoucherCharge(int ledgerId, String ledgerName, double amount, boolean isPercentage, double rate) {
            this.ledgerId = ledgerId; this.ledgerName = ledgerName; this.amount = amount; this.isPercentage = isPercentage; this.rate = rate;
        }
    }
    
    private static class ChargesAdapter extends RecyclerView.Adapter<ChargesAdapter.ChargeViewHolder> {
        private List<VoucherCharge> list;
        private OnRemoveListener listener;
        
        interface OnRemoveListener {
            void onRemove(int position);
        }
        
        ChargesAdapter(List<VoucherCharge> list, OnRemoveListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @androidx.annotation.NonNull
        @Override
        public ChargeViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            android.view.LayoutInflater inflater = android.view.LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.item_charge_row, parent, false);
            return new ChargeViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull ChargeViewHolder holder, int position) {
            VoucherCharge charge = list.get(position);
            holder.tvName.setText(charge.ledgerName);
            if (charge.isPercentage) {
                holder.tvValue.setText("@ " + charge.rate + "%");
            } else {
                holder.tvValue.setText("(Fixed)");
            }
            holder.tvAmount.setText(String.format("₹%.2f", charge.amount));
            holder.btnRemove.setOnClickListener(v -> listener.onRemove(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
        
        class ChargeViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvValue, tvAmount;
            android.widget.ImageButton btnRemove;
            
            ChargeViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvChargeName);
                tvValue = itemView.findViewById(R.id.tvChargeValue);
                tvAmount = itemView.findViewById(R.id.tvChargeAmount);
                btnRemove = itemView.findViewById(R.id.btnRemoveCharge);
            }
        }
    }
}
