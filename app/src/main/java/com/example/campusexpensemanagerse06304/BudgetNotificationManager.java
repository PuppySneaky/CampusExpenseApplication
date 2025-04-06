package com.example.campusexpensemanagerse06304;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
 * Enhanced Manager class for handling budget notifications with more user control
 */
public class BudgetNotificationManager {
    private static final String TAG = "BudgetNotification";

    // Notification channel constants
    private static final String CHANNEL_ID = "budget_alerts";
    private static final String CHANNEL_NAME = "Budget Alerts";
    private static final String CHANNEL_DESC = "Notifications about budget limits";

    // Extra channel for important alerts
    private static final String CRITICAL_CHANNEL_ID = "critical_budget_alerts";
    private static final String CRITICAL_CHANNEL_NAME = "Critical Budget Alerts";
    private static final String CRITICAL_CHANNEL_DESC = "High priority notifications for budget overruns";

    // Notification thresholds (configurable via preferences)
    private static final double DEFAULT_WARNING_THRESHOLD = 0.8; // 80% of budget
    private static final double DEFAULT_EXCEEDED_THRESHOLD = 1.0; // 100% of budget

    // Shared Preferences keys
    private static final String PREFS_NAME = "NotificationPreferences";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_WARNING_THRESHOLD = "warning_threshold";
    private static final String KEY_EXCEEDED_THRESHOLD = "exceeded_threshold";
    private static final String KEY_WARNING_FREQUENCY = "warning_frequency_hours";
    private static final String KEY_LAST_NOTIFICATION_TIME = "last_notification_time_";

    private Context context;
    private ExpenseDb expenseDb;
    private SharedPreferences prefs;

    public BudgetNotificationManager(Context context) {
        this.context = context;
        this.expenseDb = new ExpenseDb(context);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Create notification channels (required for Android 8.0+)
        createNotificationChannels();
    }

    /**
     * Create the notification channels for budget alerts
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Regular budget alert channel
            NotificationChannel regularChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);

            regularChannel.setDescription(CHANNEL_DESC);
            regularChannel.enableLights(true);
            regularChannel.setLightColor(Color.YELLOW);
            regularChannel.setVibrationPattern(new long[]{0, 250, 250, 250});

            // Critical budget alert channel (higher importance)
            NotificationChannel criticalChannel = new NotificationChannel(
                    CRITICAL_CHANNEL_ID,
                    CRITICAL_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);

            criticalChannel.setDescription(CRITICAL_CHANNEL_DESC);
            criticalChannel.enableLights(true);
            criticalChannel.setLightColor(Color.RED);
            criticalChannel.setVibrationPattern(new long[]{0, 500, 250, 500});
            criticalChannel.enableVibration(true);

            // Register the channels with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(regularChannel);
                notificationManager.createNotificationChannel(criticalChannel);
            }
        }
    }

    /**
     * Check if user has notifications enabled in preferences
     * @return true if notifications are enabled, false otherwise
     */
    public boolean areNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true); // Enabled by default
    }

    /**
     * Enable or disable notifications
     * @param enabled true to enable notifications, false to disable
     */
    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    /**
     * Set the warning threshold (percentage of budget that triggers a warning)
     * @param threshold Threshold percentage (0.0 to 1.0)
     */
    public void setWarningThreshold(double threshold) {
        if (threshold >= 0.0 && threshold <= 1.0) {
            prefs.edit().putFloat(KEY_WARNING_THRESHOLD, (float) threshold).apply();
        }
    }

    /**
     * Set the exceeded threshold (percentage of budget that triggers an exceeded notification)
     * @param threshold Threshold percentage (usually 1.0 = 100%)
     */
    public void setExceededThreshold(double threshold) {
        if (threshold >= 0.0) {
            prefs.edit().putFloat(KEY_EXCEEDED_THRESHOLD, (float) threshold).apply();
        }
    }

    /**
     * Set the minimum hours between warning notifications for the same budget
     * @param hours Minimum hours between notifications
     */
    public void setWarningFrequency(int hours) {
        if (hours > 0) {
            prefs.edit().putInt(KEY_WARNING_FREQUENCY, hours).apply();
        }
    }

    /**
     * Get the current warning threshold
     * @return Warning threshold (0.0 to 1.0)
     */
    public double getWarningThreshold() {
        return prefs.getFloat(KEY_WARNING_THRESHOLD, (float) DEFAULT_WARNING_THRESHOLD);
    }

    /**
     * Get the current exceeded threshold
     * @return Exceeded threshold (usually 1.0)
     */
    public double getExceededThreshold() {
        return prefs.getFloat(KEY_EXCEEDED_THRESHOLD, (float) DEFAULT_EXCEEDED_THRESHOLD);
    }

    /**
     * Get the minimum hours between warning notifications
     * @return Minimum hours between notifications
     */
    public int getWarningFrequency() {
        return prefs.getInt(KEY_WARNING_FREQUENCY, 24); // Default: 24 hours
    }

    /**
     * Check budgets for a user and send notifications if needed
     * @param userId User ID to check budgets for
     * @return Number of notifications sent (for testing/verification)
     */
    public int checkBudgetsAndNotify(int userId) {
        Log.d(TAG, "Checking budgets for user: " + userId);

        // Skip checks if notifications are disabled
        if (!areNotificationsEnabled()) {
            Log.d(TAG, "Notifications are disabled in preferences");
            return 0;
        }

        // Get current month in format YYYY-MM
        Calendar cal = Calendar.getInstance();
        String currentMonth = String.format(Locale.getDefault(), "%d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);

        // Get all budgets for the user
        List<Budget> budgets = expenseDb.getBudgetsByUser(userId);

        // Get categories for names
        List<Category> categories = expenseDb.getAllCategories();

        // Keep track of how many notifications we send
        int notificationsSent = 0;

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

            // Get threshold values from preferences
            double warningThreshold = getWarningThreshold();
            double exceededThreshold = getExceededThreshold();

            // Get minimum notification frequency
            int minHoursBetweenNotifications = getWarningFrequency();

            // Check if we need to send notification
            boolean shouldSendNotification = false;
            boolean isCritical = false;
            String notificationKey = KEY_LAST_NOTIFICATION_TIME + budget.getId();

            // Check if budget is exceeded (over threshold)
            if (percentage >= exceededThreshold) {
                // For exceeded notifications, we consider it critical
                isCritical = true;
                shouldSendNotification = shouldSendNotification(notificationKey, minHoursBetweenNotifications);

                // If notification should be sent
                if (shouldSendNotification) {
                    sendBudgetExceededNotification(userId, i, categoryName, spent, budgetAmount);
                    notificationsSent++;

                    // Update last notification time
                    updateLastNotificationTime(notificationKey);
                }
            }
            // Otherwise check if approaching warning threshold
            else if (percentage >= warningThreshold) {
                shouldSendNotification = shouldSendNotification(notificationKey, minHoursBetweenNotifications);

                if (shouldSendNotification) {
                    sendBudgetWarningNotification(userId, i, categoryName, spent, budgetAmount, percentage);
                    notificationsSent++;

                    // Update last notification time
                    updateLastNotificationTime(notificationKey);
                }
            }
        }

        return notificationsSent;
    }

    /**
     * Determine if we should send a notification based on frequency settings
     */
    private boolean shouldSendNotification(String notificationKey, int minHoursBetweenNotifications) {
        long lastNotificationTime = prefs.getLong(notificationKey, 0);
        long currentTime = System.currentTimeMillis();
        long minTimeBetweenNotifications = minHoursBetweenNotifications * 60 * 60 * 1000; // Convert to milliseconds

        return (currentTime - lastNotificationTime) >= minTimeBetweenNotifications;
    }

    /**
     * Update the last notification time for a budget
     */
    private void updateLastNotificationTime(String notificationKey) {
        prefs.edit().putLong(notificationKey, System.currentTimeMillis()).apply();
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
        intent.putExtra("NAVIGATE_TO_BUDGET", true);
        intent.putExtra("CATEGORY_ID", notificationId);
        intent.putExtra("CATEGORY_NAME", categoryName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notificationId, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Format the notification message
        String title = "Budget Alert: " + categoryName;
        String content = String.format(Locale.getDefault(),
                "You've used %.1f%% of your %s budget ($%.2f of $%.2f)",
                percentage * 100, categoryName, spent, budget);

        // Build notification with action buttons
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.account_balance_wallet_24dp)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setColor(Color.YELLOW)
                .setAutoCancel(true);

        // Add action buttons
        Intent budgetIntent = new Intent(context, MenuActivity.class);
        budgetIntent.putExtra("ID_USER", userId);
        budgetIntent.putExtra("NAVIGATE_TO_BUDGET", true);
        budgetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent budgetPendingIntent = PendingIntent.getActivity(
                context, notificationId + 1000, budgetIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.drawable.settings_24dp, "Adjust Budget", budgetPendingIntent);

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
        intent.putExtra("NAVIGATE_TO_BUDGET", true);
        intent.putExtra("CATEGORY_ID", notificationId);
        intent.putExtra("CATEGORY_NAME", categoryName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notificationId + 2000, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Format the notification message
        String title = "Budget Exceeded: " + categoryName;
        String content = String.format(Locale.getDefault(),
                "You've exceeded your %s budget! ($%.2f of $%.2f)",
                categoryName, spent, budget);

        // Build notification with higher priority & actions
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CRITICAL_CHANNEL_ID)
                .setSmallIcon(R.drawable.account_balance_wallet_24dp)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setColor(Color.RED)
                .setAutoCancel(true);

        // Add action buttons
        Intent budgetIntent = new Intent(context, MenuActivity.class);
        budgetIntent.putExtra("ID_USER", userId);
        budgetIntent.putExtra("NAVIGATE_TO_BUDGET", true);
        budgetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent budgetPendingIntent = PendingIntent.getActivity(
                context, notificationId + 3000, budgetIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.drawable.settings_24dp, "Adjust Budget", budgetPendingIntent);

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