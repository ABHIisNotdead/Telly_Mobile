package com.example.tellymobile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class Ledger extends BaseActivity {

    // Define variables
    TextInputEditText etName, etMobile, etEmail, etAddress, etGst, etBalance, etTaxRate;
    Spinner spnGroup;
    RadioGroup rgDrCr;
    RadioButton rbDr, rbCr;
    Button btnSave, btnBack;
    android.widget.CheckBox cbIsPercentage;
    View cardTaxDetails;
    DatabaseHelper myDB;
    private int updateId = -1;
    private String mode = "CREATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger);

        // Initialize UI Elements
        etName = findViewById(R.id.etLedgerName);
        etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etGst = findViewById(R.id.etGst);
        etBalance = findViewById(R.id.etOpeningBalance);
        spnGroup = findViewById(R.id.spnGroup);
        rgDrCr = findViewById(R.id.rgDrCr);
        btnSave = findViewById(R.id.btnSave);
        
        etTaxRate = findViewById(R.id.etTaxRate);
        cbIsPercentage = findViewById(R.id.cbIsPercentage);
        cardTaxDetails = findViewById(R.id.cardTaxDetails);
        View cardMailingDetails = findViewById(R.id.cardMailingDetails); // Local or Field? Best to be field if needed elsewhere, but local is fine for init/listener capture

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize Database Helper
        myDB = new DatabaseHelper(this);

        // Populate Spinner with Groups
        java.util.List<String> groups = myDB.getAllLedgerGroups();
        if (groups.isEmpty()) {
            groups.add("Primary");
            groups.add("Duties & Taxes"); // Fallback
        } else {
             // Ensure Duties & Taxes exists for testing
             boolean hasTax = false;
             for (String g : groups) {
                 if (g.equalsIgnoreCase("Duties & Taxes")) {
                     hasTax = true; 
                     break;
                 }
             }
             if (!hasTax) groups.add("Duties & Taxes");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, groups);
        spnGroup.setAdapter(adapter);
        
        // Listener for Group Selection to toggle Tax Details visibility
        spnGroup.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedGroup = groups.get(position);
                // Toast.makeText(Ledger.this, "Selected: " + selectedGroup, Toast.LENGTH_SHORT).show(); // Debug
                if (selectedGroup != null && (selectedGroup.contains("Duties & Taxes") || selectedGroup.contains("Tax"))) {
                    cardTaxDetails.setVisibility(View.VISIBLE);
                    cardMailingDetails.setVisibility(View.GONE);
                } else {
                    cardTaxDetails.setVisibility(View.GONE);
                    cardMailingDetails.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        mode = getIntent().getStringExtra("MODE");
        if ("EDIT".equals(mode)) {
            updateId = getIntent().getIntExtra("ID", -1);
            loadLedgerData(updateId);
            btnSave.setText("Update Ledger");
        }

        // Handle Save Button Click
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get data from inputs
                String name = etName.getText().toString().trim();
                String group = spnGroup.getSelectedItem().toString();
                String mobile = etMobile.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String address = etAddress.getText().toString().trim();
                String gst = etGst.getText().toString().trim();
                String balanceStr = etBalance.getText().toString().trim();
                
                String taxRateStr = etTaxRate.getText().toString().trim();
                double taxRate = taxRateStr.isEmpty() ? 0 : Double.parseDouble(taxRateStr);
                boolean isPercentage = cbIsPercentage.isChecked();

                // Determine Dr or Cr
                int selectedId = rgDrCr.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = findViewById(selectedId);
                String type = selectedRadioButton != null ? selectedRadioButton.getText().toString() : "Dr";

                // Validation
                if (TextUtils.isEmpty(name)) {
                    etName.setError("Ledger Name is required");
                    return;
                }

                double balance = 0.0;
                if (!balanceStr.isEmpty()) {
                    balance = Double.parseDouble(balanceStr);
                }

                // Save to Database
                if ("EDIT".equals(mode) && updateId != -1) {
                    myDB.updateLedger(updateId, name, group, mobile, email, address, gst, balance, type, taxRate, isPercentage);
                    Toast.makeText(Ledger.this, "Ledger Updated Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    myDB.addLedger(name, group, mobile, email, address, gst, balance, type, taxRate, isPercentage);
                    // Toast handled in addLedger but good to have consistency
                }
                finish();
            }
        });
    }

    private void loadLedgerData(int id) {
        android.database.Cursor cursor = myDB.getLedger(id);
        if (cursor != null && cursor.moveToFirst()) {
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow("ledger_name")));
            etMobile.setText(cursor.getString(cursor.getColumnIndexOrThrow("mobile")));
            etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            etAddress.setText(cursor.getString(cursor.getColumnIndexOrThrow("address")));
            etGst.setText(cursor.getString(cursor.getColumnIndexOrThrow("gst")));
            etBalance.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("opening_balance"))));
            
            // Tax Details
            int rateIdx = cursor.getColumnIndex("tax_rate");
            if (rateIdx != -1) etTaxRate.setText(String.valueOf(cursor.getDouble(rateIdx)));
            
            int pctIdx = cursor.getColumnIndex("is_percentage");
            if (pctIdx != -1) cbIsPercentage.setChecked(cursor.getInt(pctIdx) == 1);
            
            // Set Spinner Selection (Simplified loop)
            String group = cursor.getString(cursor.getColumnIndexOrThrow("ledger_group"));
            android.widget.SpinnerAdapter adapter = spnGroup.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(group)) {
                    spnGroup.setSelection(i);
                    break;
                }
            }
            cursor.close();
        }
    }
}