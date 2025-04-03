package com.example.campusexpensemanagerse06304;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

public class SimpleBudgetFragment extends Fragment {
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
            }
        }

        // Setup RecyclerView
        budgetList = new ArrayList<>();
        budgetAdapter = new SimpleBudgetAdapter(getContext(), budgetList);
        recyclerBudgets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerBudgets.setAdapter(budgetAdapter);

        // Load budgets
        loadBudgets();

        // Setup Set Budget button
        btnSetBudget.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                saveBudget();
            } else {
                Toast.makeText(getContext(), "This feature requires Android 8.0 or higher", Toast.LENGTH_SHORT).show();
            }
        });

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

        // Save to database (using default category ID 1)
        long result = expenseDb.insertBudget(userId, 1, amount, "monthly", startDate, endDate);

        if (result != -1) {
            Toast.makeText(getContext(), "Budget set successfully", Toast.LENGTH_SHORT).show();
            // Clear input fields
            etBudgetAmount.setText("");
            etBudgetDescription.setText("");

            // Refresh the budget list
            loadBudgets();
        } else {
            Toast.makeText(getContext(), "Failed to set budget", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBudgets() {
        if (userId != -1) {
            // Get budgets from database
            List<Budget> budgets = expenseDb.getBudgetsByUser(userId);

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

                // Add spending information to each budget
                for (Budget budget : budgets) {
                    double spent = expenseDb.getTotalExpensesByCategoryAndMonth(userId, budget.getCategoryId(), currentMonth);
                    budget.setSpent(spent);
                }

                // Update adapter
                budgetList.clear();
                budgetList.addAll(budgets);
                budgetAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh budget list when fragment becomes visible
        loadBudgets();
    }
}