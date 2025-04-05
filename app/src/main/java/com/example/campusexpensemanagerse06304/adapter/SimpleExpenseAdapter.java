package com.example.campusexpensemanagerse06304.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagerse06304.R;
import com.example.campusexpensemanagerse06304.model.Expense;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class SimpleExpenseAdapter extends RecyclerView.Adapter<SimpleExpenseAdapter.ExpenseViewHolder> {

    private boolean isMultiSelectMode = false;
    private Set<Integer> selectedItems = new HashSet<>();
    private OnExpenseActionListener listener;





    public interface OnExpenseActionListener {
        void onEditExpense(Expense expense);
        void onDeleteExpense(Expense expense);
        void onMultiDeleteExpenses(List<Expense> expenses);
    }


    // Add this setter method
    public void setOnExpenseActionListener(OnExpenseActionListener listener) {
        this.listener = listener;
    }


    // Add these methods to the SimpleExpenseAdapter class
    public void toggleMultiSelectMode() {
        isMultiSelectMode = !isMultiSelectMode;
        if (!isMultiSelectMode) {
            selectedItems.clear();
        }
        notifyDataSetChanged();
    }


    public boolean isMultiSelectMode() {
        return isMultiSelectMode;
    }

    public void toggleItemSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifyItemChanged(position);
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Expense> getSelectedExpenses() {
        List<Expense> selected = new ArrayList<>();
        for (Integer position : selectedItems) {
            if (position >= 0 && position < expenseList.size()) {
                selected.add(expenseList.get(position));
            }
        }
        return selected;
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }



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

    // Update the onBindViewHolder method in SimpleExpenseAdapter
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

        // Add this section for multi-select and edit buttons
        if (isMultiSelectMode) {
            // Show selection state
            holder.itemContainer.setBackgroundColor(selectedItems.contains(position) ?
                    ContextCompat.getColor(context, R.color.selectedItem) :
                    ContextCompat.getColor(context, R.color.normalItem));

            holder.ivEdit.setVisibility(View.GONE);
            holder.ivDelete.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(v -> {
                toggleItemSelection(position);
            });

        } else {
            // Normal mode
            holder.itemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.normalItem));

            // Set visibility based on whether this is a recurring expense
            boolean isRecurring = expense.isRecurring();
            holder.ivEdit.setVisibility(isRecurring ? View.GONE : View.VISIBLE);
            holder.ivDelete.setVisibility(View.VISIBLE);

            // Set click listeners
            holder.ivEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditExpense(expense);
                }
            });

            holder.ivDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteExpense(expense);
                }
            });

            holder.itemView.setOnLongClickListener(v -> {
                toggleMultiSelectMode();
                toggleItemSelection(position);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    // Replace/Update the current ExpenseViewHolder class in SimpleExpenseAdapter
    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount, tvDate, tvCategory;
        View vCategoryColor;
        ImageView ivEdit, ivDelete; // Add these fields
        View itemContainer; // Add this field for the container view

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvDate = itemView.findViewById(R.id.tvExpenseDate);
            tvCategory = itemView.findViewById(R.id.tvExpenseCategory);
            vCategoryColor = itemView.findViewById(R.id.vCategoryColor);
            ivEdit = itemView.findViewById(R.id.ivEditExpense); // Add this line
            ivDelete = itemView.findViewById(R.id.ivDeleteExpense); // Add this line
            itemContainer = itemView.findViewById(R.id.expenseItemContainer); // Add this line
        }
    }
}