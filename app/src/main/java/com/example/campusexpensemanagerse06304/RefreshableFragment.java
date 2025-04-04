
package com.example.campusexpensemanagerse06304;

/**
 * Interface for fragments that need to refresh their data
 * when application state changes (like when expenses are added/modified)
 */
public interface RefreshableFragment {
    /**
     * Refresh the fragment data
     */
    void refreshData();
}