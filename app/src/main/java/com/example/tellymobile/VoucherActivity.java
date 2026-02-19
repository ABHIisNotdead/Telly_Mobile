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
    private Spinner spnPaymentSide; // New Field for Journal By/To selection
    private Spinner spnPaymentMode; // New Field for Payment Mode selection
    
    // Purchase Specific Fields
    private LinearLayout llPurchaseDetails;
    private TextInputEditText etSupplierInvNo, etSupplierTin, etSupplierCst, etBuyerVatTin;


    
    private LinearLayout llPartyInfo, llInventorySection;
    private Button btnAddItem, btnSave, btnAddCharge, btnViewPdf, btnExcelShare;
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
    private boolean isEdit = false;

    private boolean isRoundOffRefined(String name) {
        if (name == null) return false;
        String n = name.toLowerCase().replace(" ", "").replace("-", "").replace("_", "").trim();
        return n.contains("roundoff") || (n.contains("round") && n.contains("off"));
    }

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
            isEdit = "EDIT".equals(mode);
            if ("VIEW".equals(mode) || isEdit) {
                int id = getIntent().getIntExtra("ID", -1);
                String type = getIntent().getStringExtra("TYPE");
                if (id != -1 && type != null) {
                    loadVoucherData(id, type, "VIEW".equals(mode));
                    
                    if ("EDIT".equals(mode)) {
                        if (getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("Update " + type);
                        }
                        btnSave.setText("Update " + type);
                    }
                } else {
                     Toast.makeText(this, "Invalid Voucher Data", Toast.LENGTH_SHORT).show();
                }
            } else {
                // CREATE MODE - Check if TYPE was passed
                String initialType = getIntent().getStringExtra("TYPE");
                if (initialType != null) {
                    android.widget.Adapter adapter = spnVoucherType.getAdapter();
                    for (int i = 0; i < adapter.getCount(); i++) {
                        if (adapter.getItem(i).toString().equals(initialType)) {
                            spnVoucherType.setSelection(i);
                            break;
                        }
                    }
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
        etVoucherNo.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etVoucherNo.hasFocus()) {
                    etVoucherNo.setTag(null);
                }
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
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
        btnExcelShare = findViewById(R.id.btnExcelShare);

        // Purchase Details Section (Programmatically added or finding if exists - assuming programmatic for now as layout XML not editable easily)
        llPurchaseDetails = new LinearLayout(this);
        llPurchaseDetails.setOrientation(LinearLayout.VERTICAL);
        llPurchaseDetails.setVisibility(View.GONE);
        
        
        com.google.android.material.textfield.TextInputLayout tilSupplierInvNo = createTextInput("Supplier Invoice No");
        etSupplierInvNo = getEditText(tilSupplierInvNo);
        llPurchaseDetails.addView(tilSupplierInvNo);

        com.google.android.material.textfield.TextInputLayout tilSupplierTin = createTextInput("Company's TIN/Sales Tax No.");
        etSupplierTin = getEditText(tilSupplierTin);
        llPurchaseDetails.addView(tilSupplierTin);
        
        com.google.android.material.textfield.TextInputLayout tilBuyerVatTin = createTextInput("Buyer's VAT TIN");
        etBuyerVatTin = getEditText(tilBuyerVatTin);
        llPurchaseDetails.addView(tilBuyerVatTin);

        com.google.android.material.textfield.TextInputLayout tilSupplierCst = createTextInput("Company's CST No.");
        etSupplierCst = getEditText(tilSupplierCst);
        llPurchaseDetails.addView(tilSupplierCst);
        
        // Add to main layout - need to find a good spot. 
        // llPartyInfo is a good reference. 
        if(llPartyInfo != null && llPartyInfo.getParent() instanceof LinearLayout) {
            LinearLayout parent = (LinearLayout) llPartyInfo.getParent();
            int index = parent.indexOfChild(llPartyInfo);
            parent.addView(llPurchaseDetails, index + 1);
        }

        // Journal Entry Logic Container
        LinearLayout llJournalEntryContainer = new LinearLayout(this);
        llJournalEntryContainer.setOrientation(LinearLayout.VERTICAL);
        llJournalEntryContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT));
        
        // Row 1: Payment Mode
        LinearLayout llModeRow = new LinearLayout(this);
        llModeRow.setOrientation(LinearLayout.HORIZONTAL);
        llModeRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        TextView tvModeLabel = new TextView(this);
        tvModeLabel.setText("Payment Mode: ");
        tvModeLabel.setPadding(8, 0, 8, 0);
        
        spnPaymentMode = new Spinner(this);
        String[] modes = {"None", "Cash", "Bank", "Cheque", "E-Fund", "Online"};
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, modes);
        spnPaymentMode.setAdapter(modeAdapter);
        
        llModeRow.addView(tvModeLabel);
        llModeRow.addView(spnPaymentMode);
        
        // Row 2: Entry (By/To + Ledger + Amount + Add)
        LinearLayout llEntryRow = new LinearLayout(this);
        llEntryRow.setOrientation(LinearLayout.HORIZONTAL);
        llEntryRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        llEntryRow.setPadding(0, 8, 0, 8);

        spnPaymentSide = new Spinner(this);
        String currentType = spnVoucherType.getSelectedItem() != null ? spnVoucherType.getSelectedItem().toString() : "";
        String[] sides = currentType.equals("Contra") ? new String[]{"(Dr)", "(Cr)"} : new String[]{"By", "To"};
        final ArrayAdapter<String> sideAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sides);
        spnPaymentSide.setAdapter(sideAdapter);
        
        // Re-use actvPartyName by moving it into Journals' entry row when needed? 
        // No, easier to just hide tilPartyName and use a dedicated one here, OR just keep it simple.
        // User said "2 input... clean it". I'll move Ledger Name here.
        
        com.google.android.material.textfield.TextInputLayout tilAmount = new com.google.android.material.textfield.TextInputLayout(this, null, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox);
        tilAmount.setHint("Amount");
        LinearLayout.LayoutParams tilParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        tilParams.setMargins(8, 0, 8, 0);
        tilAmount.setLayoutParams(tilParams);
        
        etPaymentAmount = new EditText(tilAmount.getContext());
        etPaymentAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tilAmount.addView(etPaymentAmount);

        Button btnAddLine = new Button(this);
        btnAddLine.setText("+");
        btnAddLine.setOnClickListener(v -> addPaymentLine());
        
        llEntryRow.addView(spnPaymentSide);
        llEntryRow.addView(tilAmount);
        llEntryRow.addView(btnAddLine);

        llJournalEntryContainer.addView(llModeRow);
        llJournalEntryContainer.addView(llEntryRow);
        
        llJournalEntryContainer.setVisibility(View.GONE);
        etPaymentAmount.setTag(llJournalEntryContainer); 

        if(llPartyInfo != null) llPartyInfo.addView(llJournalEntryContainer);

        spnPaymentSide.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = spnVoucherType.getSelectedItem().toString();
                if (type.equals("Journal") || type.equals("Contra")) {
                    double totalDr = 0, totalCr = 0;
                    for (VoucherCharge c : chargesList) {
                        if (c.isDebit) totalDr += c.amount;
                        else totalCr += c.amount;
                    }
                    if (position == 0) { // By
                        if (totalCr > totalDr) etPaymentAmount.setText(String.format("%.2f", totalCr - totalDr));
                    } else { // To
                        if (totalDr > totalCr) etPaymentAmount.setText(String.format("%.2f", totalDr - totalCr));
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

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
                    try {
                        ivLogo.setVisibility(View.VISIBLE);
                        ivLogo.setImageURI(android.net.Uri.parse(logoUri));
                    } catch (Exception e) {
                        e.printStackTrace();
                        ivLogo.setVisibility(View.GONE); // Hide if permission/load fails
                    }
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
                llPurchaseDetails.setVisibility(View.GONE); // Ensure hidden by default
                if(etPaymentAmount.getTag() instanceof View) ((View)etPaymentAmount.getTag()).setVisibility(View.GONE);

                if (type.equals("Sales") || type.equals("Purchase")) {
                    llInventorySection.setVisibility(View.VISIBLE);
                    ((com.google.android.material.textfield.TextInputLayout)findViewById(R.id.tilPartyName)).setHint("Party Name");
                     if(!actvPartyName.getText().toString().isEmpty()) llPartyInfo.setVisibility(View.VISIBLE);
                     
                     if (type.equals("Purchase")) {
                         llPurchaseDetails.setVisibility(View.VISIBLE);
                     } else {
                         llPurchaseDetails.setVisibility(View.GONE);
                     }
                } else if (type.equals("Payment") || type.equals("Receipt")) {
                    String hint = type.equals("Payment") ? "Account (Paid To)" : "Account (Received From)";
                    ((com.google.android.material.textfield.TextInputLayout)findViewById(R.id.tilPartyName)).setHint(hint);
                    findViewById(R.id.tilBankLedger).setVisibility(View.VISIBLE);
                    ((com.google.android.material.textfield.TextInputLayout)findViewById(R.id.tilBankLedger)).setHint("Through (Bank/Cash)");
                    
                    if(etPaymentAmount != null && etPaymentAmount.getTag() instanceof View) ((View)etPaymentAmount.getTag()).setVisibility(View.VISIBLE);
                    llPartyInfo.setVisibility(View.VISIBLE);
                    
                     
                     if (type.equals("Receipt")) {
                          // Hide Dispatch by default (resetting what we did before)
                          if (llDispatchDetails != null) llDispatchDetails.setVisibility(View.GONE);

                          // Programmatically add Payment Mode ACTV if not exists
                          if (findViewById(R.id.tilReceiptMode) == null) {
                              com.google.android.material.textfield.TextInputLayout tilReceiptMode = new com.google.android.material.textfield.TextInputLayout(VoucherActivity.this, null, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox);
                              tilReceiptMode.setId(R.id.tilReceiptMode); // Need an ID to find it later
                              tilReceiptMode.setHint("Payment Mode");
                              
                              android.widget.AutoCompleteTextView actvMode = new android.widget.AutoCompleteTextView(tilReceiptMode.getContext());
                              actvMode.setId(R.id.actvReceiptMode); // Custom ID
                              actvMode.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
                              actvMode.setThreshold(0); // Show on focus
                              
                              String[] modes = {"Cash", "Cheque", "Online Transfer", "Other"};
                              ArrayAdapter<String> adapter = new ArrayAdapter<>(VoucherActivity.this, android.R.layout.simple_dropdown_item_1line, modes);
                              actvMode.setAdapter(adapter);
                              actvMode.setOnFocusChangeListener((v, hasFocus) -> {
                                  if (hasFocus) actvMode.showDropDown();
                              });
                              actvMode.setOnClickListener(v -> actvMode.showDropDown());

                              tilReceiptMode.addView(actvMode);
                              
                              // Add to layout below Bank Ledger. 
                              // Current structure: ... -> tilBankLedger -> llPartyInfo ...
                              // We want it between tilBankLedger and llPartyInfo
                              if (findViewById(R.id.tilBankLedger) != null && findViewById(R.id.tilBankLedger).getParent() instanceof LinearLayout) {
                                  LinearLayout receiptParent = (LinearLayout) findViewById(R.id.tilBankLedger).getParent();
                                  int idx = receiptParent.indexOfChild(findViewById(R.id.tilBankLedger));
                                  receiptParent.addView(tilReceiptMode, idx + 1);
                              }
                          } else {
                              findViewById(R.id.tilReceiptMode).setVisibility(View.VISIBLE);
                          }
                     } else {
                         if (findViewById(R.id.tilReceiptMode) != null) {
                             findViewById(R.id.tilReceiptMode).setVisibility(View.GONE);
                         }
                        if (type.equals("Sales") || type.equals("Purchase")) {
                            if (llDispatchDetails != null) {
                                // Reset dispatch visibility for Sales/Purchase
                                llDispatchDetails.setVisibility(View.GONE); // Default is gone, user toggles it.
                                // Restore children visibility
                                 for(int i=0; i<llDispatchDetails.getChildCount(); i++) {
                                     llDispatchDetails.getChildAt(i).setVisibility(View.VISIBLE);
                                 }
                            }
                        }
                     }
                } else if (type.equals("Journal") || type.equals("Contra")) {
                    findViewById(R.id.tilPartyName).setVisibility(View.VISIBLE);
                    ((com.google.android.material.textfield.TextInputLayout)findViewById(R.id.tilPartyName)).setHint("Ledger Name");
                    if (spnPaymentSide != null) spnPaymentSide.setVisibility(View.VISIBLE);
                    if (spnPaymentMode != null) spnPaymentMode.setVisibility(View.VISIBLE);

                    // Update Side labels dynamically
                    if (spnPaymentSide != null) {
                        String[] sideItems = type.equals("Contra") ? new String[]{"(Dr)", "(Cr)"} : new String[]{"By", "To"};
                        ArrayAdapter<String> sideAdapter = new ArrayAdapter<>(VoucherActivity.this, android.R.layout.simple_spinner_dropdown_item, sideItems);
                        spnPaymentSide.setAdapter(sideAdapter);
                        
                        // Auto-balancing logic
                        double tDr = 0, tCr = 0;
                        if (chargesList != null) {
                            for (VoucherCharge c : chargesList) {
                                if (c.isDebit) tDr += c.amount; else tCr += c.amount;
                            }
                        }
                        if (tDr > tCr) spnPaymentSide.setSelection(1); // Set to "To" if Debits > Credits
                        else spnPaymentSide.setSelection(0);           // Set to "By" if Credits >= Debits
                    }

                    if(etPaymentAmount != null && etPaymentAmount.getTag() instanceof View) ((View)etPaymentAmount.getTag()).setVisibility(View.VISIBLE);
                    llPartyInfo.setVisibility(View.VISIBLE);
                    
                    // The main Ledger Name input (tilPartyName) serves as the "Ledger" for each entry
                    ((com.google.android.material.textfield.TextInputLayout)findViewById(R.id.tilPartyName)).setHint("Ledger Name");
                    
                    if (btnAddCharge != null) btnAddCharge.setVisibility(View.GONE); // Hide original Add Charge button
                }

                if (!type.equals("Journal") && !type.equals("Contra")) {
                    if (spnPaymentSide != null) spnPaymentSide.setVisibility(View.GONE);
                    if (spnPaymentMode != null) spnPaymentMode.setVisibility(View.GONE);
                    if (btnAddCharge != null) {
                        btnAddCharge.setVisibility(View.VISIBLE);
                        btnAddCharge.setText("Add Charge / Tax");
                    }
                }

                
                // Clear inputs on switch
                actvPartyName.setText("");
                etPaymentAmount.setText("");


                
                com.google.android.material.textfield.TextInputLayout tilDeliveryNote = findViewById(R.id.tilDeliveryNote);
                if (tilDeliveryNote != null) {
                    if (type.equals("Sales") || type.equals("Purchase")) {
                         tilDeliveryNote.setHint("Delivery Note");
                    } else {
                         tilDeliveryNote.setHint("Narration");
                    }
                }
                
                // Auto-fill Logic
                if (!isEdit && (etVoucherNo.getText().toString().isEmpty() || "AUTO".equals(etVoucherNo.getTag()))) {
                    long nextNum = databaseHelper.getNextVoucherNumber(type, selectedCompanyId);
                    etVoucherNo.setText(String.valueOf(nextNum));
                    etVoucherNo.setTag("AUTO");
                }
                
                if (!isEdit) {
                    updatePartyAutocomplete(type);
                }
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
            partyNames.addAll(databaseHelper.getLedgersByGroupList("Sundry Debtors")); // Also allow Debtors
            partyNames.add("Cash");
        } else if ("Payment".equals(type)) {
            // For Payment, "Party" is not used in the main header in this design (it's in Particulars/Charges)
            // But "Through" (Bank Ledger) needs to be populated
             List<String> bankLedgers = databaseHelper.getLedgersByGroupList("Bank Accounts");
             bankLedgers.add("Cash");
             ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bankLedgers);
             actvBankLedger.setAdapter(bankAdapter);
             return;
        } else if ("Contra".equals(type)) {
             partyNames = databaseHelper.getContraLedgerNames();
        } else {
             // Fallback to all if unknown type
             partyNames = databaseHelper.getAllLedgerNames(); 
        }
        
        ArrayAdapter<String> partyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, partyNames);
        actvPartyName.setAdapter(partyAdapter);
        
        // Setup Bank Ledger Adapter generally (for Receipt/Sales settings)
        // Ensure it includes Cash-in-hand for Receipts
        List<String> bankLedgers = databaseHelper.getLedgersByGroupList("Bank Accounts");
        bankLedgers.addAll(databaseHelper.getLedgersByGroupList("Cash-in-hand")); 
        if (!bankLedgers.contains("Cash")) bankLedgers.add("Cash");
        
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
        btnSave.setOnClickListener(v -> saveVoucher(true));
        
        // Hidden feature to delete all journals (for testing/rebuild)
        etVoucherNo.setOnLongClickListener(v -> {
            String type = spnVoucherType.getSelectedItem().toString();
            if (type.equals("Journal") || type.equals("Contra")) {
                new AlertDialog.Builder(this)
                    .setTitle("Cleanup " + type + "s")
                    .setMessage("Delete all " + type + " vouchers and start over?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (type.equals("Journal")) databaseHelper.clearAllJournals(selectedCompanyId);
                        else databaseHelper.clearAllContras(selectedCompanyId);
                        Toast.makeText(this, type + "s Cleared", Toast.LENGTH_SHORT).show();
                        loadVoucherData(-1, type, false); // Reload to reset number and view
                    })
                    .setNegativeButton("No", null)
                    .show();
                return true;
            }
            return false;
        });
        btnViewPdf.setOnClickListener(v -> printAndSharePdf());
        btnExcelShare.setOnClickListener(v -> shareExcel());
        
        btnToggleDispatch.setOnClickListener(v -> {
            boolean isVisible = llDispatchDetails.getVisibility() == View.VISIBLE;
            llDispatchDetails.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            btnToggleDispatch.setText(isVisible ? "Show Dispatch Details" : "Hide Dispatch Details");
        });
    }

    private void loadVoucherData(int id, String type, boolean isViewMode) {
        // Set Spinner Selection to match loaded voucher type
        android.widget.Adapter adapter = spnVoucherType.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(type)) {
                spnVoucherType.setSelection(i);
                break;
            }
        }

        Cursor cursor = databaseHelper.getVoucher(id, type);
        if (cursor != null && cursor.moveToFirst()) {
            if (type.equals("Sales") || type.equals("Purchase")) {
                etVoucherNo.setText(cursor.getString(cursor.getColumnIndexOrThrow(type.equals("Sales") ? "invoice_number" : "purchase_number")));
                etDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("date")));
                actvPartyName.setText(cursor.getString(cursor.getColumnIndexOrThrow(type.equals("Sales") ? "customer_name" : "party_name")));
                totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                
                if (type.equals("Purchase")) {
                    int supInvIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SUPPLIER_INV_NO);
                    if (supInvIdx != -1) etSupplierInvNo.setText(cursor.getString(supInvIdx));
                    
                    int tinIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SUPPLIER_TIN);
                    if (tinIdx != -1) etSupplierTin.setText(cursor.getString(tinIdx));
                    
                    int cstIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_SUPPLIER_CST);
                    if (cstIdx != -1) etSupplierCst.setText(cursor.getString(cstIdx));
                    
                    int bVatIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_BUYER_VAT_TIN);
                    if (bVatIdx != -1) etBuyerVatTin.setText(cursor.getString(bVatIdx));
                }

                loadVoucherItems(id, type);
                loadVoucherCharges(id, type);
            } else if (type.equals("Payment") || type.equals("Receipt")) {
                String vNo = cursor.getString(cursor.getColumnIndexOrThrow(type.equals("Payment") ? "voucher_no" : "receipt_no"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String narration = cursor.getString(cursor.getColumnIndexOrThrow("narration"));
                
                etVoucherNo.setText(vNo);
                etDate.setText(date);
                etDeliveryNote.setText(narration);
                
                if (type.equals("Payment")) {
                    // Payment specific logic if any (e.g. party name from narration parsing if needed)
                } 
                // Load Through Ledger for BOTH Payment and Receipt
                int throughIdx = cursor.getColumnIndex("through_ledger");
                if (throughIdx != -1) {
                    actvBankLedger.setText(cursor.getString(throughIdx));
                }
                
                if (type.equals("Receipt")) {
                    int modeIdx = cursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_PAYMENT_MODE);
                    if (modeIdx != -1) {
                         String mode = cursor.getString(modeIdx);
                         android.widget.EditText actvMode = findViewById(R.id.actvReceiptMode);
                         if (actvMode != null) actvMode.setText(mode);
                         // Since we create it programmatically, we might need to ensure it's created first. 
                         // setupVoucherTypeSpinner calls this loader? No, initViews calls setupVoucherTypeSpinner.
                         // But for existing vouchers, we load data. We must ensure the view exists.
                         // The view creation happens in setupVoucherTypeSpinner -> onItemSelected.
                         // If we are loading data, likely we set selection, which triggers creation. 
                         // But we need to be safely sure.
                    }
                }
                loadVoucherCharges(id, type);
            } else if (type.equals("Journal") || type.equals("Contra")) {
                etVoucherNo.setText(cursor.getString(cursor.getColumnIndexOrThrow("journal_no")));
                etDate.setText(cursor.getString(cursor.getColumnIndexOrThrow("date"))); // Fixed column name
                etDeliveryNote.setText(cursor.getString(cursor.getColumnIndexOrThrow("narration"))); // Fixed column name
                loadVoucherCharges(id, type);
            }
            cursor.close();
            
            if (isViewMode) {
                disableEditing();
            }
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
                boolean isDebit = cursor.getInt(cursor.getColumnIndexOrThrow("is_debit")) == 1;
                String pMode = "None";
                try {
                    int modeCol = cursor.getColumnIndex("payment_mode");
                    if (modeCol != -1) pMode = cursor.getString(modeCol);
                } catch (Exception e) {}
                
                chargesList.add(new VoucherCharge(ledgerId, ledgerName, amount, isPercentage, rate, isDebit, pMode));
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
        
        // Use Dialog Context for better theme support
        android.content.Context dialogContext = builder.getContext();
        
        // programmatically build layout
        android.widget.ScrollView scrollView = new android.widget.ScrollView(dialogContext);
        android.widget.LinearLayout layout = new android.widget.LinearLayout(dialogContext);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);
        scrollView.addView(layout);
        
        final AutoCompleteTextView actvChargeName = new AutoCompleteTextView(dialogContext);
        actvChargeName.setHint("Select Ledger");
        actvChargeName.setTextColor(android.graphics.Color.BLACK);
        layout.addView(actvChargeName);
        
        String type = spnVoucherType.getSelectedItem().toString();
        List<String> ledgers = type.equals("Contra") ? databaseHelper.getContraLedgerNames() : databaseHelper.getAllLedgerNames();
        ArrayAdapter<String> ledgerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ledgers);
        actvChargeName.setAdapter(ledgerAdapter);
        actvChargeName.setThreshold(1);
        actvChargeName.setOnClickListener(v -> actvChargeName.showDropDown());
        actvChargeName.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus) actvChargeName.showDropDown();
        });

        final EditText etChargeAmount = new EditText(dialogContext);
        etChargeAmount.setHint("Amount or Rate (%)");
        etChargeAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etChargeAmount.setTextColor(android.graphics.Color.BLACK);
        layout.addView(etChargeAmount);
        
        final CheckBox cbIsPercentage = new CheckBox(dialogContext);
        cbIsPercentage.setText("Is Percentage (%)");
        cbIsPercentage.setTextColor(android.graphics.Color.BLACK);
        layout.addView(cbIsPercentage);
        
        final Spinner spnSide = new Spinner(dialogContext);
        if (type.equals("Journal") || type.equals("Contra")) {
             String[] sides = type.equals("Contra") ? new String[]{"(Dr)", "(Cr)"} : new String[]{"By (Dr)", "To (Cr)"};
             ArrayAdapter<String> sideAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sides);
             spnSide.setAdapter(sideAdapter);
             layout.addView(spnSide, 1); // Add after name
             cbIsPercentage.setVisibility(View.GONE);

             // Calculate current totals for auto-balancing
             double totalDr = 0;
             double totalCr = 0;
             for(VoucherCharge c : chargesList) {
                 if(c.isDebit) totalDr += c.amount;
                 else totalCr += c.amount;
             }
             final double currentDr = totalDr;
             final double currentCr = totalCr;

             spnSide.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    if (position == 1) { // To (Cr)
                        if (currentDr > currentCr) {
                            etChargeAmount.setText(String.valueOf(currentDr - currentCr));
                        }
                    } else { // By (Dr)
                        if (currentCr > currentDr) {
                            etChargeAmount.setText(String.valueOf(currentCr - currentDr));
                        }
                    }
                }
                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
             });
             
             // Auto-select "To" if Debits exist and exceed Credits
             if(currentDr > currentCr) spnSide.setSelection(1);
        }

        // Rounding Mode RadioGroup
        final TextView tvRoundMode = new TextView(dialogContext);
        tvRoundMode.setText("Select Rounding Logic:");
        tvRoundMode.setTextColor(android.graphics.Color.parseColor("#3F51B5"));
        tvRoundMode.setPadding(0, 24, 0, 8);
        tvRoundMode.setVisibility(View.GONE);
        layout.addView(tvRoundMode);

        final android.widget.RadioGroup rgRoundMode = new android.widget.RadioGroup(dialogContext);
        rgRoundMode.setOrientation(android.widget.RadioGroup.VERTICAL); 
        rgRoundMode.setVisibility(View.GONE);
        String[] modes = {"Auto (Nearest)", "Plus (+)", "Minus (-)"};
        for (int i = 0; i < modes.length; i++) {
            android.widget.RadioButton rb = new android.widget.RadioButton(dialogContext);
            rb.setText(modes[i]);
            rb.setId(i); 
            rb.setTextColor(android.graphics.Color.BLACK);
            rgRoundMode.addView(rb);
        }
        android.content.SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int lastMode = prefs.getInt("round_off_mode", 0); 
        rgRoundMode.check(lastMode);
        layout.addView(rgRoundMode);

        actvChargeName.setOnItemClickListener((parent, view, position, id) -> {
             String selectedName = (String) parent.getItemAtPosition(position);
             boolean isRoundOff = isRoundOffRefined(selectedName);
             
             tvRoundMode.setVisibility(isRoundOff ? View.VISIBLE : View.GONE);
             rgRoundMode.setVisibility(isRoundOff ? View.VISIBLE : View.GONE);
             etChargeAmount.setVisibility(isRoundOff ? View.GONE : View.VISIBLE);
             cbIsPercentage.setVisibility(isRoundOff ? View.GONE : View.VISIBLE);
             
             if (isRoundOff) {
                 cbIsPercentage.setChecked(false);
                 etChargeAmount.setText("0");
             } else {
                 android.database.Cursor c = databaseHelper.getLedgerDetails(selectedName);
                 if (c != null && c.moveToFirst()) {
                     int rateIdx = c.getColumnIndex("tax_rate");
                     int pctIdx = c.getColumnIndex("is_percentage");
                     
                     if (rateIdx != -1) {
                         double rate = c.getDouble(rateIdx);
                         if (rate > 0) etChargeAmount.setText(String.valueOf(rate));
                         else etChargeAmount.setText("");
                     }
                     if (pctIdx != -1) {
                         cbIsPercentage.setChecked(c.getInt(pctIdx) == 1);
                     }
                     c.close();
                 }
             }
        });
        
        builder.setView(scrollView);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = actvChargeName.getText().toString();
            String amtStr = etChargeAmount.getText().toString();
            if (!name.isEmpty() && (!amtStr.isEmpty() || isRoundOffRefined(name))) {
                
                boolean isRoundOff = isRoundOffRefined(name);
                double amount = isRoundOff ? 0 : Double.parseDouble(amtStr);
                boolean isPercentage = isRoundOff ? false : cbIsPercentage.isChecked();
                boolean isDebit = true;
                
                if (type.equals("Journal") || type.equals("Contra")) {
                    isDebit = spnSide.getSelectedItemPosition() == 0;
                }
                
                if (isRoundOff) {
                    int mode = rgRoundMode.getCheckedRadioButtonId();
                    if (mode == -1) mode = 0;
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putInt("round_off_mode", mode).apply();
                }

                chargesList.add(new VoucherCharge(0, name, amount, isPercentage, isPercentage ? amount : 0, isDebit));
                chargesAdapter.notifyDataSetChanged();
                updateTotals();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.setNeutralButton("Create New", (dialog, which) -> showCreateLedgerDialog(actvChargeName.getText().toString()));
        builder.show();
    }

    private boolean saveVoucher(boolean shouldFinish) {
        String type = spnVoucherType.getSelectedItem().toString();
        String voucherNo = etVoucherNo.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String partyName = actvPartyName.getText().toString().trim();
        String narration = etDeliveryNote.getText().toString().trim();

        if (voucherNo.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Number and Date are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Auto-add pending Journal/Contra line
        if (type.equals("Journal") || type.equals("Contra")) {
            String pAmtStr = etPaymentAmount.getText().toString();
            String pName = actvPartyName.getText().toString();
            if (!pAmtStr.isEmpty() && !pName.isEmpty()) {
                addPaymentLine();
            }
            
            if (chargesList.isEmpty()) {
                Toast.makeText(this, "Empty Journal/Contra cannot be saved", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            // Unbalanced Check (Optional but helpful)
            double tDr = 0, tCr = 0;
            for (VoucherCharge c : chargesList) {
                if (c.isDebit) tDr += c.amount; else tCr += c.amount;
            }
            if (Math.abs(tDr - tCr) > 0.01) {
                Toast.makeText(this, "Voucher is Unbalanced: Dr " + String.format("%.2f", tDr) + " != Cr " + String.format("%.2f", tCr), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        String mode = getIntent().getStringExtra("MODE");
        boolean isEdit = "EDIT".equals(mode);
        int editId = getIntent().getIntExtra("ID", -1);

        long voucherId = -1;

        if (isEdit && editId != -1) {
             // UPDATE LOGIC
             if (type.equals("Sales")) {
                 Toast.makeText(this, "Please use standard Invoice screen for editing Sales", Toast.LENGTH_SHORT).show();
                 return false;
             } else if (type.equals("Purchase")) {
                  String supplierInvNo = etSupplierInvNo.getText().toString();
                  String supplierTin = etSupplierTin.getText().toString();
                  String supplierCst = etSupplierCst.getText().toString();
                  String buyerVatTin = etBuyerVatTin.getText().toString();
                  String supplierInvDate = date; // defaulting

                  databaseHelper.updatePurchase(editId, voucherNo, date, supplierInvDate, supplierInvNo, partyName, supplierCst, supplierTin, buyerVatTin, totalAmount);
                  
                  // Re-add items (simplest update strategy: delete all items and re-add from list)
                  databaseHelper.deletePurchaseItems(editId);
                  for (InvoiceItem item : itemList) {
                    databaseHelper.addPurchaseItem(editId, item.getItemName(), item.getQuantity(), item.getRate(), item.getAmount(), item.getUnit(), item.getHsn());
                  }
                  
                  voucherId = editId;
             } else if (type.equals("Payment")) {
                 String throughLedger = actvBankLedger.getText().toString().trim();
                 if (throughLedger.isEmpty()) {
                     Toast.makeText(this, "Through Ledger is required", Toast.LENGTH_SHORT).show();
                     return false;
                 }
                 // Mimic addPayment logic: append party name to narration if typical pattern
                 String finalNarration = narration + (partyName.isEmpty() ? "" : " (Party: " + partyName + ")");
                 databaseHelper.updatePayment(editId, voucherNo, date, throughLedger, totalAmount, finalNarration);
                 voucherId = editId;
             } else if (type.equals("Receipt")) {
                 String throughLedger = actvBankLedger.getText().toString().trim();
                 if (throughLedger.isEmpty()) {
                     Toast.makeText(this, "Through Ledger is required", Toast.LENGTH_SHORT).show();
                     return false;
                 }
                 
                 String paymentMode = "";
                 android.widget.EditText actvMode = findViewById(R.id.actvReceiptMode);
                 if (actvMode != null) paymentMode = actvMode.getText().toString();
                 
                 databaseHelper.updateReceipt(editId, voucherNo, date, narration, throughLedger, totalAmount, paymentMode);
                 voucherId = editId;
             } else if (type.equals("Journal") || type.equals("Contra")) {
                 databaseHelper.updateJournal(editId, voucherNo, date, narration, type);
                 voucherId = editId;
             }
             
             // Delete existing charges
             databaseHelper.deleteVoucherCharges(voucherId, type);
             
        } else {
             // INSERT LOGIC
            if (type.equals("Sales")) {
                if (partyName.isEmpty()) {
                    Toast.makeText(this, "Party Name is required", Toast.LENGTH_SHORT).show();
                    return false;
                }
                double totalGst = 0;
                for (InvoiceItem item : itemList) {
                    totalGst += (item.getCgstAmount() + item.getSgstAmount());
                }
                voucherId = databaseHelper.addVoucher(voucherNo, date, partyName, subtotalAmount, totalGst, totalChargesAmount, type, selectedCompanyId);
                for (InvoiceItem item : itemList) {
                    databaseHelper.addVoucherItem(voucherId, type, item.getItemName(), item.getQuantity(), item.getRate(), item.getAmount(), item.getGstRate(), item.getCgstAmount(), item.getSgstAmount(), item.getUnit(), item.getHsn());
                }
            } else if (type.equals("Purchase")) {
                 if (partyName.isEmpty()) {
                    Toast.makeText(this, "Supplier Name is required", Toast.LENGTH_SHORT).show();
                    return false;
                }
                String supplierInvNo = etSupplierInvNo.getText().toString();
                String supplierTin = etSupplierTin.getText().toString();
                String supplierCst = etSupplierCst.getText().toString();
                String buyerVatTin = etBuyerVatTin.getText().toString();
                // supplierInvDate not explicitly in UI, assuming same as voucher date or add field? 
                // User requirement didn't specify date field, but common. stick to what requested.
                String supplierInvDate = date; 

                voucherId = databaseHelper.addPurchase(voucherNo, date, supplierInvDate, supplierInvNo, partyName, supplierCst, supplierTin, buyerVatTin, totalAmount, selectedCompanyId);
                
                for (InvoiceItem item : itemList) {
                    databaseHelper.addPurchaseItem(voucherId, item.getItemName(), item.getQuantity(), item.getRate(), item.getAmount(), item.getUnit(), item.getHsn());
                }
            } else if (type.equals("Payment") || type.equals("Receipt")) {
                 String throughLedger = actvBankLedger.getText().toString().trim();
                 if (throughLedger.isEmpty()) {
                     Toast.makeText(this, "Through Ledger is required", Toast.LENGTH_SHORT).show();
                     return false;
                 }
                 if (type.equals("Payment")) {
                     voucherId = databaseHelper.addPayment(voucherNo, date, partyName, totalAmount, throughLedger, narration, selectedCompanyId);
                  } else {
                      String paymentMode = "";
                      android.widget.EditText actvMode = findViewById(R.id.actvReceiptMode);
                      if (actvMode != null) paymentMode = actvMode.getText().toString();
                      
                      voucherId = databaseHelper.addReceipt(voucherNo, date, narration, throughLedger, totalAmount, selectedCompanyId, paymentMode);
                  }
            } else if (type.equals("Journal") || type.equals("Contra")) {
                voucherId = databaseHelper.addJournal(voucherNo, date, narration, type, selectedCompanyId);
            }
        }

        if (voucherId != -1) {
            for (VoucherCharge charge : chargesList) {
                databaseHelper.addVoucherCharge(voucherId, type, charge.ledgerId, charge.ledgerName, charge.amount, charge.isPercentage, charge.rate, charge.isDebit, charge.paymentMode);
            }
            if (shouldFinish) {
                Toast.makeText(this, isEdit ? type + " Updated Successfully" : type + " Saved Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
            return true;
        } else {
            Toast.makeText(this, "Failed to save " + type, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void showCreateLedgerDialog(String initialName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Ledger");

        android.content.Context dialogContext = builder.getContext();
        android.widget.LinearLayout layout = new android.widget.LinearLayout(dialogContext);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);

        final EditText etName = new EditText(dialogContext);
        etName.setHint("Ledger Name");
        etName.setText(initialName);
        etName.setTextColor(android.graphics.Color.BLACK);
        layout.addView(etName);

        final Spinner spnGroup = new Spinner(dialogContext);
        // Common groups for charges/taxes
        String[] groups = {"Indirect Expenses", "Direct Expenses", "Duties & Taxes", "Indirect Incomes", "Direct Incomes"};
        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, groups);
        spnGroup.setAdapter(groupAdapter);
        layout.addView(spnGroup);

        builder.setView(layout);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String group = (String) spnGroup.getSelectedItem();

            if (!name.isEmpty()) {
                // Simplified creation - assumes other fields optional/default
                databaseHelper.addLedger(name, group, "", "", "", "", 0, "", 0, false, "", "", "", "");
                Toast.makeText(this, "Ledger Created", Toast.LENGTH_SHORT).show();
                // Re-open Add Charge dialog to use it immediately
                showAddChargeDialog(); 
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> showAddChargeDialog()); // Re-open parent
        builder.show();
    }

    private void updateTotals() {
        subtotalAmount = 0;
        double totalGst = 0;
        for (InvoiceItem item : itemList) {
            subtotalAmount += item.getAmount();
            totalGst += (item.getCgstAmount() + item.getSgstAmount());
        }
        
        // Move Round Off to end if it exists
        VoucherCharge roundOffCharge = null;
        for (int i = 0; i < chargesList.size(); i++) {
            if (isRoundOffRefined(chargesList.get(i).ledgerName)) {
                roundOffCharge = chargesList.remove(i);
                chargesList.add(roundOffCharge);
                break;
            }
        }

        totalChargesAmount = 0;
        String type = spnVoucherType.getSelectedItem().toString();
        
        if (type.equals("Journal") || type.equals("Contra")) {
             totalAmount = 0;
             for (VoucherCharge c : chargesList) if (c.isDebit) totalAmount += c.amount;
        } else {
            double intermediateTotal = subtotalAmount + totalGst;
            
            android.content.SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            int roundMode = prefs.getInt("round_off_mode", 0);

            for (VoucherCharge charge : chargesList) {
                if (isRoundOffRefined(charge.ledgerName)) {
                    double currentTotal = intermediateTotal + totalChargesAmount;
                    double roundedTotal;
                    switch (roundMode) {
                        case 1: // Plus
                            roundedTotal = Math.ceil(currentTotal);
                            break;
                        case 2: // Minus
                            roundedTotal = Math.floor(currentTotal);
                            break;
                        default: // Auto
                            roundedTotal = Math.round(currentTotal);
                            break;
                    }
                    charge.amount = roundedTotal - currentTotal;
                    charge.isPercentage = false;
                } else if (charge.isPercentage) {
                    charge.amount = (subtotalAmount * charge.rate) / 100;
                }
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
        boolean isDr;
        if (type.equals("Journal") || type.equals("Contra")) {
            isDr = spnPaymentSide.getSelectedItemPosition() == 0;
        } else {
            isDr = type.equals("Payment");
        }
        String pMode = spnPaymentMode.getSelectedItem().toString();
        chargesList.add(new VoucherCharge(0, partyName, amt, false, 0, isDr, pMode));


        chargesAdapter.notifyDataSetChanged();
        etPaymentAmount.setText("");
        updateTotals();
    }

    private void printAndSharePdf() {
        if (saveVoucher(false)) {
            Invoice inv = createInvoiceObject();
            if (inv != null) {
                new PdfGenerator(this).generateAndOpenPdf(inv);
            } else {
                Toast.makeText(this, "Failed to generate PDF data.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void shareExcel() {
        if (saveVoucher(false)) {
            Invoice inv = createInvoiceObject();
            if (inv != null) {
                new ExcelGenerator(this).generateAndOpenExcel(inv);
            } else {
                Toast.makeText(this, "Failed to generate Excel data.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Invoice createInvoiceObject() {
        String type = spnVoucherType.getSelectedItem().toString();
        String voucherNo = etVoucherNo.getText().toString();
        
        if (voucherNo.isEmpty()) {
            Toast.makeText(this, "No Voucher Data to Generate", Toast.LENGTH_SHORT).show();
            return null;
        }
        
        // Use database query to ensure we export saved state
        // (Similar to previous viewPdf logic)
        
        if (type.equals("Sales") || type.equals("Purchase")) {
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
                           String mode = ch.getString(ch.getColumnIndexOrThrow("payment_mode"));
                           chList.add(new InvoiceCharge(ch.getString(ch.getColumnIndexOrThrow("ledger_name")), ch.getDouble(ch.getColumnIndexOrThrow("amount")), ch.getDouble(ch.getColumnIndexOrThrow("rate")), ch.getInt(ch.getColumnIndexOrThrow("is_percentage")) == 1, false, mode));

                      }
                      ch.close();
                 }
                 Invoice inv = new Invoice(voucherNo, date, pName, itList, total, 0, 0, total);
                 
                 if (type.equals("Purchase")) {
                     inv.setSupplierInvNo(etSupplierInvNo.getText().toString());
                     inv.setSupplierTin(etSupplierTin.getText().toString());
                     inv.setSupplierCst(etSupplierCst.getText().toString());
                     inv.setBuyerVatTin(etBuyerVatTin.getText().toString());
                     inv.setSupplierInvDate(etDate.getText().toString());
                 }
                 
                 inv.setExtraCharges(chList);
                 inv.setBuyersOrderNo(type.toUpperCase());
                 return inv;
             }
        } else if (type.equals("Payment") || type.equals("Receipt")) {
             String tbl = type.equals("Payment") ? "payments" : "receipts";
             String vNoCol = type.equals("Payment") ? "voucher_no" : "receipt_no";
             Cursor c = databaseHelper.getReadableDatabase().query(tbl, null, vNoCol + "=? AND company_id=?", new String[]{voucherNo, String.valueOf(selectedCompanyId)}, null, null, null);
             if (c != null && c.moveToFirst()) {
                 int id = c.getInt(c.getColumnIndexOrThrow("_id"));
                 String date = c.getString(c.getColumnIndexOrThrow("date"));
                 String totalStr = c.getString(c.getColumnIndexOrThrow("total_amount"));
                 double total = (totalStr != null && !totalStr.isEmpty()) ? Double.parseDouble(totalStr) : 0;
                 String narration = c.getString(c.getColumnIndexOrThrow("narration"));
                 String through = c.getString(c.getColumnIndexOrThrow("through_ledger"));
                 
                 String paymentMode = "";
                 if (type.equals("Receipt")) {
                     int pmIdx = c.getColumnIndex(DatabaseHelper.COLUMN_RECEIPT_PAYMENT_MODE);
                     if (pmIdx != -1) paymentMode = c.getString(pmIdx);
                 }
                 c.close();

                 List<InvoiceCharge> chList = new ArrayList<>();
                 Cursor ch = databaseHelper.getVoucherCharges(id, type);
                 if (ch != null) {
                      while(ch.moveToNext()) {
                          String name = ch.getString(ch.getColumnIndexOrThrow("ledger_name"));
                          double amt = ch.getDouble(ch.getColumnIndexOrThrow("amount"));
                          boolean isDr = ch.getInt(ch.getColumnIndexOrThrow("is_debit")) == 1;
                           String mode = ch.getString(ch.getColumnIndexOrThrow("payment_mode"));
                           chList.add(new InvoiceCharge(name, amt, 0, false, isDr, mode));

                          if (type.equals("Receipt")) total += amt;
                      }
                      ch.close();
                  
                      // Extract Party Name for Receipt/Payment if available from charges
                      String partyName = type.equals("Payment") ? "Payment Voucher" : "Receipt Voucher";
                      if (!chList.isEmpty()) {
                          // Uses the first charge as the primary party if multiple? 
                          // Or join them? For now, use first.
                          partyName = chList.get(0).getChargeName(); 
                          if (chList.size() > 1) partyName += " (Multiple)";
                      }
                  
                      Invoice inv = new Invoice(voucherNo, date, partyName, new ArrayList<>(), total, 0, 0, total);
                      inv.setBuyersOrderNo(type.toUpperCase());
                      inv.setDispatchThrough(through);
                      inv.setDeliveryNote(narration);
                      inv.setExtraCharges(chList);
                      if (type.equals("Receipt")) inv.setModeOfPayment(paymentMode);
                      return inv;
                  }
             }
        } else if (type.equals("Journal") || type.equals("Contra")) {
             Cursor c = databaseHelper.getReadableDatabase().query("journals", null, "journal_no=? AND company_id=? AND journal_type=?", new String[]{voucherNo, String.valueOf(selectedCompanyId), type}, null, null, null);
             if (c != null && c.moveToFirst()) {
                 int id = c.getInt(c.getColumnIndexOrThrow("_id"));
                 String date = c.getString(c.getColumnIndexOrThrow("date"));
                 // Toast.makeText(this, "Debug: Found Journal ID: " + id, Toast.LENGTH_SHORT).show();

                 String narration = c.getString(c.getColumnIndexOrThrow("narration"));
                 c.close();

                 List<InvoiceCharge> chList = new ArrayList<>();
                 Cursor ch = databaseHelper.getVoucherCharges(id, type);
                 double total = 0;
                 if (ch != null) {
                      while(ch.moveToNext()) {
                          String name = ch.getString(ch.getColumnIndexOrThrow("ledger_name"));
                          double amt = ch.getDouble(ch.getColumnIndexOrThrow("amount"));
                          boolean isDr = ch.getInt(ch.getColumnIndexOrThrow("is_debit")) == 1;
                           String mode = ch.getString(ch.getColumnIndexOrThrow("payment_mode"));
                           chList.add(new InvoiceCharge(name, amt, 0, false, isDr, mode));

                          if (isDr) total += amt;
                      }
                      ch.close();
                 }
                 // Toast.makeText(this, "Debug: Charges count: " + chList.size(), Toast.LENGTH_SHORT).show();
                 String title = type.equals("Contra") ? "Contra Voucher" : "Journal Voucher";
                 Invoice inv = new Invoice(voucherNo, date, title, new ArrayList<>(), total, 0, 0, total);
                 inv.setBuyersOrderNo(type.toUpperCase());
                 inv.setModeOfPayment(type); // Set Transaction Mode
                 inv.setDeliveryNote(narration);
                 inv.setExtraCharges(chList);
                 return inv;
             } else {
                 Toast.makeText(this, "Debug: Journal not found in DB", Toast.LENGTH_SHORT).show();
             }
        }

        return null;
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
    

    
    interface OnRemoveListener {
        void onRemove(int position);
    }

    private class ChargesAdapter extends RecyclerView.Adapter<ChargesAdapter.ChargeViewHolder> {
        private List<VoucherCharge> list;
        private OnRemoveListener listener;
        
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
            if (type.equalsIgnoreCase("Journal") || type.equalsIgnoreCase("Contra")) {
                prefix = charge.isDebit ? "By " : "To ";
            }
            holder.tvName.setText(prefix + charge.ledgerName + (charge.paymentMode != null && !"None".equals(charge.paymentMode) ? " [" + charge.paymentMode + "]" : ""));

            if (charge.isPercentage) {
                holder.tvValue.setText("@ " + charge.rate + "%");
            } else {
                holder.tvValue.setText("(Fixed)");
            }
            holder.tvAmount.setText(String.format("\u20B9%.2f", charge.amount));
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

    private com.google.android.material.textfield.TextInputLayout createTextInput(String hint) {
        com.google.android.material.textfield.TextInputLayout til = new com.google.android.material.textfield.TextInputLayout(this, null, com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox);
        til.setHint(hint);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        til.setLayoutParams(params);
        
        TextInputEditText et = new TextInputEditText(til.getContext());
        til.addView(et);
        return til;
    }
    
    // Helper to extract EditText from TIL
    private TextInputEditText getEditText(com.google.android.material.textfield.TextInputLayout til) {
        return (TextInputEditText) til.getEditText();
    }
}

