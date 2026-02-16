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
    private EditText etPaymentAmount; // New Field for Payment Amount
    
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
    private static final String KEY_LAST_PAYMENT_ACCOUNT = "last_payment_account";

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
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
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

        // Payment Amount Field & Add Button Initialization
        LinearLayout llPaymentRow = new LinearLayout(this);
        llPaymentRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, 16, 0, 16);
        llPaymentRow.setLayoutParams(rowParams);

        etPaymentAmount = new EditText(this);
        etPaymentAmount.setHint("Amount â‚¹");
        etPaymentAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                0, 
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        etPaymentAmount.setLayoutParams(etParams);
        
        Button btnAddLine = new Button(this);
        btnAddLine.setText("+");
        btnAddLine.setOnClickListener(v -> addPaymentLine());
        
        llPaymentRow.addView(etPaymentAmount);
        llPaymentRow.addView(btnAddLine);
        
        llPaymentRow.setVisibility(View.GONE); // Initially GONE
        
        // Store reference to this wrapper layout to toggle visibility instead of etPaymentAmount directly if needed
        // For now, I'll tag it or just keep reference if I made it a field, but for simplicity:
        etPaymentAmount.setTag(llPaymentRow); // Hack to access parent layout for visibility toggling
        
        if(llPartyInfo != null) llPartyInfo.addView(llPaymentRow); // Adding to Party Info container
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
        String[] voucherTypes = {"Sales", "Purchase", "Receipt", "Payment", "Journal", "Contra"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, voucherTypes);
        spnVoucherType.setAdapter(adapter);

        spnVoucherType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = voucherTypes[position];
                
                // Reset common visibility
                llInventorySection.setVisibility(View.GONE);
                findViewById(R.id.tilPartyName).setVisibility(View.VISIBLE);
                findViewById(R.id.tilBankLedger).setVisibility(View.GONE);
                llPartyInfo.setVisibility(View.GONE);
                if(etPaymentAmount.getTag() instanceof View) ((View)etPaymentAmount.getTag()).setVisibility(View.GONE);

                if (type.equals("Sales") || type.equals("Purchase")) {
                    llInventorySection.setVisibility(View.VISIBLE);
                    ((com.google.android.material.textfield.TextInputLayout)findViewById(R.id.tilPartyName)).setHint("Party Name");
                     if(!actvPartyName.getText().toString().isEmpty()) llPartyInfo.setVisibility(View.VISIBLE);
                } else if (type.equals("Payment") || type.equals("Receipt")) {
                    String hint = type.equals("Payment") ? "Account (Paid To)" : "Account (Received From)";
                    ((com.google.android.material.textfield.TextInputLayout)findViewById(R.id.tilPartyName)).setHint(hint);
                    findViewById(R.id.tilBankLedger).setVisibility(View.VISIBLE);
                    ((com.google.android.material.textfield.TextInputLayout)findViewById(R.id.tilBankLedger)).setHint("Through (Bank/Cash)");
                    
                    if(etPaymentAmount.getTag() instanceof View) ((View)etPaymentAmount.getTag()).setVisibility(View.VISIBLE);
                    llPartyInfo.setVisibility(View.VISIBLE);
                } else if (type.equals("Journal") || type.equals("Contra")) {
                    findViewById(R.id.tilPartyName).setVisibility(View.GONE);
                    llPartyInfo.setVisibility(View.VISIBLE);
                    btnAddCharge.setText("Add Entry (Dr/Cr)");
                }

                if (!type.equals("Journal") && !type.equals("Contra")) {
                    btnAddCharge.setText("Add Charge / Tax");
                }
                
                // Auto-fill Logic
                if (etVoucherNo.getText().toString().isEmpty() || etVoucherNo.getTag() == null) {
                    long nextNum = databaseHelper.getNextVoucherNumber(type, selectedCompanyId);
                    etVoucherNo.setText(String.valueOf(nextNum));
                    etVoucherNo.setTag("AUTO");
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
    }

    private void updatePartyAutocomplete(String type) {
        List<String> partyNames = new ArrayList<>();
        if ("Sales".equals(type) || "Receipt".equals(type)) {
            partyNames.addAll(databaseHelper.getLedgersByGroupList("Sundry Debtors"));
            partyNames.add("Cash");
        } else if ("Purchase".equals(type)) {
            partyNames.addAll(databaseHelper.getLedgersByGroupList("Sundry Creditors"));
            partyNames.add("Cash");
        } else if ("Payment".equals(type)) {
            // For Payment, "Party" is not used in the main header in this design (it's in Particulars/Charges)
            // But "Through" (Bank Ledger) needs to be populated
             List<String> bankLedgers = databaseHelper.getLedgersByGroupList("Bank Accounts");
             bankLedgers.add("Cash");
             ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bankLedgers);
             actvBankLedger.setAdapter(bankAdapter);
             return;
        } else {
             // Fallback to all if unknown type
             partyNames = databaseHelper.getAllLedgerNames(); 
        }
        
        ArrayAdapter<String> partyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, partyNames);
        actvPartyName.setAdapter(partyAdapter);
        
        // Setup Bank Ledger Adapter for Invoice Settings (Optional for Sales)
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
            if (type.equals("Sales") || type.equals("Purchase")) {
                etVoucherNo.setText(cursor.getString(cursor.getColumnIndexOrThrow(type.equals("Sales") ? "invoice_number" : "purchase_number")));
                etDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                actvPartyName.setText(cursor.getString(cursor.getColumnIndexOrThrow(type.equals("Sales") ? "customer_name" : "party_name")));
                totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                
                loadVoucherItems(id, type);
                loadVoucherCharges(id, type);
            } else if (type.equals("Payment") || type.equals("Receipt")) {
                String vNo = cursor.getString(cursor.getColumnIndexOrThrow(type.equals("Payment") ? "voucher_no" : "receipt_no"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(type.equals("Payment") ? "date" : "receipt_date"));
                String narration = cursor.getString(cursor.getColumnIndexOrThrow(type.equals("Payment") ? "narration" : "receipt_narration"));
                
                etVoucherNo.setText(vNo);
                etDate.setText(date);
                etDeliveryNote.setText(narration);
                
                if (type.equals("Payment")) {
                    actvPartyName.setText(cursor.getString(cursor.getColumnIndexOrThrow("party_name")));
                    actvBankLedger.setText(cursor.getString(cursor.getColumnIndexOrThrow("through_ledger")));
                }
                loadVoucherCharges(id, type);
            } else if (type.equals("Journal") || type.equals("Contra")) {
                etVoucherNo.setText(cursor.getString(cursor.getColumnIndexOrThrow("journal_no")));
                etDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("journal_date")));
                etDeliveryNote.setText(cursor.getString(cursor.getColumnIndexOrThrow("journal_narration")));
                loadVoucherCharges(id, type);
            }
            cursor.close();
            disableEditing();
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
                chargesList.add(new VoucherCharge(ledgerId, ledgerName, amount, isPercentage, rate, cursor.getInt(cursor.getColumnIndexOrThrow("is_debit")) == 1));
            }
            cursor.close();
            chargesAdapter.notifyDataSetChanged();
            updateTotals();
        }
    }

    private void addItem() {
        String name = actvItemName.getText().toString();
        String qtyStr = etQuantity.getText().toString();
        String rateStr = etRate.getText().toString();
        String gstStr = etGstRate.getText().toString();
        String unit = etUnit.getText().toString();
        String hsn = etHsn.getText().toString();

        if (name.isEmpty() || qtyStr.isEmpty() || rateStr.isEmpty()) {
            Toast.makeText(this, "Please fill Item, Qty and Rate", Toast.LENGTH_SHORT).show();
            return;
        }

        double qty = Double.parseDouble(qtyStr);
        double rate = Double.parseDouble(rateStr);
        double amount = qty * rate;
        double gstRate = gstStr.isEmpty() ? 0 : Double.parseDouble(gstStr);
        
        double cgst = (amount * (gstRate/2)) / 100;
        double sgst = (amount * (gstRate/2)) / 100;

        itemList.add(new InvoiceItem(name, qty, rate, amount, gstRate, cgst, sgst, unit, hsn));
        adapter.notifyItemInserted(itemList.size() - 1);
        
        actvItemName.setText("");
        etQuantity.setText("");
        etRate.setText("");
        etGstRate.setText("");
        etUnit.setText("");
        etHsn.setText("");
        
        updateTotals();
    }

    private void showAddChargeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Ledger Entry");
        View view = getLayoutInflater().inflate(R.layout.dialog_add_charge, null);
        builder.setView(view);

        AutoCompleteTextView actvChargeName = view.findViewById(R.id.actvChargeLedger);
        EditText etChargeAmount = view.findViewById(R.id.etChargeAmount);
        CheckBox cbIsPercentage = view.findViewById(R.id.cbIsPercentage);
        Spinner spnSide = new Spinner(this);
        
        String type = spnVoucherType.getSelectedItem().toString();
        List<String> ledgers = databaseHelper.getAllLedgerNames();
        ArrayAdapter<String> ledgerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ledgers);
        actvChargeName.setAdapter(ledgerAdapter);

        if (type.equals("Journal") || type.equals("Contra")) {
             String[] sides = {"By (Dr)", "To (Cr)"};
             ArrayAdapter<String> sideAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sides);
             spnSide.setAdapter(sideAdapter);
             ((LinearLayout)view).addView(spnSide, 0);
             cbIsPercentage.setVisibility(View.GONE);
        }

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = actvChargeName.getText().toString();
            String amtStr = etChargeAmount.getText().toString();
            if (!name.isEmpty() && !amtStr.isEmpty()) {
                double amount = Double.parseDouble(amtStr);
                boolean isPercentage = cbIsPercentage.isChecked();
                boolean isDebit = true;
                if (type.equals("Journal") || type.equals("Contra")) {
                    isDebit = spnSide.getSelectedItemPosition() == 0;
                }
                chargesList.add(new VoucherCharge(0, name, amount, isPercentage, isPercentage ? amount : 0, isDebit));
                chargesAdapter.notifyItemInserted(chargesList.size() - 1);
                updateTotals();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveVoucher() {
        String type = spnVoucherType.getSelectedItem().toString();
        String voucherNo = etVoucherNo.getText().toString();
        String date = etDate.getText().toString();
        String partyName = actvPartyName.getText().toString();
        String narration = etDeliveryNote.getText().toString();

        if (voucherNo.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Number and Date are required", Toast.LENGTH_SHORT).show();
            return;
        }

        long voucherId = -1;
        if (type.equals("Sales") || type.equals("Purchase")) {
            if (partyName.isEmpty()) {
                Toast.makeText(this, "Party Name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            voucherId = databaseHelper.addVoucher(voucherNo, date, partyName, totalAmount, type, selectedCompanyId);
            for (InvoiceItem item : itemList) {
                databaseHelper.addVoucherItem(voucherId, type, item.getItemName(), item.getQuantity(), item.getRate(), item.getAmount(), item.getGstRate(), item.getCgstAmount(), item.getSgstAmount(), item.getUnit(), item.getHsn());
            }
        } else if (type.equals("Payment") || type.equals("Receipt")) {
             String throughLedger = actvBankLedger.getText().toString();
             if (type.equals("Payment")) {
                 voucherId = databaseHelper.addPayment(voucherNo, date, partyName, totalAmount, throughLedger, narration, selectedCompanyId);
             } else {
                 voucherId = databaseHelper.addReceipt(voucherNo, date, narration, selectedCompanyId);
             }
        } else if (type.equals("Journal") || type.equals("Contra")) {
            voucherId = databaseHelper.addJournal(voucherNo, date, narration, selectedCompanyId);
        }

        if (voucherId != -1) {
            for (VoucherCharge charge : chargesList) {
                databaseHelper.addVoucherCharge(voucherId, type, charge.ledgerId, charge.ledgerName, charge.amount, charge.isPercentage, charge.isDebit);
            }
            Toast.makeText(this, type + " Saved Successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save " + type, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotals() {
        subtotalAmount = 0;
        double totalGst = 0;
        for (InvoiceItem item : itemList) {
            subtotalAmount += item.getAmount();
            totalGst += (item.getCgstAmount() + item.getSgstAmount());
        }
        totalChargesAmount = 0;
        String type = spnVoucherType.getSelectedItem().toString();
        if (type.equals("Journal") || type.equals("Contra")) {
             totalAmount = 0;
             for (VoucherCharge c : chargesList) if (c.isDebit) totalAmount += c.amount;
        } else {
            for (VoucherCharge charge : chargesList) {
                if (charge.isPercentage) charge.amount = (subtotalAmount * charge.rate) / 100;
                totalChargesAmount += charge.amount;
            }
            totalAmount = subtotalAmount + totalGst + totalChargesAmount;
        }
        tvTotalAmount.setText(String.format("Total: ₹%.2f", totalAmount));
        if (chargesAdapter != null) chargesAdapter.notifyDataSetChanged();
    }

    private void addPaymentLine() {
        String amountStr = etPaymentAmount.getText().toString();
        String partyName = actvPartyName.getText().toString();
        String type = spnVoucherType.getSelectedItem().toString();
        if (amountStr.isEmpty() || partyName.isEmpty()) {
            Toast.makeText(this, "Enter Party and Amount", Toast.LENGTH_SHORT).show();
            return;
        }
        double amt = Double.parseDouble(amountStr);
        boolean isDr = type.equals("Payment");
        chargesList.add(new VoucherCharge(0, partyName, amt, false, 0, isDr));
        chargesAdapter.notifyDataSetChanged();
        etPaymentAmount.setText("");
        updateTotals();
    }

    private void viewPdf() {
        String type = spnVoucherType.getSelectedItem().toString();
        String voucherNo = etVoucherNo.getText().toString();
        if (voucherNo.isEmpty()) {
            Toast.makeText(this, "No Voucher Loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (type.equals("Sales") || type.equals("Purchase")) {
            // Simplify Sales/Purchase export for now or call generator
            // (Previous logic for Sales was complex, but let's re-implement simply for Excel)
             Cursor c = databaseHelper.getReadableDatabase().query(type.equals("Sales") ? "invoices" : "purchases", null, 
                    (type.equals("Sales") ? "invoice_number" : "purchase_number") + "=? AND company_id=?", 
                    new String[]{voucherNo, String.valueOf(selectedCompanyId)}, null, null, null);
             
             if (c != null && c.moveToFirst()) {
                 int id = c.getInt(c.getColumnIndexOrThrow("_id"));
                 String date = c.getString(c.getColumnIndexOrThrow("date"));
                 String pName = c.getString(c.getColumnIndexOrThrow(type.equals("Sales") ? "customer_name" : "party_name"));
                 double total = c.getDouble(c.getColumnIndexOrThrow("total_amount"));
                 c.close();
                 
                 List<InvoiceItem> itList = new ArrayList<>();
                 Cursor it = databaseHelper.getVoucherItems(id, type);
                 if (it != null) {
                      while(it.moveToNext()) {
                          itList.add(new InvoiceItem(it.getString(it.getColumnIndexOrThrow("item_name")), it.getDouble(it.getColumnIndexOrThrow("quantity")), it.getDouble(it.getColumnIndexOrThrow("rate")), it.getDouble(it.getColumnIndexOrThrow("amount")), it.getDouble(it.getColumnIndexOrThrow("gst_rate")), it.getDouble(it.getColumnIndexOrThrow("cgst_amount")), it.getDouble(it.getColumnIndexOrThrow("sgst_amount")), it.getString(it.getColumnIndexOrThrow("unit")), it.getString(it.getColumnIndexOrThrow("hsn"))));
                      }
                      it.close();
                 }
                 List<InvoiceCharge> chList = new ArrayList<>();
                 Cursor ch = databaseHelper.getVoucherCharges(id, type);
                 if (ch != null) {
                      while(ch.moveToNext()) {
                          chList.add(new InvoiceCharge(ch.getString(ch.getColumnIndexOrThrow("ledger_name")), ch.getDouble(ch.getColumnIndexOrThrow("amount")), ch.getDouble(ch.getColumnIndexOrThrow("rate")), ch.getInt(ch.getColumnIndexOrThrow("is_percentage")) == 1));
                      }
                      ch.close();
                 }
                 Invoice inv = new Invoice(voucherNo, date, pName, itList, total, 0, 0, total);
                 inv.setExtraCharges(chList);
                 inv.setBuyersOrderNo(type.toUpperCase());
                 new ExcelGenerator(this).generateAndOpenExcel(inv);
             }
        } else if (type.equals("Payment") || type.equals("Receipt")) {
             String tbl = type.equals("Payment") ? "payments" : "receipts";
             String vNoCol = type.equals("Payment") ? "voucher_no" : "receipt_no";
             Cursor c = databaseHelper.getReadableDatabase().query(tbl, null, vNoCol + "=? AND company_id=?", new String[]{voucherNo, String.valueOf(selectedCompanyId)}, null, null, null);
             if (c != null && c.moveToFirst()) {
                 int id = c.getInt(c.getColumnIndexOrThrow("_id"));
                 String date = c.getString(c.getColumnIndexOrThrow(type.equals("Payment") ? "date" : "receipt_date"));
                 double total = type.equals("Payment") ? c.getDouble(c.getColumnIndexOrThrow("total_amount")) : 0;
                 String narration = c.getString(c.getColumnIndexOrThrow(type.equals("Payment") ? "narration" : "receipt_narration"));
                 String through = type.equals("Payment") ? c.getString(c.getColumnIndexOrThrow("through_ledger")) : "";
                 c.close();
                 
                 List<InvoiceCharge> chList = new ArrayList<>();
                 Cursor ch = databaseHelper.getVoucherCharges(id, type);
                 if (ch != null) {
                      while(ch.moveToNext()) {
                          String name = ch.getString(ch.getColumnIndexOrThrow("ledger_name"));
                          double amt = ch.getDouble(ch.getColumnIndexOrThrow("amount"));
                          boolean isDr = ch.getInt(ch.getColumnIndexOrThrow("is_debit")) == 1;
                          chList.add(new InvoiceCharge((isDr ? "By " : "To ") + name, amt, 0, false));
                          if (type.equals("Receipt")) total += amt;
                      }
                      ch.close();
                 }
                 Invoice inv = new Invoice(voucherNo, date, type.equals("Payment") ? "Payment Voucher" : "Receipt Voucher", new ArrayList<>(), total, 0, 0, total);
                 inv.setBuyersOrderNo(type.toUpperCase());
                 inv.setDispatchThrough(through);
                 inv.setDeliveryNote(narration);
                 inv.setExtraCharges(chList);
                 new ExcelGenerator(this).generateAndOpenExcel(inv);
             }
        } else if (type.equals("Journal") || type.equals("Contra")) {
             Cursor c = databaseHelper.getReadableDatabase().query("journals", null, "journal_no=? AND company_id=?", new String[]{voucherNo, String.valueOf(selectedCompanyId)}, null, null, null);
             if (c != null && c.moveToFirst()) {
                 int id = c.getInt(c.getColumnIndexOrThrow("_id"));
                 String date = c.getString(c.getColumnIndexOrThrow("journal_date"));
                 String narration = c.getString(c.getColumnIndexOrThrow("journal_narration"));
                 c.close();
                 
                 List<InvoiceCharge> chList = new ArrayList<>();
                 Cursor ch = databaseHelper.getVoucherCharges(id, type);
                 if (ch != null) {
                      while(ch.moveToNext()) {
                          String name = ch.getString(ch.getColumnIndexOrThrow("ledger_name"));
                          double amt = ch.getDouble(ch.getColumnIndexOrThrow("amount"));
                          boolean isDr = ch.getInt(ch.getColumnIndexOrThrow("is_debit")) == 1;
                          chList.add(new InvoiceCharge((isDr ? "By " : "To ") + name, amt, 0, false));
                      }
                      ch.close();
                 }
                 Invoice inv = new Invoice(voucherNo, date, type + " Voucher", new ArrayList<>(), 0, 0, 0, 0);
                 inv.setBuyersOrderNo(type.toUpperCase());
                 inv.setDeliveryNote(narration);
                 inv.setExtraCharges(chList);
                 new ExcelGenerator(this).generateAndOpenExcel(inv);
             }
        } else {
            Toast.makeText(this, "Excel generation available for all types.", Toast.LENGTH_SHORT).show();
        }
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
        
        String type = spnVoucherType.getSelectedItem().toString();
        if (type.equals("Sales")) {
            btnSave.setVisibility(View.GONE);
            btnViewPdf.setVisibility(View.VISIBLE);
        } else {
             Toast.makeText(this, "Excel generation available for Sales, Purchase, Payment, Receipt, Journal and Contra", Toast.LENGTH_SHORT).show();
        }
    }
    }

    private Invoice createInvoiceObject(String voucherNo, String date, String partyName, double itemTaxTotal) {
         Invoice inv = new Invoice(voucherNo, date, partyName, itemList, subtotalAmount, totalChargesAmount, itemTaxTotal, totalAmount);
         
         inv.setDispatchDetails(
                etDeliveryNote.getText().toString(),
                etModePayment.getText().toString(),
                etRefNo.getText().toString(),
                etOtherRef.getText().toString(),
                etBuyerOrderNo.getText().toString(),
                "", // buyersOrderDate placeholder
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

    private void removeCharge(int position) {
        chargesList.remove(position);
        chargesAdapter.notifyItemRemoved(position);
        updateTotals();
    }

    // Inner Classes
    

    
    private class ChargesAdapter extends RecyclerView.Adapter<ChargesAdapter.ChargeViewHolder> {
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
            String prefix = "";
            String type = spnVoucherType.getSelectedItem().toString();
            if (type.equals("Journal") || type.equals("Contra")) {
                prefix = charge.isDebit ? "By " : "To ";
            }
            holder.tvName.setText(prefix + charge.ledgerName);
            if (charge.isPercentage) {
                holder.tvValue.setText("@ " + charge.rate + "%");
            } else {
                holder.tvValue.setText("(Fixed)");
            }
            holder.tvAmount.setText(String.format("â‚¹%.2f", charge.amount));
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

