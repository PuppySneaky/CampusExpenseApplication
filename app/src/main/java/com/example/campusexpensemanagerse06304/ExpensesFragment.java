package com.example.campusexpensemanagerse06304;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

import com.example.campusexpensemanagerse06304.adapter.ExpenseAdapter;
import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.example.campusexpensemanagerse06304.model.Category;
import com.example.campusexpensemanagerse06304.model.Expense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpensesFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private View rootView;
    private RecyclerView recyclerView;
    private ExpenseAdapter expenseAdapter;
    private List<Expense> expenseList;
    private List<Category> categoryList;
    private ExpenseDb expenseDb;

    private EditText etAmount, etDescription;
    private Spinner spinnerCategory, spinnerPaymentMethod;
    private TextView tvDate;
    private Button btnAddExpense;
    private FloatingActionButton fabAddExpense;
    private View addExpenseLayout;

    private int userId; // Current logged in user ID
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;

    public ExpensesFragment() {
        // Required empty public constructor
    }

    public static ExpensesFragment newInstance(String param1, String param2) {
        ExpensesFragment fragment = new ExpensesFragment();
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

        // Initialize date formatter and default date
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = Calendar.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_expenses, container, false);

        // Initialize views
        recyclerView = rootView.findViewById(R.id.recyclerExpenses);
        fabAddExpense = rootView.findViewById(R.id.fabAddExpense);
        addExpenseLayout = rootView.findViewById(R.id.layoutAddExpense);

        etAmount = rootView.findViewById(R.id.etExpenseAmount);
        etDescription = rootView.findViewById(R.id.etExpenseDescription);
        spinnerCategory = rootView.findViewById(R.id.spinnerCategory);
        spinnerPaymentMethod = rootView.findViewById(R.id.spinnerPaymentMethod);
        tvDate = rootView.findViewById(R.id.tvExpenseDate);
        btnAddExpense = rootView.findViewById(R.id.btnSaveExpense);

        // Setup the recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        expenseList = new ArrayList<>();
        expenseAdapter = new ExpenseAdapter(getContext(), expenseList);
        recyclerView.setAdapter(expenseAdapter);

        // Initialize the date display
        tvDate.setText(dateFormat.format(selectedDate.getTime()));

        // Setup FAB click listener
        fabAddExpense.setOnClickListener(view -> {
            // Show the add expense layout
            addExpenseLayout.setVisibility(View.VISIBLE);
            fabAddExpense.setVisibility(View.GONE);
        });

        // Setup date picker
        tvDate.setOnClickListener(view -> showDatePicker());

        // Setup category spinner
        setupCategorySpinner();

        // Setup payment method spinner
        setupPaymentMethodSpinner();

        // Setup Add Expense button
        btnAddExpense.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                saveExpense();
            }
        });

        // Load expenses
        loadExpenses();

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

    private void setupPaymentMethodSpinner() {
        // Create a list of payment methods
        String[] paymentMethods = {"Cash", "Credit Card", "Debit Card", "Bank Transfer", "Mobile Payment", "Other"};

        // Create adapter for the spinner
        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, paymentMethods);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerPaymentMethod.setAdapter(paymentAdapter);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, monthOfYear);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    tvDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
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
        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        int categoryId = selectedCategory.getId();

        // Get selected payment method
        String paymentMethod = spinnerPaymentMethod.getSelectedItem().toString();

        // Get date
        String date = tvDate.getText().toString();

        // Save to database
        long result = expenseDb.insertExpense(userId, categoryId, amount, description, date, paymentMethod, false, null);

        if (result != -1) {
            Toast.makeText(getContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
            // Clear input fields
            etAmount.setText("");
            etDescription.setText("");
            tvDate.setText(dateFormat.format(Calendar.getInstance().getTime()));

            // Hide add expense layout
            addExpenseLayout.setVisibility(View.GONE);
            fabAddExpense.setVisibility(View.VISIBLE);

            // Refresh expenses list
            loadExpenses();
        } else {
            Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadExpenses() {
        if (userId != -1) {
            // Load expenses from database
            List<Expense> expenses = expenseDb.getExpensesByUser(userId);

            // Add category information to expenses
            for (Expense expense : expenses) {
                for (Category category : categoryList) {
                    if (category.getId() == expense.getCategoryId()) {
                        expense.setCategoryName(category.getName());
                        expense.setCategoryColor(category.getColor());
                        break;
                    }
                }
            }

            // Update the adapter
            expenseList.clear();
            expenseList.addAll(expenses);
            expenseAdapter.notifyDataSetChanged();
        }
    }
}