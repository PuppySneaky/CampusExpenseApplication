package com.example.campusexpensemanagerse06304;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagerse06304.adapter.RecurringExpenseAdapter;
import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.example.campusexpensemanagerse06304.model.Category;
import com.example.campusexpensemanagerse06304.model.RecurringExpense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecurringExpenseActivity extends AppCompatActivity {
    private EditText etAmount, etDescription;
    private Spinner spinnerCategory, spinnerFrequency;
    private TextView tvStartDate, tvEndDate, tvNoRecurring;
    private Button btnSave, btnCancel;
    private FloatingActionButton fabAddRecurring;
    private View formContainer;
    private RecyclerView recyclerRecurring;
    private RecurringExpenseAdapter recurringAdapter;
    private List<RecurringExpense> recurringList;
    private List<Category> categoryList;
    private ExpenseDb expenseDb;
    private int userId;
    private Calendar startDate, endDate;
    private SimpleDateFormat dateFormat;
    private String[] frequencies = {"Daily", "Weekly", "Monthly", "Yearly"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_expense);

        // Initialize database
        expenseDb = new ExpenseDb(this);

        // Get user ID from intent
        userId = getIntent().getIntExtra("ID_USER", -1);

        // Initialize views
        etAmount = findViewById(R.id.etRecurringAmount);
        etDescription = findViewById(R.id.etRecurringDescription);
        spinnerCategory = findViewById(R.id.spinnerRecurringCategory);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        btnSave = findViewById(R.id.btnSaveRecurring);
        btnCancel = findViewById(R.id.btnCancelRecurring);
        fabAddRecurring = findViewById(R.id.fabAddRecurring);
        formContainer = findViewById(R.id.formContainer);
        recyclerRecurring = findViewById(R.id.recyclerRecurring);
        tvNoRecurring = findViewById(R.id.tvNoRecurring);

        // Set up toolbar
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Initialize date formatter and dates
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 3); // Default to 3 months

        // Set initial date display
        tvStartDate.setText(dateFormat.format(startDate.getTime()));
        tvEndDate.setText(dateFormat.format(endDate.getTime()));

        // Setup date picker dialogs
        tvStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        tvEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        // Setup frequency spinner
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, frequencies);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(frequencyAdapter);

        // Load categories
        loadCategories();

        // Setup RecyclerView
        recurringList = new ArrayList<>();
        recurringAdapter = new RecurringExpenseAdapter(this, recurringList);
        recyclerRecurring.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecurring.setAdapter(recurringAdapter);

        // Load existing recurring expenses
        loadRecurringExpenses();

        // Setup FAB
        fabAddRecurring.setOnClickListener(v -> {
            formContainer.setVisibility(View.VISIBLE);
            fabAddRecurring.setVisibility(View.GONE);
        });

        // Setup Save button
        btnSave.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                saveRecurringExpense();
            } else {
                Toast.makeText(this, "This feature requires Android 8.0 or higher", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup Cancel button
        btnCancel.setOnClickListener(v -> {
            formContainer.setVisibility(View.GONE);
            fabAddRecurring.setVisibility(View.VISIBLE);
            clearForm();
        });

        // Setup item click listener
        recurringAdapter.setOnItemClickListener(new RecurringExpenseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Not implemented
            }

            @Override
            public void onDeleteClick(int position) {
                RecurringExpense recurring = recurringList.get(position);
                deleteRecurringExpense(recurring.getId());
            }
        });
    }

    private void loadCategories() {
        categoryList = expenseDb.getAllCategories();

        // Create adapter for spinner
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCategory.setAdapter(adapter);
    }

    private void showDatePickerDialog(boolean isStartDate) {
        Calendar calendar = isStartDate ? startDate : endDate;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    if (isStartDate) {
                        tvStartDate.setText(dateFormat.format(calendar.getTime()));
                    } else {
                        tvEndDate.setText(dateFormat.format(calendar.getTime()));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveRecurringExpense() {
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
            description = "Recurring Expense"; // Default description
        }

        // Get selected category
        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        int categoryId = selectedCategory.getId();

        // Get selected frequency
        String frequency = frequencies[spinnerFrequency.getSelectedItemPosition()].toLowerCase();

        // Get dates
        String startDateStr = tvStartDate.getText().toString();
        String endDateStr = tvEndDate.getText().toString();

        // Save to database
        long result = expenseDb.insertRecurringExpense(userId, categoryId, amount, description,
                frequency, startDateStr, endDateStr);

        if (result != -1) {
            Toast.makeText(this, "Recurring expense added successfully", Toast.LENGTH_SHORT).show();

            // Clear form and hide it
            clearForm();
            formContainer.setVisibility(View.GONE);
            fabAddRecurring.setVisibility(View.VISIBLE);

            // Create initial expense
            expenseDb.insertExpense(userId, categoryId, amount, description + " (Recurring)",
                    startDateStr, "Automatic", true, (int)result);

            // Refresh the list
            loadRecurringExpenses();
        } else {
            Toast.makeText(this, "Failed to add recurring expense", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        etAmount.setText("");
        etDescription.setText("");
        spinnerFrequency.setSelection(0);
        if (spinnerCategory.getCount() > 0) {
            spinnerCategory.setSelection(0);
        }
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 3);
        tvStartDate.setText(dateFormat.format(startDate.getTime()));
        tvEndDate.setText(dateFormat.format(endDate.getTime()));
    }

    private void loadRecurringExpenses() {
        if (userId != -1) {
            // Get recurring expenses from database
            List<RecurringExpense> recurring = expenseDb.getRecurringExpensesByUser(userId);

            // Update UI based on results
            if (recurring.isEmpty()) {
                tvNoRecurring.setVisibility(View.VISIBLE);
                recyclerRecurring.setVisibility(View.GONE);
            } else {
                tvNoRecurring.setVisibility(View.GONE);
                recyclerRecurring.setVisibility(View.VISIBLE);

                // Add category info to recurring expenses
                for (RecurringExpense rec : recurring) {
                    for (Category category : categoryList) {
                        if (category.getId() == rec.getCategoryId()) {
                            rec.setCategoryName(category.getName());
                            rec.setCategoryColor(category.getColor());
                            break;
                        }
                    }
                }

                // Update adapter
                recurringList.clear();
                recurringList.addAll(recurring);
                recurringAdapter.notifyDataSetChanged();
            }
        }
    }

    private void deleteRecurringExpense(int id) {
        // In a real app, you'd want to confirm deletion
        boolean result = expenseDb.deleteRecurringExpense(id);
        if (result) {
            Toast.makeText(this, "Recurring expense deleted", Toast.LENGTH_SHORT).show();
            loadRecurringExpenses();
        } else {
            Toast.makeText(this, "Failed to delete recurring expense", Toast.LENGTH_SHORT).show();
        }
    }
}