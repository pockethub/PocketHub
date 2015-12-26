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
package com.github.pockethub.util;

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
     * Private repository icon
     */
    public static final String ICON_PRIVATE = "\uf26a";

    /**
     * Public repository icon
     */
    public static final String ICON_PUBLIC = "\uf201";

    /**
     * Fork icon
     */
    public static final String ICON_FORK = "\uf202";

    /**
     * Create icon
     */
    public static final String ICON_CREATE = "\uf203";

    /**
     * Delete icon
     */
    public static final String ICON_DELETE = "\uf204";

    /**
     * Push icon
     */
    public static final String ICON_PUSH = "\uf205";

    /**
     * Wiki icon
     */
    public static final String ICON_WIKI = "\uf207";

    /**
     * Upload icon
     */
    public static final String ICON_UPLOAD = "\uf20C";

    /**
     * Gist icon
     */
    public static final String ICON_GIST = "\uf20E";

    /**
     * Add member icon
     */
    public static final String ICON_ADD_MEMBER = "\uf21A";

    /**
     * Public mirror repository icon
     */
    public static final String ICON_MIRROR_PUBLIC = "\uf224";

    /**
     * Public mirror repository icon
     */
    public static final String ICON_MIRROR_PRIVATE = "\uf225";

    /**
     * Follow icon
     */
    public static final String ICON_FOLLOW = "\uf21C";

    /**
     * Star icon
     */
    public static final String ICON_STAR = "\uf02A";

    /**
     * Pull request icon
     */
    public static final String ICON_PULL_REQUEST = "\uf222";

    /**
     * Issue open icon
     */
    public static final String ICON_ISSUE_OPEN = "\uf226";

    /**
     * Issue reopen icon
     */
    public static final String ICON_ISSUE_REOPEN = "\uf227";

    /**
     * Issue close icon
     */
    public static final String ICON_ISSUE_CLOSE = "\uf228";

    /**
     * Issue comment icon
     */
    public static final String ICON_ISSUE_COMMENT = "\uf229";

    /**
     * Comment icon
     */
    public static final String ICON_COMMENT = "\uf22b";

    /**
     * News icon
     */
    public static final String ICON_NEWS = "\uf234";

    /**
     * Watch icon
     */
    public static final String ICON_WATCH = "\uf04e";

    /**
     * Team icon
     */
    public static final String ICON_TEAM = "\uf019";

    /**
     * Code icon
     */
    public static final String ICON_CODE = "\uf010";

    /**
     * Tag icon
     */
    public static final String ICON_TAG = "\uf015";

    /**
     * Commit icon
     */
    public static final String ICON_COMMIT = "\uf01f";

    /**
     * Merge icon
     */
    public static final String ICON_MERGE = "\uf023";

    /**
     * Key icon
     */
    public static final String ICON_KEY = "\uf049";

    /**
     * Lock icon
     */
    public static final String ICON_LOCK = "\uf06a";

    /**
     * Milestone icon
     */
    public static final String ICON_MILESTONE = "\uf075";

    /**
     * Bookmark icon
     */
    public static final String ICON_BOOKMARK = "\uf07b";

    /**
     * Person icon
     */
    public static final String ICON_PERSON = "\uf218";

    /**
     * Add icon
     */
    public static final String ICON_ADD = "\uf05d";

    /**
     * Broadcast icon
     */
    public static final String ICON_BROADCAST = "\uf030";

    /**
     * Edit icon
     */
    public static final String ICON_EDIT = "\uf058";

    private static Typeface OCTICONS;

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
     * Get octicons typeface
     *
     * @param context
     * @return octicons typeface
     */
    public static Typeface getOcticons(final Context context) {
        if (OCTICONS == null)
            OCTICONS = getTypeface(context, "octicons-regular-webfont.ttf");
        return OCTICONS;
    }

    /**
     * Set octicons typeface on given text view(s)
     *
     * @param textViews
     */
    public static void setOcticons(final TextView... textViews) {
        if (textViews == null || textViews.length == 0)
            return;

        Typeface typeface = getOcticons(textViews[0].getContext());
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
    public static Typeface getTypeface(final Context context, final String name) {
        return Typeface.createFromAsset(context.getAssets(), name);
    }
}
