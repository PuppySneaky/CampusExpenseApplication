package com.example.campusexpensemanagerse06304.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagerse06304.R;
import com.example.campusexpensemanagerse06304.model.Budget;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private Context context;
    private List<Budget> budgetList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public BudgetAdapter(Context context, List<Budget> budgetList) {
        this.context = context;
        this.budgetList = budgetList;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);

        // Format amount with currency symbol
        String budgetAmountText = String.format(Locale.getDefault(), "$%.2f", budget.getAmount());
        holder.tvAmount.setText(budgetAmountText);

        // Set category name
        holder.tvCategory.setText(budget.getCategoryName());

        // Set period
        String periodText = budget.getPeriod().substring(0, 1).toUpperCase() + budget.getPeriod().substring(1);
        holder.tvPeriod.setText(periodText);

        // Calculate and set progress percentage
        int progressPercent = (int) budget.getSpentPercentage();
        holder.progressBudget.setProgress(progressPercent);

        // Set spending details
        String spentText = String.format(Locale.getDefault(), "$%.2f / $%.2f", budget.getSpent(), budget.getAmount());
        holder.tvSpent.setText(spentText);

        // Set percentage text
        String percentageText = String.format(Locale.getDefault(), "%.1f%%", budget.getSpentPercentage());
        holder.tvPercentage.setText(percentageText);

        // Set remaining amount
        String remainingText = String.format(Locale.getDefault(), "$%.2f remaining", budget.getRemaining());
        holder.tvRemaining.setText(remainingText);

        // Set date range
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        String startDateText = sdf.format(budget.getStartDate());
        String dateRangeText = "From " + startDateText;

        if (budget.getEndDate() != null) {
            String endDateText = sdf.format(budget.getEndDate());
            dateRangeText += " to " + endDateText;
        }

        holder.tvDateRange.setText(dateRangeText);

        // Set progress bar color based on percentage
        if (progressPercent < 70) {
            holder.progressBudget.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Green
        } else if (progressPercent < 90) {
            holder.progressBudget.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))); // Orange
        } else {
            holder.progressBudget.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))); // Red
        }

        // Set category color indicator
        if (budget.getCategoryColor() != null && !budget.getCategoryColor().isEmpty()) {
            try {
                int color = Color.parseColor(budget.getCategoryColor());
                holder.vCategoryColor.setBackgroundColor(color);
            } catch (Exception e) {
                // Use default color if parsing fails
                holder.vCategoryColor.setBackgroundColor(Color.GRAY);
            }
        } else {
            holder.vCategoryColor.setBackgroundColor(Color.GRAY);
        }
    }

    @Override
    public int getItemCount() {
        return budgetList == null ? 0 : budgetList.size();
    }

    public Budget getItemAt(int position) {
        return budgetList.get(position);
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView tvCategory, tvAmount, tvPeriod, tvSpent, tvPercentage, tvRemaining, tvDateRange;
        public ProgressBar progressBudget;
        public ImageView ivDelete;
        public View vCategoryColor;

        public BudgetViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardBudget);
            tvCategory = itemView.findViewById(R.id.tvBudgetCategory);
            tvAmount = itemView.findViewById(R.id.tvBudgetAmount);
            tvPeriod = itemView.findViewById(R.id.tvBudgetPeriod);
            tvSpent = itemView.findViewById(R.id.tvBudgetSpent);
            tvPercentage = itemView.findViewById(R.id.tvBudgetPercentage);
            tvRemaining = itemView.findViewById(R.id.tvBudgetRemaining);
            tvDateRange = itemView.findViewById(R.id.tvBudgetDateRange);
            progressBudget = itemView.findViewById(R.id.progressBudget);
            ivDelete = itemView.findViewById(R.id.ivDeleteBudget);
            vCategoryColor = itemView.findViewById(R.id.vBudgetCategoryColor);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });

            ivDelete.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });
        }
    }
}