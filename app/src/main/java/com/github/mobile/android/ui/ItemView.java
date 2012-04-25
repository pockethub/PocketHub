package com.github.mobile.android.ui;

import android.view.View;

/**
 * Class that stores references to children of a view that get updated when the item in the view changes
 */
public abstract class ItemView {

    /**
     * Create item view storing references to children of given view to be accessed when the view is ready to display an
     * item
     *
     * @param view
     */
    public ItemView(View view) {
        // Intentionally left blank
    }
}
