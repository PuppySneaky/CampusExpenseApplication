package com.example.campusexpensemanagerse06304;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.example.campusexpensemanagerse06304.adapter.BudgetAdapter;
import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.example.campusexpensemanagerse06304.model.Budget;
import com.example.campusexpensemanagerse06304.model.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BudgetFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private View rootView;
    private RecyclerView recyclerView;
    private BudgetAdapter budgetAdapter;
    private List<Budget> budgetList;
    private List<Category> categoryList;
    private ExpenseDb expenseDb;

    private EditText etAmount;
    private Spinner spinnerCategory, spinnerPeriod;
    private TextView tvStartDate, tvEndDate;
    private Button btnAddBudget;
    private FloatingActionButton fabAddBudget;
    private View addBudgetLayout;

    private int userId; // Current logged in user ID
    private Calendar startDate, endDate;
    private SimpleDateFormat dateFormat;

    private String[] periods = {"Monthly", "Weekly", "Yearly"};

    public BudgetFragment() {
        // Required empty public constructor
    }

    public static BudgetFragment newInstance(String param1, String param2) {
        BudgetFragment fragment = new BudgetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Initialize database helper
        expenseDb = new ExpenseDb(getContext());

        // Get the current user ID from the activity
        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.getExtras() != null) {
                userId = intent.getExtras().getInt("ID_USER", -1);
            }
        }

        // Initialize date formatter and default dates
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1); // Default to one month later
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_budget, container, false);

        // Initialize views
        recyclerView = rootView.findViewById(R.id.recyclerBudgets);
        fabAddBudget = rootView.findViewById(R.id.fabAddBudget);
        addBudgetLayout = rootView.findViewById(R.id.layoutAddBudget);

        etAmount = rootView.findViewById(R.id.etBudgetAmount);
        spinnerCategory = rootView.findViewById(R.id.spinnerBudgetCategory);
        spinnerPeriod = rootView.findViewById(R.id.spinnerBudgetPeriod);
        tvStartDate = rootView.findViewById(R.id.tvBudgetStartDate);
        tvEndDate = rootView.findViewById(R.id.tvBudgetEndDate);
        btnAddBudget = rootView.findViewById(R.id.btnSaveBudget);

        // Setup the recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        budgetList = new ArrayList<>();
        budgetAdapter = new BudgetAdapter(getContext(), budgetList);
        recyclerView.setAdapter(budgetAdapter);

        // Initialize the date displays
        tvStartDate.setText(dateFormat.format(startDate.getTime()));
        tvEndDate.setText(dateFormat.format(endDate.getTime()));

        // Setup FAB click listener
        fabAddBudget.setOnClickListener(view -> {
            // Show the add budget layout
            addBudgetLayout.setVisibility(View.VISIBLE);
            fabAddBudget.setVisibility(View.GONE);
        });

        // Setup date pickers
        tvStartDate.setOnClickListener(view -> showStartDatePicker());
        tvEndDate.setOnClickListener(view -> showEndDatePicker());

        // Setup category spinner
        setupCategorySpinner();

        // Setup period spinner
        setupPeriodSpinner();

        // Setup Add Budget button
        btnAddBudget.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                saveBudget();
            }
        });

        // Setup item click listener for the adapter
        budgetAdapter.setOnItemClickListener(new BudgetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Open budget detail or edit (not implemented in this sample)
                Toast.makeText(getContext(), "Budget details coming soon", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(int position) {
                Budget budget = budgetAdapter.getItemAt(position);
                expenseDb.deleteBudget(budget.getId());
                loadBudgets();
                Toast.makeText(getContext(), "Budget deleted", Toast.LENGTH_SHORT).show();
            }
        });

        // Load budgets
        loadBudgets();

        return rootView;
    }

    private void setupCategorySpinner() {
        // Load categories from the database
        categoryList = expenseDb.getAllCategories();

        // Create adapter for the spinner
        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, categoryList);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupPeriodSpinner() {
        // Create adapter for the spinner
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, periods);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerPeriod.setAdapter(periodAdapter);
    }

    private void showStartDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    startDate.set(Calendar.YEAR, year);
                    startDate.set(Calendar.MONTH, month);
                    startDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    tvStartDate.setText(dateFormat.format(startDate.getTime()));
                },
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showEndDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    endDate.set(Calendar.YEAR, year);
                    endDate.set(Calendar.MONTH, month);
                    endDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    tvEndDate.setText(dateFormat.format(endDate.getTime()));
                },
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveBudget() {
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

        // Get selected category
        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        int categoryId = selectedCategory.getId();

        // Get selected period
        String period = spinnerPeriod.getSelectedItem().toString().toLowerCase();

        // Get dates
        String startDateStr = tvStartDate.getText().toString();
        String endDateStr = tvEndDate.getText().toString();

        // Save to database
        long result = expenseDb.insertBudget(userId, categoryId, amount, period, startDateStr, endDateStr);

        if (result != -1) {
            Toast.makeText(getContext(), "Budget added successfully", Toast.LENGTH_SHORT).show();
            // Clear input fields
            etAmount.setText("");

            // Hide add budget layout
            addBudgetLayout.setVisibility(View.GONE);
            fabAddBudget.setVisibility(View.VISIBLE);

            // Refresh budgets list
            loadBudgets();
        } else {
            Toast.makeText(getContext(), "Failed to add budget", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBudgets() {
        if (userId != -1) {
            // Load budgets from database
            List<Budget> budgets = expenseDb.getBudgetsByUser(userId);

            // Get current month in format YYYY-MM
            Calendar cal = Calendar.getInstance();
            String currentMonth = String.format(Locale.getDefault(), "%d-%02d",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);

            // Add category information and spending data to budgets
            for (Budget budget : budgets) {
                for (Category category : categoryList) {
                    if (category.getId() == budget.getCategoryId()) {
                        budget.setCategoryName(category.getName());
                        budget.setCategoryColor(category.getColor());
                        break;
                    }
                }

                // Get amount spent for this category in current month
                double spent = expenseDb.getTotalExpensesByCategoryAndMonth(userId, budget.getCategoryId(), currentMonth);
                budget.setSpent(spent);
            }

            // Update the adapter
            budgetList.clear();
            budgetList.addAll(budgets);
            budgetAdapter.notifyDataSetChanged();
        }
    }
}