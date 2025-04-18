package com.example.campusexpensemanagerse06304;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
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
    private static final String TAG = "SimpleHistoryFragment";

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

        // Setup Generate Report button with enhanced functionality
        btnGenerateReport = view.findViewById(R.id.btnGenerateReport);
        btnGenerateReport.setOnClickListener(v -> showReportOptions());

// Make sure the button is visually prominent
        btnGenerateReport.setText("GENERATE REPORT");
        btnGenerateReport.setBackgroundResource(R.drawable.button_report);

        // Initialize database helper
        expenseDb = new ExpenseDb(getContext());

        // Get the current user ID from the activity
        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.getExtras() != null) {
                userId = intent.getExtras().getInt("ID_USER", -1);
                Log.d(TAG, "User ID loaded: " + userId);
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

        // Setup Generate Report button with enhanced functionality
        btnGenerateReport.setOnClickListener(v -> showReportOptions());

        // Load initial data
        filterExpenses();

        return view;
    }

// Add these methods to SimpleHistoryFragment.java to enhance report generation

    /**
     * Show dialog with report generation options and buttons to select format
     */
    private void showReportOptions() {
        if (filteredExpensesList.isEmpty()) {
            Toast.makeText(getContext(), "No expenses to generate report", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Generate Report");

        // Use a custom layout for the dialog
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_report_options, null);
        builder.setView(dialogView);

        // Find buttons in the custom layout
        Button btnCsvReport = dialogView.findViewById(R.id.btnCsvReport);
        Button btnPdfReport = dialogView.findViewById(R.id.btnPdfReport);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelReport);

        // Create dialog
        final AlertDialog dialog = builder.create();

        // Set button click listeners
        btnCsvReport.setOnClickListener(v -> {
            generateAndShareReport("csv");
            dialog.dismiss();
        });

        btnPdfReport.setOnClickListener(v -> {
            generateAndShareReport("pdf");
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Generate and share the report with progress indication
     * @param format Report format (csv or pdf)
     */
    private void generateAndShareReport(String format) {
        if (userId == -1) {
            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String startDateStr = tvStartDate.getText().toString();
        String endDateStr = tvEndDate.getText().toString();

        // Show progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Generating Report");
        builder.setMessage("Please wait while your report is being generated...");
        builder.setCancelable(false);

        final AlertDialog progressDialog = builder.create();
        progressDialog.show();

        // Use a background thread for report generation
        new Thread(() -> {
            ReportGenerator reportGenerator = new ReportGenerator(getContext());
            String filePath;

            if ("csv".equals(format)) {
                filePath = reportGenerator.generateCSVReport(userId, startDateStr, endDateStr);
            } else {
                filePath = reportGenerator.generatePDFReport(userId, startDateStr, endDateStr);
            }

            // Update UI on main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();

                    if (filePath != null) {
                        // Show success message with file details
                        AlertDialog.Builder resultBuilder = new AlertDialog.Builder(getContext());
                        resultBuilder.setTitle("Report Generated Successfully");

                        String fileType = format.toUpperCase();
                        String message = "Your " + fileType + " report has been created with the following details:\n\n" +
                                "• Date Range: " + startDateStr + " to " + endDateStr + "\n" +
                                "• Total Transactions: " + filteredExpensesList.size() + "\n" +
                                "• Categories Included: " + getCategoryCount() + "\n\n" +
                                "Would you like to share this report now?";

                        resultBuilder.setMessage(message);
                        resultBuilder.setPositiveButton("Share", (dialog, which) -> {
                            reportGenerator.shareReport(filePath);
                        });
                        resultBuilder.setNegativeButton("Done", (dialog, which) -> dialog.dismiss());
                        resultBuilder.show();
                    } else {
                        Toast.makeText(getContext(),
                                "Failed to generate report. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    /**
     * Count the number of unique categories in the filtered expenses
     */
    private int getCategoryCount() {
        if (filteredExpensesList.isEmpty()) return 0;

        // Use a Set to count unique category IDs
        java.util.Set<Integer> uniqueCategories = new java.util.HashSet<>();
        for (Expense expense : filteredExpensesList) {
            uniqueCategories.add(expense.getCategoryId());
        }

        return uniqueCategories.size();
    }

// Replace the existing showReportOptions() and generateAndShareReport() methods
// with these enhanced versions in your SimpleHistoryFragment class.

    private void loadCategories() {
        categoryList = expenseDb.getAllCategories();
        Log.d(TAG, "Loaded " + categoryList.size() + " categories");

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
                Log.d(TAG, "Selected category: " + selectedCategory.getName() + " (ID: " + selectedCategoryId + ")");
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
            Log.d(TAG, "Total filtered amount: $" + totalAmount);

            // Update UI based on results
            if (filteredExpenses.isEmpty()) {
                tvNoExpenses.setVisibility(View.VISIBLE);
                recyclerHistory.setVisibility(View.GONE);
                Log.d(TAG, "No expenses match the filters");
            } else {
                tvNoExpenses.setVisibility(View.GONE);
                recyclerHistory.setVisibility(View.VISIBLE);
                Log.d(TAG, "Found " + filteredExpenses.size() + " expenses matching filters");

                // Update adapter
                filteredExpensesList.clear();
                filteredExpensesList.addAll(filteredExpenses);
                expenseAdapter.notifyDataSetChanged();
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing dates", e);
            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        filterExpenses();
    }
}