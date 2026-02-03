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

public class MainPage extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

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
        if (item.getItemId() == R.id.action_theme) {
            showThemeSelectionDialog();
            return true;
        } else if (item.getItemId() == R.id.action_messages) {
            startActivity(new Intent(MainPage.this, MessageActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_backup) {
            BackupUtils.backupDatabase(MainPage.this);
            return true;
        } else if (item.getItemId() == R.id.action_restore) {
            BackupUtils.restoreDatabase(MainPage.this);
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
}