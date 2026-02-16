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

    private TextInputEditText etInvoiceNo, etDate, etSupplierName, etQuantity, etUnit, etRate, etHsn;
    private android.widget.AutoCompleteTextView etItemName;
    private Button btnAddItem, btnSavePurchase;
    private RecyclerView rvInvoiceItems;
    private InvoiceAdapter adapter; // Reusing InvoiceAdapter since logic is same
    private List<InvoiceItem> purchaseItemList;
    private DatabaseHelper databaseHelper;
    private double totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        databaseHelper = new DatabaseHelper(this);
        purchaseItemList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupListeners();
    }

    private void initViews() {
        etInvoiceNo = findViewById(R.id.etInvoiceNo);
        etDate = findViewById(R.id.etDate);
        etSupplierName = findViewById(R.id.etSupplierName);
        etItemName = (android.widget.AutoCompleteTextView) findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etQuantity);
        etUnit = findViewById(R.id.etUnit);
        etHsn = findViewById(R.id.etHsn);
        etRate = findViewById(R.id.etRate);
        
        // Note: Total Amount logic in UI is currently just a TextView update, need to bind it
        // The layout has tvTotalAmount - wait, I need to find it
        // tvTotalAmount = findViewById(R.id.tvTotalAmount); 
        
        btnAddItem = findViewById(R.id.btnAddItem);
        btnSavePurchase = findViewById(R.id.btnSaveInvoice); // Reused ID in XML: btnSaveInvoice for Save Purchase
        rvInvoiceItems = findViewById(R.id.rvInvoiceItems);

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
        btnSavePurchase.setOnClickListener(v -> savePurchase());
    }

    private void addItem() {
        String name = etItemName.getText().toString();
        String qtyStr = etQuantity.getText().toString();
        String unit = etUnit.getText().toString();
        String hsn = etHsn.getText().toString();
        String rateStr = etRate.getText().toString();

        if (name.isEmpty() || qtyStr.isEmpty() || rateStr.isEmpty()) {
            Toast.makeText(this, "Please fill all item details", Toast.LENGTH_SHORT).show();
            return;
        }

        double qty = Double.parseDouble(qtyStr);
        double rate = Double.parseDouble(rateStr);
        double amount = qty * rate;

        InvoiceItem item = new InvoiceItem(name, qty, rate, amount, 0, 0, 0, unit, hsn);
        purchaseItemList.add(item);
        adapter.notifyDataSetChanged();

        totalAmount += amount;
        updateTotalDisplay();

        // Clear item fields
        etItemName.setText("");
        etQuantity.setText("");
        etRate.setText("");
        etUnit.setText("");
        etHsn.setText("");
    }
    
    private void updateTotalDisplay() {
        android.widget.TextView tvTotal = findViewById(R.id.tvTotalAmount);
        if (tvTotal != null) {
            tvTotal.setText("Total: ₹" + totalAmount);
        }
    }

    private void savePurchase() {
        String invoiceNo = etInvoiceNo.getText().toString();
        String date = etDate.getText().toString();
        String supplier = etSupplierName.getText().toString();

        if (invoiceNo.isEmpty() || date.isEmpty() || supplier.isEmpty() || purchaseItemList.isEmpty()) {
            Toast.makeText(this, "Please fill purchase details and add items", Toast.LENGTH_SHORT).show();
            return;
        }

        long savedId = databaseHelper.addPurchase(invoiceNo, date, supplier, totalAmount);
        
        if (savedId != -1) {
            for (InvoiceItem item : purchaseItemList) {
                databaseHelper.addPurchaseItem(savedId, item.getItemName(), item.getQuantity(), item.getRate(), item.getAmount(), item.getUnit(), item.getHsn());
            }
            Toast.makeText(this, "Purchase Saved & Stock Updated!", Toast.LENGTH_LONG).show();
            finish(); // Close activity
        } else {
            Toast.makeText(this, "Failed to save purchase", Toast.LENGTH_SHORT).show();
        }
    }
}
