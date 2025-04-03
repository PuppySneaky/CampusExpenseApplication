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
import com.example.campusexpensemanagerse06304.model.RecurringExpense;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RecurringExpenseAdapter extends RecyclerView.Adapter<RecurringExpenseAdapter.RecurringViewHolder> {

    private Context context;
    private List<RecurringExpense> recurringList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public RecurringExpenseAdapter(Context context, List<RecurringExpense> recurringList) {
        this.context = context;
        this.recurringList = recurringList;
    }

    @NonNull
    @Override
    public RecurringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recurring_expense, parent, false);
        return new RecurringViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecurringViewHolder holder, int position) {
        RecurringExpense recurring = recurringList.get(position);

        // Format amount with currency symbol
        String amountText = String.format(Locale.getDefault(), "$%.2f", recurring.getAmount());
        holder.tvAmount.setText(amountText);

        // Set description
        holder.tvDescription.setText(recurring.getDescription());

        // Set frequency with capitalized first letter
        String frequency = recurring.getFrequency();
        if (frequency != null && !frequency.isEmpty()) {
            frequency = frequency.substring(0, 1).toUpperCase() + frequency.substring(1);
            holder.tvFrequency.setText(frequency);
        } else {
            holder.tvFrequency.setText("Unknown");
        }

        // Set category with color
        if (recurring.getCategoryName() != null) {
            holder.tvCategory.setText(recurring.getCategoryName());

            // Set category background color
            if (recurring.getCategoryColor() != null && !recurring.getCategoryColor().isEmpty()) {
                try {
                    int color = Color.parseColor(recurring.getCategoryColor());
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

        // Set date period
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        StringBuilder datePeriod = new StringBuilder();

        if (recurring.getStartDate() != null) {
            datePeriod.append("From ").append(sdf.format(recurring.getStartDate()));

            if (recurring.getEndDate() != null) {
                datePeriod.append(" to ").append(sdf.format(recurring.getEndDate()));
            } else {
                datePeriod.append(" (ongoing)");
            }
        }

        holder.tvDatePeriod.setText(datePeriod.toString());

        // Set next charge info
        if (recurring.getNextCharge() != null) {
            holder.tvNextCharge.setText("Next: " + sdf.format(recurring.getNextCharge()));
            holder.tvNextCharge.setVisibility(View.VISIBLE);
        } else {
            holder.tvNextCharge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return recurringList.size();
    }

    static class RecurringViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvDescription, tvAmount, tvCategory, tvFrequency, tvDatePeriod, tvNextCharge;
        View vCategoryColor;
        ImageView ivDelete;

        public RecurringViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardRecurring);
            tvDescription = itemView.findViewById(R.id.tvRecurringDescription);
            tvAmount = itemView.findViewById(R.id.tvRecurringAmount);
            tvCategory = itemView.findViewById(R.id.tvRecurringCategory);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
            tvDatePeriod = itemView.findViewById(R.id.tvDatePeriod);
            tvNextCharge = itemView.findViewById(R.id.tvNextCharge);
            vCategoryColor = itemView.findViewById(R.id.vCategoryColor);
            ivDelete = itemView.findViewById(R.id.ivDeleteRecurring);

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