package com.example.tellymobile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

public class StockCategoryActivity extends BaseActivity {

    private TextInputEditText etName;
    private Button btnSave;
    private DatabaseHelper databaseHelper;

    private int updateId = -1;
    private String mode = "CREATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_category);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
        
        mode = getIntent().getStringExtra("MODE");
         if ("EDIT".equals(mode)) {
            updateId = getIntent().getIntExtra("ID", -1);
            loadCategoryData(updateId);
            btnSave.setText("Update Category");
             if (getSupportActionBar() != null) getSupportActionBar().setTitle("Update Category");
        }
    }
    
    private void loadCategoryData(int id) {
         android.database.Cursor cursor = databaseHelper.getStockCategory(id);
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

        etName = findViewById(R.id.etCategoryName);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveCategory());
    }

    private void saveCategory() {
        String name = etName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        if ("EDIT".equals(mode) && updateId != -1) {
             databaseHelper.updateStockCategory(updateId, name, "Primary");
             NotificationUtils.showTopNotification(this, databaseHelper, "Category Updated", false);
        } else {
            databaseHelper.addStockCategory(name, "Primary");
            NotificationUtils.showTopNotification(this, databaseHelper, "Category Saved Successfully", false);
        }
        finish();
    }
}
