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
package com.github.pockethub.ui.issue;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.alorma.github.sdk.bean.dto.response.IssueState;
import com.alorma.github.sdk.bean.dto.response.Label;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.kevinsawicki.wishlist.ViewUtils;
import com.github.pockethub.R;
import com.github.pockethub.ui.StyledText;
import com.github.pockethub.util.AvatarLoader;
import com.github.pockethub.util.TypefaceUtils;

import java.util.Date;
import java.util.List;

import static android.graphics.Paint.STRIKE_THRU_TEXT_FLAG;

/**
 * Base list adapter to display issues
 *
 * @param <V>
 */
public abstract class IssueListAdapter<V> extends SingleTypeAdapter<V> {

    /**
     * Maximum number of label bands to display
     */
    protected static final int MAX_LABELS = 8;

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
    public IssueListAdapter(int viewId, LayoutInflater inflater,
            Object[] elements, AvatarLoader avatars) {
        super(inflater, viewId);

        this.avatars = avatars;
        numberView = (TextView) inflater.inflate(viewId, null).findViewById(
                R.id.tv_issue_number);
        setItems(elements);
    }

    /**
     * Get number of issue
     *
     * @param issue
     * @return issue number
     */
    protected abstract int getNumber(V issue);

    @SuppressWarnings("unchecked")
    private void computeNumberWidth(final Object[] items) {
        int[] numbers = new int[items.length];
        for (int i = 0; i < numbers.length; i++)
            numbers[i] = getNumber((V) items[i]);
        int digits = Math.max(TypefaceUtils.getMaxDigits(numbers), 4);
        numberWidth = TypefaceUtils.getWidth(numberView, digits)
                + numberView.getPaddingLeft() + numberView.getPaddingRight();
    }

    @Override
    public void setItems(final Object[] items) {
        super.setItems(items);

        computeNumberWidth(items);
    }

    /**
     * Update issue number displayed in given text view
     *
     *
     * @param number
     * @param state
     * @param flags
     * @param viewIndex
     */
    protected void updateNumber(int number, IssueState state, int flags,
            int viewIndex) {
        TextView view = textView(viewIndex);
        view.setText(Integer.toString(number));
        if (state.equals(IssueState.closed))
            view.setPaintFlags(flags | STRIKE_THRU_TEXT_FLAG);
        else
            view.setPaintFlags(flags);
        view.getLayoutParams().width = numberWidth;
    }

    /**
     * Update reporter details in given text view
     *
     *
     * @param reporter
     * @param date
     * @param viewIndex
     */
    protected void updateReporter(String reporter, Date date, int viewIndex) {
        StyledText reporterText = new StyledText();
        reporterText.bold(reporter);
        reporterText.append(' ');
        reporterText.append(date);
        setText(viewIndex, reporterText);
    }

    /**
     * Update label views with values from given label models
     *
     * @param labels
     * @param viewIndex
     */
    protected void updateLabels(final List<Label> labels, final int viewIndex) {
        if (labels != null && !labels.isEmpty()) {
            int size = Math.min(labels.size(), MAX_LABELS);
            for (int i = 0; i < size; i++) {
                String color = labels.get(i).color;
                if (!TextUtils.isEmpty(color)) {
                    View view = view(viewIndex + i);
                    view.setBackgroundColor(Color.parseColor('#' + color));
                    ViewUtils.setGone(view, false);
                } else
                    setGone(viewIndex + i, true);
            }
            for (int i = size; i < MAX_LABELS; i++)
                setGone(viewIndex + i, true);
        } else
            for (int i = 0; i < MAX_LABELS; i++)
                setGone(viewIndex + i, true);
    }
}
