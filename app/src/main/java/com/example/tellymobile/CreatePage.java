package com.example.tellymobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

public class CreatePage extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_page);
        
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button LedgerButton = findViewById(R.id.btnLedger);
        LedgerButton.setOnClickListener(v -> {
            Intent intent = new Intent(CreatePage.this, Ledger.class);
            startActivity(intent);
        });

        Button ItemButton = findViewById(R.id.btnStockItem);
        ItemButton.setOnClickListener(v -> {
            Intent intent = new Intent(CreatePage.this, ItemActivity.class);
            startActivity(intent);
        });

        Button GroupButton = findViewById(R.id.btnGroup);
        GroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(CreatePage.this, GroupActivity.class);
            startActivity(intent);
        });

        Button StockGroupButton = findViewById(R.id.btnStockGroup);
        StockGroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(CreatePage.this, StockGroupActivity.class);
            startActivity(intent);
        });

        Button StockCategoryButton = findViewById(R.id.btnStockCategory);
        StockCategoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(CreatePage.this, StockCategoryActivity.class);
            startActivity(intent);
        });
    }
}
