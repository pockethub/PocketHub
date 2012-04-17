package com.github.mobile.android.util;

import com.github.mobile.android.R.drawable;

import android.content.Context;
import android.widget.ListView;

/**
 * Helpers to configure a {@link ListView}
 * <p>
 * Used for list views that aren't defined in local XML files and must be configured at runtime
 */
public class ListViewHelper {

    /**
     * Configure list view
     *
     * @param context
     * @param listView
     * @param fastScroll
     * @return specified list view
     */
    public static ListView configure(final Context context, final ListView listView, final boolean fastScroll) {
        listView.setFastScrollEnabled(fastScroll);
        listView.setDivider(context.getResources().getDrawable(drawable.list_divider));
        listView.setDividerHeight(2);
        return listView;
    }
}
