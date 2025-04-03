package com.example.campusexpensemanagerse06304.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.campusexpensemanagerse06304.BudgetFragment;
import com.example.campusexpensemanagerse06304.ExpensesFragment;
import com.example.campusexpensemanagerse06304.HistoryFragment;
import com.example.campusexpensemanagerse06304.HomeFragment;
import com.example.campusexpensemanagerse06304.SettingFragment;
import com.example.campusexpensemanagerse06304.SimpleBudgetFragment;
import com.example.campusexpensemanagerse06304.SimpleExpensesFragment;
import com.example.campusexpensemanagerse06304.SimpleHistoryFragment;
import com.example.campusexpensemanagerse06304.SimpleHomeFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new SimpleHomeFragment(); // Use our new home fragment
        } else if (position == 1) {
            return new SimpleExpensesFragment();
        } else if (position == 2) {
            return new SimpleBudgetFragment();
        } else if (position == 3) {
            return new SimpleHistoryFragment();
        } else if (position == 4) {
            return new SettingFragment();
        } else {
            return new SimpleHomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
