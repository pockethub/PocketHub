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
package com.github.mobile.ui.issue;

import static android.graphics.Paint.STRIKE_THRU_TEXT_FLAG;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemView;
import com.github.mobile.ui.StyledText;
import com.github.mobile.util.AvatarLoader;
import com.github.mobile.util.TypefaceUtils;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * Base list adapter to display issues
 *
 * @param <I>
 * @param <V>
 */
public abstract class IssueListAdapter<I, V extends ItemView> extends
        ItemListAdapter<I, V> {

    /**
     * Number formatter
     */
    private static final NumberFormat FORMAT = NumberFormat
            .getIntegerInstance();

    /**
     * Avatar loader
     */
    protected final AvatarLoader avatars;

    /**
     * View containing the issue number
     */
    private final TextView numberView;

    /**
     * Width of widest issue number
     */
    private int numberWidth;

    /**
     * @param viewId
     * @param inflater
     * @param elements
     * @param avatars
     */
    public IssueListAdapter(int viewId, LayoutInflater inflater, I[] elements,
            AvatarLoader avatars) {
        super(viewId, inflater, elements);

        this.avatars = avatars;
        this.numberView = (TextView) inflater.inflate(viewId, null)
                .findViewById(id.tv_issue_number);

        if (elements != null && elements.length > 0)
            computeNumberWidth(elements);
    }

    /**
     * Get number of issue
     *
     * @param issue
     * @return issue number
     */
    protected abstract int getNumber(I issue);

    @SuppressWarnings("unchecked")
    private void computeNumberWidth(final Object[] items) {
        int[] numbers = new int[items.length];
        for (int i = 0; i < numbers.length; i++)
            numbers[i] = getNumber((I) items[i]);
        int digits = Math.max(TypefaceUtils.getMaxDigits(numbers), 4);
        numberWidth = TypefaceUtils.getWidth(numberView, digits)
                + numberView.getPaddingLeft() + numberView.getPaddingRight();
    }

    @Override
    public ItemListAdapter<I, V> setItems(final Object[] items) {
        computeNumberWidth(items);

        return super.setItems(items);
    }

    /**
     * Update issue number displayed in given text view
     *
     * @param number
     * @param state
     * @param flags
     * @param view
     */
    protected void updateNumber(int number, String state, int flags,
            TextView view) {
        view.setText(Integer.toString(number));
        if (IssueService.STATE_CLOSED.equals(state))
            view.setPaintFlags(flags | STRIKE_THRU_TEXT_FLAG);
        else
            view.setPaintFlags(flags);
        view.getLayoutParams().width = numberWidth;
    }

    /**
     * Update comment count in given text view
     *
     * @param comments
     * @param view
     */
    protected void updateComments(int comments, TextView view) {
        view.setText(FORMAT.format(comments));
    }

    /**
     * Update reporter details in given text view
     *
     * @param reporter
     * @param date
     * @param view
     */
    protected void updateReporter(String reporter, Date date, TextView view) {
        StyledText reporterText = new StyledText();
        reporterText.bold(reporter);
        reporterText.append(' ');
        reporterText.append(date);
        view.setText(reporterText);
    }

    /**
     * Update label views with values from given label models
     *
     * @param labels
     * @param views
     */
    protected void updateLabels(List<Label> labels, View[] views) {
        if (labels != null && !labels.isEmpty()) {
            int size = Math.min(labels.size(), views.length);
            for (int i = 0; i < size; i++) {
                String color = labels.get(i).getColor();
                if (!TextUtils.isEmpty(color)) {
                    views[i].setBackgroundColor(Color.parseColor('#' + color));
                    views[i].setVisibility(VISIBLE);
                } else
                    views[i].setVisibility(GONE);
            }
            for (int i = size; i < views.length; i++)
                views[i].setVisibility(GONE);
        } else
            for (View label : views)
                label.setVisibility(GONE);
    }
}
