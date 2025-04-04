package com.example.campusexpensemanagerse06304.adapter;

import android.util.Log;

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
    private static final String TAG = "ViewPagerAdapter";

    private final Map<Integer, Fragment> fragmentMap = new HashMap<>();

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d(TAG, "Creating fragment at position " + position);

        // Check if we already have a fragment for this position
        if (fragmentMap.containsKey(position)) {
            Log.d(TAG, "Returning existing fragment for position " + position);
            return fragmentMap.get(position);
        }

        // Create a new fragment based on position
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new SimpleHomeFragment();
                break;
            case 1:
                fragment = new SimpleExpensesFragment();
                break;
            case 2:
                fragment = new SimpleBudgetFragment();
                break;
            case 3:
                fragment = new SimpleHistoryFragment();
                break;
            case 4:
                fragment = new SettingFragment();
                break;
            default:
                fragment = new SimpleHomeFragment();
                break;
        }

        // Store the fragment in our map
        fragmentMap.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 5; // Home, Expenses, Budget, History, Settings
    }

    /**
     * Get the fragment at the specified position
     * @param position The position of the fragment to retrieve
     * @return The fragment at the specified position, or null if not found
     */
    public Fragment getFragment(int position) {
        return fragmentMap.get(position);
    }

    /**
     * Get all fragments currently managed by this adapter
     * @return A map of position to fragment
     */
    public Map<Integer, Fragment> getAllFragments() {
        return fragmentMap;
    }
}