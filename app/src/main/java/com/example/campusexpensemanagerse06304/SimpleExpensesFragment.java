package com.example.campusexpensemanagerse06304;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.example.campusexpensemanagerse06304.adapter.SimpleExpenseAdapter;
import com.example.campusexpensemanagerse06304.adapter.ViewPagerAdapter;
import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.example.campusexpensemanagerse06304.model.Budget;
import com.example.campusexpensemanagerse06304.model.Category;
import com.example.campusexpensemanagerse06304.model.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SimpleExpensesFragment extends Fragment implements RefreshableFragment {
    private static final String TAG = "SimpleExpensesFragment";

    private EditText etAmount, etDescription;
    private Spinner spinnerCategory;
    private Button btnAddExpense;
    private TextView tvNoExpenses;
    private RecyclerView recyclerExpenses;
    private SimpleExpenseAdapter expenseAdapter;
    private List<Expense> expenseList;
    private List<Category> categoryList;
    private ExpenseDb expenseDb;
    private int userId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_expense, container, false);

        etAmount = view.findViewById(R.id.etExpenseAmount);
        etDescription = view.findViewById(R.id.etExpenseDescription);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        btnAddExpense = view.findViewById(R.id.btnAddExpense);
        tvNoExpenses = view.findViewById(R.id.tvNoExpenses);
        recyclerExpenses = view.findViewById(R.id.recyclerExpenses);

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

        // Load categories for spinner
        loadCategories();

        // Setup RecyclerView
        expenseList = new ArrayList<>();
        expenseAdapter = new SimpleExpenseAdapter(getContext(), expenseList);
        recyclerExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerExpenses.setAdapter(expenseAdapter);

        // Load expenses
        loadExpenses();

        // Setup Add Expense button
        btnAddExpense.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                saveExpense();
            } else {
                Toast.makeText(getContext(), "This feature requires Android 8.0 or higher", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadCategories() {
        categoryList = expenseDb.getAllCategories();
        Log.d(TAG, "Loaded " + categoryList.size() + " categories");

        // Create adapter for spinner
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCategory.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveExpense() {
        // Validate input fields
        if (etAmount.getText().toString().trim().isEmpty()) {
            etAmount.setError("Please enter an amount");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(etAmount.getText().toString());
            if (amount <= 0) {
                etAmount.setError("Amount must be greater than zero");
                return;
            }
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount format");
            return;
        }

        String description = etDescription.getText().toString().trim();
        if (description.isEmpty()) {
            description = "Expense"; // Default description
        }

        // Get selected category
        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        int categoryId = selectedCategory.getId();
        Log.d(TAG, "Selected category: " + selectedCategory.getName() + " (ID: " + categoryId + ")");


        // Check if this category has a budget
        if (!categoryHasBudget(categoryId)) {
            // Show dialog to prompt user to set up a budget first
            String finalDescription = description;
            new AlertDialog.Builder(getContext())
                    .setTitle("Budget Required")
                    .setMessage("This category doesn't have a budget yet. Would you like to set one up first?")
                    .setPositiveButton("Set Budget", (dialog, which) -> {
                        // Navigate to budget screen
                        if (getActivity() instanceof MenuActivity) {
                            MenuActivity activity = (MenuActivity) getActivity();

                            // Switch to budget tab (index 2)
                            activity.viewPager2.setCurrentItem(2);

                            // Allow layout to be drawn first, then update spinner selection
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                Fragment budgetFragment = activity.getViewPagerAdapter().getFragment(2);
                                if (budgetFragment instanceof SimpleBudgetFragment) {
                                    ((SimpleBudgetFragment) budgetFragment).selectCategory(categoryId);
                                }
                            }, 300);
                        }
                    })
                    .setNegativeButton("Continue Anyway", (dialog, which) -> {
                        // Proceed with saving expense
                        proceedWithSavingExpense(categoryId, amount, finalDescription);
                    })
                    .show();
            return;
        }


        // NEW CODE: Check if this expense would exceed the category budget
        if (!expenseDb.checkCategoryBudgetBalance(userId, categoryId, amount)) {
            // Show an error dialog with more details
            showBudgetExceededDialog(selectedCategory.getName(), amount);
            return;
        }

        // Get current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        // Save to database with selected category
        long result = expenseDb.insertExpense(userId, categoryId, amount, description, currentDate, "Cash", false, null);

        if (result != -1) {
            Log.d(TAG, "Expense added successfully with ID: " + result);
            Toast.makeText(getContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
            // Clear input fields
            etAmount.setText("");
            etDescription.setText("");

            // Refresh the expense list
            loadExpenses();

            // Force update budget information with a small delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                updateBudgetFragment();
                updateHomeFragment();
            }, 500);
        } else {
            Log.e(TAG, "Failed to add expense");
            Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show();
        }

        proceedWithSavingExpense(categoryId, amount, description);
    }

    // Add this new method to show a detailed budget exceeded dialog
    private void showBudgetExceededDialog(String categoryName, double expenseAmount) {
        if (getContext() == null) return;

        // Get remaining budget for this category
        int categoryId = ((Category) spinnerCategory.getSelectedItem()).getId();
        double remainingBudget = expenseDb.getRemainingCategoryBudget(userId, categoryId);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Budget Limit Exceeded");
        builder.setMessage("Adding this expense of $" + String.format(Locale.getDefault(), "%.2f", expenseAmount) +
                " would exceed your budget for " + categoryName + ".\n\n" +
                "Remaining budget: $" + String.format(Locale.getDefault(), "%.2f", remainingBudget) + "\n\n" +
                "Would you like to increase your budget for this category?");

        // Add buttons
        builder.setPositiveButton("Increase Budget", (dialog, which) -> {
            // Navigate to budget tab and select this category
            if (getActivity() instanceof MenuActivity) {
                MenuActivity activity = (MenuActivity) getActivity();
                // Switch to budget tab (index 2)
                activity.viewPager2.setCurrentItem(2);

                // Pre-select the category in the budget fragment
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Fragment budgetFragment = activity.getViewPagerAdapter().getFragment(2);
                    if (budgetFragment instanceof SimpleBudgetFragment) {
                        // Use reflection to access private fields (not ideal, but works for this example)
                        try {
                            Spinner categorySpinner = budgetFragment.getView().findViewById(R.id.spinnerBudgetCategory);
                            for (int i = 0; i < categorySpinner.getCount(); i++) {
                                Category cat = (Category) categorySpinner.getItemAtPosition(i);
                                if (cat.getId() == categoryId) {
                                    categorySpinner.setSelection(i);
                                    break;
                                }
                            }

                            // Also pre-fill the amount field with the expense amount
                            EditText amountField = budgetFragment.getView().findViewById(R.id.etCategoryBudgetAmount);
                            amountField.setText(String.valueOf(expenseAmount));
                        } catch (Exception e) {
                            Log.e(TAG, "Error pre-selecting category in budget fragment", e);
                        }
                    }
                }, 300);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void updateBudgetFragment() {
        if (getActivity() instanceof MenuActivity) {
            MenuActivity activity = (MenuActivity) getActivity();
            ViewPagerAdapter adapter = activity.getViewPagerAdapter();

            Log.d(TAG, "Forcing budget fragment update");

            // Get budget fragment directly
            Fragment budgetFragment = adapter.getFragment(2);
            if (budgetFragment instanceof SimpleBudgetFragment) {
                Log.d(TAG, "Directly calling loadBudgets on SimpleBudgetFragment");
                ((SimpleBudgetFragment) budgetFragment).loadBudgets();
            }

            // If we're currently on the Budget tab, force refresh display
            if (activity.viewPager2.getCurrentItem() == 2) {
                activity.refreshFragmentAtPosition(2);
            }
        }
    }

    private void updateHomeFragment() {
        if (getActivity() instanceof MenuActivity) {
            MenuActivity activity = (MenuActivity) getActivity();
            ViewPagerAdapter adapter = activity.getViewPagerAdapter();

            Log.d(TAG, "Forcing home fragment update");

            // Update home fragment
            Fragment homeFragment = adapter.getFragment(0);
            if (homeFragment instanceof RefreshableFragment) {
                ((RefreshableFragment) homeFragment).refreshData();
            }
        }
    }

    private void loadExpenses() {
        if (userId != -1) {
            // Get expenses from database
            List<Expense> expenses = expenseDb.getExpensesByUser(userId);
            Log.d(TAG, "Loaded " + expenses.size() + " expenses");

            // Update UI based on results
            if (expenses.isEmpty()) {
                tvNoExpenses.setVisibility(View.VISIBLE);
                recyclerExpenses.setVisibility(View.GONE);
            } else {
                tvNoExpenses.setVisibility(View.GONE);
                recyclerExpenses.setVisibility(View.VISIBLE);

                // Add category info to expenses
                for (Expense expense : expenses) {
                    for (Category category : categoryList) {
                        if (category.getId() == expense.getCategoryId()) {
                            expense.setCategoryName(category.getName());
                            expense.setCategoryColor(category.getColor());
                            break;
                        }
                    }
                }

                // Update adapter
                expenseList.clear();
                expenseList.addAll(expenses);
                expenseAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void refreshData() {
        if (isAdded() && getContext() != null) {
            Log.d(TAG, "refreshData called");
            loadCategories();
            loadExpenses();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        // Refresh data when fragment becomes visible
        refreshData();
    }

    // Here's the implementation of the budget check for normal expenses
// This should be added to the SimpleExpensesFragment.java file

    /**
     * Checks if the selected category has a budget allocated
     * @param categoryId The category ID to check
     * @return true if the category has a budget, false otherwise
     */
    private boolean categoryHasBudget(int categoryId) {
        // Get all budgets for the current user
        List<Budget> budgets = expenseDb.getBudgetsByUser(userId);

        // Check if any budget matches the selected category
        for (Budget budget : budgets) {
            if (budget.getCategoryId() == categoryId) {
                return true;
            }
        }

        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void proceedWithSavingExpense(int categoryId, double amount, String description) {
        // Get current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        // Save to database with selected category
        long result = expenseDb.insertExpense(userId, categoryId, amount, description, currentDate, "Cash", false, null);

        if (result != -1) {
            Log.d(TAG, "Expense added successfully with ID: " + result);
            Toast.makeText(getContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
            // Clear input fields
            etAmount.setText("");
            etDescription.setText("");

            // Refresh the expense list
            loadExpenses();

            // Force update budget information with a small delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                updateBudgetFragment();
                updateHomeFragment();
            }, 500);
        } else {
            Log.e(TAG, "Failed to add expense");
            Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show();
        }
    }



}