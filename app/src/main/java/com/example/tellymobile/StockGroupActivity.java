package com.example.tellymobile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

public class StockGroupActivity extends BaseActivity {

    private TextInputEditText etName;
    private Button btnSave;
    private DatabaseHelper databaseHelper;

    private int updateId = -1;
    private String mode = "CREATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_group);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
        
        mode = getIntent().getStringExtra("MODE");
         if ("EDIT".equals(mode)) {
            updateId = getIntent().getIntExtra("ID", -1);
            loadGroupData(updateId);
            btnSave.setText("Update Stock Group");
             if (getSupportActionBar() != null) getSupportActionBar().setTitle("Update Stock Group");
        }
    }
    
    private void loadGroupData(int id) {
         android.database.Cursor cursor = databaseHelper.getStockGroup(id);
        if (cursor != null && cursor.moveToFirst()) {
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            // Parent logic if implemented
            cursor.close();
        }
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.etGroupName);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveGroup());
    }

    private void saveGroup() {
        String name = etName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        if ("EDIT".equals(mode) && updateId != -1) {
            databaseHelper.updateStockGroup(updateId, name, "Primary");
            Toast.makeText(this, "Stock Group Updated", Toast.LENGTH_SHORT).show();
        } else {
            databaseHelper.addStockGroup(name, "Primary");
            Toast.makeText(this, "Stock Group Saved Successfully", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
