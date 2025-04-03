package com.example.campusexpensemanagerse06304.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagerse06304.R;
import com.example.campusexpensemanagerse06304.model.Expense;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SimpleExpenseAdapter extends RecyclerView.Adapter<SimpleExpenseAdapter.ExpenseViewHolder> {

    private Context context;
    private List<Expense> expenseList;

    public SimpleExpenseAdapter(Context context, List<Expense> expenseList) {
        this.context = context;
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        // Format amount with currency symbol
        String amountText = String.format(Locale.getDefault(), "$%.2f", expense.getAmount());
        holder.tvAmount.setText(amountText);

        // Set description
        holder.tvDescription.setText(expense.getDescription());

        // Set category with color
        if (expense.getCategoryName() != null) {
            holder.tvCategory.setText(expense.getCategoryName());

            // Set category background color
            if (expense.getCategoryColor() != null && !expense.getCategoryColor().isEmpty()) {
                try {
                    int color = Color.parseColor(expense.getCategoryColor());
                    holder.tvCategory.setBackgroundColor(color);
                    holder.vCategoryColor.setBackgroundColor(color);
                } catch (Exception e) {
                    // Use default color if parsing fails
                    holder.tvCategory.setBackgroundColor(Color.GRAY);
                    holder.vCategoryColor.setBackgroundColor(Color.GRAY);
                }
            } else {
                holder.tvCategory.setBackgroundColor(Color.GRAY);
                holder.vCategoryColor.setBackgroundColor(Color.GRAY);
            }
        } else {
            holder.tvCategory.setText("Uncategorized");
            holder.tvCategory.setBackgroundColor(Color.GRAY);
            holder.vCategoryColor.setBackgroundColor(Color.GRAY);
        }

        // Set date in readable format
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        if (expense.getDate() != null) {
            holder.tvDate.setText(sdf.format(expense.getDate()));
        } else {
            holder.tvDate.setText("Unknown date");
        }
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount, tvDate, tvCategory;
        View vCategoryColor;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvDate = itemView.findViewById(R.id.tvExpenseDate);
            tvCategory = itemView.findViewById(R.id.tvExpenseCategory);
            vCategoryColor = itemView.findViewById(R.id.vCategoryColor);
        }
    }
}