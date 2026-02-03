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

    private TextInputEditText etInvoiceNo, etDate, etSupplierName, etItemName, etQuantity, etRate;
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
        etItemName = findViewById(R.id.etItemName);
        etQuantity = findViewById(R.id.etQuantity);
        etRate = findViewById(R.id.etRate);
        
        // Note: Total Amount logic in UI is currently just a TextView update, need to bind it
        // The layout has tvTotalAmount - wait, I need to find it
        // tvTotalAmount = findViewById(R.id.tvTotalAmount); 
        
        btnAddItem = findViewById(R.id.btnAddItem);
        btnSavePurchase = findViewById(R.id.btnSaveInvoice); // Reused ID in XML: btnSaveInvoice for Save Purchase
        rvInvoiceItems = findViewById(R.id.rvInvoiceItems);
    }

    private void setupRecyclerView() {
        adapter = new InvoiceAdapter(purchaseItemList);
        rvInvoiceItems.setLayoutManager(new LinearLayoutManager(this));
        rvInvoiceItems.setAdapter(adapter);
    }

    private void setupListeners() {
        btnAddItem.setOnClickListener(v -> addItem());
        btnSavePurchase.setOnClickListener(v -> savePurchase());
    }

    private void addItem() {
        String name = etItemName.getText().toString();
        String qtyStr = etQuantity.getText().toString();
        String rateStr = etRate.getText().toString();

        if (name.isEmpty() || qtyStr.isEmpty() || rateStr.isEmpty()) {
            Toast.makeText(this, "Please fill all item details", Toast.LENGTH_SHORT).show();
            return;
        }

        double qty = Double.parseDouble(qtyStr);
        double rate = Double.parseDouble(rateStr);
        double amount = qty * rate;

        InvoiceItem item = new InvoiceItem(name, qty, rate, amount, 0, 0, 0);
        purchaseItemList.add(item);
        adapter.notifyDataSetChanged();

        totalAmount += amount;
        updateTotalDisplay();

        // Clear item fields
        etItemName.setText("");
        etQuantity.setText("");
        etRate.setText("");
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
                databaseHelper.addPurchaseItem(savedId, item.getItemName(), item.getQuantity(), item.getRate(), item.getAmount());
            }
            Toast.makeText(this, "Purchase Saved & Stock Updated!", Toast.LENGTH_LONG).show();
            finish(); // Close activity
        } else {
            Toast.makeText(this, "Failed to save purchase", Toast.LENGTH_SHORT).show();
        }
    }
}
