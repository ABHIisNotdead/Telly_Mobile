package com.example.tellymobile;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import android.app.DatePickerDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import android.widget.Button;

public class ReportsActivity extends BaseActivity {

    private BarChart barChart;
    private com.github.mikephil.charting.charts.HorizontalBarChart stockChart;
    private LineChart lineChart;
    private PieChart pieChart;
    private Spinner spnReportType, spnPeriod;
    private RadioGroup rgChartType;
    private DatabaseHelper databaseHelper;
    private RecyclerView rvTrendDetails, rvStockDetails;
    private ReportDetailAdapter trendAdapter, stockAdapter;
    private List<ReportDetailAdapter.ReportDetail> trendDetails, stockDetails;

    private Button btnFromDate, btnToDate;
    private Date fromDate = null;
    private Date toDate = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupSpinners();
        setupDateFilters();
        
        rgChartType.setOnCheckedChangeListener((group, checkedId) -> updateChart());
        
        findViewById(R.id.btnLedgerReport).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, LedgerReportActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.btnTrialBalance).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, TrialBalanceActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        barChart = findViewById(R.id.barChart);
        stockChart = findViewById(R.id.stockChart);
        lineChart = findViewById(R.id.lineChart);
        pieChart = findViewById(R.id.pieChart);
        spnReportType = findViewById(R.id.spnReportType);
        spnPeriod = findViewById(R.id.spnPeriod);
        rgChartType = findViewById(R.id.rgChartType);

        btnFromDate = findViewById(R.id.btnFromDate);
        btnToDate = findViewById(R.id.btnToDate);

        rvTrendDetails = findViewById(R.id.rvTrendDetails);
        rvStockDetails = findViewById(R.id.rvStockDetails);
        rvTrendDetails.setLayoutManager(new LinearLayoutManager(this));
        rvStockDetails.setLayoutManager(new LinearLayoutManager(this));
        
        trendDetails = new ArrayList<>();
        stockDetails = new ArrayList<>();
        trendAdapter = new ReportDetailAdapter(trendDetails);
        stockAdapter = new ReportDetailAdapter(stockDetails);
        
        rvTrendDetails.setAdapter(trendAdapter);
        rvStockDetails.setAdapter(stockAdapter);
    }

    private void setupSpinners() {
        String[] types = {"Sales Trend", "Purchase Trend", "Category Analysis"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spnReportType.setAdapter(typeAdapter);

        String[] periods = {"Daily", "Monthly", "Yearly"};
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, periods);
        spnPeriod.setAdapter(periodAdapter);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spnReportType.setOnItemSelectedListener(listener);
        spnPeriod.setOnItemSelectedListener(listener);
    }

    private void setupDateFilters() {
        btnFromDate.setOnClickListener(v -> showDatePickerDialog(true));
        btnToDate.setOnClickListener(v -> showDatePickerDialog(false));
    }

    private void showDatePickerDialog(boolean isFromDate) {
        Calendar c = Calendar.getInstance();
        if (isFromDate && fromDate != null) c.setTime(fromDate);
        else if (!isFromDate && toDate != null) c.setTime(toDate);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth, 0, 0, 0); // Start of day
            
            if (!isFromDate) {
                selected.set(Calendar.HOUR_OF_DAY, 23);
                selected.set(Calendar.MINUTE, 59);
                selected.set(Calendar.SECOND, 59);
            }

            if (isFromDate) {
                fromDate = selected.getTime();
                btnFromDate.setText("From: " + dateFormat.format(fromDate));
            } else {
                toDate = selected.getTime();
                btnToDate.setText("To: " + dateFormat.format(toDate));
            }
            updateChart();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void updateChart() {
        String type = spnReportType.getSelectedItem().toString();
        String period = spnPeriod.getSelectedItem().toString();

        barChart.setVisibility(View.GONE);
        lineChart.setVisibility(View.GONE);
        pieChart.setVisibility(View.GONE);
        rvTrendDetails.setVisibility(View.GONE);
        spnPeriod.setVisibility(View.VISIBLE);
        rgChartType.setVisibility(View.VISIBLE);

        if (type.equals("Category Analysis")) {
            spnPeriod.setVisibility(View.GONE); 
            rgChartType.setVisibility(View.GONE);
            loadCategoryChart();
        } else {
            loadTrendChart(type, period);
        }
        
        loadStockChart(); // Always load stock chart
    }
    
    private void loadStockChart() {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Cursor cursor = databaseHelper.getItemStockSummary();
        
        stockDetails.clear();
        if (cursor != null) {
            int i = 0;
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                float stock = cursor.getFloat(1);
                // Only show positive stock or all? Let's show all non-zero
                if (stock != 0) {
                    entries.add(new BarEntry(i, stock));
                    labels.add(name);
                    stockDetails.add(new ReportDetailAdapter.ReportDetail(name, String.format(Locale.US, "%.0f", stock)));
                    i++;
                }
            }
            cursor.close();
        }
        stockAdapter.notifyDataSetChanged();
        rvStockDetails.setVisibility(stockDetails.isEmpty() ? View.GONE : View.VISIBLE);
        
        BarDataSet dataSet = new BarDataSet(entries, "Current Stock Quantity");
        int[] chartColors = new int[]{Color.parseColor("#3F51B5"), Color.parseColor("#4CAF50"), Color.parseColor("#FF9800"), Color.parseColor("#E91E63"), Color.parseColor("#00BCD4")};
        dataSet.setColors(chartColors);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);
        
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);
        data.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.US, "%.0f", value); // No decimals for quantity typically
            }
        });
        
        stockChart.setData(data);
        stockChart.setFitBars(true);
        stockChart.getDescription().setEnabled(false);
        stockChart.getLegend().setEnabled(true);
        stockChart.animateY(1500);
        
        XAxis xAxis = stockChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());
        
        stockChart.getAxisLeft().setDrawGridLines(true);
        stockChart.getAxisRight().setEnabled(false); // Hide right axis
        
        stockChart.invalidate();
    }
    private void loadCategoryChart() {
        pieChart.setVisibility(View.VISIBLE);
        List<PieEntry> entries = new ArrayList<>();
        Cursor cursor = databaseHelper.getCategoryWiseSales();

        trendDetails.clear();
        SimpleDateFormat inputFormat1 = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        SimpleDateFormat inputFormat2 = new SimpleDateFormat("dd-MM-yyyy", Locale.US); 
        
        Map<String, Float> categoryTotals = new HashMap<>();

        if (cursor != null) {
            int categoryIdx = cursor.getColumnIndexOrThrow("category");
            int totalIdx = cursor.getColumnIndexOrThrow("total");
            int dateIdx = cursor.getColumnIndexOrThrow("date");

            while (cursor.moveToNext()) {
                String category = cursor.getString(categoryIdx);
                float amount = cursor.getFloat(totalIdx);
                String dateStr = cursor.getString(dateIdx);
                
                Date date = null;
                try {
                    date = inputFormat1.parse(dateStr);
                } catch (ParseException e) {
                     try { date = inputFormat2.parse(dateStr); } catch (ParseException e2) {}
                }
                
                if (date != null) {
                    if (fromDate != null && date.before(fromDate)) continue;
                    if (toDate != null && date.after(toDate)) continue;
                }

                 // Handle null category
                if (category == null || category.isEmpty()) category = "Uncategorized";
                
                categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
            }
            cursor.close();
        }
        
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            trendDetails.add(new ReportDetailAdapter.ReportDetail(entry.getKey(), String.format(Locale.US, "₹%.2f", entry.getValue())));
        }
        trendAdapter.notifyDataSetChanged();
        rvTrendDetails.setVisibility(trendDetails.isEmpty() ? View.GONE : View.VISIBLE);

        PieDataSet dataSet = new PieDataSet(entries, "Sales Distribution");
        int[] chartColors = new int[]{Color.parseColor("#3F51B5"), Color.parseColor("#4CAF50"), Color.parseColor("#FF9800"), Color.parseColor("#E91E63"), Color.parseColor("#00BCD4")};
        dataSet.setColors(chartColors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);
        
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.US, "%.0f", value);
            }
        });
        
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Sales by\nCategory");
        pieChart.setCenterTextSize(16f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
        pieChart.getLegend().setWordWrapEnabled(true);
        pieChart.invalidate();
    }
    
    private void loadTrendChart(String type, String period) {
        boolean isBar = rgChartType.getCheckedRadioButtonId() == R.id.rbBar;
        
        if (isBar) barChart.setVisibility(View.VISIBLE);
        else lineChart.setVisibility(View.VISIBLE);

        android.content.SharedPreferences prefs = getSharedPreferences("TellyPrefs", MODE_PRIVATE);
        int companyId = prefs.getInt("selected_company_id", 0);
        List<DatabaseHelper.VoucherSummary> vouchers = databaseHelper.getAllVouchers(companyId);
        
        class AggData {
            Date date;
            String label;
            float amount;
            AggData(Date d, String l, float a) { date = d; label = l; amount = a; }
        }
        Map<String, AggData> tempMap = new HashMap<>(); 
        SimpleDateFormat inputFormat1 = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        SimpleDateFormat inputFormat2 = new SimpleDateFormat("dd-MM-yyyy", Locale.US); 
        
        SimpleDateFormat dailyFmt = new SimpleDateFormat("dd MMM", Locale.US);
        SimpleDateFormat monthlyFmt = new SimpleDateFormat("MMM yyyy", Locale.US);
        SimpleDateFormat yearlyFmt = new SimpleDateFormat("yyyy", Locale.US);

        for (DatabaseHelper.VoucherSummary v : vouchers) {
            if (type.contains("Sales") && !v.type.equals("Sales")) continue;
            if (type.contains("Purchase") && !v.type.equals("Purchase")) continue;

            Date date = null;
            try {
                date = inputFormat1.parse(v.date);
            } catch (ParseException e) {
                 try { date = inputFormat2.parse(v.date); } catch (ParseException e2) {}
            }
            
            if (date != null) {
                if (fromDate != null && date.before(fromDate)) continue;
                if (toDate != null && date.after(toDate)) continue;

                String key = "";
                if (period.equals("Daily")) key = dailyFmt.format(date);
                else if (period.equals("Monthly")) key = monthlyFmt.format(date);
                else key = yearlyFmt.format(date);
                
                if (tempMap.containsKey(key)) {
                    AggData ad = tempMap.get(key);
                    ad.amount += (float) v.amount;
                } else {
                    tempMap.put(key, new AggData(date, key, (float) v.amount));
                }
            }
        }
        
        List<AggData> aggList = new ArrayList<>(tempMap.values());
        Collections.sort(aggList, new Comparator<AggData>() {
            public int compare(AggData o1, AggData o2) {
                return o1.date.compareTo(o2.date);
            }
        });
        
        Map<String, Float> aggregatedData = new LinkedHashMap<>();
        for (AggData ad : aggList) aggregatedData.put(ad.label, ad.amount);

        trendDetails.clear();

        List<String> labels = new ArrayList<>();
        int i = 0;
        
        if (isBar) {
            List<BarEntry> entries = new ArrayList<>();
            for (Map.Entry<String, Float> entry : aggregatedData.entrySet()) {
                entries.add(new BarEntry(i, entry.getValue()));
                labels.add(entry.getKey());
                trendDetails.add(new ReportDetailAdapter.ReportDetail(entry.getKey(), String.format(Locale.US, "₹%.2f", entry.getValue())));
                i++;
            }
            trendAdapter.notifyDataSetChanged();
            rvTrendDetails.setVisibility(trendDetails.isEmpty() ? View.GONE : View.VISIBLE);
            
            BarDataSet dataSet = new BarDataSet(entries, type);
            // Use Telly Mobile Theme Colors
            if (type.contains("Sales")) {
                dataSet.setColor(Color.parseColor("#4CAF50")); // Green
            } else {
                dataSet.setColor(Color.parseColor("#F44336")); // Red for purchase
            }
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setValueTextSize(12f);
            
            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.6f); // Thinner bars look neater
            barChart.setData(barData);
            
            setupAxis(barChart.getXAxis(), labels);
            barChart.getAxisLeft().setDrawGridLines(true);
            barChart.getAxisRight().setEnabled(false);
            barChart.getDescription().setEnabled(false);
            barChart.animateY(1200);
            barChart.invalidate();
            
        } else {
            List<Entry> entries = new ArrayList<>();
            for (Map.Entry<String, Float> entry : aggregatedData.entrySet()) {
                entries.add(new Entry(i, entry.getValue()));
                labels.add(entry.getKey());
                trendDetails.add(new ReportDetailAdapter.ReportDetail(entry.getKey(), String.format(Locale.US, "₹%.2f", entry.getValue())));
                i++;
            }
            trendAdapter.notifyDataSetChanged();
            rvTrendDetails.setVisibility(trendDetails.isEmpty() ? View.GONE : View.VISIBLE);
            
            LineDataSet dataSet = new LineDataSet(entries, type);
            if (type.contains("Sales")) {
                dataSet.setColor(Color.parseColor("#4CAF50")); // Green
                dataSet.setFillColor(Color.parseColor("#4CAF50"));
            } else {
                dataSet.setColor(Color.parseColor("#F44336"));
                dataSet.setFillColor(Color.parseColor("#F44336"));
            }
            dataSet.setCircleColor(Color.RED);
            dataSet.setLineWidth(3f);
            dataSet.setCircleRadius(5f);
            dataSet.setDrawCircleHole(true);
            dataSet.setValueTextSize(12f);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smooth curves
            dataSet.setDrawFilled(true);
            dataSet.setFillAlpha(50);
            
            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            
            setupAxis(lineChart.getXAxis(), labels);
            lineChart.getAxisLeft().setDrawGridLines(true);
            lineChart.getAxisRight().setEnabled(false);
            lineChart.getDescription().setEnabled(false);
            lineChart.animateX(1200);
            lineChart.invalidate();
        }
    }
    
    private void setupAxis(XAxis xAxis, List<String> labels) {
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);
    }
}
