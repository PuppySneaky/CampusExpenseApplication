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
import com.example.campusexpensemanagerse06304.model.Budget;
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
 * Enhanced Utility class to generate expense reports in different formats
 *
 * This class uses the following database tables:
 * - expenses: For transaction details (amount, date, description, etc.)
 * - categories: For category names and other details
 * - budgets: For comparing expenses against budget limits
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
     *
     * The report includes:
     * 1. Transaction details from the 'expenses' table (date, amount, description, category, payment method)
     * 2. Category summary totals
     * 3. Budget comparison (if budgets exist)
     *
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

        // *** QUERY 1: Get all expenses for the user and date range ***
        List<Expense> filteredExpenses = getFilteredExpenses(userId, start, end);

        if (filteredExpenses.isEmpty()) {
            return null;
        }

        // *** QUERY 2: Get all categories for mapping ***
        List<Category> categories = expenseDb.getAllCategories();
        Map<Integer, String> categoryMap = createCategoryMap(categories);

        // *** QUERY 3: Get budgets for comparison (if applicable) ***
        Map<Integer, Double> budgetMap = getBudgetMap(userId);

        // Build CSV content
        StringBuilder csvBuilder = new StringBuilder();

        // Add report header with metadata
        csvBuilder.append("# CampusExpense Manager Report\n");
        csvBuilder.append("# Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())).append("\n");
        csvBuilder.append("# Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");

        // SECTION 1: Transaction details
        csvBuilder.append("## EXPENSE TRANSACTIONS\n");
        csvBuilder.append("Date,Amount,Description,Category,Payment Method\n");

        for (Expense expense : filteredExpenses) {
            csvBuilder.append(dateFormat.format(expense.getDate())).append(",");
            csvBuilder.append(expense.getAmount()).append(",");
            csvBuilder.append("\"").append(expense.getDescription().replace("\"", "\"\"")).append("\",");
            csvBuilder.append("\"").append(categoryMap.getOrDefault(expense.getCategoryId(), "Unknown")).append("\",");
            csvBuilder.append("\"").append(expense.getPaymentMethod()).append("\"\n");
        }

        // SECTION 2: Category Summary
        csvBuilder.append("\n## CATEGORY SUMMARY\n");
        csvBuilder.append("Category,Total Amount,Budget Amount,Remaining Budget,% of Budget Used\n");

        // Calculate total by category
        Map<Integer, Double> categoryTotals = calculateCategoryTotals(filteredExpenses);
        double grandTotal = 0;

        // Add category summaries to CSV
        for (Map.Entry<Integer, Double> entry : categoryTotals.entrySet()) {
            int categoryId = entry.getKey();
            double totalSpent = entry.getValue();
            grandTotal += totalSpent;

            String categoryName = categoryMap.getOrDefault(categoryId, "Unknown");
            double budgetAmount = budgetMap.getOrDefault(categoryId, 0.0);
            double remaining = budgetAmount - totalSpent;
            double percentUsed = budgetAmount > 0 ? (totalSpent / budgetAmount) * 100 : 0;

            csvBuilder.append("\"").append(categoryName).append("\",");
            csvBuilder.append(totalSpent).append(",");
            csvBuilder.append(budgetAmount).append(",");
            csvBuilder.append(remaining).append(",");
            csvBuilder.append(String.format(Locale.getDefault(), "%.1f%%", percentUsed)).append("\n");
        }

        // Add grand total
        csvBuilder.append("\"Total\",").append(grandTotal).append("\n");

        // SECTION 3: Monthly Distribution (if spanning multiple months)
        if (isReportSpanningMultipleMonths(start, end)) {
            csvBuilder.append("\n## MONTHLY DISTRIBUTION\n");
            csvBuilder.append("Month,Total Amount\n");

            Map<String, Double> monthlyTotals = calculateMonthlyTotals(filteredExpenses);
            for (Map.Entry<String, Double> entry : monthlyTotals.entrySet()) {
                csvBuilder.append("\"").append(entry.getKey()).append("\",");
                csvBuilder.append(entry.getValue()).append("\n");
            }
        }

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
     * Generate a PDF expense report with better table formatting
     * Note: This is still a text-based implementation, but with improved visual formatting
     */
    public String generatePDFReport(int userId, String startDate, String endDate) {
        // In a real app, you would use a PDF library like iText or PDFBox
        // For this example, we'll create a better-formatted text file with .pdf extension

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date start, end;

        try {
            start = dateFormat.parse(startDate);
            end = dateFormat.parse(endDate);
        } catch (ParseException e) {
            Log.e(TAG, "Invalid date format", e);
            return null;
        }

        // *** QUERY 1: Get filtered expenses ***
        List<Expense> filteredExpenses = getFilteredExpenses(userId, start, end);

        if (filteredExpenses.isEmpty()) {
            return null;
        }

        // *** QUERY 2: Get categories ***
        List<Category> categories = expenseDb.getAllCategories();
        Map<Integer, String> categoryMap = createCategoryMap(categories);

        // *** QUERY 3: Get budgets ***
        Map<Integer, Double> budgetMap = getBudgetMap(userId);

        // Calculate totals and summaries
        double totalExpenses = calculateTotalExpenses(filteredExpenses);
        double totalBudget = calculateTotalBudget(budgetMap);
        Map<Integer, Double> categoryTotals = calculateCategoryTotals(filteredExpenses);

        // Get current date and time for header
        String generatedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Build report content with better table formatting
        StringBuilder reportBuilder = new StringBuilder();

        // ====== HEADER SECTION ======
        reportBuilder.append(createCenteredTitle("CAMPUS EXPENSE MANAGER EXPENSE REPORT", 100));
        reportBuilder.append("Generated: ").append(generatedTime).append("\n");
        reportBuilder.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");

        // ====== SUMMARY SECTION ======
        reportBuilder.append(createBoxedSection("SUMMARY", 100));
        reportBuilder.append(String.format("Total Expenses: $%.2f | Total Budget: $%.2f | Remaining Budget: $%.2f | Budget Utilization: %.1f%%\n\n",
                totalExpenses, totalBudget, totalBudget - totalExpenses,
                (totalBudget > 0 ? (totalExpenses / totalBudget) * 100 : 0)));

        // ====== EXPENSE DETAILS SECTION ======
        reportBuilder.append(createBoxedSection("EXPENSE DETAILS", 100));

        // Create a table header with fixed width columns
        reportBuilder.append(String.format("%-12s %-10s %-25s %-25s\n", "Date", "Amount", "Description", "Category"));
        reportBuilder.append(createRepeatedChar('-', 80)).append('\n');

        // Add expense rows with fixed width columns
        for (Expense expense : filteredExpenses) {
            reportBuilder.append(String.format("%-12s $%-9.2f %-25s %-25s\n",
                    dateFormat.format(expense.getDate()),
                    expense.getAmount(),
                    truncateString(expense.getDescription(), 23),
                    truncateString(categoryMap.getOrDefault(expense.getCategoryId(), "Unknown"), 23)));
        }
        reportBuilder.append('\n');

        // ====== CATEGORY SUMMARY SECTION ======
        reportBuilder.append(createBoxedSection("CATEGORY SUMMARY", 100));

        // Create a table header with fixed width columns
        reportBuilder.append(String.format("%-20s %-15s %-15s %-15s %-15s\n",
                "Category", "Spent", "Budget", "Remaining", "% Used"));
        reportBuilder.append(createRepeatedChar('-', 90)).append('\n');

        // Add category summary rows
        for (Map.Entry<Integer, Double> entry : categoryTotals.entrySet()) {
            int categoryId = entry.getKey();
            double totalSpent = entry.getValue();

            String categoryName = categoryMap.getOrDefault(categoryId, "Unknown");
            double budgetAmount = budgetMap.getOrDefault(categoryId, 0.0);
            double remaining = budgetAmount - totalSpent;
            double percentUsed = budgetAmount > 0 ? (totalSpent / budgetAmount) * 100 : 0;

            reportBuilder.append(String.format("%-20s $%-14.2f $%-14.2f $%-14.2f %-15s\n",
                    truncateString(categoryName, 18),
                    totalSpent,
                    budgetAmount,
                    remaining,
                    String.format("%.1f%%", percentUsed)));
        }

        // Add grand total
        reportBuilder.append(createRepeatedChar('-', 90)).append('\n');
        reportBuilder.append(String.format("%-20s $%-14.2f $%-14.2f $%-14.2f\n",
                "TOTAL",
                totalExpenses,
                totalBudget,
                totalBudget - totalExpenses));
        reportBuilder.append('\n');

        // ====== MONTHLY DISTRIBUTION (if applicable) ======
        if (isReportSpanningMultipleMonths(start, end)) {
            reportBuilder.append(createBoxedSection("MONTHLY DISTRIBUTION", 100));

            // Create a table header
            reportBuilder.append(String.format("%-20s %-15s\n", "Month", "Amount"));
            reportBuilder.append(createRepeatedChar('-', 40)).append('\n');

            Map<String, Double> monthlyTotals = calculateMonthlyTotals(filteredExpenses);
            for (Map.Entry<String, Double> entry : monthlyTotals.entrySet()) {
                reportBuilder.append(String.format("%-20s $%-14.2f\n",
                        entry.getKey(),
                        entry.getValue()));
            }
            reportBuilder.append('\n');
        }

        // ====== FOOTER ======
        reportBuilder.append(createRepeatedChar('=', 100)).append('\n');
        reportBuilder.append("End of Report\n");

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
     * Create a centered title with decoration
     */
    private String createCenteredTitle(String title, int width) {
        StringBuilder sb = new StringBuilder();
        sb.append(createRepeatedChar('=', width)).append('\n');

        int padding = (width - title.length()) / 2;
        sb.append(createRepeatedChar(' ', padding))
                .append(title)
                .append(createRepeatedChar(' ', width - padding - title.length()))
                .append('\n');

        sb.append(createRepeatedChar('=', width)).append('\n');
        return sb.toString();
    }

    /**
     * Create a boxed section header
     */
    private String createBoxedSection(String title, int width) {
        StringBuilder sb = new StringBuilder();
        sb.append(createRepeatedChar('-', width)).append('\n');
        sb.append(title).append('\n');
        sb.append(createRepeatedChar('-', width)).append('\n');
        return sb.toString();
    }

    /**
     * Create a string with a repeated character
     */
    private String createRepeatedChar(char c, int count) {
        char[] chars = new char[count];
        java.util.Arrays.fill(chars, c);
        return new String(chars);
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

    // ====== HELPER METHODS ======

    /**
     * Get expenses filtered by date range
     */
    private List<Expense> getFilteredExpenses(int userId, Date start, Date end) {
        List<Expense> allExpenses = expenseDb.getExpensesByUser(userId);
        List<Expense> filteredExpenses = new ArrayList<>();

        for (Expense expense : allExpenses) {
            // Check date range
            if (expense.getDate() != null &&
                    (expense.getDate().after(start) || expense.getDate().equals(start)) &&
                    (expense.getDate().before(end) || expense.getDate().equals(end))) {
                filteredExpenses.add(expense);
            }
        }

        return filteredExpenses;
    }

    /**
     * Create a map of category IDs to names
     */
    private Map<Integer, String> createCategoryMap(List<Category> categories) {
        Map<Integer, String> categoryMap = new HashMap<>();
        for (Category category : categories) {
            categoryMap.put(category.getId(), category.getName());
        }
        return categoryMap;
    }

    /**
     * Get map of category IDs to budget amounts
     */
    private Map<Integer, Double> getBudgetMap(int userId) {
        Map<Integer, Double> budgetMap = new HashMap<>();
        List<Budget> budgets = expenseDb.getBudgetsByUser(userId);

        for (Budget budget : budgets) {
            budgetMap.put(budget.getCategoryId(), budget.getAmount());
        }

        return budgetMap;
    }

    /**
     * Calculate total expenses by category
     */
    private Map<Integer, Double> calculateCategoryTotals(List<Expense> expenses) {
        Map<Integer, Double> categoryTotals = new HashMap<>();

        for (Expense expense : expenses) {
            int categoryId = expense.getCategoryId();
            double currentTotal = categoryTotals.getOrDefault(categoryId, 0.0);
            categoryTotals.put(categoryId, currentTotal + expense.getAmount());
        }

        return categoryTotals;
    }

    /**
     * Calculate total expenses
     */
    private double calculateTotalExpenses(List<Expense> expenses) {
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        return total;
    }

    /**
     * Calculate total budget
     */
    private double calculateTotalBudget(Map<Integer, Double> budgetMap) {
        double total = 0;
        for (Double amount : budgetMap.values()) {
            total += amount;
        }
        return total;
    }

    /**
     * Calculate expenses by month
     */
    private Map<String, Double> calculateMonthlyTotals(List<Expense> expenses) {
        Map<String, Double> monthlyTotals = new HashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        for (Expense expense : expenses) {
            String month = monthFormat.format(expense.getDate());
            double currentTotal = monthlyTotals.getOrDefault(month, 0.0);
            monthlyTotals.put(month, currentTotal + expense.getAmount());
        }

        return monthlyTotals;
    }

    /**
     * Check if the report spans multiple months
     */
    private boolean isReportSpanningMultipleMonths(Date start, Date end) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(start);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);

        return (startCal.get(Calendar.YEAR) != endCal.get(Calendar.YEAR)) ||
                (startCal.get(Calendar.MONTH) != endCal.get(Calendar.MONTH));
    }

    /**
     * Truncate string to a maximum length
     */
    private String truncateString(String input, int maxLength) {
        if (input == null) return "";
        return input.length() > maxLength ? input.substring(0, maxLength - 3) + "..." : input;
    }
}