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

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static android.graphics.Paint.Style.FILL;
import static android.graphics.Paint.Style.STROKE;
import static android.graphics.Typeface.DEFAULT_BOLD;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Locale.US;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.PaintDrawable;
import android.util.TypedValue;
import android.view.View;

import com.github.mobile.R.color;
import com.github.mobile.util.ServiceUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.eclipse.egit.github.core.Label;

/**
 * Custom drawable for labels applied to an issue
 */
public class LabelsDrawable extends PaintDrawable {

    private static final int PADDING_LEFT = 10;

    private static final int PADDING_RIGHT = 10;

    private static final int PADDING_TOP = 8;

    private static final int PADDING_BOTTOM = 8;

    private static final int SPACING = 8;

    private static final int SHADOW_WIDTH = 2;

    private static final int CORNERS = 2;

    private static final int BORDER = 1;

    private static float getPixels(final Resources resources, final int dp) {
        return TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    private final String[] names;

    private final int[] colors;

    private final float[] heights;

    private final float[] widths;

    private final float[] rows;

    private float rowHeight;

    private int rowStart;

    private final int borderColor;

    private final float paddingLeft;

    private final float paddingRight;

    private final float paddingTop;

    private final float paddingBottom;

    private final float shadowWidth;

    private final float border;

    private final float corners;

    private final float spacing;

    /**
     * Create drawable for labels
     *
     * @param resources
     * @param view
     * @param textSize
     * @param labels
     */
    public LabelsDrawable(final Resources resources, final View view, final float textSize,
            final Collection<Label> labels) {
        borderColor = resources.getColor(color.label_border);
        paddingTop = getPixels(resources, PADDING_TOP);
        paddingLeft = getPixels(resources, PADDING_LEFT);
        paddingRight = getPixels(resources, PADDING_RIGHT);
        paddingBottom = getPixels(resources, PADDING_BOTTOM);
        shadowWidth = getPixels(resources, SHADOW_WIDTH);
        corners = getPixels(resources, CORNERS);
        border = getPixels(resources, BORDER);
        spacing = getPixels(resources, SPACING);

        final int maxWidth = ServiceUtils.getDisplayWidth(view) - view.getPaddingLeft() - view.getPaddingRight();

        rowStart = view.getPaddingLeft();

        Label[] sortedLabels = labels.toArray(new Label[labels.size()]);
        Arrays.sort(sortedLabels, new Comparator<Label>() {

            public int compare(Label lhs, Label rhs) {
                return CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
            }
        });

        names = new String[sortedLabels.length];
        colors = new int[sortedLabels.length];
        rows = new float[sortedLabels.length];
        heights = new float[sortedLabels.length];
        widths = new float[sortedLabels.length];

        for (int i = 0; i < sortedLabels.length; i++) {
            names[i] = sortedLabels[i].getName().toUpperCase(US);
            colors[i] = Color.parseColor('#' + sortedLabels[i].getColor());
        }

        Paint p = getPaint();
        p.setTypeface(DEFAULT_BOLD);
        p.setTextSize(textSize);

        rowHeight = paddingTop + paddingBottom;
        int rowCount = 0;

        final Rect bounds = new Rect();
        bounds.right = maxWidth;
        float availableRowSpace = maxWidth;
        final Rect textBounds = new Rect();
        for (int i = 0; i < names.length; i++) {
            getSize(names[i], i, textBounds);
            widths[i] = textBounds.width();
            heights[i] = textBounds.height();
            rowHeight = Math.max(rowHeight, heights[i] + paddingTop + paddingBottom);
            availableRowSpace -= widths[i];
            availableRowSpace -= spacing;
            if (availableRowSpace < 0) {
                rowCount++;
                availableRowSpace = maxWidth;
            }
            rows[i] = rowHeight * rowCount;
            if (rowCount > 0)
                rows[i] += spacing;
        }
        bounds.bottom = Math.round((rowCount + 1) * rowHeight + rowCount * paddingBottom + 0.5F);

        setBounds(bounds);
    }

    private void getSize(final String name, final int index, Rect tBounds) {
        tBounds.setEmpty();
        getPaint().getTextBounds(name, 0, name.length(), tBounds);
        tBounds.right += paddingLeft + paddingRight;
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        final Paint paint = getPaint();
        final int original = paint.getColor();

        final RectF rect = new RectF();
        float lastRow = -1F;
        float start = -1F;
        float rowStart = -1F;

        for (int i = 0; i < names.length; i++) {
            rowStart = rows[i];
            if (rowStart > lastRow)
                start = this.rowStart;

            rect.left = start;
            rect.right = start + widths[i];
            rect.top = rowStart;
            rect.bottom = rowStart + rowHeight;

            paint.setStyle(FILL);
            paint.setColor(colors[i]);
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
            canvas.drawText(names[i], start + paddingLeft, rect.bottom - ((rowHeight - heights[i]) / 2), paint);
            paint.clearShadowLayer();

            lastRow = rowStart;
            start = rect.right + spacing;
        }

        paint.setColor(original);
    }
}
