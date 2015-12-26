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

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.View.OnClickListener;

import com.github.pockethub.util.TimeUtils;

import java.util.Date;
import java.util.Locale;

import static android.graphics.Typeface.BOLD;

/**
 * Helpers on top of {@link SpannableStringBuilder}
 */
public class StyledText extends SpannableStringBuilder {

    /**
     * Append text and span to end of this text
     *
     * @param text
     * @param span
     * @return this text
     */
    public StyledText append(final CharSequence text, final Object span) {
        if (!TextUtils.isEmpty(text)) {
            append(text);
            if (span != null) {
                final int length = length();
                setSpan(span, length - text.length(), length,
                        SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return this;
    }

    @Override
    public StyledText append(char text) {
        super.append(text);
        return this;
    }

    @Override
    public StyledText append(CharSequence text) {
        if (text != null)
            super.append(text);
        return this;
    }

    /**
     * Append text and span to end of this text
     *
     * @param text
     * @param span
     * @return this text
     */
    public StyledText append(final char text, final Object span) {
        append(text);
        if (span != null) {
            final int length = length();
            setSpan(span, length - 1, length, SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return this;
    }

    /**
     * Append text in bold
     *
     * @param text
     * @return this text
     */
    public StyledText bold(final CharSequence text) {
        return append(text, new StyleSpan(BOLD));
    }

    /**
     * Append text in bold
     *
     * @param text
     * @param color
     * @return this text
     */
    public StyledText background(final CharSequence text, final int color) {
        return append(text, new BackgroundColorSpan(color));
    }

    /**
     * Append text in with custom foreground color
     *
     * @param text
     * @param color
     * @return this text
     */
    public StyledText foreground(final CharSequence text, final int color) {
        return append(text, new ForegroundColorSpan(color));
    }

    /**
     * Append text in with custom foreground color
     *
     * @param text
     * @param color
     * @return this text
     */
    public StyledText foreground(final char text, final int color) {
        return append(text, new ForegroundColorSpan(color));
    }

    /**
     * Append text in monospace typeface
     *
     * @param text
     * @return this text
     */
    public StyledText monospace(final CharSequence text) {
        return append(text, new TypefaceSpan("monospace"));
    }

    /**
     * Append text as URL
     *
     * @param text
     * @param listener
     * @return this text
     */
    public StyledText url(final CharSequence text,
            final OnClickListener listener) {
        return append(text, new URLSpan(text.toString()) {

            @Override
            public void onClick(View widget) {
                listener.onClick(widget);
            }
        });
    }

    /**
     * Append text as URL
     *
     * @param text
     * @return this text
     */
    public StyledText url(final CharSequence text) {
        return append(text, new URLSpan(text.toString()));
    }

    /**
     * Append given date in relative time format
     *
     * @param date
     * @return this text
     */
    public StyledText append(final Date date) {
        final CharSequence time = TimeUtils.getRelativeTime(date);
        // Un-capitalize time string if there is already a prefix.
        // So you get "opened in 5 days" instead of "opened In 5 days".
        final int timeLength = time.length();
        if (length() > 0 && timeLength > 0
                && Character.isUpperCase(time.charAt(0))) {
            append(time.subSequence(0, 1).toString()
                    .toLowerCase(Locale.getDefault()));
            append(time.subSequence(1, timeLength));
        } else
            append(time);

        return this;
    }
}
