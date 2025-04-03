package com.example.campusexpensemanagerse06304;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.campusexpensemanagerse06304.adapter.ViewPagerAdapter;
import com.example.campusexpensemanagerse06304.database.ExpenseDb;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    BottomNavigationView bottomNavigationView;
    ViewPager2 viewPager2;
    DrawerLayout drawerLayout;
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        viewPager2 = findViewById(R.id.viewPager);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);

        initializeDatabase();

        bottomNavigationView = findViewById(R.id.bottom_navigation);

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

        // bat su kien logout
        logout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                Intent intentLogout = new Intent(MenuActivity.this, SignInActivity.class);
                startActivity(intentLogout);
                finish();
                return false;
            }
        });

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


    }

    private void initializeDatabase() {
        ExpenseDb expenseDb = new ExpenseDb(this);
        SQLiteDatabase db = expenseDb.getWritableDatabase();

        try {
            // First check if the categories table exists
            Cursor cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='categories'", null);
            boolean tableExists = cursor.getCount() > 0;
            cursor.close();

            if (!tableExists) {
                // Table doesn't exist, so we need to create it
                expenseDb.onCreate(db);
            } else {
                // Table exists, check if it has data
                cursor = db.rawQuery("SELECT COUNT(*) FROM categories", null);
                cursor.moveToFirst();
                int count = cursor.getInt(0);
                cursor.close();

                if (count == 0) {
                    // Insert default categories
                    String[] categories = {"Housing", "Food", "Transportation", "Entertainment", "Education", "Health"};
                    String[] colors = {"#FF5722", "#4CAF50", "#2196F3", "#9C27B0", "#FFC107", "#E91E63"};

                    for (int i = 0; i < categories.length; i++) {
                        ContentValues values = new ContentValues();
                        values.put("name", categories[i]);
                        values.put("description", categories[i] + " expenses");
                        values.put("color", colors[i]);
                        db.insert("categories", null, values);
                    }
                }
            }
        } catch (Exception e) {
            // If an error occurs, create tables directly
            Log.e("MenuActivity", "Database initialization error: " + e.getMessage());
            expenseDb.onCreate(db);
        } finally {
            db.close();
        }
    }
    private void setupViewPager(){
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
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
                }else {
                    bottomNavigationView.getMenu().findItem(R.id.menu_home).setChecked(true);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
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
        }else {
            viewPager2.setCurrentItem(0);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
