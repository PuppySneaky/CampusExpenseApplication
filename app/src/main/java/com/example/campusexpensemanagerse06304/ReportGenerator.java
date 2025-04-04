package com.example.campusexpensemanagerse06304;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.example.campusexpensemanagerse06304.model.Category;
import com.example.campusexpensemanagerse06304.model.Expense;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class to generate expense reports in different formats
 */
public class ReportGenerator {
    private static final String TAG = "ReportGenerator";
    private Context context;
    private ExpenseDb expenseDb;

    public ReportGenerator(Context context) {
        this.context = context;
        this.expenseDb = new ExpenseDb(context);
    }

    /**
     * Generate a CSV expense report for a specific time period
     * @param userId User ID
     * @param startDate Start date in format yyyy-MM-dd
     * @param endDate End date in format yyyy-MM-dd
     * @return Path to the generated CSV file, or null if generation failed
     */
    public String generateCSVReport(int userId, String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date start, end;

        try {
            start = dateFormat.parse(startDate);
            end = dateFormat.parse(endDate);
        } catch (ParseException e) {
            Log.e(TAG, "Invalid date format", e);
            return null;
        }

        // Get all expenses for the user
        List<Expense> allExpenses = expenseDb.getExpensesByUser(userId);
        List<Expense> filteredExpenses = new ArrayList<>();

        // Filter expenses by date range
        for (Expense expense : allExpenses) {
            if (expense.getDate() != null &&
                    (expense.getDate().after(start) || expense.getDate().equals(start)) &&
                    (expense.getDate().before(end) || expense.getDate().equals(end))) {
                filteredExpenses.add(expense);
            }
        }

        if (filteredExpenses.isEmpty()) {
            return null;
        }

        // Get all categories for mapping
        List<Category> categories = expenseDb.getAllCategories();
        Map<Integer, String> categoryMap = new HashMap<>();
        for (Category category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }

        // Build CSV content
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Date,Amount,Description,Category,Payment Method\n");

        for (Expense expense : filteredExpenses) {
            csvBuilder.append(dateFormat.format(expense.getDate())).append(",");
            csvBuilder.append(expense.getAmount()).append(",");
            csvBuilder.append("\"").append(expense.getDescription().replace("\"", "\"\"")).append("\",");
            csvBuilder.append("\"").append(categoryMap.getOrDefault(expense.getCategoryId(), "Unknown")).append("\",");
            csvBuilder.append("\"").append(expense.getPaymentMethod()).append("\"\n");
        }

        // Create summary section
        csvBuilder.append("\n\nSummary by Category\n");
        csvBuilder.append("Category,Total Amount\n");

        // Calculate total by category
        Map<Integer, Double> categoryTotals = new HashMap<>();
        for (Expense expense : filteredExpenses) {
            int categoryId = expense.getCategoryId();
            double currentTotal = categoryTotals.getOrDefault(categoryId, 0.0);
            categoryTotals.put(categoryId, currentTotal + expense.getAmount());
        }

        // Add category summaries to CSV
        double grandTotal = 0;
        for (Map.Entry<Integer, Double> entry : categoryTotals.entrySet()) {
            String categoryName = categoryMap.getOrDefault(entry.getKey(), "Unknown");
            double total = entry.getValue();
            grandTotal += total;

            csvBuilder.append("\"").append(categoryName).append("\",");
            csvBuilder.append(total).append("\n");
        }

        // Add grand total
        csvBuilder.append("\"Total\",").append(grandTotal).append("\n");

        // Write to file
        String fileName = "Expense_Report_" + startDate + "_to_" + endDate + ".csv";
        File file = new File(context.getExternalFilesDir(null), fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(csvBuilder.toString().getBytes());
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error writing CSV file", e);
            return null;
        }
    }

    /**
     * Generate a PDF expense report for a specific time period
     * Note: A real implementation would use a PDF library, this is simplified
     */
    public String generatePDFReport(int userId, String startDate, String endDate) {
        // In a real app, you would use a PDF library like iText or PDFBox
        // For this example, we'll just create a text file with .pdf extension

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date start, end;

        try {
            start = dateFormat.parse(startDate);
            end = dateFormat.parse(endDate);
        } catch (ParseException e) {
            Log.e(TAG, "Invalid date format", e);
            return null;
        }

        // Get all expenses for the user
        List<Expense> allExpenses = expenseDb.getExpensesByUser(userId);
        List<Expense> filteredExpenses = new ArrayList<>();

        // Filter expenses by date range
        for (Expense expense : allExpenses) {
            if (expense.getDate() != null &&
                    (expense.getDate().after(start) || expense.getDate().equals(start)) &&
                    (expense.getDate().before(end) || expense.getDate().equals(end))) {
                filteredExpenses.add(expense);
            }
        }

        if (filteredExpenses.isEmpty()) {
            return null;
        }

        // Get all categories for mapping
        List<Category> categories = expenseDb.getAllCategories();
        Map<Integer, String> categoryMap = new HashMap<>();
        for (Category category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }

        // Build report content
        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append("EXPENSE REPORT\n");
        reportBuilder.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");

        reportBuilder.append("EXPENSE DETAILS\n");
        reportBuilder.append("Date\t\tAmount\t\tDescription\t\tCategory\n");
        reportBuilder.append("-------------------------------------------------------------\n");

        for (Expense expense : filteredExpenses) {
            reportBuilder.append(dateFormat.format(expense.getDate())).append("\t\t");
            reportBuilder.append(String.format("$%.2f", expense.getAmount())).append("\t\t");
            reportBuilder.append(expense.getDescription()).append("\t\t");
            reportBuilder.append(categoryMap.getOrDefault(expense.getCategoryId(), "Unknown")).append("\n");
        }

        reportBuilder.append("\n\nCATEGORY SUMMARY\n");
        reportBuilder.append("Category\t\tTotal Amount\n");
        reportBuilder.append("------------------------------\n");

        // Calculate total by category
        Map<Integer, Double> categoryTotals = new HashMap<>();
        for (Expense expense : filteredExpenses) {
            int categoryId = expense.getCategoryId();
            double currentTotal = categoryTotals.getOrDefault(categoryId, 0.0);
            categoryTotals.put(categoryId, currentTotal + expense.getAmount());
        }

        // Add category summaries to report
        double grandTotal = 0;
        for (Map.Entry<Integer, Double> entry : categoryTotals.entrySet()) {
            String categoryName = categoryMap.getOrDefault(entry.getKey(), "Unknown");
            double total = entry.getValue();
            grandTotal += total;

            reportBuilder.append(categoryName).append("\t\t");
            reportBuilder.append(String.format("$%.2f", total)).append("\n");
        }

        // Add grand total
        reportBuilder.append("------------------------------\n");
        reportBuilder.append("TOTAL\t\t").append(String.format("$%.2f", grandTotal)).append("\n");

        // Write to file
        String fileName = "Expense_Report_" + startDate + "_to_" + endDate + ".pdf";
        File file = new File(context.getExternalFilesDir(null), fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(reportBuilder.toString().getBytes());
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error writing PDF file", e);
            return null;
        }
    }

    /**
     * Share the generated report
     * @param filePath Path to the report file
     */
    public void shareReport(String filePath) {
        if (filePath == null) {
            Toast.makeText(context, "No report to share", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(context, "Report file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (context instanceof Activity) {
            context.startActivity(Intent.createChooser(shareIntent, "Share Expense Report"));
        } else {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(shareIntent);
        }
    }
}