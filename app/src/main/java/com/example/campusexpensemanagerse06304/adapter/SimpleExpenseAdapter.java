package com.example.campusexpensemanagerse06304.adapter;

import android.content.Context;
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

        holder.tvDescription.setText(expense.getDescription());
        holder.tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", expense.getAmount()));

        if (expense.getDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            holder.tvDate.setText(dateFormat.format(expense.getDate()));
        } else {
            holder.tvDate.setText("Unknown date");
        }
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount, tvDate;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvDate = itemView.findViewById(R.id.tvExpenseDate);
        }
    }
}