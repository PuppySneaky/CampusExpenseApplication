package com.example.campusexpensemanagerse06304.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.campusexpensemanagerse06304.SimpleBudgetFragment;
import com.example.campusexpensemanagerse06304.SimpleExpensesFragment;
import com.example.campusexpensemanagerse06304.SimpleHistoryFragment;
import com.example.campusexpensemanagerse06304.SimpleHomeFragment;
import com.example.campusexpensemanagerse06304.SettingFragment;

import java.util.HashMap;
import java.util.Map;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private Map<Integer, Fragment> fragmentMap = new HashMap<>();

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;

        if (fragmentMap.containsKey(position)) {
            return fragmentMap.get(position);
        }

        if (position == 0) {
            fragment = new SimpleHomeFragment();
        } else if (position == 1) {
            fragment = new SimpleExpensesFragment();
        } else if (position == 2) {
            fragment = new SimpleBudgetFragment();
        } else if (position == 3) {
            fragment = new SimpleHistoryFragment();
        } else if (position == 4) {
            fragment = new SettingFragment();
        } else {
            fragment = new SimpleHomeFragment();
        }

        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    // Method to get existing fragments
    public Fragment getFragment(int position) {
        return fragmentMap.get(position);
    }
}