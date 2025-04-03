package com.example.campusexpensemanagerse06304;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagerse06304.adapter.CategoryAdapter;
import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.example.campusexpensemanagerse06304.model.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CategoryManagementActivity extends AppCompatActivity {
    private RecyclerView recyclerCategories;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;
    private ExpenseDb expenseDb;
    private FloatingActionButton fabAddCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        // Initialize views
        recyclerCategories = findViewById(R.id.recyclerCategories);
        fabAddCategory = findViewById(R.id.fabAddCategory);

        // Set up toolbar
        TextView tvTitle = findViewById(R.id.tvTitle);
        Button btnBack = findViewById(R.id.btnBack);
        tvTitle.setText("Manage Categories");
        btnBack.setOnClickListener(v -> finish());

        // Initialize database
        expenseDb = new ExpenseDb(this);

        // Set up RecyclerView
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categoryList);
        recyclerCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerCategories.setAdapter(categoryAdapter);

        // Set up item click listener
        categoryAdapter.setOnItemClickListener(new CategoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                editCategory(categoryList.get(position));
            }

            @Override
            public void onDeleteClick(int position) {
                deleteCategory(categoryList.get(position));
            }
        });

        // Set up FAB
        fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        // Load categories
        loadCategories();
    }

    private void loadCategories() {
        List<Category> categories = expenseDb.getAllCategories();
        categoryList.clear();
        categoryList.addAll(categories);
        categoryAdapter.notifyDataSetChanged();
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        final EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        final EditText etCategoryDescription = dialogView.findViewById(R.id.etCategoryDescription);
        Button btnSave = dialogView.findViewById(R.id.btnSaveCategory);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelCategory);

        final AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();
            String description = etCategoryDescription.getText().toString().trim();

            if (name.isEmpty()) {
                etCategoryName.setError("Category name cannot be empty");
                return;
            }

            // Generate a random color
            String color = generateRandomColor();

            // Save to database
            long result = expenseDb.insertCategory(name, description, "category", color);

            if (result != -1) {
                Toast.makeText(CategoryManagementActivity.this, "Category added successfully", Toast.LENGTH_SHORT).show();
                loadCategories();
                dialog.dismiss();
            } else {
                Toast.makeText(CategoryManagementActivity.this, "Failed to add category", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void editCategory(Category category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        final EditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        final EditText etCategoryDescription = dialogView.findViewById(R.id.etCategoryDescription);
        Button btnSave = dialogView.findViewById(R.id.btnSaveCategory);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelCategory);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);

        // Set existing values
        tvDialogTitle.setText("Edit Category");
        etCategoryName.setText(category.getName());
        etCategoryDescription.setText(category.getDescription());

        final AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();
            String description = etCategoryDescription.getText().toString().trim();

            if (name.isEmpty()) {
                etCategoryName.setError("Category name cannot be empty");
                return;
            }

            // Update category in database
            category.setName(name);
            category.setDescription(description);

            // This would need a new method in ExpenseDb
            boolean result = expenseDb.updateCategory(category);

            if (result) {
                Toast.makeText(CategoryManagementActivity.this, "Category updated successfully", Toast.LENGTH_SHORT).show();
                loadCategories();
                dialog.dismiss();
            } else {
                Toast.makeText(CategoryManagementActivity.this, "Failed to update category", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void deleteCategory(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete this category? This will affect any expenses using this category.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete from database
                    boolean result = expenseDb.deleteCategory(category.getId());

                    if (result) {
                        Toast.makeText(CategoryManagementActivity.this, "Category deleted successfully", Toast.LENGTH_SHORT).show();
                        loadCategories();
                    } else {
                        Toast.makeText(CategoryManagementActivity.this, "Failed to delete category", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String generateRandomColor() {
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);

        // Ensure color is not too light
        while ((r + g + b) > 600) {
            r = random.nextInt(256);
            g = random.nextInt(256);
            b = random.nextInt(256);
        }

        return String.format("#%02X%02X%02X", r, g, b);
    }
}