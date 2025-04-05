package com.example.campusexpensemanagerse06304;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

    // Then, update the setupCategorySpinner method to filter out categories that already have budgets
    private void setupCategorySpinner() {
        // Load categories from database
        categoryList = expenseDb.getAllCategories();
        Log.d(TAG, "Loaded " + categoryList.size() + " categories");

        // Create a filtered list of categories that don't already have budgets
        List<Category> availableCategories = new ArrayList<>();
        for (Category category : categoryList) {
            if (!categoryHasBudget(category.getId())) {
                availableCategories.add(category);
            }
        }

        // If there are no available categories, show a message
        if (availableCategories.isEmpty()) {
            // Disable the allocation section
            spinnerBudgetCategory.setEnabled(false);
            etCategoryBudgetAmount.setEnabled(false);
            btnSetCategoryBudget.setEnabled(false);

            // Create an adapter with a placeholder message
            List<Category> placeholderList = new ArrayList<>();
            Category placeholder = new Category();
            placeholder.setId(-1);
            placeholder.setName("All categories have budgets");
            placeholderList.add(placeholder);

            ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                    getContext(), android.R.layout.simple_spinner_item, placeholderList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerBudgetCategory.setAdapter(adapter);

            // Show a toast to explain
            Toast.makeText(getContext(),
                    "All categories already have budgets. You can adjust or delete existing budgets.",
                    Toast.LENGTH_LONG).show();
        } else {
            // Enable the allocation section if it was disabled
            spinnerBudgetCategory.setEnabled(true);
            etCategoryBudgetAmount.setEnabled(true);
            btnSetCategoryBudget.setEnabled(true);

            // Create adapter with available categories
            ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                    getContext(), android.R.layout.simple_spinner_item, availableCategories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerBudgetCategory.setAdapter(adapter);
        }

        // Set listener to get selected category
        spinnerBudgetCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = (Category) parent.getItemAtPosition(position);
                Log.d(TAG, "Selected category: " + selectedCategory.getName());

                // No need to show current budget since this category doesn't have one
                etCategoryBudgetAmount.setHint("Enter amount for " + selectedCategory.getName());
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

        // NEW CODE: Check if the new total budget is less than the sum of category budgets
        double totalCategoryBudgets = calculateTotalCategoryBudgetsAllocated();
        if (amount < totalCategoryBudgets) {
            // Show error message
            String errorMsg = "Total budget cannot be less than the sum of category budgets ($" +
                    String.format(Locale.getDefault(), "%.2f", totalCategoryBudgets) + ")";
            etTotalBudgetAmount.setError(errorMsg);
            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
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

    // NEW METHOD: Calculate total category budgets allocated
    private double calculateTotalCategoryBudgetsAllocated() {
        double total = 0;
        for (Budget budget : budgetList) {
            total += budget.getAmount();
        }
        Log.d(TAG, "Total category budgets allocated: $" + total);
        return total;
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

    // Update the saveCategoryBudget method to double-check
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveCategoryBudget() {
        if (userId == -1 || selectedCategory == null) {
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extra check to make sure the category doesn't already have a budget
        if (categoryHasBudget(selectedCategory.getId())) {
            Toast.makeText(getContext(),
                    "This category already has a budget. Please adjust the existing budget instead.",
                    Toast.LENGTH_LONG).show();
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

        // Get total budget
        double totalBudget = expenseDb.getTotalBudget(userId, "monthly");
        if (totalBudget <= 0) {
            Toast.makeText(getContext(), "Please set a total budget first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate how much is already allocated to other categories
        double otherCategoriesTotal = 0;
        double currentCategoryBudget = 0;
        for (Budget budget : budgetList) {
            if (budget.getCategoryId() == selectedCategory.getId()) {
                // Remember current budget for this category if it exists
                currentCategoryBudget = budget.getAmount();
            } else {
                // Sum up budgets for other categories
                otherCategoriesTotal += budget.getAmount();
            }
        }

        // Calculate available budget (total budget - other categories + current category)
        double availableBudget = totalBudget - otherCategoriesTotal;

        // If we're updating an existing category budget, add its current amount to available
        availableBudget += currentCategoryBudget;

        // Check if the new amount would exceed available budget
        if (amount > availableBudget) {
            String errorMsg = "Amount exceeds available budget ($" +
                    String.format(Locale.getDefault(), "%.2f", availableBudget) +
                    "). Please enter a lower amount or increase your total budget.";
            etCategoryBudgetAmount.setError(errorMsg);
            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
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
        double allocatedBudget = calculateTotalCategoryBudgetsAllocated();

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

    // Finally, update onResume to refresh the spinner when returning to this fragment
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        // Refresh budget data when fragment becomes visible
        loadTotalBudget();
        loadBudgets();
        // Refresh category spinner to filter out categories with budgets
        setupCategorySpinner();
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

    /**
     * Checks if a category already has a budget allocated
     * @param categoryId The category ID to check
     * @return true if the category already has a budget, false otherwise
     */
    private boolean categoryHasBudget(int categoryId) {
        for (Budget budget : budgetList) {
            if (budget.getCategoryId() == categoryId) {
                return true;
            }
        }
        return false;
    }


    // Add this method to SimpleBudgetFragment.java to allow selecting a specific category
    public boolean selectCategory(int categoryId) {
        try {
            Log.d(TAG, "Attempting to select category with ID: " + categoryId);

            // Make sure the fragment is attached to context
            if (!isAdded() || getContext() == null) {
                Log.e(TAG, "Fragment not attached to context");
                return false;
            }

            // Make sure the spinner has adapter
            if (spinnerBudgetCategory == null || spinnerBudgetCategory.getAdapter() == null) {
                Log.e(TAG, "Spinner or adapter is null");
                return false;
            }

            // First check if the category is already in the list
            boolean found = false;
            int positionToSelect = -1;

            for (int i = 0; i < spinnerBudgetCategory.getCount(); i++) {
                try {
                    Object item = spinnerBudgetCategory.getItemAtPosition(i);
                    if (item instanceof Category) {
                        Category category = (Category) item;
                        if (category.getId() == categoryId) {
                            positionToSelect = i;
                            found = true;
                            Log.d(TAG, "Found category at position " + i + ": " + category.getName());
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error checking category at position " + i, e);
                }
            }

            // If category found, select it
            if (found && positionToSelect >= 0) {
                spinnerBudgetCategory.setSelection(positionToSelect);

                // Focus on the amount field for easier input
                if (etCategoryBudgetAmount != null) {
                    etCategoryBudgetAmount.requestFocus();

                    // Show keyboard
                    try {
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.showSoftInput(etCategoryBudgetAmount, InputMethodManager.SHOW_IMPLICIT);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error showing keyboard", e);
                    }
                }

                return true;
            } else {
                // If category not found, it might already have a budget
                // Refresh the category list to make sure we have the latest data
                Log.d(TAG, "Category not found in spinner, refreshing data");
                loadBudgets(); // Refresh the budget list
                setupCategorySpinner(); // Refresh the spinner

                // Try one more time with refreshed data
                for (int i = 0; i < spinnerBudgetCategory.getCount(); i++) {
                    try {
                        Object item = spinnerBudgetCategory.getItemAtPosition(i);
                        if (item instanceof Category) {
                            Category category = (Category) item;
                            if (category.getId() == categoryId) {
                                spinnerBudgetCategory.setSelection(i);
                                if (etCategoryBudgetAmount != null) {
                                    etCategoryBudgetAmount.requestFocus();
                                }
                                Log.d(TAG, "Found category on second attempt");
                                return true;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error checking category at position " + i + " on second attempt", e);
                    }
                }

                // If we got here, category wasn't found even after refresh
                Log.e(TAG, "Category not found even after refresh");

                // Check if it already has a budget
                for (Budget budget : budgetList) {
                    if (budget.getCategoryId() == categoryId) {
                        // It already has a budget, show a message
                        Toast.makeText(getContext(),
                                "This category already has a budget. You can adjust it below.",
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                }

                // Otherwise, let the user know
                Toast.makeText(getContext(),
                        "Could not find the selected category. Please select one manually.",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in selectCategory", e);
            if (getContext() != null) {
                Toast.makeText(getContext(),
                        "Error selecting category: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    }


}