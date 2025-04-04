package com.example.campusexpensemanagerse06304;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.example.campusexpensemanagerse06304.model.Budget;
import com.example.campusexpensemanagerse06304.model.Category;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Manager class for handling budget notifications
 */
public class BudgetNotificationManager {
    private static final String TAG = "BudgetNotification";

    // Notification channel constants
    private static final String CHANNEL_ID = "budget_alerts";
    private static final String CHANNEL_NAME = "Budget Alerts";
    private static final String CHANNEL_DESC = "Notifications about budget limits";

    // Notification thresholds
    private static final double WARNING_THRESHOLD = 0.8; // 80% of budget
    private static final double EXCEEDED_THRESHOLD = 1.0; // 100% of budget

    private Context context;
    private ExpenseDb expenseDb;

    public BudgetNotificationManager(Context context) {
        this.context = context;
        this.expenseDb = new ExpenseDb(context);

        // Create notification channel (required for Android 8.0+)
        createNotificationChannel();
    }

    /**
     * Create the notification channel for budget alerts
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);

            channel.setDescription(CHANNEL_DESC);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Check budgets for a user and send notifications if needed
     * @param userId User ID to check budgets for
     */
    public void checkBudgetsAndNotify(int userId) {
        Log.d(TAG, "Checking budgets for user: " + userId);

        // Get current month in format YYYY-MM
        Calendar cal = Calendar.getInstance();
        String currentMonth = String.format(Locale.getDefault(), "%d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);

        // Get all budgets for the user
        List<Budget> budgets = expenseDb.getBudgetsByUser(userId);

        // Get categories for names
        List<Category> categories = expenseDb.getAllCategories();

        // Check each budget
        for (int i = 0; i < budgets.size(); i++) {
            Budget budget = budgets.get(i);

            // Get spent amount for this category this month
            double spent = expenseDb.getTotalExpensesByCategoryAndMonth(
                    userId, budget.getCategoryId(), currentMonth);

            // Calculate percentage of budget used
            double budgetAmount = budget.getAmount();
            double percentage = budgetAmount > 0 ? spent / budgetAmount : 0;

            // Find category name
            String categoryName = "Unknown";
            for (Category category : categories) {
                if (category.getId() == budget.getCategoryId()) {
                    categoryName = category.getName();
                    break;
                }
            }

            // Check if we need to send notification
            if (percentage >= EXCEEDED_THRESHOLD) {
                sendBudgetExceededNotification(userId, i, categoryName, spent, budgetAmount);
            } else if (percentage >= WARNING_THRESHOLD) {
                sendBudgetWarningNotification(userId, i, categoryName, spent, budgetAmount, percentage);
            }
        }
    }

    /**
     * Send a notification when user approaches budget limit
     */
    private void sendBudgetWarningNotification(int userId, int notificationId,
                                               String categoryName, double spent,
                                               double budget, double percentage) {
        // Create intent to open the app
        Intent intent = new Intent(context, MenuActivity.class);
        intent.putExtra("ID_USER", userId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Format the notification message
        String title = "Budget Warning: " + categoryName;
        String content = String.format(Locale.getDefault(),
                "You've used %.1f%% of your %s budget ($%.2f of $%.2f)",
                percentage * 100, categoryName, spent, budget);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.account_balance_wallet_24dp)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show notification
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Sent budget warning notification for " + categoryName);
        } catch (SecurityException e) {
            Log.e(TAG, "No permission to show notification", e);
        }
    }

    /**
     * Send a notification when user exceeds budget limit
     */
    private void sendBudgetExceededNotification(int userId, int notificationId,
                                                String categoryName, double spent,
                                                double budget) {
        // Create intent to open the app
        Intent intent = new Intent(context, MenuActivity.class);
        intent.putExtra("ID_USER", userId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Format the notification message
        String title = "Budget Exceeded: " + categoryName;
        String content = String.format(Locale.getDefault(),
                "You've exceeded your %s budget! ($%.2f of $%.2f)",
                categoryName, spent, budget);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.account_balance_wallet_24dp)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show notification
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId + 1000, builder.build());
            Log.d(TAG, "Sent budget exceeded notification for " + categoryName);
        } catch (SecurityException e) {
            Log.e(TAG, "No permission to show notification", e);
        }
    }
}