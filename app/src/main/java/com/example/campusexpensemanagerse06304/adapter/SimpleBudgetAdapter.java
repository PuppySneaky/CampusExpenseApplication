package com.example.campusexpensemanagerse06304.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagerse06304.R;
import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.example.campusexpensemanagerse06304.model.Budget;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SimpleBudgetAdapter extends RecyclerView.Adapter<SimpleBudgetAdapter.BudgetViewHolder> {

    private static final String TAG = "SimpleBudgetAdapter";
    private final Context context;
    private final List<Budget> budgetList;
    private ExpenseDb expenseDb;
    private OnBudgetActionListener listener;

    public interface OnBudgetActionListener {
        void onBudgetAdjusted();
    }

    public SimpleBudgetAdapter(Context context, List<Budget> budgetList) {
        this.context = context;
        this.budgetList = budgetList;
        this.expenseDb = new ExpenseDb(context);
    }

    public void setOnBudgetActionListener(OnBudgetActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);
        int userId = budget.getUserId();

        // Set category name and color
        holder.tvCategory.setText(budget.getCategoryName());
        if (budget.getCategoryColor() != null && !budget.getCategoryColor().isEmpty()) {
            try {
                int color = Color.parseColor(budget.getCategoryColor());
                holder.vCategoryColor.setBackgroundColor(color);
            } catch (Exception e) {
                holder.vCategoryColor.setBackgroundColor(Color.GRAY);
            }
        } else {
            holder.vCategoryColor.setBackgroundColor(Color.GRAY);
        }

        // Set budget period (monthly, weekly, etc)
        String period = budget.getPeriod();
        if (period != null && !period.isEmpty()) {
            period = period.substring(0, 1).toUpperCase() + period.substring(1);
        } else {
            period = "Monthly";
        }
        holder.tvPeriod.setText(period);

        // Set budget amount
        holder.tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", budget.getAmount()));

        // Calculate percentage of total budget
        double totalBudget = expenseDb.getTotalBudget(userId, "monthly");
        double percentOfTotal = 0;
        if (totalBudget > 0) {
            percentOfTotal = (budget.getAmount() / totalBudget) * 100;
        }
        holder.tvPercentageOfTotal.setText(String.format(Locale.getDefault(), "(%.1f%% of total)", percentOfTotal));

        // Calculate progress
        double spent = budget.getSpent();
        double amount = budget.getAmount();
        int progressPercent = amount > 0 ? (int)((spent / amount) * 100) : 0;
        holder.progressBar.setProgress(progressPercent);

        // Set progress color based on percentage
        if (progressPercent < 70) {
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50)); // Green
        } else if (progressPercent < 90) {
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFFF9800)); // Orange
        } else {
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFF44336)); // Red
        }

        // Set spent/budget text
        holder.tvSpent.setText(String.format(Locale.getDefault(), "$%.2f / $%.2f", spent, amount));

        // Set percentage text
        holder.tvPercentage.setText(String.format(Locale.getDefault(), "%d%%", progressPercent));

        // Set remaining amount
        double remaining = amount - spent;
        holder.tvRemaining.setText(String.format(Locale.getDefault(), "$%.2f", remaining));
        if (remaining < 0) {
            holder.tvRemaining.setTextColor(0xFFF44336); // Red for negative
        } else {
            holder.tvRemaining.setTextColor(0xFF4CAF50); // Green for positive
        }

        // Setup action buttons
        holder.btnAdjust.setOnClickListener(v -> showAdjustBudgetDialog(budget, position));
        holder.btnDelete.setOnClickListener(v -> showDeleteBudgetDialog(budget, position));
    }

    private void showAdjustBudgetDialog(Budget budget, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Adjust Budget for " + budget.getCategoryName());

        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_adjust_budget, null);
        builder.setView(dialogView);

        // Get dialog views
        EditText etNewAmount = dialogView.findViewById(R.id.etNewBudgetAmount);
        etNewAmount.setHint("Current: $" + String.format(Locale.getDefault(), "%.2f", budget.getAmount()));

        builder.setPositiveButton("Update", null); // Set button later to prevent auto-dismiss
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override positive button to validate input before dismissing
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Validate input
            String amountStr = etNewAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                etNewAmount.setError("Please enter an amount");
                return;
            }

            try {
                double newAmount = Double.parseDouble(amountStr);
                if (newAmount <= 0) {
                    etNewAmount.setError("Amount must be greater than zero");
                    return;
                }

                // Check if new amount would exceed total budget
                if (!expenseDb.validateCategoryBudget(budget.getUserId(), newAmount)) {
                    Toast.makeText(context, "This amount would exceed your total budget", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update budget
                updateBudgetAmount(budget, newAmount);
                dialog.dismiss();
            } catch (NumberFormatException e) {
                etNewAmount.setError("Invalid number format");
            }
        });
    }

    private void showDeleteBudgetDialog(Budget budget, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete the budget for " + budget.getCategoryName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteBudget(budget, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateBudgetAmount(Budget budget, double newAmount) {
        try {
            // Get date info - this would normally come from the budget date range
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String startDate = budget.getFormattedStartDate();
            String endDate = budget.getFormattedEndDate();

            // Update in database
            int result = expenseDb.updateBudget(
                    budget.getId(),
                    budget.getCategoryId(),
                    newAmount,
                    budget.getPeriod(),
                    startDate,
                    endDate
            );

            if (result > 0) {
                // Update in local list
                budget.setAmount(newAmount);
                notifyDataSetChanged();

                // Notify listeners
                if (listener != null) {
                    listener.onBudgetAdjusted();
                }

                Toast.makeText(context, "Budget updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to update budget", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating budget", e);
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteBudget(Budget budget, int position) {
        try {
            // Delete from database
            int result = expenseDb.deleteBudget(budget.getId());

            if (result > 0) {
                // Remove from local list
                budgetList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, budgetList.size());

                // Notify listeners
                if (listener != null) {
                    listener.onBudgetAdjusted();
                }

                Toast.makeText(context, "Budget deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete budget", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting budget", e);
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvPeriod, tvAmount, tvPercentageOfTotal,
                tvSpent, tvPercentage, tvRemaining;
        View vCategoryColor;
        ProgressBar progressBar;
        Button btnAdjust, btnDelete;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvBudgetCategory);
            tvPeriod = itemView.findViewById(R.id.tvBudgetPeriod);
            tvAmount = itemView.findViewById(R.id.tvBudgetAmount);
            tvPercentageOfTotal = itemView.findViewById(R.id.tvPercentageOfTotal);
            tvSpent = itemView.findViewById(R.id.tvBudgetSpent);
            tvPercentage = itemView.findViewById(R.id.tvBudgetPercentage);
            tvRemaining = itemView.findViewById(R.id.tvBudgetRemaining);
            vCategoryColor = itemView.findViewById(R.id.vBudgetCategoryColor);
            progressBar = itemView.findViewById(R.id.progressBudget);
            btnAdjust = itemView.findViewById(R.id.btnAdjustBudget);
            btnDelete = itemView.findViewById(R.id.btnDeleteBudget);
        }
    }
}