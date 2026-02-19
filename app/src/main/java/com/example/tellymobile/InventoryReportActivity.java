package com.example.tellymobile;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class InventoryReportActivity extends AppCompatActivity {

    private RecyclerView rvInventory;
    private InventoryAdapter adapter;
    private List<InventoryAdapter.InventoryItem> itemList;
    private DatabaseHelper dbHelper;
    private MaterialCardView cardLowStockWarning;
    private TextView tvLowStockMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_report);

        dbHelper = new DatabaseHelper(this);
        itemList = new ArrayList<>();

        initViews();
        loadInventory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInventory(); // Refresh when returning from ItemActivity
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvInventory = findViewById(R.id.rvInventory);
        rvInventory.setLayoutManager(new LinearLayoutManager(this));
        
        cardLowStockWarning = findViewById(R.id.cardLowStockWarning);
        tvLowStockMessage = findViewById(R.id.tvLowStockMessage);
    }

    private void loadInventory() {
        itemList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // Select items for the selected company or legacy items (company_id = 0)
        int companyId = getSharedPreferences("TellyPrefs", MODE_PRIVATE).getInt("selected_company_id", -1);
        
        String query = "SELECT " + DatabaseHelper.COLUMN_ITEM_ID + ", " +
                       DatabaseHelper.COLUMN_ITEM_NAME + ", " +
                       DatabaseHelper.COLUMN_ITEM_STOCK + ", " +
                       DatabaseHelper.COLUMN_ITEM_COST + ", " +
                       DatabaseHelper.COLUMN_ITEM_LOW_STOCK_LIMIT + ", " +
                       DatabaseHelper.COLUMN_ITEM_RATE +
                       " FROM " + DatabaseHelper.TABLE_ITEMS + 
                       " WHERE " + DatabaseHelper.COLUMN_COMPANY_ID + "=? OR " + DatabaseHelper.COLUMN_COMPANY_ID + "=0"; // Show legacy items
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(companyId)});
        
        int lowStockCount = 0;
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                double stock = cursor.getDouble(2);
                double cost = cursor.getDouble(3);
                double lowLimit = cursor.getDouble(4);
                double rate = cursor.getDouble(5);
                
                // If cost is 0, fallback to Standard Rate
                if (cost == 0) cost = rate;
                
                itemList.add(new InventoryAdapter.InventoryItem(id, name, stock, cost, lowLimit));
                
                if (stock <= lowLimit && lowLimit > 0) {
                    lowStockCount++;
                }
            }
            cursor.close();
        }

        adapter = new InventoryAdapter(itemList, item -> {
            // Open ItemActivity in EDIT mode
            android.content.Intent intent = new android.content.Intent(InventoryReportActivity.this, ItemActivity.class);
            intent.putExtra("MODE", "EDIT");
            intent.putExtra("ID", item.id);
            startActivity(intent);
        });
        rvInventory.setAdapter(adapter);
        
        if (lowStockCount > 0) {
            cardLowStockWarning.setVisibility(View.VISIBLE);
            tvLowStockMessage.setText(lowStockCount + " items are running low on stock!");
        } else {
            cardLowStockWarning.setVisibility(View.GONE);
        }
    }
}
