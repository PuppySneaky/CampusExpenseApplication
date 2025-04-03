package com.example.campusexpensemanagerse06304;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campusexpensemanagerse06304.adapter.ExpenseAdapter;
import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.example.campusexpensemanagerse06304.model.Category;
import com.example.campusexpensemanagerse06304.model.Expense;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private View rootView;
    private TextView tvStartDate, tvEndDate, tvTotalAmount, tvNoHistory;
    private Spinner spinnerCategory, spinnerSortBy;
    private Button btnApplyFilter, btnGenerateReport;
    private RecyclerView recyclerHistory;
    private BarChart barChart;
    private ExpenseAdapter historyAdapter;
    private List<Expense> filteredExpensesList;
    private List<Category> categoryList;
    private ExpenseDb expenseDb;

    private int userId;
    private Calendar startDate, endDate;
    private SimpleDateFormat dateFormat;
    private int selectedCategoryId = -1; // -1 means all categories

    private String[] sortOptions = {"Date (Newest)", "Date (Oldest)", "Amount (Highest)", "Amount (Lowest)"};

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Initialize database helper
        expenseDb = new ExpenseDb(getContext());

        // Get the current user ID from the activity
        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.getExtras() != null) {
                userId = intent.getExtras().getInt("ID_USER", -1);
            }
        }

        // Initialize date formatter and default date range (last 30 days)
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        endDate = Calendar.getInstance();
        startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1); // Default to one month back
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_history, container, false);

        // Initialize views
        tvStartDate = rootView.findViewById(R.id.tvHistoryStartDate);
        tvEndDate = rootView.findViewById(R.id.tvHistoryEndDate);
        tvTotalAmount = rootView.findViewById(R.id.tvHistoryTotalAmount);
        tvNoHistory = rootView.findViewById(R.id.tvNoHistoryData);
        spinnerCategory = rootView.findViewById(R.id.spinnerHistoryCategory);
        spinnerSortBy = rootView.findViewById(R.id.spinnerHistorySortBy);
        btnApplyFilter = rootView.findViewById(R.id.btnApplyHistoryFilter);
        btnGenerateReport = rootView.findViewById(R.id.btnGenerateReport);
        recyclerHistory = rootView.findViewById(R.id.recyclerHistory);
        barChart = rootView.findViewById(R.id.barChartHistory);

        // Setup recycler view
        recyclerHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        filteredExpensesList = new ArrayList<>();
        historyAdapter = new ExpenseAdapter(getContext(), filteredExpensesList);
        recyclerHistory.setAdapter(historyAdapter);

        // Initialize the date displays
        tvStartDate.setText(dateFormat.format(startDate.getTime()));
        tvEndDate.setText(dateFormat.format(endDate.getTime()));

        // Setup date pickers
        tvStartDate.setOnClickListener(v -> showStartDatePicker());
        tvEndDate.setOnClickListener(v -> showEndDatePicker());

        // Setup category spinner
        setupCategorySpinner();

        // Setup sort spinner
        setupSortBySpinner();

        // Setup filter button
        btnApplyFilter.setOnClickListener(v -> applyFilters());

        // Setup report generation button
        btnGenerateReport.setOnClickListener(v -> generateReport());

        // Setup bar chart
        setupBarChart();

        // Apply initial filters to load data
        applyFilters();

        return rootView;
    }

    private void setupCategorySpinner() {
        // Load categories from the database
        categoryList = expenseDb.getAllCategories();

        // Add "All Categories" option
        List<Category> spinnerCategories = new ArrayList<>();
        Category allCategory = new Category();
        allCategory.setId(-1);
        allCategory.setName("All Categories");
        spinnerCategories.add(allCategory);
        spinnerCategories.addAll(categoryList);

        // Create adapter for the spinner
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, spinnerCategories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCategory.setAdapter(categoryAdapter);

        // Set listener to update selected category
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category selected = (Category) parent.getItemAtPosition(position);
                selectedCategoryId = selected.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = -1; // All categories
            }
        });
    }

    private void setupSortBySpinner() {
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerSortBy.setAdapter(sortAdapter);
    }

    private void showStartDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    startDate.set(Calendar.YEAR, year);
                    startDate.set(Calendar.MONTH, month);
                    startDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    tvStartDate.setText(dateFormat.format(startDate.getTime()));
                },
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showEndDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    endDate.set(Calendar.YEAR, year);
                    endDate.set(Calendar.MONTH, month);
                    endDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    tvEndDate.setText(dateFormat.format(endDate.getTime()));
                },
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(true);

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
    }

    private void applyFilters() {
        if (userId == -1) return;

        // Get all expenses
        List<Expense> allExpenses = expenseDb.getExpensesByUser(userId);
        List<Expense> filteredExpenses = new ArrayList<>();

        // Filter by date range and category
        String startDateStr = tvStartDate.getText().toString();
        String endDateStr = tvEndDate.getText().toString();

        try {
            Date filterStartDate = dateFormat.parse(startDateStr);
            Date filterEndDate = dateFormat.parse(endDateStr);

            for (Expense expense : allExpenses) {
                // Check date range
                if (expense.getDate().after(filterStartDate) &&
                        expense.getDate().before(filterEndDate) ||
                        expense.getDate().equals(filterStartDate) ||
                        expense.getDate().equals(filterEndDate)) {

                    // Check category filter
                    if (selectedCategoryId == -1 || expense.getCategoryId() == selectedCategoryId) {
                        // Add category information
                        for (Category category : categoryList) {
                            if (category.getId() == expense.getCategoryId()) {
                                expense.setCategoryName(category.getName());
                                expense.setCategoryColor(category.getColor());
                                break;
                            }
                        }

                        filteredExpenses.add(expense);
                    }
                }
            }

            // Apply sorting
            int sortOption = spinnerSortBy.getSelectedItemPosition();
            sortExpenses(filteredExpenses, sortOption);

            // Update the adapter
            filteredExpensesList.clear();
            filteredExpensesList.addAll(filteredExpenses);
            historyAdapter.notifyDataSetChanged();

            // Update total amount
            double total = 0;
            for (Expense expense : filteredExpenses) {
                total += expense.getAmount();
            }
            tvTotalAmount.setText(String.format(Locale.getDefault(), "$%.2f", total));

            // Show/hide no data message
            if (filteredExpenses.isEmpty()) {
                tvNoHistory.setVisibility(View.VISIBLE);
                recyclerHistory.setVisibility(View.GONE);
                barChart.setVisibility(View.GONE);
            } else {
                tvNoHistory.setVisibility(View.GONE);
                recyclerHistory.setVisibility(View.VISIBLE);

                // Update the bar chart
                updateBarChart(filteredExpenses);
            }

        } catch (ParseException e) {
            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
        }
    }

    private void sortExpenses(List<Expense> expenses, int sortOption) {
        switch (sortOption) {
            case 0: // Date (Newest)
                Collections.sort(expenses, (e1, e2) -> e2.getDate().compareTo(e1.getDate()));
                break;
            case 1: // Date (Oldest)
                Collections.sort(expenses, (e1, e2) -> e1.getDate().compareTo(e2.getDate()));
                break;
            case 2: // Amount (Highest)
                Collections.sort(expenses, (e1, e2) -> Double.compare(e2.getAmount(), e1.getAmount()));
                break;
            case 3: // Amount (Lowest)
                Collections.sort(expenses, (e1, e2) -> Double.compare(e1.getAmount(), e2.getAmount()));
                break;
        }
    }

    private void updateBarChart(List<Expense> expenses) {
        // Group expenses by category
        Map<Integer, Double> categoryTotals = new HashMap<>();
        Map<Integer, String> categoryNames = new HashMap<>();

        for (Expense expense : expenses) {
            int categoryId = expense.getCategoryId();
            double amount = expense.getAmount();

            if (categoryTotals.containsKey(categoryId)) {
                double currentTotal = categoryTotals.get(categoryId);
                categoryTotals.put(categoryId, currentTotal + amount);
            } else {
                categoryTotals.put(categoryId, amount);
            }

            if (!categoryNames.containsKey(categoryId)) {
                categoryNames.put(categoryId, expense.getCategoryName());
            }
        }

        // Convert to bar entries
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int index = 0;

        for (Map.Entry<Integer, Double> entry : categoryTotals.entrySet()) {
            int categoryId = entry.getKey();
            double amount = entry.getValue();

            entries.add(new BarEntry(index, (float) amount));
            labels.add(categoryNames.get(categoryId));
            index++;
        }

        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Expense Categories");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setDrawValues(true);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        // Update chart
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelRotationAngle(45);
        barChart.setVisibility(View.VISIBLE);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void generateReport() {
        // In a real app, this would generate a PDF or sharable report
        // For this demo, we'll just show a toast
        Toast.makeText(getContext(), "Report generation functionality would be implemented here", Toast.LENGTH_SHORT).show();

        // Basic report info that could be included:
        String startDateStr = tvStartDate.getText().toString();
        String endDateStr = tvEndDate.getText().toString();
        String totalAmount = tvTotalAmount.getText().toString();

        String reportInfo = "Report Period: " + startDateStr + " to " + endDateStr + "\n";
        reportInfo += "Total Expenses: " + totalAmount + "\n";
        reportInfo += "Number of Transactions: " + filteredExpensesList.size();

        Toast.makeText(getContext(), reportInfo, Toast.LENGTH_LONG).show();
    }
}