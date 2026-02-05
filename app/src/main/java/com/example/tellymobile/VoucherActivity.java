package com.example.tellymobile;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import android.widget.CheckBox;
import android.widget.EditText;
import android.app.AlertDialog;

public class VoucherActivity extends BaseActivity {

    private Spinner spnVoucherType;
    private AutoCompleteTextView actvPartyName, actvItemName, actvBankLedger;
    private TextInputEditText etVoucherNo, etDate, etQuantity, etRate, etUnit, etHsn, etGstRate;
    private TextView tvPartyAddress, tvPartyMobile, tvPartyGst, tvTotalAmount;
    
    // Company Header
    private TextView tvCompanyHeader, tvCompanyAddress, tvCompanyGST;
    
    // Dispatch Fields
    private LinearLayout llDispatchDetails;
    private Button btnToggleDispatch;
    private TextInputEditText etDispatchDocNo, etDestination, etDispatchThrough, etMotorVehicleNo;
    private TextInputEditText etDeliveryNote, etModePayment, etRefNo, etOtherRef, etBuyerOrderNo, etDeliveryNoteDate, etTermsDelivery, etBillOfLading;
    
    private LinearLayout llPartyInfo, llInventorySection;
    private Button btnAddItem, btnSave, btnAddCharge, btnViewPdf;
    private RecyclerView rvItems, rvCharges;

    private DatabaseHelper databaseHelper;
    private InvoiceAdapter adapter;
    private ChargesAdapter chargesAdapter;
    private List<InvoiceItem> itemList;
    private List<VoucherCharge> chargesList;
    private double totalAmount = 0;
    private double subtotalAmount = 0;
    private double totalChargesAmount = 0;
    
    private int selectedCompanyId = 0; // Default 0
    private static final String PREFS_NAME = "TellyPrefs";
    private static final String KEY_COMPANY_ID = "selected_company_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_voucher); // Can crash if layout invalid

            databaseHelper = new DatabaseHelper(this);
            itemList = new ArrayList<>();
            chargesList = new ArrayList<>();

            initViews();
            
            // Load Company Context
            android.content.SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            selectedCompanyId = prefs.getInt(KEY_COMPANY_ID, 0);
            loadCompanyDetails();
            
            setupVoucherTypeSpinner();
            setupAutocompleteAdapters(); // TODO: Filter by ID if segregation implemented in future
            setupRecyclerView();
            setupChargesRecyclerView();
            setupListeners();
            
            // Auto-fill Date
            if (etDate.getText().toString().isEmpty()) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                etDate.setText(sdf.format(new java.util.Date()));
            }
            
            String mode = getIntent().getStringExtra("MODE");
            if ("VIEW".equals(mode)) {
                int id = getIntent().getIntExtra("ID", -1);
                String type = getIntent().getStringExtra("TYPE");
                if (id != -1 && type != null) {
                    loadVoucherData(id, type);
                } else {
                     Toast.makeText(this, "Invalid Voucher Data", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening voucher: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        // Back Button
        // Setup Toolbar
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        spnVoucherType = findViewById(R.id.spnVoucherType);
        actvPartyName = findViewById(R.id.actvPartyName);
        actvBankLedger = findViewById(R.id.actvBankLedger);
        etVoucherNo = findViewById(R.id.etVoucherNo);
        etDate = findViewById(R.id.etDate);
        
        llPartyInfo = findViewById(R.id.llPartyInfo);
        tvPartyAddress = findViewById(R.id.tvPartyAddress);
        tvPartyMobile = findViewById(R.id.tvPartyMobile);
        tvPartyGst = findViewById(R.id.tvPartyGst);
        
        // Company Header
        tvCompanyHeader = findViewById(R.id.tvCompanyHeader);
        tvCompanyAddress = findViewById(R.id.tvCompanyAddress);
        tvCompanyGST = findViewById(R.id.tvCompanyGST);
        
        // Dispatch Section
        llDispatchDetails = findViewById(R.id.llDispatchDetails);
        btnToggleDispatch = findViewById(R.id.btnToggleDispatch);
        etDispatchDocNo = findViewById(R.id.etDispatchDocNo);
        etDestination = findViewById(R.id.etDestination);
        etDispatchThrough = findViewById(R.id.etDispatchThrough);
        etMotorVehicleNo = findViewById(R.id.etMotorVehicleNo);
        
        etDeliveryNote = findViewById(R.id.etDeliveryNote);
        etModePayment = findViewById(R.id.etModePayment);
        etRefNo = findViewById(R.id.etRefNo);
        etOtherRef = findViewById(R.id.etOtherRef);
        etBuyerOrderNo = findViewById(R.id.etBuyerOrderNo);
        etDeliveryNoteDate = findViewById(R.id.etDeliveryNoteDate);
        etTermsDelivery = findViewById(R.id.etTermsDelivery);
        etBillOfLading = findViewById(R.id.etBillOfLading);
        
        llInventorySection = findViewById(R.id.llInventorySection);
        actvItemName = findViewById(R.id.actvItemName);
        etHsn = findViewById(R.id.etHsn);
        etQuantity = findViewById(R.id.etQuantity);
        etUnit = findViewById(R.id.etUnit);
        etRate = findViewById(R.id.etRate);
        etGstRate = findViewById(R.id.etGstRate);
        btnAddItem = findViewById(R.id.btnAddItem);
        rvItems = findViewById(R.id.rvItems);
        
        // Charges
        rvCharges = findViewById(R.id.rvCharges);
        btnAddCharge = findViewById(R.id.btnAddCharge);
        
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnSave = findViewById(R.id.btnSave);
        btnViewPdf = findViewById(R.id.btnViewPdf);
    }
    
    private void loadCompanyDetails() {
        if (selectedCompanyId > 0) {
            Cursor c = databaseHelper.getCompany(selectedCompanyId);
            if (c != null && c.moveToFirst()) {
                tvCompanyHeader.setText(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_NAME)));
                tvCompanyAddress.setText(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_ADDRESS)));
                tvCompanyGST.setText("GST: " + c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPANY_GST)));
                
                String logoUri = c.getString(c.getColumnIndexOrThrow("company_logo")); // Use literal column name if constant check fails
                android.widget.ImageView ivLogo = findViewById(R.id.ivVoucherCompanyLogo);
                if (logoUri != null && !logoUri.isEmpty()) {
                    ivLogo.setVisibility(View.VISIBLE);
                    ivLogo.setImageURI(android.net.Uri.parse(logoUri));
                } else {
                    ivLogo.setVisibility(View.GONE);
                }
                c.close();
            }
        } else {
             // Default / No Company Selected
             tvCompanyHeader.setText("Default Company");
             tvCompanyAddress.setText("Configure in Settings");
        }
    }

    private void setupVoucherTypeSpinner() {
        String[] voucherTypes = {"Sales", "Purchase", "Receipt", "Payment"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, voucherTypes);
        spnVoucherType.setAdapter(adapter);

        spnVoucherType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = voucherTypes[position];
                if (type.equals("Sales") || type.equals("Purchase")) {
                    llInventorySection.setVisibility(View.VISIBLE);
                } else {
                    llInventorySection.setVisibility(View.GONE);
                }
                
                // Auto-fill Logic if fields are empty or default
                if (etVoucherNo.getText().toString().isEmpty()) {
                    long nextNum = databaseHelper.getNextVoucherNumber(type);
                    etVoucherNo.setText(String.valueOf(nextNum));
                }
                
                updatePartyAutocomplete(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupAutocompleteAdapters() {
        // Party Name Autocomplete - Initial Setup handled by Spinner selection
        // List<String> partyNames = databaseHelper.getAllLedgerNames();
        // ArrayAdapter<String> partyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, partyNames);
        // actvPartyName.setAdapter(partyAdapter);

        actvPartyName.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            fetchAndPopulatePartyDetails(selectedName);
        });

        // Item Name Autocomplete
        List<String> itemNames = databaseHelper.getAllItemNames();
        ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, itemNames);
        actvItemName.setAdapter(itemAdapter);
        
        actvItemName.setOnItemClickListener((parent, view, position, id) -> {
             String selectedItem = (String) parent.getItemAtPosition(position);
             double rate = databaseHelper.getItemRate(selectedItem);
             if(rate > 0) {
                 etRate.setText(String.valueOf(rate));
             }
        });
    }

    private void updatePartyAutocomplete(String type) {
        List<String> partyNames = new ArrayList<>();
        if ("Sales".equals(type) || "Receipt".equals(type)) {
            partyNames.addAll(databaseHelper.getLedgersByGroupList("Sundry Debtors"));
            partyNames.add("Cash");
        } else if ("Purchase".equals(type) || "Payment".equals(type)) {
            partyNames.addAll(databaseHelper.getLedgersByGroupList("Sundry Creditors"));
            partyNames.add("Cash");
        } else {
             // Fallback to all if unknown type
             partyNames = databaseHelper.getAllLedgerNames(); 
        }
        
        ArrayAdapter<String> partyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, partyNames);
        actvPartyName.setAdapter(partyAdapter);
        
        // Setup Bank Ledger Adapter
        List<String> bankLedgers = databaseHelper.getLedgersByGroupList("Bank Accounts");
        ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bankLedgers);
        actvBankLedger.setAdapter(bankAdapter);
    }

    private void fetchAndPopulatePartyDetails(String name) {
        Cursor cursor = databaseHelper.getLedgerDetails(name);
        if (cursor != null && cursor.moveToFirst()) {
            String address = cursor.getString(cursor.getColumnIndexOrThrow("address")); // Using literal column name if constant not visible
            String mobile = cursor.getString(cursor.getColumnIndexOrThrow("mobile"));
            String gst = cursor.getString(cursor.getColumnIndexOrThrow("gst"));

            tvPartyAddress.setText("Address: " + address);
            tvPartyMobile.setText("Mobile: " + mobile);
            tvPartyGst.setText("GST: " + gst);

            llPartyInfo.setVisibility(View.VISIBLE);
            cursor.close();
        }
    }

    private void setupRecyclerView() {
        adapter = new InvoiceAdapter(itemList, position -> {
            // Delete Item Logic
            itemList.remove(position);
            adapter.notifyItemRemoved(position);
            updateTotals();
        });
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);
    }

    private void setupChargesRecyclerView() {
        chargesAdapter = new ChargesAdapter(chargesList, this::removeCharge);
        rvCharges.setLayoutManager(new LinearLayoutManager(this));
        rvCharges.setAdapter(chargesAdapter);
    }

    private void setupListeners() {
        btnAddItem.setOnClickListener(v -> addItem());
        btnAddCharge.setOnClickListener(v -> showAddChargeDialog());
        btnSave.setOnClickListener(v -> saveVoucher());
        btnViewPdf.setOnClickListener(v -> viewPdf());
        
        btnToggleDispatch.setOnClickListener(v -> {
            boolean isVisible = llDispatchDetails.getVisibility() == View.VISIBLE;
            llDispatchDetails.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            btnToggleDispatch.setText(isVisible ? "Show Dispatch Details" : "Hide Dispatch Details");
        });
    }

    private void loadVoucherData(int id, String type) {
        Cursor cursor = databaseHelper.getVoucher(id, type);
        if (cursor != null && cursor.moveToFirst()) {
            if (type.equals("Sales")) {
                etVoucherNo.setText(cursor.getString(cursor.getColumnIndexOrThrow("invoice_number")));
                etDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                actvPartyName.setText(cursor.getString(cursor.getColumnIndexOrThrow("customer_name")));
                totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
            } else {
                etVoucherNo.setText(cursor.getString(cursor.getColumnIndexOrThrow("purchase_inv_no")));
                etDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("purchase_date")));
                actvPartyName.setText(cursor.getString(cursor.getColumnIndexOrThrow("supplier_name")));
                totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("purchase_total"));
            }
            cursor.close();
            
            // tvTotalAmount.setText("Total: ₹" + totalAmount); // Will be updated by load functions
            fetchAndPopulatePartyDetails(actvPartyName.getText().toString());
            
            // Set Spinner Selection
            for (int i = 0; i < spnVoucherType.getCount(); i++) {
                if (spnVoucherType.getItemAtPosition(i).toString().equals(type)) {
                    spnVoucherType.setSelection(i);
                    break;
                }
            }
            
            // Load Items
            loadVoucherItems(id, type);
            // Load Charges
            loadVoucherCharges(id, type);
            
            // Disable Editing
            disableEditing();
        }
    }

    // ... (loadVoucherItems, loadVoucherCharges, disableEditing, addItem are already defined/updated)

    private void saveVoucher() {
        String type = spnVoucherType.getSelectedItem().toString();
        String voucherNo = etVoucherNo.getText().toString();
        String date = etDate.getText().toString();
        String partyName = actvPartyName.getText().toString();

        if (voucherNo.isEmpty() || date.isEmpty() || partyName.isEmpty()) {
             Toast.makeText(this, "Please fill basic voucher details", Toast.LENGTH_SHORT).show();
             return;
        }
        
        if ((type.equals("Sales") || type.equals("Purchase")) && itemList.isEmpty()) {
             Toast.makeText(this, "Please add items for this voucher", Toast.LENGTH_SHORT).show();
             return;
        }

        long result = -1;
        
        // Calculate Item Tax Total for Sales
        double itemTaxTotal = 0;
        for (InvoiceItem item : itemList) {
            itemTaxTotal += (item.getCgstAmount() + item.getSgstAmount());
        }
        
        if (type.equals("Sales")) {
            // Create Full Invoice Object
            Invoice inv = createInvoiceObject(voucherNo, date, partyName, itemTaxTotal);
            
            // Set Bank Ledger ID
            String selectedBank = actvBankLedger.getText().toString();
            if (!selectedBank.isEmpty()) {
                Cursor flags = databaseHelper.getLedgerDetails(selectedBank);
                if (flags != null && flags.moveToFirst()) {
                     try {
                          int idIndex = flags.getColumnIndex("_id");
                          if(idIndex != -1) inv.setBankLedgerId(flags.getInt(idIndex));
                     } catch (Exception e) {}
                     flags.close();
                }
            }
            
            result = databaseHelper.addInvoiceObject(inv, selectedCompanyId);
            
            // Passing subtotal, charges, tax, grand total
            // result = databaseHelper.addInvoice(voucherNo, date, partyName, subtotalAmount, totalChargesAmount, itemTaxTotal, totalAmount);
            if (result != -1) {
                for (InvoiceItem item : itemList) {
                    databaseHelper.addInvoiceItem(result, item.getItemName(), item.getQuantity(), item.getRate(), item.getAmount(), item.getGstRate(), item.getCgstAmount(), item.getSgstAmount());
                }
                // Save Charges
                for (VoucherCharge charge : chargesList) {
                    databaseHelper.addVoucherCharge(result, "Sales", charge.ledgerId, charge.ledgerName, charge.amount, charge.isPercentage, charge.rate);
                }
            }
        } else if (type.equals("Purchase")) {
            result = databaseHelper.addPurchase(voucherNo, date, partyName, totalAmount, selectedCompanyId);
             if (result != -1) {
                for (InvoiceItem item : itemList) {
                    databaseHelper.addPurchaseItem(result, item.getItemName(), item.getQuantity(), item.getRate(), item.getAmount());
                }
                // Save Charges
                for (VoucherCharge charge : chargesList) {
                    databaseHelper.addVoucherCharge(result, "Purchase", charge.ledgerId, charge.ledgerName, charge.amount, charge.isPercentage, charge.rate);
                }
            }
        } else {
            // Receipt / Payment
            Toast.makeText(this, "Receipt/Payment Saving not yet implemented", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (result != -1) {
             Toast.makeText(this, type + " Voucher Saved Successfully!", Toast.LENGTH_LONG).show();
             databaseHelper.addNotification("Voucher Created", type + " Voucher " + voucherNo + " created for " + partyName, "Success");
             finish();
        } else {
             Toast.makeText(this, "Failed to save voucher", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadVoucherItems(int id, String type) {
        Cursor cursor = databaseHelper.getVoucherItems(id, type);
        if (cursor != null) {
            itemList.clear();
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("item_name"));
                double qty = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"));
                double rate = cursor.getDouble(cursor.getColumnIndexOrThrow("rate"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                
                double gst = 0, cgst = 0, sgst = 0;
                if (type.equals("Sales")) {
                     // Try to get GST info if available (for new invoices)
                     int gstIdx = cursor.getColumnIndex("gst_rate");
                     if (gstIdx != -1) gst = cursor.getDouble(gstIdx);
                     
                     int cgstIdx = cursor.getColumnIndex("cgst_amount");
                     if (cgstIdx != -1) cgst = cursor.getDouble(cgstIdx);
                     
                     int sgstIdx = cursor.getColumnIndex("sgst_amount");
                     if (sgstIdx != -1) sgst = cursor.getDouble(sgstIdx);
                }

                String unit = "";
                int unitIdx = cursor.getColumnIndex("unit");
                if (unitIdx != -1) unit = cursor.getString(unitIdx);

                String hsn = "";
                int hsnIdx = cursor.getColumnIndex("hsn");
                if (hsnIdx != -1) hsn = cursor.getString(hsnIdx);

                itemList.add(new InvoiceItem(name, qty, rate, amount, gst, cgst, sgst, unit, hsn));
            }
            cursor.close();
            adapter.notifyDataSetChanged();
            updateTotals(); // Recalculate based on loaded items
        }
    }

    private void loadVoucherCharges(int id, String type) {
        Cursor cursor = databaseHelper.getVoucherCharges(id, type);
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

    // ...

    private void disableEditing() {
        spnVoucherType.setEnabled(false);
        etVoucherNo.setEnabled(false);
        etDate.setEnabled(false);
        actvPartyName.setEnabled(false);
        
        actvItemName.setEnabled(false);
        etHsn.setEnabled(false);
        etQuantity.setEnabled(false);
        etUnit.setEnabled(false);
        etRate.setEnabled(false);
        etGstRate.setEnabled(false);
        btnAddItem.setVisibility(View.GONE);
        if (spnVoucherType.getSelectedItem().toString().equals("Sales")) {
            btnSave.setVisibility(View.GONE);
            btnViewPdf.setVisibility(View.VISIBLE);
        } else {
             btnSave.setVisibility(View.GONE);
             btnViewPdf.setVisibility(View.GONE); // Or create Pdf for Purchase too later
        }
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("View Voucher");
        }
    }

    private void showAddChargeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Charge / Tax");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        
        final Spinner spnLedger = new Spinner(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        spnLedger.setLayoutParams(params);
        
        Button btnNew = new Button(this);
        btnNew.setText("New");
        
        row.addView(spnLedger);
        row.addView(btnNew);
        layout.addView(row);
        
        final List<String> ledgers = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ledgers);
        spnLedger.setAdapter(adapter);
        
        refreshChargeLedgers(ledgers, adapter);
        
        btnNew.setOnClickListener(v -> showCreateTaxDialog(() -> refreshChargeLedgers(ledgers, adapter)));
        
        final EditText etValue = new EditText(this);
        etValue.setHint("Amount or Rate (%)");
        etValue.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etValue);
        
        final CheckBox cbIsPercentage = new CheckBox(this);
        cbIsPercentage.setText("Is Percentage (%)");
        layout.addView(cbIsPercentage);
        
        spnLedger.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedName = ledgers.get(position);
                Cursor c = databaseHelper.getLedgerDetails(selectedName);
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
            public void onNothingSelected(AdapterView<?> parent) {}
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
                
                // Ledger ID
                int ledgerId = 0; 
                Cursor c = databaseHelper.getLedgerDetails(ledgerName);
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

    private void refreshChargeLedgers(List<String> list, ArrayAdapter<String> adapter) {
        list.clear();
        list.addAll(databaseHelper.getLedgersByGroupList("Duties & Taxes"));
        list.addAll(databaseHelper.getLedgersByGroupList("Indirect Expenses"));
        list.addAll(databaseHelper.getLedgersByGroupList("Direct Expenses"));
        list.addAll(databaseHelper.getLedgersByGroupList("Indirect Incomes"));
        adapter.notifyDataSetChanged();
    }

    private void showCreateTaxDialog(Runnable onSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Tax Ledger");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        final EditText etName = new EditText(this);
        etName.setHint("Ledger Name (e.g., GST 18%)");
        layout.addView(etName);
        
        final EditText etRate = new EditText(this);
        etRate.setHint("Tax Rate (%)");
        etRate.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etRate);
        
        final CheckBox cbIsPercentage = new CheckBox(this);
        cbIsPercentage.setText("Is Percentage");
        cbIsPercentage.setChecked(true);
        layout.addView(cbIsPercentage);
        
        builder.setView(layout);
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString();
            String rateStr = etRate.getText().toString();
            
            if (!name.isEmpty()) {
                double rate = 0;
                if (!rateStr.isEmpty()) rate = Double.parseDouble(rateStr);
                
                // Defaults: Group="Duties & Taxes", type="Credit"
                databaseHelper.addLedger(name, "Duties & Taxes", "", "", "", "", 0, "Credit", rate, cbIsPercentage.isChecked());
                
                Toast.makeText(this, "Tax Ledger Created", Toast.LENGTH_SHORT).show();
                if (onSuccess != null) onSuccess.run();
            } else {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addItem() {
        String name = actvItemName.getText().toString();
        String qtyStr = etQuantity.getText().toString();
        String rateStr = etRate.getText().toString();

        if (name.isEmpty() || qtyStr.isEmpty() || rateStr.isEmpty()) {
            Toast.makeText(this, "Please fill item details", Toast.LENGTH_SHORT).show();
            return;
        }

        double qty = Double.parseDouble(qtyStr);
        double rate = Double.parseDouble(rateStr);
        
        String gstStr = etGstRate.getText().toString();
        double gstRate = gstStr.isEmpty() ? 0 : Double.parseDouble(gstStr);
        
        // Calculate Amounts
        double amount = qty * rate; // Taxable Vaue
        
        // Tax Calculation (Item level)
        double taxAmount = amount * (gstRate / 100);
        double cgst = taxAmount / 2;
        double sgst = taxAmount / 2;
        
        String unit = etUnit.getText().toString();
        String hsn = etHsn.getText().toString();

        itemList.add(new InvoiceItem(name, qty, rate, amount, gstRate, cgst, sgst, unit, hsn));
        adapter.notifyDataSetChanged();
        
        updateTotals();
        
        actvItemName.setText("");
        etQuantity.setText("");
        etRate.setText("");
        etUnit.setText("");
        etHsn.setText("");
        etGstRate.setText("");
        actvItemName.requestFocus();
    }

    private void updateTotals() {
        subtotalAmount = 0;
        double itemTaxTotal = 0;
        
        for (InvoiceItem item : itemList) {
            subtotalAmount += item.getAmount(); 
            itemTaxTotal += (item.getCgstAmount() + item.getSgstAmount());
        }
        
        // Recalculate Charges
        totalChargesAmount = 0;
        for (VoucherCharge charge : chargesList) {
            if (charge.isPercentage) {
                charge.amount = subtotalAmount * (charge.rate / 100);
            }
            totalChargesAmount += charge.amount;
        }
        if (chargesAdapter != null) chargesAdapter.notifyDataSetChanged();
        
        totalAmount = subtotalAmount + itemTaxTotal + totalChargesAmount;
        tvTotalAmount.setText(String.format("Total: ₹%.2f", totalAmount));
    }

    private Invoice createInvoiceObject(String voucherNo, String date, String partyName, double itemTaxTotal) {
         Invoice inv = new Invoice(voucherNo, date, partyName, itemList, subtotalAmount, totalChargesAmount, itemTaxTotal, totalAmount);
         
         inv.setDispatchDetails(
                etDeliveryNote.getText().toString(),
                etModePayment.getText().toString(),
                etRefNo.getText().toString(),
                etOtherRef.getText().toString(),
                etBuyerOrderNo.getText().toString(),
                etDispatchDocNo.getText().toString(),
                etDeliveryNoteDate.getText().toString(),
                etDispatchThrough.getText().toString(),
                etDestination.getText().toString(),
                etTermsDelivery.getText().toString(),
                etBillOfLading.getText().toString(),
                etMotorVehicleNo.getText().toString()
            );
            
         // Fetch Party Details again to ensure they are populated in the object
         // (Or rely on what's visible in TextViews, but better to get from DB/Cursor if needed)
         // For now, simple implementation assuming user hasn't changed them manually if they weren't editable
         
         return inv;
    }

    private void viewPdf() {
         String voucherNo = etVoucherNo.getText().toString();
         String date = etDate.getText().toString();
         String partyName = actvPartyName.getText().toString();
         
         double itemTaxTotal = 0;
         for (InvoiceItem item : itemList) {
            itemTaxTotal += (item.getCgstAmount() + item.getSgstAmount());
         }
         
         Invoice inv = createInvoiceObject(voucherNo, date, partyName, itemTaxTotal);
         
         PdfGenerator pdfGenerator = new PdfGenerator(this);
         pdfGenerator.generateAndOpenPdf(inv);
    }

    private void removeCharge(int position) {
        chargesList.remove(position);
        chargesAdapter.notifyItemRemoved(position);
        updateTotals();
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
