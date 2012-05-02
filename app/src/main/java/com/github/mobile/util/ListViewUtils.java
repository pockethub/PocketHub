/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.util;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.widget.ListView;

import com.github.mobile.R.drawable;

/**
 * Helpers to configure a {@link ListView}
 * <p>
 * Used for list views that aren't defined in local XML files and must be configured at runtime
 */
public class ListViewUtils {

    /**
     * DP Height of the list divider
     */
    private static final int HEIGHT_DIVIDER = 2;

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
        Resources resources = context.getResources();
        listView.setDivider(resources.getDrawable(drawable.list_divider));
        int dividerHeight = (int) (TypedValue.applyDimension(COMPLEX_UNIT_DIP, HEIGHT_DIVIDER,
                resources.getDisplayMetrics()) + 0.5F);
        listView.setDividerHeight(dividerHeight);
        return listView;
    }
}
