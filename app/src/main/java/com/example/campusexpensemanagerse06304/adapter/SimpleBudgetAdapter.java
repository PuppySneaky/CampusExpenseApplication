package com.example.campusexpensemanagerse06304.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
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
        View view = LayoutInflater.from(context).inflate(R.layout.budget_item, parent, false);
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
        SeekBar sliderBudgetPercentage = dialogView.findViewById(R.id.sliderBudgetPercentage);
        TextView tvSliderValue = dialogView.findViewById(R.id.tvSliderValue);
        EditText etNewAmount = dialogView.findViewById(R.id.etNewBudgetAmount);
        TextView tvCurrentBudgetInfo = dialogView.findViewById(R.id.tvCurrentBudgetInfo);
        TextView tvTotalBudgetInfo = dialogView.findViewById(R.id.tvTotalBudgetInfo);
        TextView tvAllocatedBudgetInfo = dialogView.findViewById(R.id.tvAllocatedBudgetInfo);
        TextView tvAvailableBudgetInfo = dialogView.findViewById(R.id.tvAvailableBudgetInfo);

        // Get important values
        int userId = budget.getUserId();
        double totalBudget = expenseDb.getTotalBudget(userId, "monthly");
        double currentAmount = budget.getAmount();
        double currentSpent = budget.getSpent();
        double currentPercentOfTotal = totalBudget > 0 ? (currentAmount / totalBudget) * 100 : 0;

        // Calculate available budget using our helper method
        double availableBudget = calculateAvailableBudget(budget);
        double maxAllowable = availableBudget + currentAmount;

        // Calculate total allocated across ALL categories
        double totalAllocated = 0;
        for (Budget b : budgetList) {
            totalAllocated += b.getAmount();
        }

        // Set initial values
        etNewAmount.setHint("Current: $" + String.format(Locale.getDefault(), "%.2f", currentAmount));
        tvCurrentBudgetInfo.setText(String.format(Locale.getDefault(),
                "Current budget: $%.2f (Spent: $%.2f)", currentAmount, currentSpent));
        tvTotalBudgetInfo.setText(String.format(Locale.getDefault(),
                "Your total monthly budget: $%.2f", totalBudget));
        tvAllocatedBudgetInfo.setText(String.format(Locale.getDefault(),
                "Total allocated across all categories: $%.2f", totalAllocated));
        tvAvailableBudgetInfo.setText(String.format(Locale.getDefault(),
                "Maximum for this category: $%.2f",
                maxAllowable));

        // Set initial slider position
        sliderBudgetPercentage.setProgress((int)currentPercentOfTotal);
        tvSliderValue.setText(String.format(Locale.getDefault(), "%.1f%% of total budget ($%.2f)",
                currentPercentOfTotal, currentAmount));

        // Calculate the maximum allowable percentage
        int maxPercentage = totalBudget > 0 ? (int)((maxAllowable / totalBudget) * 100) : 100;

        // Limit slider to maximum available budget
        sliderBudgetPercentage.setMax(maxPercentage);

        // Set up slider change listener
        sliderBudgetPercentage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // Ensure progress doesn't exceed max allowable value
                    if (progress > maxPercentage) {
                        progress = maxPercentage;
                        seekBar.setProgress(progress);
                    }

                    double amount = (progress / 100.0) * totalBudget;

                    // Ensure amount doesn't exceed max allowable
                    if (amount > maxAllowable) {
                        amount = maxAllowable;
                    }

                    tvSliderValue.setText(String.format(Locale.getDefault(),
                            "%.1f%% of total budget ($%.2f)", (double)progress, amount));

                    // Update the amount field
                    etNewAmount.setText(String.format(Locale.getDefault(), "%.2f", amount));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Set up amount field change listener
        etNewAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (!s.toString().isEmpty()) {
                        double amount = Double.parseDouble(s.toString());

                        // Check if amount exceeds max allowable
                        if (amount > maxAllowable) {
                            // Set error immediately on the EditText
                            etNewAmount.setError("Cannot exceed maximum: $" +
                                    String.format(Locale.getDefault(), "%.2f", maxAllowable));
                            return;
                        }

                        int percentage = totalBudget > 0 ? (int)((amount / totalBudget) * 100) : 0;

                        // Update slider without triggering its listener
                        sliderBudgetPercentage.setOnSeekBarChangeListener(null);
                        sliderBudgetPercentage.setProgress(percentage);
                        sliderBudgetPercentage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    double newAmount = (progress / 100.0) * totalBudget;
                                    if (newAmount > maxAllowable) {
                                        newAmount = maxAllowable;
                                    }
                                    tvSliderValue.setText(String.format(Locale.getDefault(),
                                            "%.1f%% of total budget ($%.2f)", (double)progress, newAmount));

                                    // Update the amount field
                                    etNewAmount.setText(String.format(Locale.getDefault(), "%.2f", newAmount));
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                            }
                        });

                        tvSliderValue.setText(String.format(Locale.getDefault(),
                                "%.1f%% of total budget ($%.2f)", (double)percentage, amount));
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        builder.setPositiveButton("Update", null); // Set button later to prevent auto-dismiss
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override positive button to validate input before dismissing
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Validate input
            String amountStr = etNewAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                // If empty, use the slider value
                double amount = (sliderBudgetPercentage.getProgress() / 100.0) * totalBudget;
                amountStr = String.format(Locale.getDefault(), "%.2f", amount);
                etNewAmount.setText(amountStr);
            }

            try {
                double newAmount = Double.parseDouble(amountStr);
                if (newAmount <= 0) {
                    etNewAmount.setError("Amount must be greater than zero");
                    return;
                }

                // Final check to ensure amount doesn't exceed available budget
                if (newAmount > maxAllowable) {
                    etNewAmount.setError("This amount exceeds your available budget");
                    Toast.makeText(context,
                            String.format(Locale.getDefault(),
                                    "Maximum allowable is $%.2f", maxAllowable),
                            Toast.LENGTH_SHORT).show();
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

    // This method to the SimpleBudgetAdapter class to calculate the correct available budget
    // considering all other category allocations

    private double calculateAvailableBudget(Budget currentBudget) {
        int userId = currentBudget.getUserId();
        double totalBudget = expenseDb.getTotalBudget(userId, "monthly");
        double totalAllocated = 0;

        // Sum up all allocations EXCEPT the current budget being adjusted
        for (Budget budget : budgetList) {
            if (budget.getId() != currentBudget.getId()) {
                totalAllocated += budget.getAmount();
            }
        }

        // Available budget is total budget minus all other allocations
        double availableBudget = totalBudget - totalAllocated;

        Log.d(TAG, "Total budget: $" + totalBudget);
        Log.d(TAG, "Total allocated to other categories: $" + totalAllocated);
        Log.d(TAG, "Available budget for this category: $" + availableBudget);
        Log.d(TAG, "Current category budget: $" + currentBudget.getAmount());

        return availableBudget;
    }


}