package com.example.campusexpensemanagerse06304.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Expense {
    private int id;
    private int userId;
    private int categoryId;
    private double amount;
    private String description;
    private Date date;
    private String paymentMethod;
    private boolean isRecurring;
    private Integer recurringExpenseId;
    private String categoryName; // For display purposes
    private String categoryColor; // For display purposes

    public Expense() {
        // Default constructor
    }

    public Expense(int id, int userId, int categoryId, double amount, String description,
                   Date date, String paymentMethod, boolean isRecurring, Integer recurringExpenseId) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.isRecurring = isRecurring;
        this.recurringExpenseId = recurringExpenseId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public Integer getRecurringExpenseId() {
        return recurringExpenseId;
    }

    public void setRecurringExpenseId(Integer recurringExpenseId) {
        this.recurringExpenseId = recurringExpenseId;
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

    @Override
    public String toString() {
        return description + " - $" + amount;
    }
}