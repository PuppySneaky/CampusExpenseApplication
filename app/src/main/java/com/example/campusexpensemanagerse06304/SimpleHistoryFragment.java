package com.example.campusexpensemanagerse06304;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagerse06304.adapter.SimpleExpenseAdapter;
import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.example.campusexpensemanagerse06304.model.Category;
import com.example.campusexpensemanagerse06304.model.Expense;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SimpleHistoryFragment extends Fragment {
    private TextView tvStartDate, tvEndDate, tvTotalAmount, tvNoExpenses;
    private Spinner spinnerHistoryCategory;
    private Button btnApplyFilter, btnGenerateReport;
    private RecyclerView recyclerHistory;
    private SimpleExpenseAdapter expenseAdapter;
    private List<Expense> filteredExpensesList;
    private List<Category> categoryList;
    private ExpenseDb expenseDb;
    private int userId = -1;
    private int selectedCategoryId = -1; // -1 means all categories
    private Calendar startDate, endDate;
    private SimpleDateFormat dateFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_history, container, false);

        // Initialize views
        tvStartDate = view.findViewById(R.id.tvStartDate);
        tvEndDate = view.findViewById(R.id.tvEndDate);
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvNoExpenses = view.findViewById(R.id.tvNoExpenses);
        spinnerHistoryCategory = view.findViewById(R.id.spinnerHistoryCategory);
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        btnGenerateReport = view.findViewById(R.id.btnGenerateReport);
        recyclerHistory = view.findViewById(R.id.recyclerHistory);

        // Initialize database helper
        expenseDb = new ExpenseDb(getContext());

        // Get the current user ID from the activity
        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.getExtras() != null) {
                userId = intent.getExtras().getInt("ID_USER", -1);
            }
        }

        // Initialize date range (default to current month)
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        startDate = Calendar.getInstance();
        startDate.set(Calendar.DAY_OF_MONTH, 1); // First day of current month
        endDate = Calendar.getInstance(); // Current date

        // Set initial date display
        tvStartDate.setText(dateFormat.format(startDate.getTime()));
        tvEndDate.setText(dateFormat.format(endDate.getTime()));

        // Load categories for spinner
        loadCategories();

        // Setup RecyclerView
        filteredExpensesList = new ArrayList<>();
        expenseAdapter = new SimpleExpenseAdapter(getContext(), filteredExpensesList);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerHistory.setAdapter(expenseAdapter);

        // Setup date picker dialogs
        tvStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        tvEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        // Setup Apply Filter button
        btnApplyFilter.setOnClickListener(v -> filterExpenses());

        // Setup Generate Report button
        btnGenerateReport.setOnClickListener(v -> generateReport());

        // Load initial data
        filterExpenses();

        return view;
    }

    private void loadCategories() {
        categoryList = expenseDb.getAllCategories();

        // Create a list with "All Categories" option
        List<Category> spinnerCategories = new ArrayList<>();

        // Add "All Categories" option
        Category allCategory = new Category();
        allCategory.setId(-1);
        allCategory.setName("All Categories");
        spinnerCategories.add(allCategory);

        // Add actual categories
        spinnerCategories.addAll(categoryList);

        // Create adapter for spinner
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, spinnerCategories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerHistoryCategory.setAdapter(adapter);

        // Set listener
        spinnerHistoryCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Category selectedCategory = (Category) parent.getItemAtPosition(position);
                selectedCategoryId = selectedCategory.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = -1; // All categories
            }
        });
    }

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDate : endDate;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    if (isStartDate) {
                        tvStartDate.setText(dateFormat.format(calendar.getTime()));
                    } else {
                        tvEndDate.setText(dateFormat.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void filterExpenses() {
        if (userId == -1) return;

        try {
            // Parse selected dates
            Date start = dateFormat.parse(tvStartDate.getText().toString());
            Date end = dateFormat.parse(tvEndDate.getText().toString());

            // Validate date range
            if (start != null && end != null && start.after(end)) {
                Toast.makeText(getContext(), "Start date cannot be after end date", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get all expenses from database
            List<Expense> allExpenses = expenseDb.getExpensesByUser(userId);
            List<Expense> filteredExpenses = new ArrayList<>();
            double totalAmount = 0;

            // Filter by date range and category
            for (Expense expense : allExpenses) {
                // Check date range
                boolean dateMatches = expense.getDate() != null &&
                        (start == null || !expense.getDate().before(start)) &&
                        (end == null || !expense.getDate().after(end));

                // Check category
                boolean categoryMatches = selectedCategoryId == -1 || expense.getCategoryId() == selectedCategoryId;

                if (dateMatches && categoryMatches) {
                    // Add category info
                    for (Category category : categoryList) {
                        if (category.getId() == expense.getCategoryId()) {
                            expense.setCategoryName(category.getName());
                            expense.setCategoryColor(category.getColor());
                            break;
                        }
                    }

                    filteredExpenses.add(expense);
                    totalAmount += expense.getAmount();
                }
            }

            // Update total amount
            tvTotalAmount.setText(String.format(Locale.getDefault(), "$%.2f", totalAmount));

            // Update UI based on results
            if (filteredExpenses.isEmpty()) {
                tvNoExpenses.setVisibility(View.VISIBLE);
                recyclerHistory.setVisibility(View.GONE);
            } else {
                tvNoExpenses.setVisibility(View.GONE);
                recyclerHistory.setVisibility(View.VISIBLE);

                // Update adapter
                filteredExpensesList.clear();
                filteredExpensesList.addAll(filteredExpenses);
                expenseAdapter.notifyDataSetChanged();
            }
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateReport() {
        // In a real app, you might generate a PDF or CSV file here
        // For now, just show a summary toast

        String startDateStr = tvStartDate.getText().toString();
        String endDateStr = tvEndDate.getText().toString();
        String totalAmountStr = tvTotalAmount.getText().toString();
        int expenseCount = filteredExpensesList.size();

        // Get category name if filtering by category
        String categoryName = "All Categories";
        if (selectedCategoryId != -1) {
            for (Category category : categoryList) {
                if (category.getId() == selectedCategoryId) {
                    categoryName = category.getName();
                    break;
                }
            }
        }

        String reportSummary = "Expense Report\n" +
                "Period: " + startDateStr + " to " + endDateStr + "\n" +
                "Category: " + categoryName + "\n" +
                "Total Amount: " + totalAmountStr + "\n" +
                "Number of Expenses: " + expenseCount;

        Toast.makeText(getContext(), "Report Generated!\n\n" + reportSummary, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadCategories();
        filterExpenses();
    }
}