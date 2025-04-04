package com.example.campusexpensemanagerse06304.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagerse06304.R;
import com.example.campusexpensemanagerse06304.model.Budget;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SimpleBudgetAdapter extends RecyclerView.Adapter<SimpleBudgetAdapter.BudgetViewHolder> {

    private final Context context;
    private final List<Budget> budgetList;

    public SimpleBudgetAdapter(Context context, List<Budget> budgetList) {
        this.context = context;
        this.budgetList = budgetList;
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

        // Set description
        holder.tvDescription.setText(budget.getPeriod().substring(0, 1).toUpperCase() +
                budget.getPeriod().substring(1) + " Budget");

        // Set amount
        holder.tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", budget.getAmount()));

        // Calculate progress and set progress bar
        double spent = budget.getSpent();
        double amount = budget.getAmount();
        int progress = amount > 0 ? (int)((spent / amount) * 100) : 0;
        holder.progressBar.setProgress(progress);

        // Set color based on progress
        if (progress < 70) {
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50)); // Green
        } else if (progress < 90) {
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFFF9800)); // Orange
        } else {
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFF44336)); // Red
        }

        // Set spent and remaining text
        holder.tvSpent.setText(String.format(Locale.getDefault(), "Spent: $%.2f", spent));
        holder.tvRemaining.setText(String.format(Locale.getDefault(), "$%.2f remaining",
                Math.max(0, amount - spent)));

        // Set period
        if (budget.getStartDate() != null) {
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            holder.tvPeriod.setText(monthFormat.format(budget.getStartDate()));
        } else {
            holder.tvPeriod.setText("Current Month");
        }
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount, tvSpent, tvRemaining, tvPeriod;
        ProgressBar progressBar;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvBudgetDescription);
            tvAmount = itemView.findViewById(R.id.tvBudgetAmount);
            tvSpent = itemView.findViewById(R.id.tvSpent);
            tvRemaining = itemView.findViewById(R.id.tvRemaining);
            tvPeriod = itemView.findViewById(R.id.tvBudgetPeriod);
            progressBar = itemView.findViewById(R.id.progressBudget);
        }
    }

    // Add method to update the budget list with new data
    public void updateBudgets(List<Budget> newBudgets) {
        this.budgetList.clear();
        this.budgetList.addAll(newBudgets);
        notifyDataSetChanged();
    }
}