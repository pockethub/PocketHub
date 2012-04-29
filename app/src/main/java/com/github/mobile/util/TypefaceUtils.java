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

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.Arrays;

/**
 * Helpers for dealing with custom typefaces and measuring text to display
 */
public class TypefaceUtils {

    /**
     * Fork icon
     */
    public static final char ICON_FORK = '\uf202';

    /**
     * Create icon
     */
    public static final char ICON_CREATE = '\uf203';

    /**
     * Delete icon
     */
    public static final char ICON_DELETE = '\uf204';

    /**
     * Push icon
     */
    public static final char ICON_PUSH = '\uf205';

    /**
     * Wiki icon
     */
    public static final char ICON_WIKI = '\uf207';

    /**
     * Upload icon
     */
    public static final char ICON_UPLOAD = '\uf212';

    /**
     * Gist icon
     */
    public static final char ICON_GIST = '\uf214';

    /**
     * Add member icon
     */
    public static final char ICON_ADD_MEMBER = '\uf226';

    /**
     * Follow icon
     */
    public static final char ICON_FOLLOW = '\uf228';

    /**
     * Watch icon
     */
    public static final char ICON_WATCH = '\uf229';

    /**
     * Pull request icon
     */
    public static final char ICON_PULL_REQUEST = '\uf234';

    /**
     * Issue open icon
     */
    public static final char ICON_ISSUE_OPEN = '\uf238';

    /**
     * Issue reopen icon
     */
    public static final char ICON_ISSUE_REOPEN = '\uf239';

    /**
     * Issue close icon
     */
    public static final char ICON_ISSUE_CLOSE = '\uf240';

    /**
     * Issue comment icon
     */
    public static final char ICON_ISSUE_COMMENT = '\uf241';

    /**
     * Comment icon
     */
    public static final char ICON_COMMENT = '\uf243';

    /**
     * Find the maximum number of digits in the given numbers
     *
     * @param numbers
     * @return max digits
     */
    public static int getMaxDigits(int... numbers) {
        int max = 1;
        for (int number : numbers)
            max = Math.max(max, (int) Math.log10(number) + 1);
        return max;
    }

    /**
     * Get width of number of digits
     *
     * @param view
     * @param numberOfDigits
     * @return number width
     */
    public static int getWidth(TextView view, int numberOfDigits) {
        Paint paint = new Paint();
        paint.setTypeface(view.getTypeface());
        paint.setTextSize(view.getTextSize());
        char[] text = new char[numberOfDigits];
        Arrays.fill(text, '0');
        return Math.round(paint.measureText(text, 0, text.length));
    }

    /**
     * Get octocons typeface
     *
     * @param context
     * @return octocons typeface
     */
    public static Typeface getOctocons(Context context) {
        return getTypeface(context, "octocons-regular-webfont.ttf");
    }

    /**
     * Set octocons typeface on given text view(s)
     *
     * @param textViews
     */
    public static void setOctocons(TextView... textViews) {
        if (textViews == null || textViews.length == 0)
            return;

        Typeface typeface = getOctocons(textViews[0].getContext());
        for (TextView textView : textViews)
            textView.setTypeface(typeface);
    }

    /**
     * Get typeface with name
     *
     * @param context
     * @param name
     * @return typeface
     */
    public static Typeface getTypeface(Context context, String name) {
        return Typeface.createFromAsset(context.getAssets(), name);
    }
}
