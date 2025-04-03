package com.example.campusexpensemanagerse06304.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Budget {
    private int id;
    private int userId;
    private int categoryId;
    private double amount;
    private String period; // monthly, weekly, etc.
    private Date startDate;
    private Date endDate;
    private String categoryName; // For display purposes
    private String categoryColor; // For display purposes
    private double spent; // For tracking against budget

    public Budget() {
        // Default constructor
    }

    public Budget(int id, int userId, int categoryId, double amount, String period,
                  Date startDate, Date endDate) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getFormattedStartDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(startDate);
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getFormattedEndDate() {
        if (endDate == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(endDate);
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public double getSpent() {
        return spent;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    public double getRemaining() {
        return amount - spent;
    }

    public double getSpentPercentage() {
        if (amount == 0) return 0;
        return (spent / amount) * 100;
    }

    @Override
    public String toString() {
        return categoryName + " - $" + amount;
    }
}