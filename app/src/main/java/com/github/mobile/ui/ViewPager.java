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
package com.github.mobile.ui;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * {@link ViewPager} extension with support for horizontally scrolling an
 * embedded {@link WebView}
 */
public class ViewPager extends android.support.v4.view.ViewPager {

    /**
     * @param context
     */
    public ViewPager(final Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public ViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(final View v, final boolean checkV,
            final int dx, final int x, final int y) {
        if (SDK_INT < ICE_CREAM_SANDWICH && v instanceof WebView)
            return ((WebView) v).canScrollHorizontally(-dx);
        else
            return super.canScroll(v, checkV, dx, x, y);
    }
}
