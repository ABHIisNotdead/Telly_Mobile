package com.example.tellymobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DayBookPage extends BaseActivity {

    private RecyclerView recyclerView;
    private DatabaseHelper databaseHelper;
    private DayBookAdapter adapter;
    private List<DatabaseHelper.VoucherSummary> originalList;
    private List<DatabaseHelper.VoucherSummary> filteredList;

    // Filter States
    private String filterType = "All";
    private String filterLedger = "";
    private long filterStartDate = 0;
    private long filterEndDate = 0;

    // Sort State
    private int sortOption = 0; // 0: Date Newest, 1: Date Oldest, 2: Amount High, 3: Amount Low

    private com.google.android.material.chip.ChipGroup chipGroupFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_book_page);
        
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rvDayBook);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        setupChipFilters();

        databaseHelper = new DatabaseHelper(this);
        loadVouchers();
    }

    private void setupChipFilters() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) filterType = "All";
            else if (checkedId == R.id.chipSales) filterType = "Sales";
            else if (checkedId == R.id.chipPurchase) filterType = "Purchase";
            else if (checkedId == R.id.chipPayment) filterType = "Payment";
            else if (checkedId == R.id.chipReceipt) filterType = "Receipt";
            applyFiltersAndSort();
        });
    }
    
    private void loadVouchers() {
        android.content.SharedPreferences prefs = getSharedPreferences("TellyPrefs", MODE_PRIVATE);
        int companyId = prefs.getInt("selected_company_id", 0);
        originalList = databaseHelper.getAllVouchers(companyId);
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        if (originalList == null) return;
        
        filteredList = new java.util.ArrayList<>();
        
        // Step 1: Filter raw summaries
        for (DatabaseHelper.VoucherSummary v : originalList) {
            boolean matchesType = filterType.equals("All") || v.type.equals(filterType);
            boolean matchesLedger = filterLedger.isEmpty() || (v.partyName != null && v.partyName.toLowerCase().contains(filterLedger.toLowerCase()));
            boolean matchesDate = true;
            
            if (filterStartDate != 0 && filterEndDate != 0) {
                 try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                    java.util.Date date = sdf.parse(v.date);
                    if (date != null) {
                        long time = date.getTime();
                        matchesDate = time >= filterStartDate && time <= filterEndDate;
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            
            if (matchesType && matchesLedger && matchesDate) {
                filteredList.add(v);
            }
        }
        
        // Step 2: Sort based on user preference (default Date Newest)
        java.util.Collections.sort(filteredList, (o1, o2) -> {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
                if (sortOption == 0 || sortOption == 1) { // Date
                    java.util.Date d1 = sdf.parse(o1.date);
                    java.util.Date d2 = sdf.parse(o2.date);
                    return sortOption == 0 ? d2.compareTo(d1) : d1.compareTo(d2);
                } else { // Amount
                    return sortOption == 2 ? Double.compare(o2.amount, o1.amount) : Double.compare(o1.amount, o2.amount);
                }
            } catch (Exception e) { return 0; }
        });

        // Step 3: Inject Date Headers ("Slotting System")
        List<Object> itemsWithHeaders = new java.util.ArrayList<>();
        String lastDate = "";
        for (DatabaseHelper.VoucherSummary v : filteredList) {
            if (!v.date.equals(lastDate)) {
                itemsWithHeaders.add(formatHeaderDate(v.date));
                lastDate = v.date;
            }
            itemsWithHeaders.add(v);
        }

        if (adapter == null) {
            adapter = new DayBookAdapter(itemsWithHeaders, new DayBookAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(DatabaseHelper.VoucherSummary voucher) {
                    if (voucher.type.equals("Sales")) {
                         Intent intent = new Intent(DayBookPage.this, InvoiceActivity.class);
                         intent.putExtra("MODE", "EDIT");
                         intent.putExtra("ID", voucher.id);
                         startActivity(intent);
                    } else {
                        Intent intent = new Intent(DayBookPage.this, VoucherActivity.class);
                        intent.putExtra("MODE", "VIEW");
                        intent.putExtra("ID", voucher.id);
                        intent.putExtra("TYPE", voucher.type);
                        startActivity(intent);
                    }
                }

                @Override
                public void onEditClick(DatabaseHelper.VoucherSummary voucher) {
                    if (voucher.type.equals("Sales")) {
                        Intent intent = new Intent(DayBookPage.this, InvoiceActivity.class);
                        intent.putExtra("MODE", "EDIT");
                        intent.putExtra("ID", voucher.id);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(DayBookPage.this, VoucherActivity.class);
                        intent.putExtra("MODE", "VIEW");
                        intent.putExtra("ID", voucher.id);
                        intent.putExtra("TYPE", voucher.type);
                        startActivity(intent);
                    }
                }

                @Override
                public void onDeleteClick(DatabaseHelper.VoucherSummary voucher) {
                    new androidx.appcompat.app.AlertDialog.Builder(DayBookPage.this)
                        .setTitle("Delete Voucher")
                        .setMessage("Are you sure you want to delete this voucher?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            databaseHelper.deleteVoucher(voucher.id, voucher.type);
                            loadVouchers();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(itemsWithHeaders);
        }
    }

    private String formatHeaderDate(String dateStr) {
        try {
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault());
            java.util.Date date = in.parse(dateStr);
            if (date == null) return dateStr;
            
            // Helpful "Today/Yesterday" logic
            java.util.Calendar cal = java.util.Calendar.getInstance();
            String today = in.format(cal.getTime());
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
            String yesterday = in.format(cal.getTime());
            
            if (dateStr.equals(today)) return "Today - " + out.format(date);
            if (dateStr.equals(yesterday)) return "Yesterday - " + out.format(date);
            
            return out.format(date);
        } catch (Exception e) { return dateStr; }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadVouchers(); // Refresh list on return
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_daybook, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort) {
            showSortDialog();
            return true;
        } else if (id == R.id.action_filter) {
            showFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        String[] options = {"Date (Newest First)", "Date (Oldest First)", "Amount (High to Low)", "Amount (Low to High)"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Sort By")
            .setSingleChoiceItems(options, sortOption, (dialog, which) -> {
                sortOption = which;
                applyFiltersAndSort();
                dialog.dismiss();
            })
            .show();
    }

    private void showFilterDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Filter");

        View view = getLayoutInflater().inflate(R.layout.dialog_filter_daybook, null);
        builder.setView(view);
        
        android.widget.Spinner spinnerType = view.findViewById(R.id.spinnerType);
        android.widget.AutoCompleteTextView autoLedger = view.findViewById(R.id.autoLedger);
        Button btnStartDate = view.findViewById(R.id.btnStartDate);
        Button btnEndDate = view.findViewById(R.id.btnEndDate);
        Button btnClear = view.findViewById(R.id.btnClear);
        Button btnApply = view.findViewById(R.id.btnApply);
        
        // Type Spinner
        String[] types = {"All", "Sales", "Purchase", "Payment", "Receipt"};
        android.widget.ArrayAdapter<String> typeAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        if (filterType.equals("Sales")) spinnerType.setSelection(1);
        else if (filterType.equals("Purchase")) spinnerType.setSelection(2);
        else if (filterType.equals("Payment")) spinnerType.setSelection(3);
        else if (filterType.equals("Receipt")) spinnerType.setSelection(4);
        
        // Ledger AutoComplete
        List<String> ledgers = databaseHelper.getAllLedgerNames();
        android.widget.ArrayAdapter<String> ledgerAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ledgers);
        autoLedger.setAdapter(ledgerAdapter);
        autoLedger.setText(filterLedger);
        
        // Date Pickers
        final java.util.Calendar calendar = java.util.Calendar.getInstance();
        final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
        
        if (filterStartDate != 0) btnStartDate.setText(sdf.format(new java.util.Date(filterStartDate)));
        if (filterEndDate != 0) btnEndDate.setText(sdf.format(new java.util.Date(filterEndDate)));
        
        btnStartDate.setOnClickListener(v -> {
            new android.app.DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth, 0, 0, 0);
                filterStartDate = calendar.getTimeInMillis();
                btnStartDate.setText(sdf.format(calendar.getTime()));
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });
        
        btnEndDate.setOnClickListener(v -> {
            new android.app.DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth, 23, 59, 59);
                filterEndDate = calendar.getTimeInMillis();
                btnEndDate.setText(sdf.format(calendar.getTime()));
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });
        
        android.app.AlertDialog dialog = builder.create();
        
        btnClear.setOnClickListener(v -> {
            filterType = "All";
            filterLedger = "";
            filterStartDate = 0;
            filterEndDate = 0;
            sortOption = 0;
            applyFiltersAndSort();
            dialog.dismiss();
        });
        
        btnApply.setOnClickListener(v -> {
            filterType = spinnerType.getSelectedItem().toString();
            filterLedger = autoLedger.getText().toString();
            applyFiltersAndSort();
            dialog.dismiss();
        });
        
        dialog.show();
    }
}
