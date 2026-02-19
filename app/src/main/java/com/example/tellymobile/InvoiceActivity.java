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

    private EditText etInvoiceNo, etDate, etQuantity, etRate, etGstRate, etUnit, etHsn;
    private android.widget.AutoCompleteTextView actvItemName;
    private android.widget.Spinner spnCustomer, spnBankLedger;
    // Dispatch Fields
    private EditText etDeliveryNote, etModePayment, etRefNo, etOtherRef, etBuyerOrderNo, etBuyersOrderDate, etDispatchDocNo, etDeliveryNoteDate, etDispatchThrough, etDestination, etTermsDelivery, etBillOfLading, etMotorVehicleNo;
    // Party Fields
    private EditText etBuyerAddress, etBuyerGst, etBuyerState, etBuyerEmail, etBuyerMobile;
    private EditText etConsigneeName, etConsigneeAddress, etConsigneeGst, etConsigneeState, etConsigneeEmail, etConsigneeMobile;
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
    private int selectedCompanyId = 0;
    private static final String PREFS_NAME = "TellyPrefs";
    private static final String KEY_COMPANY_ID = "selected_company_id";

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
        
        // Load Company Context
        android.content.SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedCompanyId = prefs.getInt(KEY_COMPANY_ID, 0);

        // Auto-fill Date and Voucher No
        if (etDate.getText().toString().isEmpty()) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
            etDate.setText(sdf.format(new java.util.Date()));
        }
        if (etInvoiceNo.getText().toString().isEmpty()) {
            long nextNum = databaseHelper.getNextVoucherNumber("Sales", selectedCompanyId);
            etInvoiceNo.setText(String.valueOf(nextNum));
        }

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

                String hsn = "";
                int hsnIdx = cursor.getColumnIndex("hsn");
                if (hsnIdx != -1) hsn = cursor.getString(hsnIdx);

                InvoiceItem item = new InvoiceItem(name, qty, rate, amount, gstRate, cgst, sgst, unit, hsn);
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
            
            // Set Bank Spinner Selection
            int bankIdIdx = cursor.getColumnIndex("bank_ledger_id");
            if (bankIdIdx != -1) {
                int bankId = cursor.getInt(bankIdIdx);
                if (bankId > 0) {
                     // Need to find name from ID to set spinner, or iterate adapter
                     // Simplest: Fetch name using helper if available, or just iterate ledgers if cached.
                     // But we only have ID. Let's get name from DB.
                     // Helper getLedgerName(id) ? Not standard.
                     // Or just generic query:
                     android.database.Cursor bankC = databaseHelper.getLedger(bankId); // Implied method or similar? 
                     // databaseHelper.getLedger(id) returns cursor.
                     if (bankC != null && bankC.moveToFirst()) {
                          String bankName = bankC.getString(bankC.getColumnIndexOrThrow("name"));
                          bankC.close();
                          android.widget.ArrayAdapter<String> bAdapter = (android.widget.ArrayAdapter<String>) spnBankLedger.getAdapter();
                          if (bAdapter != null) {
                              int pos = bAdapter.getPosition(bankName);
                              if (pos >= 0) spnBankLedger.setSelection(pos);
                          }
                     }
                }
            }
            
            // New Fields
            safeSetText(etDeliveryNote, cursor, "delivery_note");
            safeSetText(etModePayment, cursor, "mode_payment");
            safeSetText(etRefNo, cursor, "reference_no");
            safeSetText(etOtherRef, cursor, "other_references");
            safeSetText(etOtherRef, cursor, "other_references");
            safeSetText(etBuyerOrderNo, cursor, "buyers_order_no");
            safeSetText(etBuyersOrderDate, cursor, "buyers_order_date");
            safeSetText(etDispatchDocNo, cursor, "dispatch_doc_no");
            safeSetText(etDeliveryNoteDate, cursor, "delivery_note_date");
            safeSetText(etDispatchThrough, cursor, "dispatch_through");
            safeSetText(etDestination, cursor, "destination");
            safeSetText(etTermsDelivery, cursor, "terms_delivery");
            safeSetText(etBillOfLading, cursor, "bill_of_lading");
            safeSetText(etMotorVehicleNo, cursor, "motor_vehicle_no");
            
            safeSetText(etBuyerAddress, cursor, "buyer_address");
            safeSetText(etBuyerGst, cursor, "buyer_gst");
            safeSetText(etBuyerGst, cursor, "buyer_gst");
            safeSetText(etBuyerState, cursor, "buyer_state");
            safeSetText(etBuyerEmail, cursor, "buyer_email");
            safeSetText(etBuyerMobile, cursor, "buyer_mobile");
            
            safeSetText(etConsigneeName, cursor, "consignee_name");
            safeSetText(etConsigneeAddress, cursor, "consignee_address");
            safeSetText(etConsigneeGst, cursor, "consignee_gst");
            safeSetText(etConsigneeGst, cursor, "consignee_gst");
            safeSetText(etConsigneeState, cursor, "consignee_state");
            safeSetText(etConsigneeEmail, cursor, "consignee_email");
            safeSetText(etConsigneeMobile, cursor, "consignee_mobile");
            
            double legacyDeliveryCharges = 0;
            int dcIdx = cursor.getColumnIndex("delivery_charges");
            if (dcIdx != -1) legacyDeliveryCharges = cursor.getDouble(dcIdx);

            cursor.close();
            
            // Load Items
            loadInvoiceItems(id);
            // Load Charges
            loadVoucherCharges(id);
            
            // Legacy Support: If no charges found in new table but delivery_charges exists in old column
            if (chargesList.isEmpty() && legacyDeliveryCharges > 0) {
                chargesList.add(new VoucherCharge(-1, "Delivery Charges", legacyDeliveryCharges, false, 0));
                chargesAdapter.notifyDataSetChanged();
                updateTotals();
            }
        }
    }
    
    private void safeSetText(EditText et, android.database.Cursor c, String col) {
        int idx = c.getColumnIndex(col);
        if (idx != -1) et.setText(c.getString(idx));
    }

    private void initViews() {
        etInvoiceNo = findViewById(R.id.etInvoiceNo);
        etDate = findViewById(R.id.etDate);
        spnCustomer = findViewById(R.id.spnCustomer);
        spnBankLedger = findViewById(R.id.spnBankLedger);
        actvItemName = findViewById(R.id.actvItemName);
        etQuantity = findViewById(R.id.etQuantity);
        etRate = findViewById(R.id.etRate);
        etGstRate = findViewById(R.id.etGstRate);
        etUnit = findViewById(R.id.etUnit);
        etHsn = findViewById(R.id.etHsn);
        
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
        
        // Populate Bank Spinner
        List<String> bankLedgers = databaseHelper.getLedgersByGroupList("Bank Accounts");
        // Optional: Add "Select Bank" or empty first item if optional? Or allow selecting none by clearing?
        // For Spinner, hard to clear. Let's add "None" or empty string if it's optional.
        if (bankLedgers.isEmpty()) {
             // bankLedgers.add("None"); 
        } else {
             // bankLedgers.add(0, "None"); // If we want to allow unselecting
        }
        // Actually, just standard list.
        android.widget.ArrayAdapter<String> bankAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bankLedgers);
        spnBankLedger.setAdapter(bankAdapter);
        
        // Setup Item Autocomplete
        List<String> itemNames = databaseHelper.getAllItemNames();
        android.widget.ArrayAdapter<String> itemAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, itemNames);
        actvItemName.setAdapter(itemAdapter);
        
        actvItemName.setOnItemClickListener((parent, view, position, id) -> {
             String selectedItem = (String) parent.getItemAtPosition(position);
             android.database.Cursor c = databaseHelper.getItemDetailsByName(selectedItem);
             if (c != null && c.moveToFirst()) {
                 double rate = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_RATE));
                 String unit = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_UNIT));
                 String hsn = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_HSN));
                 double gstVal = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_GST_RATE));
                 String gstType = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM_GST_TYPE));
                 
                 etRate.setText(String.valueOf(rate));
                 etUnit.setText(unit);
                 etHsn.setText(hsn);
                 
                 double finalGstRate = gstVal;
                 if ("Amount".equalsIgnoreCase(gstType) && rate > 0) {
                     finalGstRate = (gstVal / rate) * 100;
                 }
                 etGstRate.setText(String.format("%.2f", finalGstRate));
                 
                 c.close();
             }
        });
        
        // ... (Back logic)
        rvInvoiceItems = findViewById(R.id.rvInvoiceItems);
        rvCharges = findViewById(R.id.rvCharges);
        
        // Dispatch Fields
        etDeliveryNote = findViewById(R.id.etDeliveryNote);
        etModePayment = findViewById(R.id.etModePayment);
        etRefNo = findViewById(R.id.etRefNo);
        etOtherRef = findViewById(R.id.etOtherRef);
        etBuyerOrderNo = findViewById(R.id.etBuyerOrderNo);
        etBuyersOrderDate = findViewById(R.id.etBuyersOrderDate);
        etDispatchDocNo = findViewById(R.id.etDispatchDocNo);
        etDeliveryNoteDate = findViewById(R.id.etDeliveryNoteDate);
        etDispatchThrough = findViewById(R.id.etDispatchThrough);
        etDestination = findViewById(R.id.etDestination);
        etTermsDelivery = findViewById(R.id.etTermsDelivery);
        etBillOfLading = findViewById(R.id.etBillOfLading);
        etMotorVehicleNo = findViewById(R.id.etMotorVehicleNo);
        
        // Party Fields
        etBuyerAddress = findViewById(R.id.etBuyerAddress);
        etBuyerGst = findViewById(R.id.etBuyerGst);
        etBuyerState = findViewById(R.id.etBuyerState);
        etBuyerEmail = findViewById(R.id.etBuyerEmail);
        etBuyerMobile = findViewById(R.id.etBuyerMobile);
        
        etConsigneeName = findViewById(R.id.etConsigneeName);
        etConsigneeAddress = findViewById(R.id.etConsigneeAddress);
        etConsigneeGst = findViewById(R.id.etConsigneeGst);
        etConsigneeState = findViewById(R.id.etConsigneeState);
        etConsigneeEmail = findViewById(R.id.etConsigneeEmail);
        etConsigneeMobile = findViewById(R.id.etConsigneeMobile);
        
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
        adapter = new InvoiceAdapter(invoiceItemList, position -> {
            invoiceItemList.remove(position);
            adapter.notifyItemRemoved(position);
            updateTotals();
        });
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
                     
                     String email = c.getString(c.getColumnIndexOrThrow("email"));
                     String mobile = c.getString(c.getColumnIndexOrThrow("mobile"));
                     etBuyerEmail.setText(email);
                     etBuyerMobile.setText(mobile);
                     
                     etConsigneeEmail.setText(email);
                     etConsigneeMobile.setText(mobile);
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
        
        // Use Dialog Context for better theme support
        android.content.Context dialogContext = builder.getContext();
        
        // programmatically build layout
        android.widget.ScrollView scrollView = new android.widget.ScrollView(dialogContext);
        android.widget.LinearLayout layout = new android.widget.LinearLayout(dialogContext);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        scrollView.addView(layout);
        
        final android.widget.Spinner spnLedger = new android.widget.Spinner(dialogContext);
        // Filter for specific groups relevant to charges/taxes
        List<String> ledgers = new ArrayList<>();
        ledgers.addAll(databaseHelper.getLedgersByGroupList("Duties & Taxes"));
        ledgers.addAll(databaseHelper.getLedgersByGroupList("Indirect Expenses"));
        ledgers.addAll(databaseHelper.getLedgersByGroupList("Direct Expenses"));
        ledgers.addAll(databaseHelper.getLedgersByGroupList("Indirect Incomes")); 
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(dialogContext, android.R.layout.simple_spinner_dropdown_item, ledgers);
        spnLedger.setAdapter(adapter);
        layout.addView(spnLedger);
        
        final EditText etValue = new android.widget.EditText(dialogContext);
        etValue.setHint("Amount or Rate (%)");
        etValue.setHintTextColor(android.graphics.Color.GRAY);
        etValue.setTextColor(android.graphics.Color.BLACK);
        etValue.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etValue);
        
        final android.widget.CheckBox cbIsPercentage = new android.widget.CheckBox(dialogContext);
        cbIsPercentage.setText("Is Percentage (%)");
        cbIsPercentage.setTextColor(android.graphics.Color.BLACK);
        layout.addView(cbIsPercentage);
        
        // Rounding Mode RadioGroup
        final android.widget.TextView tvRoundMode = new android.widget.TextView(dialogContext);
        tvRoundMode.setText("Select Rounding Logic:");
        tvRoundMode.setTextColor(android.graphics.Color.parseColor("#3F51B5")); // Indigo
        tvRoundMode.setPadding(0, 24, 0, 8);
        tvRoundMode.setVisibility(View.GONE);
        tvRoundMode.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(tvRoundMode);

        final android.widget.RadioGroup rgRoundMode = new android.widget.RadioGroup(dialogContext);
        rgRoundMode.setOrientation(android.widget.RadioGroup.VERTICAL); 
        rgRoundMode.setVisibility(View.GONE);
        rgRoundMode.setPadding(32, 16, 16, 16); 
        rgRoundMode.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        
        String[] modes = {"Auto (Nearest)", "Plus (+)", "Minus (-)"};
        for (int i = 0; i < modes.length; i++) {
            android.widget.RadioButton rb = new android.widget.RadioButton(dialogContext);
            rb.setText(modes[i]);
            rb.setId(i); 
            rb.setTextColor(android.graphics.Color.BLACK);
            rgRoundMode.addView(rb);
        }
        
        // Load preference
        android.content.SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int lastMode = prefs.getInt("round_off_mode", 0); 
        rgRoundMode.check(lastMode);
        layout.addView(rgRoundMode);

        
        spnLedger.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedName = ledgers.get(position);
                boolean isRoundOff = isRoundOffRefined(selectedName);
                
                tvRoundMode.setVisibility(isRoundOff ? View.VISIBLE : View.GONE);
                rgRoundMode.setVisibility(isRoundOff ? View.VISIBLE : View.GONE);
                etValue.setVisibility(isRoundOff ? View.GONE : View.VISIBLE);
                cbIsPercentage.setVisibility(isRoundOff ? View.GONE : View.VISIBLE);
                
                // Force update UI
                layout.requestLayout();
                scrollView.fullScroll(View.FOCUS_UP);

                android.database.Cursor c = databaseHelper.getLedgerDetails(selectedName);
                if (c != null && c.moveToFirst()) {
                    int rateIdx = c.getColumnIndex("tax_rate");
                    int pctIdx = c.getColumnIndex("is_percentage");
                    
                    if (rateIdx != -1 && !isRoundOff) {
                        double rate = c.getDouble(rateIdx);
                        if (rate > 0) etValue.setText(String.valueOf(rate));
                        else etValue.setText("");
                    }
                    if (isRoundOff) {
                        cbIsPercentage.setChecked(false);
                        etValue.setText("0"); // Hidden but set to 0 to satisfy possible checks
                    } else if (pctIdx != -1) {
                         cbIsPercentage.setChecked(c.getInt(pctIdx) == 1);
                    }
                    c.close();
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Initial check immediately after setting listener
        if (spnLedger.getSelectedItem() != null) {
            boolean isRO = isRoundOffRefined(spnLedger.getSelectedItem().toString());
            tvRoundMode.setVisibility(isRO ? View.VISIBLE : View.GONE);
            rgRoundMode.setVisibility(isRO ? View.VISIBLE : View.GONE);
            etValue.setVisibility(isRO ? View.GONE : View.VISIBLE);
            cbIsPercentage.setVisibility(isRO ? View.GONE : View.VISIBLE);
        }
        
        builder.setView(scrollView);
        
        builder.setPositiveButton("Add", (dialog, which) -> {
            String ledgerName = (String) spnLedger.getSelectedItem();
            if (ledgerName == null) return;
            boolean isRoundOff = isRoundOffRefined(ledgerName);
            String valStr = etValue.getText().toString();
            
            if (isRoundOff || !valStr.isEmpty()) {
                double val = isRoundOff ? 0 : Double.parseDouble(valStr);
                boolean isPercent = isRoundOff ? false : cbIsPercentage.isChecked();
                
                if (isRoundOff) {
                    // Save preference
                    int mode = rgRoundMode.getCheckedRadioButtonId();
                    if (mode == -1) mode = 0; // Fallback to Auto
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putInt("round_off_mode", mode).apply();
                }

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
        String name = actvItemName.getText().toString();
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
        
        String hsn = etHsn.getText().toString();

        InvoiceItem item = new InvoiceItem(name, qty, rate, taxableValue, gstRate, cgst, sgst, unit, hsn);
        invoiceItemList.add(item);
        this.adapter.notifyDataSetChanged();

        updateTotals();

        // Clear item fields
        actvItemName.setText("");
        etQuantity.setText("");
        etRate.setText("");
        etUnit.setText("");
        etHsn.setText(""); // Added
        etGstRate.setText("0");
        etGstRate.setText("0");
        actvItemName.requestFocus();
    }
    
    private boolean isRoundOffRefined(String name) {
        if (name == null) return false;
        String n = name.toLowerCase().replace(" ", "").replace("-", "").replace("_", "").trim();
        return n.contains("roundoff") || (n.contains("round") && n.contains("off"));
    }

    private void updateTotals() {
        subtotalAmount = 0;
        double itemTaxTotal = 0;
        
        for (InvoiceItem item : invoiceItemList) {
            subtotalAmount += item.getAmount(); // Taxable
            itemTaxTotal += (item.getCgstAmount() + item.getSgstAmount());
        }
        
        // Move Round Off to end if it exists
        VoucherCharge roundOffCharge = null;
        for (int i = 0; i < chargesList.size(); i++) {
            if (chargesList.get(i).ledgerName.equalsIgnoreCase("Round Off")) {
                roundOffCharge = chargesList.remove(i);
                chargesList.add(roundOffCharge);
                break;
            }
        }

        // Recalculate Charges
        totalChargesAmount = 0;
        double intermediateTotalBeforeRoundOff = subtotalAmount + itemTaxTotal;
        
        android.content.SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int roundMode = prefs.getInt("round_off_mode", 0); // 0: Auto, 1: Plus, 2: Minus

        for (VoucherCharge charge : chargesList) {
            if (charge.ledgerName.equalsIgnoreCase("Round Off")) {
                // Calculate Round Off amount based on preference
                double currentTotal = intermediateTotalBeforeRoundOff + totalChargesAmount;
                double roundedTotal;
                
                switch (roundMode) {
                    case 1: // Plus (+)
                        roundedTotal = Math.ceil(currentTotal);
                        break;
                    case 2: // Minus (-)
                        roundedTotal = Math.floor(currentTotal);
                        break;
                    default: // Auto (0)
                        roundedTotal = Math.round(currentTotal);
                        break;
                }
                
                charge.amount = roundedTotal - currentTotal;
                charge.isPercentage = false;
            } else if (charge.isPercentage) {
                charge.amount = subtotalAmount * (charge.rate / 100);
            }
            totalChargesAmount += charge.amount;
        }
        
        if (chargesAdapter != null) chargesAdapter.notifyDataSetChanged();
        
        grandTotalAmount = subtotalAmount + itemTaxTotal + totalChargesAmount;
        
        tvSubtotal.setText(String.format("₹%.2f", subtotalAmount));
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
                databaseHelper.addInvoiceItem(updateId, item.getItemName(), item.getQuantity(), item.getRate(), item.getAmount(), item.getGstRate(), item.getCgstAmount(), item.getSgstAmount(), item.getUnit(), item.getHsn());
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

            savedInvoiceId = databaseHelper.addInvoiceObject(inv, selectedCompanyId);
            
            if (savedInvoiceId != -1) {
                for (InvoiceItem item : invoiceItemList) {
                    databaseHelper.addInvoiceItem(savedInvoiceId, item.getItemName(), item.getQuantity(), item.getRate(), item.getAmount(), item.getGstRate(), item.getCgstAmount(), item.getSgstAmount(), item.getUnit(), item.getHsn());
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
        
        // Set Bank Ledger ID
        if (spnBankLedger.getSelectedItem() != null) {
            String selectedBank = spnBankLedger.getSelectedItem().toString();
            android.database.Cursor flags = databaseHelper.getLedgerDetails(selectedBank);
            if (flags != null && flags.moveToFirst()) {
                 try {
                      int idIndex = flags.getColumnIndex("_id");
                      if(idIndex != -1) invoice.setBankLedgerId(flags.getInt(idIndex));
                 } catch (Exception e) {}
                 flags.close();
            }
        }
        
        invoice.setDispatchDetails(etDeliveryNote.getText().toString(), etModePayment.getText().toString(), etRefNo.getText().toString(),
                etOtherRef.getText().toString(), etBuyerOrderNo.getText().toString(), etBuyersOrderDate.getText().toString(), etDispatchDocNo.getText().toString(),
                etDeliveryNoteDate.getText().toString(), etDispatchThrough.getText().toString(), etDestination.getText().toString(),
                etTermsDelivery.getText().toString(), etBillOfLading.getText().toString(), etMotorVehicleNo.getText().toString());
        invoice.setBuyerDetails(etBuyerAddress.getText().toString(), etBuyerGst.getText().toString(), etBuyerState.getText().toString(), etBuyerEmail.getText().toString(), etBuyerMobile.getText().toString());
        invoice.setConsigneeDetails(etConsigneeName.getText().toString(), etConsigneeAddress.getText().toString(), etConsigneeGst.getText().toString(), etConsigneeState.getText().toString(), etConsigneeEmail.getText().toString(), etConsigneeMobile.getText().toString());
        
        List<InvoiceCharge> invoiceCharges = new ArrayList<>();
        for (VoucherCharge vc : chargesList) {
             invoiceCharges.add(new InvoiceCharge(vc.ledgerName, vc.amount, vc.rate, vc.isPercentage));
        }
        invoice.setExtraCharges(invoiceCharges);
        
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
    
    // VoucherCharge inner class removed to use top-level class
    
    // ChargesAdapter extracted to separate file
}
