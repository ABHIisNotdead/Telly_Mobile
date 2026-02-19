package com.example.tellymobile;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tellymobile.DatabaseHelper.TrialBalanceRow;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.List;
import java.util.Locale;

public class TrialBalanceActivity extends AppCompatActivity {

    private RecyclerView rvTrialBalance;
    private TextView tvTotalDebit, tvTotalCredit;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trial_balance);

        dbHelper = new DatabaseHelper(this);
        initViews();
        loadTrialBalance();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvTrialBalance = findViewById(R.id.rvTrialBalance);
        rvTrialBalance.setLayoutManager(new LinearLayoutManager(this));

        tvTotalDebit = findViewById(R.id.tvTotalDebit);
        tvTotalCredit = findViewById(R.id.tvTotalCredit);
    }

    private void loadTrialBalance() {
        try {
            List<TrialBalanceRow> rows = dbHelper.getTrialBalance();
            
            TrialBalanceAdapter adapter = new TrialBalanceAdapter(rows);
            rvTrialBalance.setAdapter(adapter);
    
            double totalDr = 0;
            double totalCr = 0;
            for (TrialBalanceRow row : rows) {
                totalDr += row.debit;
                totalCr += row.credit;
            }
    
            tvTotalDebit.setText(String.format(Locale.getDefault(), "%.2f", totalDr));
            tvTotalCredit.setText(String.format(Locale.getDefault(), "%.2f", totalCr));
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Error loading Trial Balance: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }
}
