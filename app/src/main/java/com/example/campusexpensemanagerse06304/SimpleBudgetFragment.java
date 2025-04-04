package com.example.campusexpensemanagerse06304;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagerse06304.adapter.SimpleBudgetAdapter;
import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.example.campusexpensemanagerse06304.model.Budget;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SimpleBudgetFragment extends Fragment implements RefreshableFragment {
    private static final String TAG = "SimpleBudgetFragment";

    private EditText etBudgetAmount, etBudgetDescription;
    private Button btnSetBudget;
    private TextView tvNoBudgets;
    private RecyclerView recyclerBudgets;
    private SimpleBudgetAdapter budgetAdapter;
    private List<Budget> budgetList;
    private ExpenseDb expenseDb;
    private int userId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_budget, container, false);

        etBudgetAmount = view.findViewById(R.id.etBudgetAmount);
        etBudgetDescription = view.findViewById(R.id.etBudgetDescription);
        btnSetBudget = view.findViewById(R.id.btnSetBudget);
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

        // Setup Set Budget button
        btnSetBudget.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                saveBudget();
            } else {
                Toast.makeText(getContext(), "This feature requires Android 8.0 or higher", Toast.LENGTH_SHORT).show();
            }
        });

        // Load budgets now
        loadBudgets();

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveBudget() {
        // Validate input fields
        if (etBudgetAmount.getText().toString().trim().isEmpty()) {
            etBudgetAmount.setError("Please enter an amount");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(etBudgetAmount.getText().toString());
            if (amount <= 0) {
                etBudgetAmount.setError("Amount must be greater than zero");
                return;
            }
        } catch (NumberFormatException e) {
            etBudgetAmount.setError("Invalid amount format");
            return;
        }

        String description = etBudgetDescription.getText().toString().trim();
        if (description.isEmpty()) {
            description = "Monthly Budget"; // Default description
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

        // Use the updateOrInsertBudget method which will update if exists or create new if not
        // Using default category ID 1
        long result = expenseDb.updateOrInsertBudget(userId, 1, amount, "monthly", startDate, endDate);

        if (result > 0) {
            Toast.makeText(getContext(), "Budget updated successfully", Toast.LENGTH_SHORT).show();
            // Clear input fields
            etBudgetAmount.setText("");
            etBudgetDescription.setText("");

            // Refresh the budget list
            loadBudgets();
        } else {
            Toast.makeText(getContext(), "Failed to update budget", Toast.LENGTH_SHORT).show();
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
                // Force reload spent amount for each budget from all expenses
                // Using category ID -1 to get all expenses regardless of category
                double spent = expenseDb.getTotalExpensesByCategoryAndMonth(userId, -1, currentMonth);
                budget.setSpent(spent);
                Log.d(TAG, "Budget: " + budget.getId() + " - Category: " + budget.getCategoryId() +
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
            loadBudgets();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        // Refresh budget data when fragment becomes visible
        loadBudgets();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isAdded()) {
            Log.d(TAG, "setUserVisibleHint: visible and refreshing");
            loadBudgets();
        }
    }
}