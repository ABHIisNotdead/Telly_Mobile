package com.example.tellymobile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class PurchaseActivity extends BaseActivity {

    private TextInputEditText etInvoiceNo, etDate, etSupplierInvDate, etSupplierInvNo, etSupplierName, etSupplierCst, etSupplierTin, etBuyerVatTin, etQuantity, etUnit, etRate, etHsn;
    private android.widget.AutoCompleteTextView etItemName;
    private Button btnAddItem, btnSavePurchase, btnViewPdf, btnExcelShare, btnAddCharge;
    private RecyclerView rvInvoiceItems, rvCharges;
    private InvoiceAdapter adapter; 
    private ChargesAdapter chargesAdapter;
    private List<InvoiceItem> purchaseItemList;
    private List<VoucherCharge> chargesList;
    private DatabaseHelper databaseHelper;
    private double totalAmount = 0;

    private long updateId = -1;
    private String mode = "CREATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        databaseHelper = new DatabaseHelper(this);
        purchaseItemList = new ArrayList<>();
        chargesList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupListeners();

        mode = getIntent().getStringExtra("MODE");
        if ("EDIT".equals(mode)) {
            updateId = getIntent().getIntExtra("ID", -1);
            if (updateId != -1) {
                loadPurchaseData(updateId);
                btnSavePurchase.setText("Update Purchase");
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("Update Purchase");
            }
        }
    }

    private void initViews() {
        etInvoiceNo = findViewById(R.id.etInvoiceNo);
        etSupplierInvNo = findViewById(R.id.etSupplierInvNo);
        etDate = findViewById(R.id.etDate);
        etSupplierInvDate = findViewById(R.id.etSupplierInvDate);
        etSupplierName = findViewById(R.id.etSupplierName);
        etSupplierCst = findViewById(R.id.etSupplierCst);
        etSupplierTin = findViewById(R.id.etSupplierTin);
        etBuyerVatTin = findViewById(R.id.etBuyerVatTin);
        etItemName = (android.widget.AutoCompleteTextView) findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etQuantity);
        etUnit = findViewById(R.id.etUnit);
        etHsn = findViewById(R.id.etHsn);
        etRate = findViewById(R.id.etRate);
        
        btnViewPdf = findViewById(R.id.btnViewPdf);
        btnExcelShare = findViewById(R.id.btnExcelShare);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnAddCharge = findViewById(R.id.btnAddCharge); // New
        btnSavePurchase = findViewById(R.id.btnSavePurchase);
        rvInvoiceItems = findViewById(R.id.rvInvoiceItems);
        rvCharges = findViewById(R.id.rvCharges); // New

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new InvoiceAdapter(purchaseItemList);
        rvInvoiceItems.setLayoutManager(new LinearLayoutManager(this));
        rvInvoiceItems.setAdapter(adapter);

        // Setup Charges Adapter
        chargesAdapter = new ChargesAdapter(chargesList, this::removeCharge);
        rvCharges.setLayoutManager(new LinearLayoutManager(this));
        rvCharges.setAdapter(chargesAdapter);
    }
    
    private void removeCharge(int position) {
        chargesList.remove(position);
        chargesAdapter.notifyItemRemoved(position);
        updateTotalDisplay();
    }

    private void setupListeners() {
        // Setup Item Name Autocomplete
        List<String> itemNames = databaseHelper.getAllItemNames();
        android.widget.ArrayAdapter<String> itemAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, itemNames);
        etItemName.setAdapter(itemAdapter);

        etItemName.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            android.database.Cursor c = databaseHelper.getItemDetailsByName(selectedItem);
            if (c != null && c.moveToFirst()) {
                double rate = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_RATE));
                String unit = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_UNIT));
                String hsn = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_HSN));
                
                etRate.setText(String.valueOf(rate));
                etUnit.setText(unit);
                etHsn.setText(hsn);
                c.close();
            }
        });

        btnAddItem.setOnClickListener(v -> addItem());
        btnAddCharge.setOnClickListener(v -> showAddChargeDialog());
        btnSavePurchase.setOnClickListener(v -> savePurchase());
        
        btnViewPdf.setOnClickListener(v -> printAndSharePdf());
        btnExcelShare.setOnClickListener(v -> printAndShareExcel());
    }

    private void showAddChargeDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add Charge / Tax");
        
        android.content.Context dialogContext = builder.getContext();
        
        android.widget.ScrollView scrollView = new android.widget.ScrollView(dialogContext);
        android.widget.LinearLayout layout = new android.widget.LinearLayout(dialogContext);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        scrollView.addView(layout);
        
        final android.widget.Spinner spnLedger = new android.widget.Spinner(dialogContext);
        List<String> ledgers = new ArrayList<>();
        ledgers.addAll(databaseHelper.getLedgersByGroupList("Duties & Taxes"));
        ledgers.addAll(databaseHelper.getLedgersByGroupList("Indirect Expenses"));
        ledgers.addAll(databaseHelper.getLedgersByGroupList("Direct Expenses"));
        ledgers.addAll(databaseHelper.getLedgersByGroupList("Indirect Incomes")); 
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(dialogContext, android.R.layout.simple_spinner_dropdown_item, ledgers);
        spnLedger.setAdapter(adapter);
        layout.addView(spnLedger);
        
        final android.widget.EditText etValue = new android.widget.EditText(dialogContext);
        etValue.setHint("Amount or Rate (%)");
        etValue.setHintTextColor(android.graphics.Color.GRAY);
        etValue.setTextColor(android.graphics.Color.BLACK);
        etValue.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etValue);
        
        final android.widget.CheckBox cbIsPercentage = new android.widget.CheckBox(dialogContext);
        cbIsPercentage.setText("Is Percentage (%)");
        cbIsPercentage.setTextColor(android.graphics.Color.BLACK);
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
        
        builder.setView(scrollView);
        
        builder.setPositiveButton("Add", (dialog, which) -> {
            String ledgerName = (String) spnLedger.getSelectedItem();
            if (ledgerName == null) return;
            String valStr = etValue.getText().toString();
            
            if (!valStr.isEmpty()) {
                double val = Double.parseDouble(valStr);
                boolean isPercent = cbIsPercentage.isChecked();
                
                double amount = isPercent ? (totalAmount * val / 100) : val; // Base amount is subtotal (totalAmount here tracks items total)
                
                int ledgerId = 0; 
                android.database.Cursor c = databaseHelper.getLedgerDetails(ledgerName);
                if(c!=null && c.moveToFirst()) {
                    ledgerId = c.getInt(c.getColumnIndexOrThrow("_id"));
                    c.close();
                }
                
                chargesList.add(new VoucherCharge(ledgerId, ledgerName, amount, isPercent, val));
                chargesAdapter.notifyDataSetChanged();
                updateTotalDisplay();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addItem() {
        String name = etItemName.getText().toString();
        String qtyStr = etQuantity.getText().toString();
        String unit = etUnit.getText().toString();
        String hsn = etHsn.getText().toString();
        String rateStr = etRate.getText().toString();

        if (name.isEmpty() || qtyStr.isEmpty() || rateStr.isEmpty()) {
            NotificationUtils.showTopNotification(this, databaseHelper, "Please fill all item details", true);
            return;
        }

        double qty = Double.parseDouble(qtyStr);
        double rate = Double.parseDouble(rateStr);
        double amount = qty * rate;

        InvoiceItem item = new InvoiceItem(name, qty, rate, amount, 0, 0, 0, unit, hsn);
        purchaseItemList.add(item);
        adapter.notifyDataSetChanged();

        updateTotalDisplay();

        // Clear item fields
        etItemName.setText("");
        etQuantity.setText("");
        etRate.setText("");
        etUnit.setText("");
        etHsn.setText("");
    }
    
    private void updateTotalDisplay() {
        double subtotal = 0;
        for (InvoiceItem item : purchaseItemList) {
            subtotal += item.getAmount();
        }
        totalAmount = subtotal; // Keeps track of items only for percentage calc

        double totalCharges = 0;
        for (VoucherCharge charge : chargesList) {
             if (charge.isPercentage) {
                 charge.amount = subtotal * (charge.rate / 100);
             }
             totalCharges += charge.amount;
        }
        if (chargesAdapter != null) chargesAdapter.notifyDataSetChanged();

        double grandTotal = subtotal + totalCharges;

        android.widget.TextView tvTotal = findViewById(R.id.tvTotalAmount);
        if (tvTotal != null) {
            tvTotal.setText("Total: ₹" + String.format("%.2f", grandTotal));
        }
        this.totalAmount = grandTotal; // Update global total for saving (This variable name is ambiguous now, should be grandTotal but reusing totalAmount)
    }

    private void loadPurchaseData(long id) {
        android.database.Cursor cursor = databaseHelper.getVoucher((int)id, "Purchase");
        if (cursor != null && cursor.moveToFirst()) {
            etInvoiceNo.setText(cursor.getString(cursor.getColumnIndexOrThrow("purchase_inv_no")));
            
            int supInvIdx = cursor.getColumnIndex("supplier_inv_no");
            if (supInvIdx != -1) etSupplierInvNo.setText(cursor.getString(supInvIdx));
            
            etDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("purchase_date")));
            
            try { 
                int siIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SUPPLIER_INV_DATE);
                if (siIdx != -1) etSupplierInvDate.setText(cursor.getString(siIdx));
                
                int cstIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SUPPLIER_CST);
                if (cstIdx != -1) etSupplierCst.setText(cursor.getString(cstIdx));
                
                int tinIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SUPPLIER_TIN);
                if (tinIdx != -1) etSupplierTin.setText(cursor.getString(tinIdx));
                
                int bvatIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_BUYER_VAT_TIN);
                if (bvatIdx != -1) etBuyerVatTin.setText(cursor.getString(bvatIdx));
            } catch (Exception e) {}

            etSupplierName.setText(cursor.getString(cursor.getColumnIndexOrThrow("supplier_name")));
            // totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("purchase_total")); // Will be recalculated
            cursor.close();
            
            // Load Items
            android.database.Cursor items = databaseHelper.getVoucherItems((int)id, "Purchase");
            if (items != null) {
                purchaseItemList.clear();
                while (items.moveToNext()) {
                    String name = items.getString(items.getColumnIndexOrThrow("item_name"));
                    double qty = items.getDouble(items.getColumnIndexOrThrow("quantity"));
                    double rate = items.getDouble(items.getColumnIndexOrThrow("rate"));
                    double amount = items.getDouble(items.getColumnIndexOrThrow("amount"));
                    
                    String unit = "";
                    int uIdx = items.getColumnIndex("unit");
                    if (uIdx != -1) unit = items.getString(uIdx);
                    
                    String hsn = "";
                    int hIdx = items.getColumnIndex("hsn");
                    if (hIdx != -1) hsn = items.getString(hIdx);
                    
                    purchaseItemList.add(new InvoiceItem(name, qty, rate, amount, 0, 0, 0, unit, hsn));
                }
                items.close();
            }
            
            // Load Charges
            android.database.Cursor charges = databaseHelper.getVoucherCharges(id, "Purchase");
            if (charges != null) {
                chargesList.clear();
                while(charges.moveToNext()) {
                    int ledgerId = charges.getInt(charges.getColumnIndexOrThrow("ledger_id"));
                    String ledgerName = charges.getString(charges.getColumnIndexOrThrow("ledger_name"));
                    double amount = charges.getDouble(charges.getColumnIndexOrThrow("amount"));
                    boolean isPercentage = charges.getInt(charges.getColumnIndexOrThrow("is_percentage")) == 1;
                    double rate = charges.getDouble(charges.getColumnIndexOrThrow("rate"));
                    
                    chargesList.add(new VoucherCharge(ledgerId, ledgerName, amount, isPercentage, rate));
                }
                charges.close();
            }
            
            adapter.notifyDataSetChanged();
            chargesAdapter.notifyDataSetChanged();
            updateTotalDisplay();
        }
    }

    private boolean savePurchase() {
        String invoiceNo = etInvoiceNo.getText().toString();
        String supplierInvNo = etSupplierInvNo.getText().toString();
        String date = etDate.getText().toString();
        String supplierInvDate = etSupplierInvDate.getText().toString();
        String supplier = etSupplierName.getText().toString();
        String supplierCst = etSupplierCst.getText().toString();
        String supplierTin = etSupplierTin.getText().toString();
        String buyerVatTin = etBuyerVatTin.getText().toString();

        if (invoiceNo.isEmpty() || date.isEmpty() || supplier.isEmpty() || purchaseItemList.isEmpty()) {
            NotificationUtils.showTopNotification(this, databaseHelper, "Please fill purchase details and add items", true);
            return false;
        }

        android.content.SharedPreferences prefs = getSharedPreferences("TellyPrefs", MODE_PRIVATE);
        int companyId = prefs.getInt("selected_company_id", 0);
        
        // Ensure total is up to date (Calculated in updateTotalDisplay)
        updateTotalDisplay();

        if (updateId != -1) {
             databaseHelper.updatePurchase(updateId, invoiceNo, date, supplierInvDate, supplierInvNo, supplier, supplierCst, supplierTin, buyerVatTin, totalAmount);
             
             // Revert items & Charges
             databaseHelper.deletePurchaseItems(updateId);
             databaseHelper.deleteVoucherCharges(updateId, "Purchase");
             
             for (InvoiceItem item : purchaseItemList) {
                databaseHelper.addPurchaseItem(updateId, item.getItemName(), item.getQuantity(), item.getRate(), item.getAmount(), item.getUnit(), item.getHsn());
             }
             for (VoucherCharge charge : chargesList) {
                 databaseHelper.addVoucherCharge(updateId, "Purchase", charge.ledgerId, charge.ledgerName, charge.amount, charge.isPercentage, charge.rate);
             }
             
             NotificationUtils.showTopNotification(this, databaseHelper, "Purchase Updated Successfully!", false);
        } else {
            long savedId = databaseHelper.addPurchase(invoiceNo, date, supplierInvDate, supplierInvNo, supplier, supplierCst, supplierTin, buyerVatTin, totalAmount, companyId);
            if (savedId != -1) {
                for (InvoiceItem item : purchaseItemList) {
                    databaseHelper.addPurchaseItem(savedId, item.getItemName(), item.getQuantity(), item.getRate(), item.getAmount(), item.getUnit(), item.getHsn());
                }
                for (VoucherCharge charge : chargesList) {
                    databaseHelper.addVoucherCharge(savedId, "Purchase", charge.ledgerId, charge.ledgerName, charge.amount, charge.isPercentage, charge.rate);
                }
                
                NotificationUtils.showTopNotification(this, databaseHelper, "Purchase Saved & Stock Updated!", false);
                updateId = savedId;
            } else {
                NotificationUtils.showTopNotification(this, databaseHelper, "Failed to save purchase", true);
                return false;
            }
        }
        btnSavePurchase.setText("Update Purchase");
        return true;
    }

    private Invoice createInvoiceObject() {
        String invoiceNo = etInvoiceNo.getText().toString(); // Internal Voucher No
        String date = etDate.getText().toString();
        String supplier = etSupplierName.getText().toString();
        
        // Calculate subtotal for generator
        double subtotal = 0;
        for (InvoiceItem item : purchaseItemList) subtotal += item.getAmount();

        // Calculate Only Extra Charges Total
        double chargesTotal = 0;
        for(VoucherCharge vc : chargesList) chargesTotal += vc.amount;

        Invoice inv = new Invoice(invoiceNo, date, supplier, purchaseItemList, subtotal, chargesTotal, 0, totalAmount);
        inv.setSupplierInvDate(etSupplierInvDate.getText().toString());
        inv.setSupplierInvNo(etSupplierInvNo.getText().toString()); // Set Explicit Supplier Inv No
        inv.setSupplierCst(etSupplierCst.getText().toString());
        inv.setSupplierTin(etSupplierTin.getText().toString());
        inv.setBuyerVatTin(etBuyerVatTin.getText().toString());
        inv.setModeOfPayment("Purchase");
        
        // Fetch Supplier Address/Details and set as "BuyerDetails" carrier or explicit fields
        fetchSupplierDetails(supplier, inv);
        
        List<InvoiceCharge> invoiceCharges = new ArrayList<>();
        for (VoucherCharge vc : chargesList) {
             invoiceCharges.add(new InvoiceCharge(vc.ledgerName, vc.amount, vc.rate, vc.isPercentage));
        }
        inv.setExtraCharges(invoiceCharges);
        return inv;
    }
    
    private void fetchSupplierDetails(String supplierName, Invoice inv) {
        if (supplierName == null || supplierName.isEmpty()) return;
        android.database.Cursor c = databaseHelper.getLedgerDetails(supplierName);
        if(c != null && c.moveToFirst()) {
            String address = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS));
            String gst = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GST));
            // Use columns if available, assuming database helper has them or we use generic getter
            // For now, map to setBuyerDetails as carrier for the "Party"
            // The ExcelGenerator will map {{BUYER_ADDRESS}} or we will add {{SUPPLIER_ADDRESS}} mapping to invoice.getBuyerAddress
            inv.setBuyerDetails(address, gst, "", "", ""); 
            c.close();
        }
    }

    private void printAndSharePdf() {
        if (updateId == -1 || btnSavePurchase.isEnabled()) {
            if (!savePurchase()) return;
        }

        PdfGenerator pdfGenerator = new PdfGenerator(this);
        pdfGenerator.generateAndOpenPdf(createInvoiceObject());
    }

    private void printAndShareExcel() {
        if (updateId == -1 || btnSavePurchase.isEnabled()) {
            if (!savePurchase()) return;
        }

        ExcelGenerator excelGenerator = new ExcelGenerator(this);
        excelGenerator.generateAndOpenExcel(createInvoiceObject());
    }
}
