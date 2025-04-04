package com.example.campusexpensemanagerse06304;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.campusexpensemanagerse06304.adapter.ViewPagerAdapter;
import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MenuActivity";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 123;

    BottomNavigationView bottomNavigationView;
    ViewPager2 viewPager2;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    private ViewPagerAdapter viewPagerAdapter;
    private int userId = -1;
    private Handler mainHandler;
    private ScheduledExecutorService scheduler;
    private BudgetNotificationManager notificationManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Get user ID from intent
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            userId = intent.getExtras().getInt("ID_USER", -1);
            Log.d(TAG, "User ID loaded: " + userId);
        }

        // Initialize main thread handler
        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize notification manager
        notificationManager = new BudgetNotificationManager(this);

        // Request notification permission if needed
        requestNotificationPermissionIfNeeded();

        // Initialize database
        initializeDatabase();

        // Initialize UI components
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        viewPager2 = findViewById(R.id.viewPager);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        MenuItem logout = menu.findItem(R.id.nav_logout);
        setupViewPager();

        // Setup logout button
        logout.setOnMenuItemClickListener(item -> {
            Intent intentLogout = new Intent(MenuActivity.this, SignInActivity.class);
            startActivity(intentLogout);
            finish();
            return false;
        });

        // Setup bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_home){
                viewPager2.setCurrentItem(0);
            } else if (item.getItemId() == R.id.menu_expense) {
                viewPager2.setCurrentItem(1);
            } else if (item.getItemId() == R.id.menu_budget) {
                viewPager2.setCurrentItem(2);
            } else if (item.getItemId() == R.id.menu_history) {
                viewPager2.setCurrentItem(3);
            }else if (item.getItemId() == R.id.menu_setting) {
                viewPager2.setCurrentItem(4);
            }
            return true;
        });

        // Set page change callback to refresh fragments when switching tabs
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Update bottom navigation
                if (position == 0){
                    bottomNavigationView.getMenu().findItem(R.id.menu_home).setChecked(true);
                } else if (position == 1) {
                    bottomNavigationView.getMenu().findItem(R.id.menu_expense).setChecked(true);
                } else if (position == 2) {
                    bottomNavigationView.getMenu().findItem(R.id.menu_budget).setChecked(true);
                } else if (position == 3) {
                    bottomNavigationView.getMenu().findItem(R.id.menu_history).setChecked(true);
                } else if (position == 4) {
                    bottomNavigationView.getMenu().findItem(R.id.menu_setting).setChecked(true);
                }

                // Refresh the current fragment
                Fragment currentFragment = viewPagerAdapter.getFragment(position);
                if (currentFragment instanceof SimpleHomeFragment) {
                    ((SimpleHomeFragment) currentFragment).refreshData();
                }
            }
        });

        // Start background budget checking service
        startBudgetChecking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shut down the scheduler
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /**
     * Start periodic budget checking in background
     */
    private void startBudgetChecking() {
        if (userId != -1) {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
            }

            // Create a new scheduled executor
            scheduler = Executors.newScheduledThreadPool(1);

            // Schedule periodic checks (every 15 minutes)
            scheduler.scheduleAtFixedRate(() -> {
                // Run notification check on background thread
                checkBudgetsAndNotify();
            }, 1, 15, TimeUnit.MINUTES);

            // Do an immediate check
            checkBudgetsAndNotify();
        }
    }

    /**
     * Check budgets and send notifications if needed
     */
    private void checkBudgetsAndNotify() {
        if (userId != -1) {
            try {
                notificationManager.checkBudgetsAndNotify(userId);
            } catch (Exception e) {
                Log.e(TAG, "Error checking budgets", e);
            }
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
                // Permission granted, you can proceed with showing notifications
            } else {
                Log.d(TAG, "Notification permission denied");
                // Permission denied, you can show a message or disable notifications
            }
        }
    }

    private void setupViewPager(){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager2.setAdapter(viewPagerAdapter);
    }

    public ViewPagerAdapter getViewPagerAdapter() {
        return viewPagerAdapter;
    }

    private void initializeDatabase() {
        ExpenseDb expenseDb = new ExpenseDb(this);

        try {
            // Force database creation
            expenseDb.getWritableDatabase().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Force refresh the fragment at the specified position
     * @param position Position of the fragment to refresh
     */
    public void refreshFragmentAtPosition(int position) {
        Fragment fragment = viewPagerAdapter.getFragment(position);
        if (fragment instanceof RefreshableFragment) {
            ((RefreshableFragment) fragment).refreshData();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_home){
            viewPager2.setCurrentItem(0);
        } else if (item.getItemId() == R.id.menu_expense) {
            viewPager2.setCurrentItem(1);
        } else if (item.getItemId() == R.id.menu_budget) {
            viewPager2.setCurrentItem(2);
        } else if (item.getItemId() == R.id.menu_history) {
            viewPager2.setCurrentItem(3);
        } else if (item.getItemId() == R.id.menu_setting) {
            viewPager2.setCurrentItem(4);
        } else {
            viewPager2.setCurrentItem(0);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}