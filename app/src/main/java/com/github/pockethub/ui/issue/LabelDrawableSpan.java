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

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.PaintDrawable;
import android.text.style.DynamicDrawableSpan;
import android.widget.TextView;

import com.alorma.github.sdk.bean.dto.response.Label;
import com.github.pockethub.R;
import com.github.pockethub.ui.StyledText;
import com.github.pockethub.util.ServiceUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import static android.graphics.Color.WHITE;
import static android.graphics.Typeface.DEFAULT_BOLD;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Locale.US;


/**
 * Span that draws a {@link Label}
 */
public class LabelDrawableSpan extends DynamicDrawableSpan {

    private static final int PADDING_LEFT = 10;

    private static final int PADDING_RIGHT = 10;

    private static final int PADDING_TOP = 8;

    private static final int PADDING_BOTTOM = 8;

    private static class LabelDrawable extends PaintDrawable {

        private final String name;

        private final float height;

        private final float paddingLeft;

        private final float textHeight;

        private final int textColor;

        private final LayerDrawable layers;

        /**
         * Create drawable for labels
         *
         * @param paddingLeft
         * @param textHeight
         * @param bounds
         * @param resources
         * @param textSize
         * @param name
         * @param bg
         */
        public LabelDrawable(final float paddingLeft, final float textHeight,
                final Rect bounds, final Resources resources,
                final float textSize, final String name, final int bg) {
            this.paddingLeft = paddingLeft;
            this.textHeight = textHeight;
            this.name = name;
            height = bounds.height();

            float[] hsv = new float[3];
            Color.colorToHSV(bg, hsv);
            if ((hsv[2] > 0.6 && hsv[1] < 0.4)
                    || (hsv[2] > 0.7 && hsv[0] > 40 && hsv[0] < 200)) {
                hsv[2] = 0.4F;
                textColor = Color.HSVToColor(hsv);
            } else
                textColor = WHITE;

            layers = (LayerDrawable) resources
                    .getDrawable(R.drawable.label_background);
            ((GradientDrawable) ((LayerDrawable) layers
                    .findDrawableByLayerId(R.id.item_outer_layer))
                    .findDrawableByLayerId(R.id.item_outer)).setColor(bg);
            ((GradientDrawable) ((LayerDrawable) layers
                    .findDrawableByLayerId(R.id.item_inner_layer))
                    .findDrawableByLayerId(R.id.item_inner)).setColor(bg);
            ((GradientDrawable) layers.findDrawableByLayerId(R.id.item_bg))
                    .setColor(bg);

            Paint p = getPaint();
            p.setAntiAlias(true);
            p.setColor(resources.getColor(android.R.color.transparent));
            p.setTypeface(DEFAULT_BOLD);
            p.setTextSize(textSize);

            layers.setBounds(bounds);
            setBounds(bounds);
        }

        @Override
        public void draw(final Canvas canvas) {
            super.draw(canvas);

            layers.draw(canvas);

            final Paint paint = getPaint();
            final int original = paint.getColor();

            paint.setColor(textColor);
            canvas.drawText(name, paddingLeft, height
                    - ((height - textHeight) / 2), paint);

            paint.setColor(original);
        }
    }

    /**
     * Set text on view to be given labels
     *
     * @param view
     * @param labels
     */
    public static void setText(final TextView view,
            final Collection<Label> labels) {
        final Label[] sortedLabels = labels.toArray(new Label[labels.size()]);
        Arrays.sort(sortedLabels, new Comparator<Label>() {

            @Override
            public int compare(final Label lhs, final Label rhs) {
                return CASE_INSENSITIVE_ORDER.compare(lhs.name,
                        rhs.name);
            }
        });
        setText(view, sortedLabels);
    }

    /**
     * Set text on view to be given label
     *
     * @param view
     * @param label
     */
    public static void setText(final TextView view, final Label label) {
        setText(view, new Label[] { label });
    }

    private static void setText(final TextView view, final Label[] labels) {
        final Resources resources = view.getResources();
        final float paddingTop = ServiceUtils.getPixels(resources, PADDING_TOP);
        final float paddingLeft = ServiceUtils.getPixels(resources,
                PADDING_LEFT);
        final float paddingRight = ServiceUtils.getPixels(resources,
                PADDING_RIGHT);
        final float paddingBottom = ServiceUtils.getPixels(resources,
                PADDING_BOTTOM);

        Paint p = new Paint();
        p.setTypeface(DEFAULT_BOLD);
        p.setTextSize(view.getTextSize());

        final Rect textBounds = new Rect();
        String[] names = new String[labels.length];
        int[] nameWidths = new int[labels.length];
        int textHeight = MIN_VALUE;
        for (int i = 0; i < labels.length; i++) {
            String name = labels[i].name.toUpperCase(US);
            textBounds.setEmpty();
            p.getTextBounds(name, 0, name.length(), textBounds);
            names[i] = name;
            textHeight = Math.max(textBounds.height(), textHeight);
            nameWidths[i] = textBounds.width();
        }

        final float textSize = view.getTextSize();
        final StyledText text = new StyledText();
        for (int i = 0; i < labels.length; i++) {
            Rect bounds = new Rect();
            bounds.right = Math.round(nameWidths[i] + paddingLeft
                    + paddingRight + 0.5F);
            bounds.bottom = Math.round(textHeight + paddingTop + paddingBottom
                    + 0.5F);

            text.append('\uFFFC', new LabelDrawableSpan(resources, textSize,
                    labels[i].color, paddingLeft, textHeight, bounds,
                    names[i]));

            if (i + 1 < labels.length)
                text.append(' ');
        }
        view.setText(text);
    }

    private final Resources resources;

    private final float textSize;

    private final String name;

    private final int color;

    private final float textHeight;

    private final float paddingLeft;

    private final Rect bounds;

    /**
     * Create background span for label
     *
     * @param resources
     * @param textSize
     * @param color
     * @param paddingLeft
     * @param textHeight
     * @param bounds
     * @param name
     */
    public LabelDrawableSpan(final Resources resources, final float textSize,
            final String color, final float paddingLeft,
            final float textHeight, final Rect bounds, final String name) {
        this.resources = resources;
        this.textSize = textSize;
        this.color = Color.parseColor('#' + color);
        this.paddingLeft = paddingLeft;
        this.textHeight = textHeight;
        this.bounds = bounds;
        this.name = name;
    }

    @Override
    public Drawable getDrawable() {
        return new LabelDrawable(paddingLeft, textHeight, bounds, resources,
                textSize, name, color);
    }
}
