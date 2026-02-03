package com.example.tellymobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DayBookPage extends BaseActivity {

    private RecyclerView recyclerView;
    private DatabaseHelper databaseHelper;
    private DayBookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_book_page);
        
        
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        
        recyclerView = findViewById(R.id.rvDayBook);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        databaseHelper = new DatabaseHelper(this);
        loadVouchers();
    }
    
    private void loadVouchers() {
        List<DatabaseHelper.VoucherSummary> vouchers = databaseHelper.getAllVouchers();
        adapter = new DayBookAdapter(vouchers, new DayBookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DatabaseHelper.VoucherSummary voucher) {
                // View Logic
                if (voucher.type.equals("Sales")) {
                     Intent intent = new Intent(DayBookPage.this, InvoiceActivity.class);
                     intent.putExtra("MODE", "EDIT"); // Or VIEW if InvoiceActivity supports read-only. Currently using EDIT for "viewing" details too effectively.
                     intent.putExtra("ID", voucher.id);
                     startActivity(intent);
                } else {
                    Intent intent = new Intent(DayBookPage.this, VoucherActivity.class);
                    intent.putExtra("MODE", "VIEW");
                    intent.putExtra("ID", voucher.id);
                    intent.putExtra("TYPE", voucher.type);
                    startActivity(intent);
                }
            }

            @Override
            public void onEditClick(DatabaseHelper.VoucherSummary voucher) {
                if (voucher.type.equals("Sales")) {
                    Intent intent = new Intent(DayBookPage.this, InvoiceActivity.class);
                    intent.putExtra("MODE", "EDIT");
                    intent.putExtra("ID", voucher.id);
                    startActivity(intent);
                } else {
                    // For Purchase, fallback to VoucherActivity (View Only or Edit if implemented)
                    Intent intent = new Intent(DayBookPage.this, VoucherActivity.class);
                    intent.putExtra("MODE", "VIEW"); // Keeping as VIEW until Purchase Edit is supported
                    intent.putExtra("ID", voucher.id);
                    intent.putExtra("TYPE", voucher.type);
                    startActivity(intent);
                }
            }

            @Override
            public void onDeleteClick(DatabaseHelper.VoucherSummary voucher) {
                new androidx.appcompat.app.AlertDialog.Builder(DayBookPage.this)
                    .setTitle("Delete Voucher")
                    .setMessage("Are you sure you want to delete this voucher?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        databaseHelper.deleteVoucher(voucher.id, voucher.type);
                        loadVouchers(); // Refresh
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
        recyclerView.setAdapter(adapter);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadVouchers(); // Refresh list on return
    }
}