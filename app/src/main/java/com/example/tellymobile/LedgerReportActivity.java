package com.example.tellymobile;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LedgerReportActivity extends AppCompatActivity {

    private AutoCompleteTextView actvLedger;
    private EditText etStartDate, etEndDate;
    private Button btnShowReport;
    private TextView tvOpening, tvNetMovement, tvClosing;
    private RecyclerView rvTransactions;
    
    private DatabaseHelper dbHelper;
    private LedgerTransactionAdapter adapter;
    private List<DatabaseHelper.LedgerTransaction> allTransactions;
    private List<DatabaseHelper.LedgerTransaction> displayedTransactions;
    
    private Calendar calendar;
    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger_report);
        
        dbHelper = new DatabaseHelper(this);
        calendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        
        initViews();
        setupListeners();
        loadLedgerNames();
    }
    
    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        actvLedger = findViewById(R.id.actvLedger);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        btnShowReport = findViewById(R.id.btnShowReport);
        
        tvOpening = findViewById(R.id.tvOpening);
        tvNetMovement = findViewById(R.id.tvNetMovement);
        tvClosing = findViewById(R.id.tvClosing);
        
        rvTransactions = findViewById(R.id.rvTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        
        displayedTransactions = new ArrayList<>();
        adapter = new LedgerTransactionAdapter(displayedTransactions);
        rvTransactions.setAdapter(adapter);
    }
    
    private void setupListeners() {
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));
        
        btnShowReport.setOnClickListener(v -> generateReport());
    }
    
    private void showDatePicker(EditText editText) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            editText.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    
    private void loadLedgerNames() {
        List<String> ledgers = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.COLUMN_NAME + " FROM " + DatabaseHelper.TABLE_NAME, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ledgers.add(cursor.getString(0));
            }
            cursor.close();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ledgers);
        actvLedger.setAdapter(adapter);
    }
    
    private void generateReport() {
        try {
            String ledgerName = actvLedger.getText().toString().trim();
            String startDateStr = etStartDate.getText().toString().trim();
            String endDateStr = etEndDate.getText().toString().trim();
            
            if (ledgerName.isEmpty()) {
                NotificationUtils.showTopNotification(this, dbHelper, "Please select a ledger", true);
                return;
            }
            
            // fetch all transactions for this ledger
            allTransactions = dbHelper.getLedgerTransactions(ledgerName);
            
            // Filter by Date and Calculate
            Date startDate = null;
            Date endDate = null;
            try {
                if (!startDateStr.isEmpty()) startDate = sdf.parse(startDateStr);
                if (!endDateStr.isEmpty()) endDate = sdf.parse(endDateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            
            double openingBalance = 0;
            // Fetch opening balance from DB
            Cursor c = dbHelper.getLedgerDetails(ledgerName);
            if (c != null && c.moveToFirst()) {
                int balIdx = c.getColumnIndex(DatabaseHelper.COLUMN_BALANCE);
                int typeIdx = c.getColumnIndex(DatabaseHelper.COLUMN_TYPE);
                
                if(balIdx != -1) openingBalance = c.getDouble(balIdx);
                
                if(typeIdx != -1) {
                    String type = c.getString(typeIdx);
                    if (type != null && type.equalsIgnoreCase("Cr")) {
                        openingBalance = -openingBalance;
                    }
                }
                c.close();
            }
            
            // Filter and Calculate Running Totals
            displayedTransactions.clear();
            
            double currentDr = 0;
            double currentCr = 0;
            
            // If we have a start date, we need to calculate the "Opening Balance" as of that date
            double periodOpening = openingBalance;
            
            for (DatabaseHelper.LedgerTransaction tx : allTransactions) {
                if (tx.date == null) continue; // Safety Check

                Date txDate = null;
                try {
                     // Try parsing with hyphen
                     txDate = sdf.parse(tx.date.replace("/", "-"));
                } catch (Exception e) {
                     // Try parsing with slash
                    try {
                         txDate = new SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(tx.date);
                    } catch (Exception e2) {}
                }
                
                if (txDate == null) continue; // Skip invalid dates
                
                boolean beforeStart = (startDate != null && txDate.before(startDate));
                boolean afterEnd = (endDate != null && txDate.after(endDate));
                
                if (beforeStart) {
                    // Add to Period Opening
                    periodOpening += (tx.debit - tx.credit);
                } else if (!afterEnd) {
                    // Within Period
                    displayedTransactions.add(tx);
                    currentDr += tx.debit;
                    currentCr += tx.credit;
                }
            }
            
            adapter.notifyDataSetChanged();
            
            // Updates UI
            tvOpening.setText(String.format(Locale.getDefault(), "Op: %.2f", periodOpening));
            
            double netMove = currentDr - currentCr;
            tvNetMovement.setText(String.format(Locale.getDefault(), "Net: %.2f (Dr: %.0f, Cr: %.0f)", netMove, currentDr, currentCr));
            
            double closing = periodOpening + netMove;
            tvClosing.setText(String.format(Locale.getDefault(), "Cl: %.2f", closing));
            
        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtils.showTopNotification(this, dbHelper, "Error generating report: " + e.getMessage(), true);
        }
    }
}
