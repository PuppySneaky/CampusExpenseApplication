package com.example.campusexpensemanagerse06304;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.example.campusexpensemanagerse06304.model.Category;
import com.example.campusexpensemanagerse06304.model.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SimpleExpensesFragment extends Fragment {
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

        // Get current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        // Save to database with selected category
        long result = expenseDb.insertExpense(userId, categoryId, amount, description, currentDate, "Cash", false, null);

        if (result != -1) {
            Toast.makeText(getContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
            // Clear input fields
            etAmount.setText("");
            etDescription.setText("");

            // Refresh the expense list
            loadExpenses();

            // Refresh the home fragment
            if (getActivity() instanceof MenuActivity) {
                MenuActivity activity = (MenuActivity) getActivity();
                Fragment homeFragment = activity.getViewPagerAdapter().getFragment(0);
                if (homeFragment instanceof SimpleHomeFragment) {
                    ((SimpleHomeFragment) homeFragment).refreshData();
                }
            }
        } else {
            Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadExpenses() {
        if (userId != -1) {
            // Get expenses from database
            List<Expense> expenses = expenseDb.getExpensesByUser(userId);

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
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadCategories();
        loadExpenses();
    }
}