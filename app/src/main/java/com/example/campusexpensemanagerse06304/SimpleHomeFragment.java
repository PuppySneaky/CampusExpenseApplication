package com.example.campusexpensemanagerse06304;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagerse06304.adapter.SimpleExpenseAdapter;
import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.example.campusexpensemanagerse06304.model.Budget;
import com.example.campusexpensemanagerse06304.model.Category;
import com.example.campusexpensemanagerse06304.model.Expense;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SimpleHomeFragment extends Fragment {
    private static final String TAG = "SimpleHomeFragment";
    private static final int MAX_RECENT_EXPENSES = 10; // Increased to 10 to show all expenses

    private TextView tvWelcome, tvTotalSpent, tvTotalBudget, tvRemainingBudget, tvNoRecentExpenses;
    private RecyclerView recyclerRecentExpenses;
    private SimpleExpenseAdapter expenseAdapter;
    private List<Expense> recentExpensesList;
    private List<Category> categoryList;
    private ExpenseDb expenseDb;
    private int userId = -1;
    private View budgetProgressView;
    private TextView tvBudgetPercentage;
    private View parentProgressView;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_home, container, false);

        // Initialize views
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
        tvTotalBudget = view.findViewById(R.id.tvTotalBudget);
        tvRemainingBudget = view.findViewById(R.id.tvRemainingBudget);
        tvNoRecentExpenses = view.findViewById(R.id.tvNoRecentExpenses);
        recyclerRecentExpenses = view.findViewById(R.id.recyclerRecentExpenses);
        budgetProgressView = view.findViewById(R.id.budgetProgressView);
        parentProgressView = view.findViewById(R.id.progressContainer);
        tvBudgetPercentage = view.findViewById(R.id.tvBudgetPercentage);

        // Initialize database helper
        expenseDb = new ExpenseDb(getContext());

        // Get the current user ID from the activity
        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.getExtras() != null) {
                userId = intent.getExtras().getInt("ID_USER", -1);
                String username = intent.getExtras().getString("USER_ACCOUNT", "");
                tvWelcome.setText("Welcome, " + username + "!");

                Log.d(TAG, "User ID loaded: " + userId);
            }
        }

        // Setup RecyclerView for recent expenses
        recentExpensesList = new ArrayList<>();
        expenseAdapter = new SimpleExpenseAdapter(getContext(), recentExpensesList);

        // Enable scrolling in the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerRecentExpenses.setLayoutManager(layoutManager);

        // Make sure nested scrolling is enabled
        recyclerRecentExpenses.setNestedScrollingEnabled(true);

        recyclerRecentExpenses.setAdapter(expenseAdapter);

        // Wait for layout to be drawn to get accurate width for progress bar
        parentProgressView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        parentProgressView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        // Now the view has been drawn and we can get accurate width
                        loadDashboardData();
                        loadRecentExpenses(); // Explicitly load recent expenses too
                    }
                });

        // Load categories
        loadCategories();

        return view;
    }

    private void loadCategories() {
        categoryList = expenseDb.getAllCategories();
        Log.d(TAG, "Loaded " + categoryList.size() + " categories");
    }

    public void refreshData() {
        if (isAdded()) {  // Check if fragment is still attached to activity
            Log.d(TAG, "Refreshing data in SimpleHomeFragment");
            loadCategories();
            loadDashboardData();
            loadRecentExpenses(); // Explicitly refresh recent expenses
        } else {
            Log.d(TAG, "Fragment not attached, skipping refresh");
        }
    }

    private void loadDashboardData() {
        if (userId != -1) {
            // Get current month in format YYYY-MM
            Calendar cal = Calendar.getInstance();
            String currentMonth = String.format(Locale.getDefault(), "%d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);

            Log.d(TAG, "Loading dashboard data for month: " + currentMonth);

            // Get total expenses for current month
            double totalSpent = expenseDb.getTotalExpensesByMonth(userId, currentMonth);
            tvTotalSpent.setText(String.format(Locale.getDefault(), "$%.2f", totalSpent));

            Log.d(TAG, "Total spent: $" + totalSpent);

            // Get total budget amount
            double totalBudget = calculateTotalBudget();
            tvTotalBudget.setText(String.format(Locale.getDefault(), "$%.2f", totalBudget));

            Log.d(TAG, "Total budget: $" + totalBudget);

            // Calculate remaining budget
            double remaining = Math.max(0, totalBudget - totalSpent);
            tvRemainingBudget.setText(String.format(Locale.getDefault(), "$%.2f", remaining));

            Log.d(TAG, "Remaining budget: $" + remaining);

            // Set budget progress visualization
            int progressPercentage = totalBudget > 0 ? (int)((totalSpent / totalBudget) * 100) : 0;

            Log.d(TAG, "Budget progress: " + progressPercentage + "%");

            // Calculate width based on percentage
            int parentWidth = parentProgressView.getWidth();
            if (parentWidth > 0) {
                ViewGroup.LayoutParams params = budgetProgressView.getLayoutParams();
                params.width = (parentWidth * progressPercentage) / 100;
                budgetProgressView.setLayoutParams(params);

                Log.d(TAG, "Set progress bar width: " + params.width + " of " + parentWidth);
            } else {
                Log.w(TAG, "Parent width is 0, can't set progress bar width");
            }

            // Set color based on percentage
            if (progressPercentage < 70) {
                budgetProgressView.setBackgroundColor(0xFF4CAF50); // Green
            } else if (progressPercentage < 90) {
                budgetProgressView.setBackgroundColor(0xFFFF9800); // Orange
            } else {
                budgetProgressView.setBackgroundColor(0xFFF44336); // Red
            }

            tvBudgetPercentage.setText(String.format(Locale.getDefault(), "%d%%", progressPercentage));
        } else {
            Log.w(TAG, "User ID is -1, can't load dashboard data");
        }
    }

    private double calculateTotalBudget() {
        double total = 0;
        List<Budget> budgets = expenseDb.getBudgetsByUser(userId);

        Log.d(TAG, "Found " + budgets.size() + " budgets for user");

        // For simplicity, sum all active budgets
        for (Budget budget : budgets) {
            total += budget.getAmount();
            Log.d(TAG, "Budget: " + budget.getCategoryId() + " - $" + budget.getAmount());
        }

        return total;
    }

    private void loadRecentExpenses() {
        if (userId == -1) {
            Log.w(TAG, "User ID is -1, can't load recent expenses");
            return;
        }

        // Get all expenses from database
        List<Expense> allExpenses = expenseDb.getExpensesByUser(userId);

        // Log for debugging
        Log.d(TAG, "Loaded " + allExpenses.size() + " expenses");
        for (Expense expense : allExpenses) {
            Log.d(TAG, "Expense ID: " + expense.getId() +
                    " - Description: " + expense.getDescription() +
                    " - $" + expense.getAmount() +
                    " - Date: " + (expense.getDate() != null ? expense.getFormattedDate() : "null"));
        }

        // Update UI based on results
        if (allExpenses.isEmpty()) {
            tvNoRecentExpenses.setVisibility(View.VISIBLE);
            recyclerRecentExpenses.setVisibility(View.GONE);
            Log.d(TAG, "No expenses found");
        } else {
            tvNoRecentExpenses.setVisibility(View.GONE);
            recyclerRecentExpenses.setVisibility(View.VISIBLE);

            // Add category information to expenses
            for (Expense expense : allExpenses) {
                for (Category category : categoryList) {
                    if (category.getId() == expense.getCategoryId()) {
                        expense.setCategoryName(category.getName());
                        expense.setCategoryColor(category.getColor());
                        break;
                    }
                }
            }

            // Complex multi-key sorting to ensure consistent order
            Collections.sort(allExpenses, new Comparator<Expense>() {
                @Override
                public int compare(Expense e1, Expense e2) {
                    // First try to compare by date (newest first)
                    if (e1.getDate() != null && e2.getDate() != null) {
                        int dateComparison = e2.getDate().compareTo(e1.getDate());
                        if (dateComparison != 0) {
                            return dateComparison;
                        }
                    } else if (e1.getDate() == null && e2.getDate() != null) {
                        return 1; // Null dates come after non-null dates
                    } else if (e1.getDate() != null && e2.getDate() == null) {
                        return -1; // Non-null dates come before null dates
                    }

                    // If dates are equal or both null, compare by ID (higher ID = newer)
                    return Integer.compare(e2.getId(), e1.getId());
                }
            });

            // Take only most recent expenses, but show more than just 3
            int count = Math.min(MAX_RECENT_EXPENSES, allExpenses.size()); // Show up to 10 expenses
            List<Expense> recentExpenses = new ArrayList<>(allExpenses.subList(0, count));

            Log.d(TAG, "Showing " + count + " recent expenses");
            for (Expense expense : recentExpenses) {
                Log.d(TAG, "Recent expense: " + expense.getDescription() + " - $" + expense.getAmount());
            }

            // Update adapter
            recentExpensesList.clear();
            recentExpensesList.addAll(recentExpenses);
            expenseAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        // Refresh data when fragment becomes visible
        if (getView() != null && parentProgressView.getWidth() > 0) {
            refreshData();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint: " + isVisibleToUser);
        if (isVisibleToUser && isAdded()) {
            refreshData();
        }
    }
}