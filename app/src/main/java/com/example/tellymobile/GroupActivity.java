package com.example.tellymobile;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class GroupActivity extends BaseActivity {

    private TextInputEditText etName;
    private Spinner spnPrimary;
    private Button btnSave;
    private DatabaseHelper databaseHelper;

    private int updateId = -1;
    private String mode = "CREATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();
        
        mode = getIntent().getStringExtra("MODE");
        if ("EDIT".equals(mode)) {
            updateId = getIntent().getIntExtra("ID", -1);
            loadGroupData(updateId);
            btnSave.setText("Update Group");
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Update Group");
        }
    }
    
    private void loadGroupData(int id) {
        android.database.Cursor cursor = databaseHelper.getLedgerGroup(id);
        if (cursor != null && cursor.moveToFirst()) {
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            String parent = cursor.getString(cursor.getColumnIndexOrThrow("parent"));
            
            android.widget.SpinnerAdapter adapter = spnPrimary.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equals(parent)) {
                    spnPrimary.setSelection(i);
                    break;
                }
            }
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
        spnPrimary = findViewById(R.id.spnPrimaryGroup);
        btnSave = findViewById(R.id.btnSave);
        
        loadPrimaryGroups();
    }

    private void loadPrimaryGroups() {
        List<String> groups = databaseHelper.getAllLedgerGroups(); // Can serve as parents
        if (groups.isEmpty()) {
            groups.add("Primary");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, groups);
        spnPrimary.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveGroup());
    }

    private void saveGroup() {
        String name = etName.getText().toString().trim();
        String primary = spnPrimary.getSelectedItem() != null ? spnPrimary.getSelectedItem().toString() : "Primary";

        if (TextUtils.isEmpty(name)) {
            etName.setError("Group Name is required");
            return;
        }

        if ("EDIT".equals(mode) && updateId != -1) {
            databaseHelper.updateLedgerGroup(updateId, name, primary);
             Toast.makeText(this, "Group Updated Successfully", Toast.LENGTH_SHORT).show();
        } else {
            databaseHelper.addLedgerGroup(name, primary);
            Toast.makeText(this, "Group Saved Successfully", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
