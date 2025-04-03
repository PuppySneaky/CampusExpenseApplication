package com.example.campusexpensemanagerse06304.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecurringExpense {
    private int id;
    private int userId;
    private int categoryId;
    private double amount;
    private String description;
    private String frequency; // daily, weekly, monthly, yearly
    private Date startDate;
    private Date endDate;
    private Date lastCharged;
    private Date nextCharge;
    private String categoryName; // For display purposes
    private String categoryColor; // For display purposes

    public RecurringExpense() {
        // Default constructor
    }

    public RecurringExpense(int id, int userId, int categoryId, double amount, String description,
                            String frequency, Date startDate, Date endDate) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description;
        this.frequency = frequency;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
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

    public Date getLastCharged() {
        return lastCharged;
    }

    public void setLastCharged(Date lastCharged) {
        this.lastCharged = lastCharged;
    }

    public String getFormattedLastCharged() {
        if (lastCharged == null) return "Never";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(lastCharged);
    }

    public Date getNextCharge() {
        return nextCharge;
    }

    public void setNextCharge(Date nextCharge) {
        this.nextCharge = nextCharge;
    }

    public String getFormattedNextCharge() {
        if (nextCharge == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(nextCharge);
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
        return description + " - $" + amount + " (" + frequency + ")";
    }
}