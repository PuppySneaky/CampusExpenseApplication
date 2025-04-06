package com.example.campusexpensemanagerse06304;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Utility class to test notification functionality in the app
 */
public class NotificationTester {
    private static final String TEST_CHANNEL_ID = "test_notifications";
    private static final String TEST_CHANNEL_NAME = "Test Notifications";
    private static final String TEST_CHANNEL_DESC = "Channel for testing notification functionality";
    private static final int TEST_NOTIFICATION_ID = 9999;

    private Context context;

    public NotificationTester(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    /**
     * Creates the notification channel for test notifications
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    TEST_CHANNEL_ID,
                    TEST_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);

            channel.setDescription(TEST_CHANNEL_DESC);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Sends a test notification to verify notification functionality
     * @return true if the notification was sent successfully, false otherwise
     */
    public boolean sendTestNotification() {
        // Create intent to open the app when notification is tapped
        Intent intent = new Intent(context, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, TEST_CHANNEL_ID)
                .setSmallIcon(R.drawable.account_balance_wallet_24dp)
                .setContentTitle("Test Notification")
                .setContentText("Congratulations! Notifications are working properly.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("This is a test notification to verify that the notification system is working correctly. If you're seeing this, notifications are properly configured!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show notification
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(TEST_NOTIFICATION_ID, builder.build());
            Toast.makeText(context, "Test notification sent", Toast.LENGTH_SHORT).show();
            return true;
        } catch (SecurityException e) {
            Toast.makeText(context,
                    "Notification permission denied. Please enable notifications in settings.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }
}