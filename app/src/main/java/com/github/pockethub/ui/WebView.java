/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Web view extension with scrolling fixes
 */
public class WebView extends android.webkit.WebView {

    private boolean intercept;

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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent p_event)
    {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent p_event) {
        if (intercept && getParent() != null)
            getParent().requestDisallowInterceptTouchEvent(true);

        return super.onTouchEvent(p_event);
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

    @Override
    public boolean canScrollHorizontally(final int direction) {
        return super.canScrollHorizontally(direction);
    }

    public void startIntercept() {
        intercept = true;
    }

    public void stopIntercept() {
        intercept = false;
    }
}
