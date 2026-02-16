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
import com.google.android.material.appbar.MaterialToolbar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.content.SharedPreferences;
import java.util.List;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MainPage extends BaseActivity {

    private ArrayAdapter<DatabaseHelper.Company> companyAdapter;
    private static final String PREFS_NAME = "TellyPrefs";
    private static final String KEY_COMPANY_ID = "selected_company_id";
    
    // Missing Fields Restored
    private DatabaseHelper dbHelper;
    private List<DatabaseHelper.Company> companyList;
    
    // Image Picker
    private android.net.Uri selectedLogoUri = null;
    private android.widget.ImageView ivLogoPreview = null;
    
    private final androidx.activity.result.ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
        new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                selectedLogoUri = uri;
                if (ivLogoPreview != null) {
                    ivLogoPreview.setImageURI(uri);
                    // Persist Permission
                    try {
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    );

    // Backup Launcher
    private final androidx.activity.result.ActivityResultLauncher<String> createBackupLauncher = registerForActivityResult(
        new androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/x-sqlite3"),
        uri -> {
            if (uri != null) {
                BackupUtils.backupToUri(this, uri);
            }
        }
    );

    // Restore Launcher
    private final androidx.activity.result.ActivityResultLauncher<String[]> openRestoreLauncher = registerForActivityResult(
        new androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        uri -> {
            if (uri != null) {
                showRestoreConfirmationDialog1(uri);
            }
        }
    );

    private void showRestoreConfirmationDialog1(android.net.Uri uri) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Restore Backup")
            .setMessage("Are you sure you want to restore? This will replace your current data with the selected backup.")
            .setPositiveButton("Yes", (dialog, which) -> showRestoreConfirmationDialog2(uri))
            .setNegativeButton("No", null)
            .show();
    }

    private void showRestoreConfirmationDialog2(android.net.Uri uri) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Restore")
            .setMessage("WARNING: This action is irreversible. All current data will be LOST. Do you really want to proceed?")
            .setPositiveButton("YES, RESTORE", (dialog, which) -> BackupUtils.restoreFromUri(this, uri))
            .setNegativeButton("CANCEL", null)
            .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        
        dbHelper = new DatabaseHelper(this); // Initialize dbHelper
        companyList = new ArrayList<>();     // Initialize List
        refreshCompanyList();
        updateToolbarTitle();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button CreateButton = findViewById(R.id.btnCreateMaster);
        CreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPage.this, CreatePage.class);
                startActivity(intent);
            }
        });


        Button AlterButton = findViewById(R.id.btnAlterMaster);
        AlterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPage.this, AlterPage.class);
                startActivity(intent);
            }
        });

        Button VoucherButton = findViewById(R.id.btnVouchers);
        VoucherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPage.this, VouchersPage.class);
                startActivity(intent);
            }
        });

        Button ChartButton = findViewById(R.id.btnChartOfAC);
        ChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPage.this, ChartOfAccPage.class);
                startActivity(intent);
            }
        });

        Button DaybookButton = findViewById(R.id.btnDayBook);
        DaybookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPage.this, DayBookPage.class);
                startActivity(intent);
            }
        });

        Button voucherButton = findViewById(R.id.btnVoucherEntry);
        voucherButton.setOnClickListener(v -> startActivity(new Intent(MainPage.this, VoucherActivity.class)));
        
        Button ReportsButton = findViewById(R.id.btnSalesReports);
        ReportsButton.setOnClickListener(v -> startActivity(new Intent(MainPage.this, ReportsActivity.class)));
        
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_change_company) {
            showCompanySelectionDialog();
            return true;
        } else if (item.getItemId() == R.id.action_theme) {
            showThemeSelectionDialog();
            return true;
        } else if (item.getItemId() == R.id.action_messages) {
            startActivity(new Intent(MainPage.this, MessageActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_backup) {
            createBackupLauncher.launch("TellyMobile_Backup.db");
            return true;
        } else if (item.getItemId() == R.id.action_restore) {
             openRestoreLauncher.launch(new String[]{"*/*"}); // Open any file, let Utils handle or fail if invalid content
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showThemeSelectionDialog() {
        final String[] themes = {"Default", "Ocean", "Sunset", "Nature", "Night/Dark"};
        final int[] themeIds = {
            ThemeManager.THEME_DEFAULT, 
            ThemeManager.THEME_OCEAN, 
            ThemeManager.THEME_SUNSET, 
            ThemeManager.THEME_NATURE, 
            ThemeManager.THEME_NIGHT
        };

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Theme");
        builder.setItems(themes, (dialog, which) -> {
            ThemeManager.saveTheme(MainPage.this, themeIds[which]);
            // Restart Activity to apply theme
            recreate();
        });
        builder.show();
    }
    
    private void showCompanySelectionDialog() {
        refreshCompanyList();
        
        // Prepare list for display (Names only)
        final String[] companyNames = new String[companyList.size()];
        for (int i = 0; i < companyList.size(); i++) {
            companyNames[i] = companyList.get(i).name;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Company");
        builder.setItems(companyNames, (dialog, which) -> {
            DatabaseHelper.Company selected = companyList.get(which);
            if (selected.id == -1) {
                showAddCompanyDialog();
            } else {
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putInt(KEY_COMPANY_ID, selected.id)
                    .apply();
                updateToolbarTitle();
                // Optionally show toast or refresh data if needed
            }
        });
        builder.show();
    }
    
    private void updateToolbarTitle() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedId = prefs.getInt(KEY_COMPANY_ID, -1);
        String companyName = "Select Company";
        
        if (savedId != -1) {
            for (DatabaseHelper.Company c : companyList) {
                if (c.id == savedId) {
                    companyName = c.name;
                    break;
                }
            }
        }
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(companyName);
        }
    }
    
    private void refreshCompanyList() {
        companyList.clear();
        companyList.addAll(dbHelper.getAllCompanies());
        // Add "Add New..." option
        companyList.add(new DatabaseHelper.Company(-1, "+ Add New Company", "", ""));
        if (companyAdapter != null) companyAdapter.notifyDataSetChanged();
    }
    
    private void showAddCompanyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Company");
        
        View view = getLayoutInflater().inflate(R.layout.dialog_add_company, null);
        builder.setView(view);
        
        final EditText etName = view.findViewById(R.id.etCompanyName);
        final EditText etAddress = view.findViewById(R.id.etCompanyAddress);
        final EditText etPhone1 = view.findViewById(R.id.etCompanyPhone1);
        final EditText etPhone2 = view.findViewById(R.id.etCompanyPhone2);
        final EditText etEmail = view.findViewById(R.id.etCompanyEmail);
        final EditText etState = view.findViewById(R.id.etCompanyState);
        final EditText etGST = view.findViewById(R.id.etCompanyGod); // Actually GST field ID
        
        ivLogoPreview = view.findViewById(R.id.ivCompanyLogo);
        Button btnSelectLogo = view.findViewById(R.id.btnSelectLogo);
        selectedLogoUri = null; // Reset
        
        btnSelectLogo.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        
        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = etName.getText().toString();
            if (!name.isEmpty()) {
                long id = dbHelper.addCompany(
                    name, 
                    etAddress.getText().toString(),
                    etPhone1.getText().toString(),
                    etPhone2.getText().toString(),
                    etEmail.getText().toString(),
                    etState.getText().toString(),
                    selectedLogoUri != null ? selectedLogoUri.toString() : "",
                    etGST.getText().toString(),
                    "" // Tagline
                );
                
                if (id != -1) {
                    refreshCompanyList();
                    // Auto Select
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putInt(KEY_COMPANY_ID, (int)id).apply();
                    updateToolbarTitle(); 
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}