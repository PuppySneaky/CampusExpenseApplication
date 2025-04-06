package com.example.campusexpensemanagerse06304;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SimpleExpensesFragment extends Fragment implements RefreshableFragment {

    private Menu actionMenu;
    private MenuItem deleteMenuItem;
    private AlertDialog editDialog;

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

        // Enable options menu for multi-select
        setHasOptionsMenu(true);

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
        recyclerExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        expenseList = new ArrayList<>();
        expenseAdapter = new SimpleExpenseAdapter(getContext(), expenseList);

        // IMPORTANT: Set the listener right after creating the adapter
        expenseAdapter.setOnExpenseActionListener(new SimpleExpenseAdapter.OnExpenseActionListener() {
            @Override
            public void onEditExpense(Expense expense) {
                showEditExpenseDialog(expense);
            }

            @Override
            public void onDeleteExpense(Expense expense) {
                showDeleteConfirmationDialog(expense);
            }

            @Override
            public void onMultiDeleteExpenses(List<Expense> expenses) {
                showMultiDeleteConfirmationDialog(expenses);
            }
        });

        // Set the adapter to the RecyclerView
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


    // Override these methods to add the action menu for multi-select
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_expense_actions, menu);
        this.actionMenu = menu;
        this.deleteMenuItem = menu.findItem(R.id.action_delete_selected);
        updateMenuVisibility();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_selected) {
            List<Expense> selectedExpenses = expenseAdapter.getSelectedExpenses();
            if (!selectedExpenses.isEmpty()) {
                showMultiDeleteConfirmationDialog(selectedExpenses);
            }
            return true;
        } else if (item.getItemId() == R.id.action_cancel_selection) {
            expenseAdapter.toggleMultiSelectMode();
            updateMenuVisibility();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // Add this method to update menu visibility
    private void updateMenuVisibility() {
        if (actionMenu != null) {
            boolean showMenu = expenseAdapter.isMultiSelectMode();
            actionMenu.findItem(R.id.action_delete_selected).setVisible(showMenu);
            actionMenu.findItem(R.id.action_cancel_selection).setVisible(showMenu);

            // Update the title to show selection count
            if (showMenu) {
                int count = expenseAdapter.getSelectedItemCount();
                getActivity().setTitle(count + " Selected");
            } else {
                getActivity().setTitle("Expenses");
            }
        }
    }


    private void showEditExpenseDialog(Expense expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Expense");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_expense, null);
        builder.setView(dialogView);

        // Find views
        EditText etAmount = dialogView.findViewById(R.id.etEditAmount);
        EditText etDescription = dialogView.findViewById(R.id.etEditDescription);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerEditCategory);
        TextView tvDate = dialogView.findViewById(R.id.tvEditDate);

        // Set up category spinner
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Set values from the expense
        double originalAmount = expense.getAmount();
        etAmount.setText(String.format(Locale.getDefault(), "%.2f", originalAmount));
        etDescription.setText(expense.getDescription());
        tvDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(expense.getDate()));

        // Set selected category
        int originalCategoryId = expense.getCategoryId();
        int originalCategoryPosition = 0;
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId() == originalCategoryId) {
                spinnerCategory.setSelection(i);
                originalCategoryPosition = i;
                break;
            }
        }

        // Store original category position for later comparison
        final int finalOriginalCategoryPosition = originalCategoryPosition;

        // Set date picker
        tvDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.setTime(expense.getDate());

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, month, dayOfMonth);
                        tvDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(newDate.getTime()));
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        builder.setPositiveButton("Save", null); // We'll set this later
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        editDialog = builder.create();
        editDialog.show();

        // Override positive button to validate input
        editDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Validate input
            if (etAmount.getText().toString().trim().isEmpty()) {
                etAmount.setError("Please enter an amount");
                return;
            }

            try {
                double newAmount = Double.parseDouble(etAmount.getText().toString());
                if (newAmount <= 0) {
                    etAmount.setError("Amount must be greater than zero");
                    return;
                }

                // Get values
                String description = etDescription.getText().toString();
                if (description.isEmpty()) {
                    description = "Expense";
                }

                Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
                int categoryId = selectedCategory.getId();
                String dateStr = tvDate.getText().toString();

                // Check if the category has changed
                boolean categoryChanged = categoryId != originalCategoryId;

                // Check budget constraints ONLY if:
                // 1. Amount has increased OR
                // 2. Category has changed
                if (newAmount > originalAmount || categoryChanged) {
                    // Calculate available budget for the selected category

                    // 1. Get the budget limit for this category
                    double categoryBudget = 0;
                    List<Budget> budgets = expenseDb.getBudgetsByUser(userId);
                    for (Budget budget : budgets) {
                        if (budget.getCategoryId() == categoryId) {
                            categoryBudget = budget.getAmount();
                            break;
                        }
                    }

                    // 2. Calculate current usage for this category (excluding this expense)
                    Calendar cal = Calendar.getInstance();
                    String currentMonth = String.format(Locale.getDefault(), "%d-%02d",
                            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);

                    double categoryUsage = expenseDb.getTotalExpensesByCategoryAndMonth(userId, categoryId, currentMonth);

                    // 3. If category hasn't changed, subtract original amount from usage
                    //    (since it's included in the total but we're replacing it)
                    if (!categoryChanged) {
                        categoryUsage -= originalAmount;
                    }

                    // 4. Check if new amount would exceed budget
                    if (categoryUsage + newAmount > categoryBudget) {
                        // Show error dialog with more details
                        AlertDialog.Builder budgetExceededDialog = new AlertDialog.Builder(getContext());
                        budgetExceededDialog.setTitle("Budget Limit Exceeded");
                        budgetExceededDialog.setMessage(
                                "Updating this expense to $" + String.format(Locale.getDefault(), "%.2f", newAmount) +
                                        " would exceed your budget for " + selectedCategory.getName() + ".\n\n" +
                                        "Category Budget: $" + String.format(Locale.getDefault(), "%.2f", categoryBudget) + "\n" +
                                        "Current Usage: $" + String.format(Locale.getDefault(), "%.2f", categoryUsage) + "\n" +
                                        "Available: $" + String.format(Locale.getDefault(), "%.2f", categoryBudget - categoryUsage) + "\n\n" +
                                        "Would you like to increase your budget for this category?"
                        );
                        budgetExceededDialog.setPositiveButton("Increase Budget", (dialog, which) -> {
                            // Navigate to budget tab to adjust budget
                            if (getActivity() instanceof MenuActivity) {
                                MenuActivity activity = (MenuActivity) getActivity();
                                activity.viewPager2.setCurrentItem(2);

                                // Pre-select the category in the budget fragment
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    Fragment budgetFragment = activity.getViewPagerAdapter().getFragment(2);
                                    if (budgetFragment instanceof SimpleBudgetFragment) {
                                        ((SimpleBudgetFragment) budgetFragment).selectCategory(categoryId);
                                    }
                                }, 500);
                            }
                            editDialog.dismiss();
                        });
                        budgetExceededDialog.setNegativeButton("Cancel", null);
                        budgetExceededDialog.show();
                        return;
                    }
                }

                // Update expense in database
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    int result = expenseDb.updateExpense(
                            expense.getId(),
                            categoryId,
                            newAmount,
                            description,
                            dateStr,
                            expense.getPaymentMethod());

                    if (result > 0) {
                        Toast.makeText(getContext(), "Expense updated successfully", Toast.LENGTH_SHORT).show();
                        loadExpenses();
                        editDialog.dismiss();

                        // Update related fragments
                        refreshAllData();
                    } else {
                        Toast.makeText(getContext(), "Failed to update expense", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (NumberFormatException e) {
                etAmount.setError("Invalid amount format");
            }
        });
    }

    private void showDeleteConfirmationDialog(Expense expense) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int result = expenseDb.deleteExpense(expense.getId());
                    if (result > 0) {
                        Toast.makeText(getContext(), "Expense deleted successfully", Toast.LENGTH_SHORT).show();
                        loadExpenses();

                        // Update related fragments
                        refreshAllData();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete expense", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showMultiDeleteConfirmationDialog(List<Expense> expenses) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Multiple Expenses")
                .setMessage("Are you sure you want to delete these " + expenses.size() + " expenses?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int successCount = 0;
                    for (Expense expense : expenses) {
                        int result = expenseDb.deleteExpense(expense.getId());
                        if (result > 0) {
                            successCount++;
                        }
                    }

                    if (successCount > 0) {
                        Toast.makeText(getContext(), successCount + " expenses deleted successfully", Toast.LENGTH_SHORT).show();
                        expenseAdapter.toggleMultiSelectMode();
                        updateMenuVisibility();
                        loadExpenses();

                        // Update related fragments
                        refreshAllData();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete expenses", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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


        // Check if this expense would exceed the category budget
        if (!expenseDb.checkCategoryBudgetBalance(userId, categoryId, amount)) {
            // Show an error dialog with more details
            showBudgetExceededDialog(selectedCategory.getName(), amount);
            return;
        }

        // Get current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        // Save to database with selected category
/*        long result = expenseDb.insertExpense(userId, categoryId, amount, description, currentDate, "Cash", false, null);

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
        }*/

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


    private void refreshAllData() {
        if (getActivity() instanceof MenuActivity) {
            ((MenuActivity) getActivity()).refreshAllDataFragments();
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

                // Add this line if there are expenses in the list
                if (!expenseList.isEmpty()) {
                    recyclerExpenses.smoothScrollToPosition(expenseList.size() - 1);
                }
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

            // Force update all data
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                refreshAllData();
            }, 500);
        } else {
            Log.e(TAG, "Failed to add expense");
            Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show();
        }
    }





}