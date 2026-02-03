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

public class AlterPage extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alter_page);
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
        findViewById(R.id.btnAltLedger).setOnClickListener(v -> openMasterList("LEDGER"));
        findViewById(R.id.btnAltGroup).setOnClickListener(v -> openMasterList("GROUP"));
        findViewById(R.id.btnAltStockItem).setOnClickListener(v -> openMasterList("ITEM"));
        findViewById(R.id.btnAltStockGroup).setOnClickListener(v -> openMasterList("STOCK_GROUP"));
        findViewById(R.id.btnAltStockCategory).setOnClickListener(v -> openMasterList("STOCK_CATEGORY"));
        // Add others if MasterListActivity supports them
    }

    private void openMasterList(String type) {
        Intent intent = new Intent(AlterPage.this, MasterListActivity.class);
        intent.putExtra("TYPE", type);
        startActivity(intent);
    }

}
