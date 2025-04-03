package com.example.campusexpensemanagerse06304.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagerse06304.R;
import com.example.campusexpensemanagerse06304.model.Expense;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private Context context;
    private List<Expense> expenseList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ExpenseAdapter(Context context, List<Expense> expenseList) {
        this.context = context;
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        // Format amount with currency symbol
        String amountText = String.format(Locale.getDefault(), "$%.2f", expense.getAmount());
        holder.tvAmount.setText(amountText);

        // Set description
        holder.tvDescription.setText(expense.getDescription());

        // Set category
        holder.tvCategory.setText(expense.getCategoryName());

        // Set date in readable format
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(expense.getDate()));

        // Set payment method
        holder.tvPaymentMethod.setText(expense.getPaymentMethod());

        // Set recurring indicator visibility
        if (expense.isRecurring()) {
            holder.ivRecurring.setVisibility(View.VISIBLE);
        } else {
            holder.ivRecurring.setVisibility(View.GONE);
        }

        // Set category color indicator
        if (expense.getCategoryColor() != null && !expense.getCategoryColor().isEmpty()) {
            try {
                int color = Color.parseColor(expense.getCategoryColor());
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
        return expenseList == null ? 0 : expenseList.size();
    }

    public Expense getItemAt(int position) {
        return expenseList.get(position);
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView tvAmount, tvDescription, tvCategory, tvDate, tvPaymentMethod;
        public ImageView ivRecurring, ivDelete;
        public View vCategoryColor;

        public ExpenseViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardExpense);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvCategory = itemView.findViewById(R.id.tvExpenseCategory);
            tvDate = itemView.findViewById(R.id.tvExpenseDate);
            tvPaymentMethod = itemView.findViewById(R.id.tvExpensePaymentMethod);
            ivRecurring = itemView.findViewById(R.id.ivRecurring);
            ivDelete = itemView.findViewById(R.id.ivDeleteExpense);
            vCategoryColor = itemView.findViewById(R.id.vCategoryColor);

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