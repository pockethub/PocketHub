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
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

/**
 * Web view extension with scrolling fixes
 */
public class WebView extends android.webkit.WebView {

    /**
     * @param context
     * @param attrs
     * @param defStyle
     * @param privateBrowsing
     */
    public WebView(final Context context, final AttributeSet attrs,
            final int defStyle, final boolean privateBrowsing) {
        super(context, attrs, defStyle, privateBrowsing);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    @SuppressLint("NewApi")
    public WebView(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * @param context
     * @param attrs
     */
    public WebView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param context
     */
    public WebView(final Context context) {
        super(context);
    }

    private boolean canScrollCodeHorizontally(final int direction) {
        final int range = computeHorizontalScrollRange()
                - computeHorizontalScrollExtent();
        if (range == 0)
            return false;

        if (direction < 0)
            return computeHorizontalScrollOffset() > 0;
        else
            return computeHorizontalScrollOffset() < range - 1;
    }

    @SuppressLint("NewApi")
    @Override
    public boolean canScrollHorizontally(final int direction) {
        if (SDK_INT >= 14)
            return super.canScrollHorizontally(direction);
        else
            return canScrollCodeHorizontally(direction);
    }
}
