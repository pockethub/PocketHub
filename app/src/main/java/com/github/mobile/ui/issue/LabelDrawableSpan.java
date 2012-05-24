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

import static android.graphics.Color.WHITE;
import static android.graphics.Typeface.DEFAULT_BOLD;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Locale.US;
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
import android.util.TypedValue;
import android.widget.TextView;

import com.actionbarsherlock.R.drawable;
import com.github.mobile.R.id;
import com.github.mobile.ui.StyledText;

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

    private static float getPixels(final Resources resources, final int dp) {
        return TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    private static class LabelDrawable extends PaintDrawable {

        private final String name;

        private final int bg;

        private final float height;

        private final float paddingLeft;

        private final float paddingRight;

        private final float paddingTop;

        private final float paddingBottom;

        private final float textHeight;

        private final int textColor;

        private final LayerDrawable layers;

        /**
         * Create drawable for labels
         *
         * @param resources
         * @param textSize
         * @param label
         */
        public LabelDrawable(final Resources resources, final float textSize, final Label label) {
            paddingTop = getPixels(resources, PADDING_TOP);
            paddingLeft = getPixels(resources, PADDING_LEFT);
            paddingRight = getPixels(resources, PADDING_RIGHT);
            paddingBottom = getPixels(resources, PADDING_BOTTOM);

            bg = Color.parseColor('#' + label.getColor());
            float[] hsv = new float[3];
            Color.colorToHSV(bg, hsv);
            if ((hsv[2] > 0.6 && hsv[1] < 0.4) || (hsv[2] > 0.7 && hsv[0] > 40 && hsv[0] < 200)) {
                hsv[2] = 0.4F;
                textColor = Color.HSVToColor(hsv);
            } else
                textColor = WHITE;

            layers = (LayerDrawable) resources.getDrawable(drawable.label_background);
            ((GradientDrawable) layers.findDrawableByLayerId(id.item_inner)).setColor(Color.argb(225, Color.red(bg),
                    Color.green(bg), Color.blue(bg)));
            ((GradientDrawable) layers.findDrawableByLayerId(id.item_bg)).setColor(bg);

            name = label.getName().toUpperCase(US);

            Paint p = getPaint();
            p.setAntiAlias(true);
            p.setColor(resources.getColor(android.R.color.transparent));
            p.setTypeface(DEFAULT_BOLD);
            p.setTextSize(textSize);

            final Rect bounds = new Rect();
            final Rect textBounds = new Rect();
            p.getTextBounds(name, 0, name.length(), textBounds);
            bounds.right = Math.round(textBounds.width() + paddingLeft + paddingRight + 0.5F);
            textHeight = textBounds.height();
            bounds.bottom = Math.round(textHeight + paddingTop + paddingBottom + 0.5F);
            height = bounds.height();

            p.setTypeface(DEFAULT_BOLD);
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
            canvas.drawText(name, paddingLeft, height - ((height - textHeight) / 2), paint);

            paint.setColor(original);
        }
    }

    /**
     * Set text on view to be given labels
     *
     * @param view
     * @param labels
     */
    public static void setText(final TextView view, final Collection<Label> labels) {
        final Label[] sortedLabels = labels.toArray(new Label[labels.size()]);
        Arrays.sort(sortedLabels, new Comparator<Label>() {

            @Override
            public int compare(final Label lhs, final Label rhs) {
                return CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
            }
        });

        final StyledText text = new StyledText();
        for (int i = 0; i < sortedLabels.length; i++) {
            text.append('\uFFFC', new LabelDrawableSpan(view.getResources(), view.getTextSize(), sortedLabels[i]));
            if (i + 1 < sortedLabels.length)
                text.append(' ');
        }
        view.setText(text);
    }

    /**
     * Set text on view to be given label
     *
     * @param view
     * @param label
     */
    public static void setText(final TextView view, final Label label) {
        StyledText text = new StyledText();
        text.append('\uFFFC', new LabelDrawableSpan(view.getResources(), view.getTextSize(), label));
        view.setText(text);
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
