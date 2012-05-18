/******************************************************************************
 *  Copyright (c) 2012 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *****************************************************************************/
package com.github.mobile.ui.issue;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static android.graphics.Paint.Style.FILL;
import static android.graphics.Paint.Style.STROKE;
import static android.graphics.Typeface.DEFAULT_BOLD;
import static android.graphics.Typeface.MONOSPACE;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Locale.US;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.text.SpannableStringBuilder;
import android.text.style.DynamicDrawableSpan;
import android.util.TypedValue;
import android.widget.TextView;

import com.github.mobile.R.color;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.eclipse.egit.github.core.Label;

/**
 * Span that draws a {@link Label}
 */
public class LabelDrawableSpan extends DynamicDrawableSpan {

    private static final int PADDING_LEFT = 10;

    private static final int PADDING_RIGHT = 10;

    private static final int PADDING_TOP = 8;

    private static final int PADDING_BOTTOM = 8;

    private static final int SHADOW_WIDTH = 2;

    private static final int CORNERS = 2;

    private static final int BORDER = 1;

    private static float getPixels(final Resources resources, final int dp) {
        return TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    private static class LabelDrawable extends PaintDrawable {

        private final String name;

        private final int bg;

        private final float height;

        private final float width;

        private final int borderColor;

        private final float paddingLeft;

        private final float paddingRight;

        private final float paddingTop;

        private final float paddingBottom;

        private final float shadowWidth;

        private final float border;

        private final float corners;

        private final float textHeight;

        /**
         * Create drawable for labels
         *
         * @param resources
         * @param textSize
         * @param label
         */
        public LabelDrawable(final Resources resources, final float textSize, Label label) {
            borderColor = resources.getColor(color.label_border);
            paddingTop = getPixels(resources, PADDING_TOP);
            paddingLeft = getPixels(resources, PADDING_LEFT);
            paddingRight = getPixels(resources, PADDING_RIGHT);
            paddingBottom = getPixels(resources, PADDING_BOTTOM);
            shadowWidth = getPixels(resources, SHADOW_WIDTH);
            corners = getPixels(resources, CORNERS);
            border = getPixels(resources, BORDER);

            name = label.getName().toUpperCase(US);
            bg = Color.parseColor('#' + label.getColor());

            Paint p = getPaint();
            p.setAntiAlias(true);
            p.setColor(resources.getColor(android.R.color.transparent));
            // Measure with monospace to ensure they are all the same height
            p.setTypeface(MONOSPACE);
            p.setTextSize(textSize);

            final Rect bounds = new Rect();
            final Rect textBounds = new Rect();
            p.getTextBounds(name, 0, name.length(), textBounds);
            bounds.right = Math.round(textBounds.width() + paddingLeft + paddingRight + 0.5F);
            width = bounds.width();
            textHeight = textBounds.height();
            bounds.bottom = Math.round(textHeight + paddingTop + paddingBottom + 0.5F);
            height = bounds.height();
            bounds.right += border;
            bounds.bottom += border;

            p.setTypeface(DEFAULT_BOLD);
            setBounds(bounds);
        }

        @Override
        public void draw(final Canvas canvas) {
            super.draw(canvas);

            final Paint paint = getPaint();
            final int original = paint.getColor();

            final RectF rect = new RectF();
            rect.left = 0;
            rect.right = width;
            rect.top = 0;
            rect.bottom = height;

            paint.setStyle(FILL);
            paint.setColor(bg);
            canvas.drawRoundRect(rect, corners + 1, corners + 1, paint);

            paint.setStyle(STROKE);
            paint.setColor(borderColor);
            rect.top += border / 2;
            rect.left += border / 2;
            paint.setStrokeWidth(border);
            canvas.drawRoundRect(rect, corners, corners, paint);

            paint.setStyle(FILL);
            paint.setColor(WHITE);
            paint.setShadowLayer(shadowWidth, 0, 0, BLACK);
            canvas.drawText(name, paddingLeft, rect.bottom - ((height - textHeight) / 2), paint);
            paint.clearShadowLayer();

            paint.setColor(original);
        }
    }

    /**
     * Create {@link CharSequence} with spans for each label
     *
     * @param view
     * @param labels
     * @return char sequence
     */
    public static CharSequence create(final TextView view, final Collection<Label> labels) {
        final Label[] sortedLabels = labels.toArray(new Label[labels.size()]);
        Arrays.sort(sortedLabels, new Comparator<Label>() {

            public int compare(Label lhs, Label rhs) {
                return CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
            }
        });

        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int i = 0; i < sortedLabels.length; i++) {
            builder.append('\uFFFC');
            builder.setSpan(new LabelDrawableSpan(view.getResources(), view.getTextSize(), sortedLabels[i]),
                    builder.length() - 1, builder.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            if (i + 1 < sortedLabels.length)
                builder.append(' ');
        }
        return builder;
    }

    private final Resources resources;

    private final float textSize;

    private final Label label;

    /**
     * Create background span for label
     *
     * @param resources
     * @param textSize
     *
     * @param label
     */
    public LabelDrawableSpan(final Resources resources, final float textSize, final Label label) {
        this.resources = resources;
        this.textSize = textSize;
        this.label = label;
    }

    @Override
    public Drawable getDrawable() {
        return new LabelDrawable(resources, textSize, label);
    }
}
