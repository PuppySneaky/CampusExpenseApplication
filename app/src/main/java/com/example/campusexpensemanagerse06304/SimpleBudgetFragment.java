package com.example.campusexpensemanagerse06304;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagerse06304.adapter.SimpleBudgetAdapter;
import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.example.campusexpensemanagerse06304.model.Budget;
import com.example.campusexpensemanagerse06304.model.Category;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SimpleBudgetFragment extends Fragment implements RefreshableFragment {
    private static final String TAG = "SimpleBudgetFragment";

    // Total budget views
    private EditText etTotalBudgetAmount;
    private Button btnSetTotalBudget;
    private TextView tvCurrentTotalBudget, tvRemainingTotalBudget;

    // Category budget views
    private Spinner spinnerBudgetCategory;
    private EditText etCategoryBudgetAmount;
    private Button btnSetCategoryBudget;

    // Budget list views
    private TextView tvNoBudgets;
    private RecyclerView recyclerBudgets;

    // Data
    private SimpleBudgetAdapter budgetAdapter;
    private List<Budget> budgetList;
    private List<Category> categoryList;
    private ExpenseDb expenseDb;
    private int userId = -1;
    private Category selectedCategory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_budget, container, false);

        // Initialize total budget views
        etTotalBudgetAmount = view.findViewById(R.id.etTotalBudgetAmount);
        btnSetTotalBudget = view.findViewById(R.id.btnSetTotalBudget);
        tvCurrentTotalBudget = view.findViewById(R.id.tvCurrentTotalBudget);
        tvRemainingTotalBudget = view.findViewById(R.id.tvRemainingTotalBudget);

        // Initialize category budget views
        spinnerBudgetCategory = view.findViewById(R.id.spinnerBudgetCategory);
        etCategoryBudgetAmount = view.findViewById(R.id.etCategoryBudgetAmount);
        btnSetCategoryBudget = view.findViewById(R.id.btnSetCategoryBudget);

        // Initialize budget list views
        tvNoBudgets = view.findViewById(R.id.tvNoBudgets);
        recyclerBudgets = view.findViewById(R.id.recyclerBudgets);

        // Initialize database helper
        expenseDb = new ExpenseDb(getContext());

        // Get the current user ID from the activity
        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.getExtras() != null) {
                userId = intent.getExtras().getInt("ID_USER", -1);
                Log.d(TAG, "User ID: " + userId);
            }
        }

        // Setup RecyclerView
        budgetList = new ArrayList<>();
        budgetAdapter = new SimpleBudgetAdapter(getContext(), budgetList);
        recyclerBudgets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerBudgets.setAdapter(budgetAdapter);

        // Setup category spinner
        setupCategorySpinner();

        // Setup Total Budget button
        btnSetTotalBudget.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                saveTotalBudget();
            } else {
                Toast.makeText(getContext(), "This feature requires Android 8.0 or higher", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup Category Budget button
        btnSetCategoryBudget.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                saveCategoryBudget();
            } else {
                Toast.makeText(getContext(), "This feature requires Android 8.0 or higher", Toast.LENGTH_SHORT).show();
            }
        });

        // Load total budget and category budgets
        loadTotalBudget();
        loadBudgets();

        return view;
    }

    private void setupCategorySpinner() {
        // Load categories from database
        categoryList = expenseDb.getAllCategories();
        Log.d(TAG, "Loaded " + categoryList.size() + " categories");

        // Create adapter for spinner
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerBudgetCategory.setAdapter(adapter);

        // Set listener to get selected category
        spinnerBudgetCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = (Category) parent.getItemAtPosition(position);
                Log.d(TAG, "Selected category: " + selectedCategory.getName());

                // Show current budget for this category if exists
                showCurrentCategoryBudget(selectedCategory.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = null;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveTotalBudget() {
        if (userId == -1) {
            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate input
        if (etTotalBudgetAmount.getText().toString().trim().isEmpty()) {
            etTotalBudgetAmount.setError("Please enter a budget amount");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(etTotalBudgetAmount.getText().toString());
            if (amount <= 0) {
                etTotalBudgetAmount.setError("Amount must be greater than zero");
                return;
            }
        } catch (NumberFormatException e) {
            etTotalBudgetAmount.setError("Invalid amount format");
            return;
        }

        // Get first day of current month
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = dateFormat.format(calendar.getTime());

        // Get last day of current month
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String endDate = dateFormat.format(calendar.getTime());

        // Save to database
        long result = expenseDb.insertOrUpdateTotalBudget(userId, amount, "monthly", startDate, endDate);

        if (result > 0) {
            Toast.makeText(getContext(), "Total budget set successfully", Toast.LENGTH_SHORT).show();

            // Clear input field
            etTotalBudgetAmount.setText("");

            // Refresh data
            loadTotalBudget();

            // Check if category budgets need adjustment
            checkCategoryBudgetsAgainstTotal();
        } else {
            Toast.makeText(getContext(), "Failed to set total budget", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTotalBudget() {
        if (userId == -1 || getContext() == null) {
            return;
        }

        // Get total budget amount
        double totalBudget = expenseDb.getTotalBudget(userId, "monthly");

        // Get remaining budget
        double remainingBudget = expenseDb.getRemainingTotalBudget(userId);

        // Update UI
        tvCurrentTotalBudget.setText(String.format(Locale.getDefault(), "$%.2f", totalBudget));
        tvRemainingTotalBudget.setText(String.format(Locale.getDefault(), "$%.2f", remainingBudget));

        // Set hint in the input field
        etTotalBudgetAmount.setHint("Enter amount (current: $" + String.format(Locale.getDefault(), "%.2f", totalBudget) + ")");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveCategoryBudget() {
        if (userId == -1 || selectedCategory == null) {
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate input
        if (etCategoryBudgetAmount.getText().toString().trim().isEmpty()) {
            etCategoryBudgetAmount.setError("Please enter a budget amount");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(etCategoryBudgetAmount.getText().toString());
            if (amount <= 0) {
                etCategoryBudgetAmount.setError("Amount must be greater than zero");
                return;
            }
        } catch (NumberFormatException e) {
            etCategoryBudgetAmount.setError("Invalid amount format");
            return;
        }

        // Check if this would exceed the total budget
        if (!expenseDb.validateCategoryBudget(userId, amount)) {
            Toast.makeText(getContext(), "This would exceed your total budget. Please set a lower amount or increase your total budget.", Toast.LENGTH_LONG).show();
            return;
        }

        // Get first day of current month
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = dateFormat.format(calendar.getTime());

        // Get last day of current month
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String endDate = dateFormat.format(calendar.getTime());

        // Save to database using existing method
        long result = expenseDb.updateOrInsertBudget(userId, selectedCategory.getId(), amount, "monthly", startDate, endDate);

        if (result > 0) {
            Toast.makeText(getContext(), "Category budget set successfully", Toast.LENGTH_SHORT).show();

            // Clear input field
            etCategoryBudgetAmount.setText("");

            // Refresh data
            loadBudgets();
            loadTotalBudget(); // Refresh total budget display too
        } else {
            Toast.makeText(getContext(), "Failed to set category budget", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCurrentCategoryBudget(int categoryId) {
        if (userId == -1 || getContext() == null) {
            return;
        }

        // Find the budget for this category
        for (Budget budget : budgetList) {
            if (budget.getCategoryId() == categoryId) {
                // Found existing budget, show it in the input field
                etCategoryBudgetAmount.setHint("Enter amount (current: $" +
                        String.format(Locale.getDefault(), "%.2f", budget.getAmount()) + ")");
                return;
            }
        }

        // No budget found for this category
        etCategoryBudgetAmount.setHint("Enter amount (no current budget)");
    }

    private void checkCategoryBudgetsAgainstTotal() {
        double totalBudget = expenseDb.getTotalBudget(userId, "monthly");
        double allocatedBudget = 0;

        // Calculate total allocated to categories
        for (Budget budget : budgetList) {
            allocatedBudget += budget.getAmount();
        }

        if (allocatedBudget > totalBudget) {
            // Alert user that category budgets exceed total
            Toast.makeText(getContext(),
                    "Warning: Your category budgets ($" + String.format(Locale.getDefault(), "%.2f", allocatedBudget) +
                            ") exceed your total budget ($" + String.format(Locale.getDefault(), "%.2f", totalBudget) +
                            "). Please adjust your category allocations.",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void loadBudgets() {
        if (userId == -1 || getContext() == null) {
            Log.d(TAG, "Cannot load budgets - userId is -1 or context is null");
            return;
        }

        Log.d(TAG, "Loading budgets for user " + userId);

        // Get budgets from database
        List<Budget> budgets = expenseDb.getBudgetsByUser(userId);
        Log.d(TAG, "Found " + budgets.size() + " budgets");

        // Update UI based on results
        if (budgets.isEmpty()) {
            tvNoBudgets.setVisibility(View.VISIBLE);
            recyclerBudgets.setVisibility(View.GONE);
        } else {
            tvNoBudgets.setVisibility(View.GONE);
            recyclerBudgets.setVisibility(View.VISIBLE);

            // Get current month in format YYYY-MM
            Calendar cal = Calendar.getInstance();
            String currentMonth = String.format(Locale.getDefault(), "%d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);

            Log.d(TAG, "Current month: " + currentMonth);

            // Add spending information to each budget
            for (Budget budget : budgets) {
                // Get category name
                for (Category category : categoryList) {
                    if (category.getId() == budget.getCategoryId()) {
                        budget.setCategoryName(category.getName());
                        budget.setCategoryColor(category.getColor());
                        break;
                    }
                }

                // Get spent amount for this category
                double spent = expenseDb.getTotalExpensesByCategoryAndMonth(
                        userId, budget.getCategoryId(), currentMonth);
                budget.setSpent(spent);

                Log.d(TAG, "Budget: " + budget.getId() + " - Category: " + budget.getCategoryName() +
                        " - Amount: " + budget.getAmount() + " - Spent: " + spent);
            }

            // Update adapter with new data
            budgetList.clear();
            budgetList.addAll(budgets);
            budgetAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void refreshData() {
        Log.d(TAG, "refreshData called");
        if (isAdded() && getContext() != null) {
            loadTotalBudget();
            loadBudgets();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        // Refresh budget data when fragment becomes visible
        loadTotalBudget();
        loadBudgets();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isAdded()) {
            Log.d(TAG, "setUserVisibleHint: visible and refreshing");
            loadTotalBudget();
            loadBudgets();
        }
    }
}