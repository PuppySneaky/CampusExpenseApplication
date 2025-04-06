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


import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;



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
     * Generate a PDF expense report with better table formatting and matching CSV features
     *
     * @param userId User ID
     * @param startDate Start date in format yyyy-MM-dd
     * @param endDate End date in format yyyy-MM-dd
     * @return Path to the generated PDF file, or null if generation failed
     */
    public String generatePDFReport(int userId, String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date start, end;

        try {
            start = dateFormat.parse(startDate);
            end = dateFormat.parse(endDate);
        } catch (ParseException e) {
            Log.e(TAG, "Invalid date format", e);
            return null;
        }

        // Get all expenses for the user and date range
        List<Expense> filteredExpenses = getFilteredExpenses(userId, start, end);

        if (filteredExpenses.isEmpty()) {
            Log.e(TAG, "No expenses found for the selected period");
            return null;
        }

        // Get all categories for mapping
        List<Category> categories = expenseDb.getAllCategories();
        Map<Integer, String> categoryMap = createCategoryMap(categories);

        // Get budgets for comparison (if applicable)
        Map<Integer, Double> budgetMap = getBudgetMap(userId);

        // Calculate category totals
        Map<Integer, Double> categoryTotals = calculateCategoryTotals(filteredExpenses);
        double grandTotal = 0;
        for (Double total : categoryTotals.values()) {
            grandTotal += total;
        }

        // Calculate monthly totals if spanning multiple months
        boolean multipleMonths = isReportSpanningMultipleMonths(start, end);
        Map<String, Double> monthlyTotals = null;
        if (multipleMonths) {
            monthlyTotals = calculateMonthlyTotals(filteredExpenses);
        }

        // Create PDF document
        Document document = new Document();
        String fileName = "Expense_Report_" + startDate + "_to_" + endDate + ".pdf";
        File file = new File(context.getExternalFilesDir(null), fileName);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Add document metadata
            document.addCreationDate();
            document.addTitle("Campus Expense Manager Report");
            document.addSubject("Expense Report for period: " + startDate + " to " + endDate);

            // Add title and metadata
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headingFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
            Font smallBoldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);

            // Report Title
            Paragraph title = new Paragraph("CAMPUS EXPENSE MANAGER REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Add metadata
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()), normalFont));
            document.add(new Paragraph("Period: " + startDate + " to " + endDate, normalFont));
            document.add(new Paragraph(" "));

            // SECTION 1: EXPENSE TRANSACTIONS
            Paragraph expenseTitle = new Paragraph("EXPENSE TRANSACTIONS", headingFont);
            document.add(expenseTitle);
            document.add(new Paragraph(" "));

            // Create transaction table
            PdfPTable expenseTable = new PdfPTable(5); // 5 columns
            expenseTable.setWidthPercentage(100);

            // Try to set column widths for better appearance
            try {
                expenseTable.setWidths(new float[]{2, 2, 3, 2, 2});
            } catch (DocumentException e) {
                Log.e(TAG, "Error setting column widths", e);
            }

            // Add table headers
            PdfPCell headerCell;

            headerCell = new PdfPCell(new Paragraph("Date", smallBoldFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
            expenseTable.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Amount", smallBoldFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
            expenseTable.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Description", smallBoldFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
            expenseTable.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Category", smallBoldFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
            expenseTable.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Payment Method", smallBoldFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
            expenseTable.addCell(headerCell);

            // Add expense data rows
            PdfPCell cell;
            for (Expense expense : filteredExpenses) {
                // Date
                cell = new PdfPCell(new Paragraph(dateFormat.format(expense.getDate()), normalFont));
                expenseTable.addCell(cell);

                // Amount
                cell = new PdfPCell(new Paragraph(String.format(Locale.getDefault(), "$%.2f", expense.getAmount()), normalFont));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                expenseTable.addCell(cell);

                // Description
                cell = new PdfPCell(new Paragraph(expense.getDescription(), normalFont));
                expenseTable.addCell(cell);

                // Category
                cell = new PdfPCell(new Paragraph(categoryMap.getOrDefault(expense.getCategoryId(), "Unknown"), normalFont));
                expenseTable.addCell(cell);

                // Payment Method
                cell = new PdfPCell(new Paragraph(expense.getPaymentMethod(), normalFont));
                expenseTable.addCell(cell);
            }

            document.add(expenseTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // SECTION 2: CATEGORY SUMMARY
            Paragraph categoryTitle = new Paragraph("CATEGORY SUMMARY", headingFont);
            document.add(categoryTitle);
            document.add(new Paragraph(" "));

            // Create category summary table
            PdfPTable categoryTable = new PdfPTable(5); // 5 columns
            categoryTable.setWidthPercentage(100);

            // Try to set column widths for better appearance
            try {
                categoryTable.setWidths(new float[]{3, 2, 2, 2, 2});
            } catch (DocumentException e) {
                Log.e(TAG, "Error setting column widths", e);
            }

            // Add table headers
            headerCell = new PdfPCell(new Paragraph("Category", smallBoldFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
            categoryTable.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Total Amount", smallBoldFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
            categoryTable.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Budget Amount", smallBoldFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
            categoryTable.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("Remaining Budget", smallBoldFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
            categoryTable.addCell(headerCell);

            headerCell = new PdfPCell(new Paragraph("% of Budget Used", smallBoldFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
            categoryTable.addCell(headerCell);

            // Add category data rows
            for (Map.Entry<Integer, Double> entry : categoryTotals.entrySet()) {
                int categoryId = entry.getKey();
                double totalSpent = entry.getValue();

                String categoryName = categoryMap.getOrDefault(categoryId, "Unknown");
                double budgetAmount = budgetMap.getOrDefault(categoryId, 0.0);
                double remaining = budgetAmount - totalSpent;
                double percentUsed = budgetAmount > 0 ? (totalSpent / budgetAmount) * 100 : 0;

                // Category
                cell = new PdfPCell(new Paragraph(categoryName, normalFont));
                categoryTable.addCell(cell);

                // Total Amount
                cell = new PdfPCell(new Paragraph(String.format(Locale.getDefault(), "$%.2f", totalSpent), normalFont));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                categoryTable.addCell(cell);

                // Budget Amount
                cell = new PdfPCell(new Paragraph(String.format(Locale.getDefault(), "$%.2f", budgetAmount), normalFont));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                categoryTable.addCell(cell);

                // Remaining Budget
                cell = new PdfPCell(new Paragraph(String.format(Locale.getDefault(), "$%.2f", remaining), normalFont));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                categoryTable.addCell(cell);

                // % of Budget Used
                cell = new PdfPCell(new Paragraph(String.format(Locale.getDefault(), "%.1f%%", percentUsed), normalFont));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                categoryTable.addCell(cell);
            }

            // Add grand total row
            cell = new PdfPCell(new Paragraph("Total", smallBoldFont));
            categoryTable.addCell(cell);

            cell = new PdfPCell(new Paragraph(String.format(Locale.getDefault(), "$%.2f", grandTotal), smallBoldFont));
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            categoryTable.addCell(cell);

            // Empty cells for the rest of the row
            categoryTable.addCell("");
            categoryTable.addCell("");
            categoryTable.addCell("");

            document.add(categoryTable);

            // SECTION 3: MONTHLY DISTRIBUTION (if applicable)
            if (multipleMonths && monthlyTotals != null && !monthlyTotals.isEmpty()) {
                document.add(new Paragraph(" "));
                document.add(new Paragraph(" "));
                Paragraph monthlyTitle = new Paragraph("MONTHLY DISTRIBUTION", headingFont);
                document.add(monthlyTitle);
                document.add(new Paragraph(" "));

                // Create monthly distribution table
                PdfPTable monthlyTable = new PdfPTable(2); // 2 columns
                monthlyTable.setWidthPercentage(70);

                // Add table headers
                headerCell = new PdfPCell(new Paragraph("Month", smallBoldFont));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
                monthlyTable.addCell(headerCell);

                headerCell = new PdfPCell(new Paragraph("Total Amount", smallBoldFont));
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
                monthlyTable.addCell(headerCell);

                // Add monthly data rows
                for (Map.Entry<String, Double> entry : monthlyTotals.entrySet()) {
                    cell = new PdfPCell(new Paragraph(entry.getKey(), normalFont));
                    monthlyTable.addCell(cell);

                    cell = new PdfPCell(new Paragraph(String.format(Locale.getDefault(), "$%.2f", entry.getValue()), normalFont));
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    monthlyTable.addCell(cell);
                }

                document.add(monthlyTable);
            }

            document.close();
            return file.getAbsolutePath();
        } catch (DocumentException | IOException e) {
            Log.e(TAG, "Error generating PDF report", e);
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