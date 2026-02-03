package com.example.tellymobile;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class MasterListActivity extends BaseActivity {

    private ListView listView;
    private DatabaseHelper databaseHelper;
    private String type; // "LEDGER" or "ITEM"
    private ArrayList<String> namesList;
    private ArrayList<Integer> idsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master_list);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        listView = findViewById(R.id.listView);
        databaseHelper = new DatabaseHelper(this);
        namesList = new ArrayList<>();
        idsList = new ArrayList<>();

        type = getIntent().getStringExtra("TYPE");
        if (type == null) type = "LEDGER";

        loadData();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, namesList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int selectedId = idsList.get(position);
                Intent intent = null;

                switch (type) {
                    case "LEDGER":
                        intent = new Intent(MasterListActivity.this, Ledger.class);
                        break;
                    case "GROUP":
                        intent = new Intent(MasterListActivity.this, GroupActivity.class);
                        break;
                    case "ITEM":
                        intent = new Intent(MasterListActivity.this, ItemActivity.class);
                        break;
                    case "STOCK_GROUP":
                        intent = new Intent(MasterListActivity.this, StockGroupActivity.class);
                        break;
                    case "STOCK_CATEGORY":
                        intent = new Intent(MasterListActivity.this, StockCategoryActivity.class);
                        break;
                }

                if (intent != null) {
                    intent.putExtra("ID", selectedId);
                    intent.putExtra("MODE", "EDIT");
                    startActivity(intent);
                }
            }
        });
    }

    private void loadData() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = null;

        switch (type) {
            case "LEDGER":
                getSupportActionBar().setTitle("Select Ledger to Alter");
                cursor = db.query(DatabaseHelper.TABLE_NAME,
                        new String[]{DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_NAME},
                        null, null, null, null, null);
                break;
            case "GROUP":
                getSupportActionBar().setTitle("Select Group to Alter");
                cursor = db.query(DatabaseHelper.TABLE_GROUPS,
                        new String[]{DatabaseHelper.COLUMN_GROUP_ID, DatabaseHelper.COLUMN_GROUP_NAME},
                        null, null, null, null, null);
                break;
            case "ITEM":
                getSupportActionBar().setTitle("Select Item to Alter");
                cursor = db.query(DatabaseHelper.TABLE_ITEMS,
                        new String[]{DatabaseHelper.COLUMN_ITEM_ID, DatabaseHelper.COLUMN_ITEM_NAME},
                        null, null, null, null, null);
                break;
            case "STOCK_GROUP":
                getSupportActionBar().setTitle("Select Stock Group to Alter");
                cursor = db.query(DatabaseHelper.TABLE_STOCK_GROUPS,
                        new String[]{DatabaseHelper.COLUMN_STOCK_GROUP_ID, DatabaseHelper.COLUMN_STOCK_GROUP_NAME},
                        null, null, null, null, null);
                break;
            case "STOCK_CATEGORY":
                getSupportActionBar().setTitle("Select Stock Category to Alter");
                cursor = db.query(DatabaseHelper.TABLE_STOCK_CATEGORIES,
                        new String[]{DatabaseHelper.COLUMN_STOCK_CAT_ID, DatabaseHelper.COLUMN_STOCK_CAT_NAME},
                        null, null, null, null, null);
                break;
        }

        if (cursor != null) {
            while (cursor.moveToNext()) {
                idsList.add(cursor.getInt(0));
                namesList.add(cursor.getString(1));
            }
            cursor.close();
        }
    }
}
    
    // In onCreate's onItemClick
    /*
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int selectedId = idsList.get(position);
                Intent intent = null;
                
                switch (type) {
                    case "LEDGER":
                        intent = new Intent(MasterListActivity.this, Ledger.class);
                        break;
                    case "GROUP":
                        intent = new Intent(MasterListActivity.this, GroupActivity.class);
                        break;
                    case "ITEM":
                        intent = new Intent(MasterListActivity.this, ItemActivity.class);
                        break;
                    case "STOCK_GROUP":
                        intent = new Intent(MasterListActivity.this, StockGroupActivity.class);
                        break;
                    case "STOCK_CATEGORY":
                        intent = new Intent(MasterListActivity.this, StockCategoryActivity.class);
                        break;
                }
                
                if (intent != null) {
                    intent.putExtra("ID", selectedId);
                    intent.putExtra("MODE", "EDIT");
                    startActivity(intent);
                }
            }
        });
    */
