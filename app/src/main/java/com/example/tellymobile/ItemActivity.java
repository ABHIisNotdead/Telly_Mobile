package com.example.tellymobile;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ItemActivity extends BaseActivity {

    private TextInputEditText etName, etHsn, etRate, etStock;
    private AutoCompleteTextView actvUnit;
    private android.widget.Spinner spnStockGroup, spnStockCategory;
    private Button btnSave;
    private DatabaseHelper databaseHelper;

    private int updateId = -1;
    private String mode = "CREATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
        
        mode = getIntent().getStringExtra("MODE");
        if ("EDIT".equals(mode)) {
            updateId = getIntent().getIntExtra("ID", -1);
            loadItemData(updateId);
            btnSave.setText("Update Item");
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Update Stock Item");
        }
    }

    private void loadItemData(int id) {
        android.database.Cursor cursor = databaseHelper.getItem(id);
        if (cursor != null && cursor.moveToFirst()) {
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow("item_name")));
            // Check if column exists (safe fallback)
            int hsnIndex = cursor.getColumnIndex("hsn_sac");
            if (hsnIndex != -1) {
                etHsn.setText(cursor.getString(hsnIndex));
            }
            actvUnit.setText(cursor.getString(cursor.getColumnIndexOrThrow("unit")));
            etRate.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("rate"))));
             etStock.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("stock_quantity"))));
             
            // Set Spinners
            setSpinnerSelection(spnStockGroup, cursor, "stock_group");
            setSpinnerSelection(spnStockCategory, cursor, "stock_category");
            
            cursor.close();
        }
    }
    
    private void setSpinnerSelection(android.widget.Spinner spinner, android.database.Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index != -1) {
            String value = cursor.getString(index);
            android.widget.SpinnerAdapter adapter = spinner.getAdapter();
            if (adapter != null) {
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (adapter.getItem(i).toString().equals(value)) {
                        spinner.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish()); // Handle Back Button

        etName = findViewById(R.id.etItemName);
        etHsn = findViewById(R.id.etHsn); // HSN
        actvUnit = findViewById(R.id.actvUnit); // Updated ID
        spnStockGroup = findViewById(R.id.spnStockGroup);
        spnStockCategory = findViewById(R.id.spnStockCategory);
        etRate = findViewById(R.id.etRate);
        etStock = findViewById(R.id.etStock);
        btnSave = findViewById(R.id.btnSave);
        
        setupUnitAdapter();
        setupSpinners();
    }
    
    private void setupSpinners() {
        // Stock Groups
        List<String> groups = databaseHelper.getAllStockGroups();
        if (groups.isEmpty()) {
            groups.add("Primary"); // Default if none
        }
        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, groups);
        spnStockGroup.setAdapter(groupAdapter);
        
        // Stock Categories
        List<String> categories = databaseHelper.getAllStockCategories();
        if (categories.isEmpty()) {
            categories.add("Primary"); // Default
        }
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spnStockCategory.setAdapter(catAdapter);
    }
    
    // Unit List Logic
    private void setupUnitAdapter() {
        // ... (lines 80-87 omitted)
        String[] standardUnits = {
            "Piece / Each (pc/ea)", "Dozen (doz/dz)", "Gross (gr)", // Count
            "Gram (g)", "Kilogram (kg)", "Milligram (mg)", "Metric Ton (t)", "Pound (lb)", "Ounce (oz)", // Weight
            "Milliliter (ml)", "Liter (L)", "Gallon (gal)", "Cubic Meter (m3/cbm)", // Volume
            "Millimeter (mm)", "Centimeter (cm)", "Meter (m)", "Inch (in)", "Foot (ft)", "Yard (yd)", // Length
            "Square Meter (m2/sqm)", "Square Foot (ft2/sqf)", // Area
            "Roll (rl)", "Box (bx)", "Packet (pkt)", "Set (st)", "Ream (rm)" // Packaging
        };
        
        // Load Recents
        List<String> recentUnits = loadRecentUnits();
        
        // Merge Lists (Recents + Standards)
        List<String> allUnits = new ArrayList<>(recentUnits);
        for (String unit : standardUnits) {
            if (!recentUnits.contains(unit)) {
                allUnits.add(unit);
            }
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allUnits);
        actvUnit.setAdapter(adapter);
    }
    
    private List<String> loadRecentUnits() {
        SharedPreferences prefs = getSharedPreferences("TellyMobilePrefs", MODE_PRIVATE);
        String recentStr = prefs.getString("RECENT_UNITS", "");
        List<String> recents = new ArrayList<>();
        if (!recentStr.isEmpty()) {
            String[] items = recentStr.split(",");
            for (String item : items) {
                if (!item.trim().isEmpty()) recents.add(item.trim());
            }
        }
        return recents;
    }
    
    private void saveRecentUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) return;
        
        List<String> recents = loadRecentUnits();
        recents.remove(unit); // Remove if exists to move to top
        recents.add(0, unit); // Add to top
        
        // Keep only top 5
        if (recents.size() > 5) {
            recents = recents.subList(0, 5);
        }
        
        // Save back
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recents.size(); i++) {
            sb.append(recents.get(i));
            if (i < recents.size() - 1) sb.append(",");
        }
        
        SharedPreferences prefs = getSharedPreferences("TellyMobilePrefs", MODE_PRIVATE);
        prefs.edit().putString("RECENT_UNITS", sb.toString()).apply();
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveItem());
    }

    private void saveItem() {
        String name = etName.getText().toString().trim();
        String hsn = etHsn.getText().toString().trim();
        String unit = actvUnit.getText().toString().trim(); // Use actvUnit
        String rateStr = etRate.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        
        String group = spnStockGroup.getSelectedItem() != null ? spnStockGroup.getSelectedItem().toString() : "Primary";
        String category = spnStockCategory.getSelectedItem() != null ? spnStockCategory.getSelectedItem().toString() : "Primary";

        if (name.isEmpty()) {
            Toast.makeText(this, "Item Name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save Unit to Recents
        if (!unit.isEmpty()) {
            saveRecentUnit(unit);
        }

        double rate = rateStr.isEmpty() ? 0.0 : Double.parseDouble(rateStr);
        double stock = stockStr.isEmpty() ? 0.0 : Double.parseDouble(stockStr);

        if ("EDIT".equals(mode) && updateId != -1) {
            databaseHelper.updateItem(updateId, name, rate, unit, stock, hsn, group, category);
            Toast.makeText(this, "Item Updated Successfully", Toast.LENGTH_SHORT).show();
        } else {
            databaseHelper.addItem(name, rate, unit, stock, hsn, group, category);
            Toast.makeText(this, "Item Saved Successfully", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
