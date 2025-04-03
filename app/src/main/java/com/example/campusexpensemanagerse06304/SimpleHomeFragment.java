package com.example.campusexpensemanagerse06304;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.List;
import java.util.Locale;

public class SimpleHomeFragment extends Fragment {
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
            }
        }

        // Setup RecyclerView for recent expenses
        recentExpensesList = new ArrayList<>();
        expenseAdapter = new SimpleExpenseAdapter(getContext(), recentExpensesList);
        recyclerRecentExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerRecentExpenses.setAdapter(expenseAdapter);

        // Wait for layout to be drawn to get accurate width for progress bar
        parentProgressView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        parentProgressView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        // Now the view has been drawn and we can get accurate width
                        loadDashboardData();
                    }
                });

        // Load categories
        loadCategories();

        return view;
    }

    private void loadCategories() {
        categoryList = expenseDb.getAllCategories();
    }

    public void refreshData() {
        if (isAdded()) {  // Check if fragment is still attached to activity
            loadCategories();
            loadDashboardData();
        }
    }

    private void loadDashboardData() {
        if (userId != -1) {
            // Get current month in format YYYY-MM
            Calendar cal = Calendar.getInstance();
            String currentMonth = String.format(Locale.getDefault(), "%d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);

            // Get total expenses for current month
            double totalSpent = expenseDb.getTotalExpensesByMonth(userId, currentMonth);
            tvTotalSpent.setText(String.format(Locale.getDefault(), "$%.2f", totalSpent));

            // Get total budget amount
            double totalBudget = calculateTotalBudget();
            tvTotalBudget.setText(String.format(Locale.getDefault(), "$%.2f", totalBudget));

            // Calculate remaining budget
            double remaining = Math.max(0, totalBudget - totalSpent);
            tvRemainingBudget.setText(String.format(Locale.getDefault(), "$%.2f", remaining));

            // Set budget progress visualization
            int progressPercentage = totalBudget > 0 ? (int)((totalSpent / totalBudget) * 100) : 0;

            // Calculate width based on percentage
            int parentWidth = parentProgressView.getWidth();
            if (parentWidth > 0) {
                ViewGroup.LayoutParams params = budgetProgressView.getLayoutParams();
                params.width = (parentWidth * progressPercentage) / 100;
                budgetProgressView.setLayoutParams(params);
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

            // Load recent expenses
            loadRecentExpenses();
        }
    }

    private double calculateTotalBudget() {
        double total = 0;
        List<Budget> budgets = expenseDb.getBudgetsByUser(userId);

        // For simplicity, sum all active budgets
        for (Budget budget : budgets) {
            total += budget.getAmount();
        }

        return total;
    }

    private void loadRecentExpenses() {
        // Get all expenses from database
        List<Expense> allExpenses = expenseDb.getExpensesByUser(userId);

        // Update UI based on results
        if (allExpenses.isEmpty()) {
            tvNoRecentExpenses.setVisibility(View.VISIBLE);
            recyclerRecentExpenses.setVisibility(View.GONE);
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

            // Take only most recent 3 expenses
            int count = Math.min(3, allExpenses.size());
            List<Expense> recentExpenses = new ArrayList<>(allExpenses.subList(0, count));

            // Update adapter
            recentExpensesList.clear();
            recentExpensesList.addAll(recentExpenses);
            expenseAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        if (getView() != null && parentProgressView.getWidth() > 0) {
            refreshData();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isAdded()) {
            refreshData();
        }
    }
}