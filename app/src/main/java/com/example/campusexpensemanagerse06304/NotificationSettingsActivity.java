package com.example.campusexpensemanagerse06304;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Activity for configuring notification settings
 */
public class NotificationSettingsActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 123;

    // UI Elements
    private SwitchCompat switchNotifications;
    private SeekBar seekBarWarning;
    private SeekBar seekBarExceeded;
    private TextView tvWarningThreshold;
    private TextView tvExceededThreshold;
    private Spinner spinnerFrequency;
    private Button btnTestNotification;
    private Button btnSaveSettings;
    private Button btnBack;

    // Notification Manager
    private BudgetNotificationManager notificationManager;

    // Frequency options in hours
    private final int[] frequencyOptions = {6, 12, 24, 48, 72};
    private final String[] frequencyLabels = {
            "Every 6 hours",
            "Every 12 hours",
            "Once a day (24 hours)",
            "Every 2 days (48 hours)",
            "Every 3 days (72 hours)"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        // Initialize notification manager
        notificationManager = new BudgetNotificationManager(this);

        // Initialize views
        initializeViews();

        // Load current settings
        loadCurrentSettings();

        // Setup listeners
        setupListeners();

        // Request notification permission if needed
        requestNotificationPermissionIfNeeded();
    }

    /**
     * Initialize UI elements
     */
    private void initializeViews() {
        // Main controls
        switchNotifications = findViewById(R.id.switchNotifications);
        seekBarWarning = findViewById(R.id.seekBarWarning);
        seekBarExceeded = findViewById(R.id.seekBarExceeded);
        tvWarningThreshold = findViewById(R.id.tvWarningThreshold);
        tvExceededThreshold = findViewById(R.id.tvExceededThreshold);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);

        // Buttons
        btnTestNotification = findViewById(R.id.btnTestNotification);
        btnSaveSettings = findViewById(R.id.btnSaveNotificationSettings);
        btnBack = findViewById(R.id.btnBack);

        // Setup frequency spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, frequencyLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);
    }

    /**
     * Load current notification settings
     */
    private void loadCurrentSettings() {
        // Load enabled/disabled state
        switchNotifications.setChecked(notificationManager.areNotificationsEnabled());

        // Load warning threshold (convert from 0-1 to 0-100)
        int warningProgress = (int)(notificationManager.getWarningThreshold() * 100);
        seekBarWarning.setProgress(warningProgress);
        tvWarningThreshold.setText(warningProgress + "%");

        // Load exceeded threshold (convert from 0-1 to 0-100)
        int exceededProgress = (int)(notificationManager.getExceededThreshold() * 100);
        seekBarExceeded.setProgress(exceededProgress);
        tvExceededThreshold.setText(exceededProgress + "%");

        // Load frequency setting
        int currentFrequency = notificationManager.getWarningFrequency();
        int spinnerPosition = 2; // Default to "Once a day"

        for (int i = 0; i < frequencyOptions.length; i++) {
            if (frequencyOptions[i] == currentFrequency) {
                spinnerPosition = i;
                break;
            }
        }

        spinnerFrequency.setSelection(spinnerPosition);
    }

    /**
     * Setup event listeners for UI controls
     */
    private void setupListeners() {
        // Warning threshold seek bar
        seekBarWarning.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Ensure value is at least 50%
                int adjustedProgress = Math.max(50, progress);
                if (progress < 50 && fromUser) {
                    seekBar.setProgress(adjustedProgress);
                }

                // Update text view
                tvWarningThreshold.setText(adjustedProgress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
        });

        // Exceeded threshold seek bar
        seekBarExceeded.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Ensure value is between 90% and 120%
                int adjustedProgress = Math.max(90, Math.min(120, progress));
                if ((progress < 90 || progress > 120) && fromUser) {
                    seekBar.setProgress(adjustedProgress);
                }

                // Update text view
                tvExceededThreshold.setText(adjustedProgress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
        });

        // Test notification button
        btnTestNotification.setOnClickListener(v -> {
            if (switchNotifications.isChecked()) {
                NotificationTester tester = new NotificationTester(this);
                boolean result = tester.sendTestNotification();

                if (!result) {
                    requestNotificationPermissionIfNeeded();
                }
            } else {
                Toast.makeText(this, "Please enable notifications first", Toast.LENGTH_SHORT).show();
            }
        });

        // Save settings button
        btnSaveSettings.setOnClickListener(v -> saveSettings());

        // Back button
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Save notification settings
     */
    private void saveSettings() {
        // Save enabled/disabled state
        notificationManager.setNotificationsEnabled(switchNotifications.isChecked());

        // Save warning threshold (convert from 0-100 to 0-1)
        double warningThreshold = seekBarWarning.getProgress() / 100.0;
        notificationManager.setWarningThreshold(warningThreshold);

        // Save exceeded threshold (convert from 0-100 to 0-1)
        double exceededThreshold = seekBarExceeded.getProgress() / 100.0;
        notificationManager.setExceededThreshold(exceededThreshold);

        // Save frequency setting
        int frequencyPosition = spinnerFrequency.getSelectedItemPosition();
        if (frequencyPosition >= 0 && frequencyPosition < frequencyOptions.length) {
            notificationManager.setWarningFrequency(frequencyOptions[frequencyPosition]);
        }

        Toast.makeText(this, "Notification settings saved", Toast.LENGTH_SHORT).show();
    }

    /**
     * Request notification permission if needed (Android 13+)
     */
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }
}