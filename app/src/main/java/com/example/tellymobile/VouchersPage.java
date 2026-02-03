package com.example.tellymobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class VouchersPage extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vouchers_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        setupListeners();
    }
    
    private void setupListeners() {
        findViewById(R.id.btnSalesV).setOnClickListener(v -> openVoucher("Sales"));
        findViewById(R.id.btnPurchaseV).setOnClickListener(v -> openVoucher("Purchase"));
        findViewById(R.id.btnPaymentV).setOnClickListener(v -> openVoucher("Payment"));
        findViewById(R.id.btnReceiptV).setOnClickListener(v -> openVoucher("Receipt"));
        findViewById(R.id.btnJournalV).setOnClickListener(v -> openVoucher("Journal"));
        findViewById(R.id.btnContraV).setOnClickListener(v -> openVoucher("Contra"));
    }
    
    private void openVoucher(String type) {
        Intent intent = new Intent(this, VoucherActivity.class);
        intent.putExtra("MODE", "CREATE");
        intent.putExtra("TYPE", type); // Check if VoucherActivity handles this Extra to pre-set spinner
        startActivity(intent);
    }
}