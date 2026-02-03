package com.example.tellymobile;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartOfAccPage extends BaseActivity {

    private ExpandableListView expandableListView;
    private DatabaseHelper databaseHelper;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_of_acc);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        prepareListData();
        
        ExpandableListAdapter adapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expandableListView.setAdapter(adapter);
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        expandableListView = findViewById(R.id.elvChart);
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        // 1. Ledger Groups -> Ledgers
        List<String> groups = databaseHelper.getAllLedgerGroups();
        for (String group : groups) {
            Cursor cursor = databaseHelper.getLedgersByGroup(group);
            List<String> childList = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("ledger_name"));
                    double balance = cursor.getDouble(cursor.getColumnIndexOrThrow("opening_balance"));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("balance_type"));
                    
                    childList.add(name + "\n" + "Op. Bal: " + balance + " " + type);
                }
                cursor.close();
            }
            
            if (!childList.isEmpty()) {
                String header = "Group: " + group;
                listDataHeader.add(header);
                listDataChild.put(header, childList);
            }
        }

        // 2. Stock Groups -> Items
        List<String> stockGroups = databaseHelper.getAllStockGroups();
        for (String sGroup : stockGroups) {
            List<String> items = databaseHelper.getItemsByStockGroup(sGroup);
            if (!items.isEmpty()) {
                String header = "Stock Group: " + sGroup;
                listDataHeader.add(header);
                listDataChild.put(header, items);
            }
        }

        // 3. Stock Categories -> Items
        List<String> stockCats = databaseHelper.getAllStockCategories();
        for (String sCat : stockCats) {
            List<String> items = databaseHelper.getItemsByStockCategory(sCat);
             if (!items.isEmpty()) {
                String header = "Stock Category: " + sCat;
                listDataHeader.add(header);
                listDataChild.put(header, items);
            }
        }
        
        // 4. All Items (Uncategorized or General View if needed, but above covers categorized)
        // If user wants specific "All Items", we could add that too.
    }
    
    // Internal Adapter Class
    public class ExpandableListAdapter extends BaseExpandableListAdapter {
        
        private android.content.Context _context;
        private List<String> _listDataHeader; // header titles
        private HashMap<String, List<String>> _listDataChild;

        public ExpandableListAdapter(android.content.Context context, List<String> listDataHeader,
                                     HashMap<String, List<String>> listChildData) {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;
        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosititon);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final String childText = (String) getChild(groupPosition, childPosition);

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(android.R.layout.simple_list_item_1, null);
            }

            TextView txtListChild = convertView.findViewById(android.R.id.text1);
            txtListChild.setText(childText);
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this._listDataHeader.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this._listDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
            }

            TextView lblListHeader = convertView.findViewById(android.R.id.text1);
            lblListHeader.setTypeface(null, android.graphics.Typeface.BOLD);
            lblListHeader.setText(headerTitle);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}