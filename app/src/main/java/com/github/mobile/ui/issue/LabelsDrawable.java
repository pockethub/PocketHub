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
import static android.graphics.Typeface.DEFAULT_BOLD;
import static java.util.Locale.US;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.PaintDrawable;

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

    private static final int PADDING_TOP = 10;

    private static final int PADDING_BOTTOM = 10;

    private static final int SIZE_SHADOW = 3;

    private static final int FIN = 8;

    private final String[] names;

    private final int[] colors;

    private final float[] heights;

    private final float[] widths;

    private final float[] rows;

    private float rowHeight;

    private int rowStart;

    /**
     * Create drawable for labels
     *
     * @param paddingLeft
     * @param textSize
     * @param maxWidth
     * @param labels
     */
    public LabelsDrawable(final int paddingLeft, final float textSize, final int maxWidth,
            final Collection<Label> labels) {
        rowStart = paddingLeft;

        Label[] sortedLabels = labels.toArray(new Label[labels.size()]);
        Arrays.sort(sortedLabels, new Comparator<Label>() {

            public int compare(Label lhs, Label rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
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

        rowHeight = PADDING_TOP + PADDING_BOTTOM;
        int rowCount = 0;

        final Rect bounds = new Rect();
        bounds.right = maxWidth;
        float availableRowSpace = maxWidth;
        final Rect textBounds = new Rect();
        for (int i = 0; i < names.length; i++) {
            getSize(names[i], i, textBounds);
            widths[i] = textBounds.width();
            heights[i] = textBounds.height();
            rowHeight = Math.max(rowHeight, heights[i] + PADDING_TOP + PADDING_BOTTOM);
            if (availableRowSpace - widths[i] >= 0)
                availableRowSpace -= widths[i];
            else {
                rowCount++;
                availableRowSpace = maxWidth;
            }
            rows[i] = rowHeight * rowCount;
            if (rowCount > 0)
                rows[i] += PADDING_BOTTOM;
        }
        bounds.bottom = Math.round((rowCount + 1) * rowHeight + rowCount * PADDING_BOTTOM + 0.5F);

        setBounds(bounds);
    }

    private void getSize(final String name, final int index, Rect tBounds) {
        tBounds.setEmpty();
        getPaint().getTextBounds(name, 0, name.length(), tBounds);
        tBounds.right += PADDING_LEFT + PADDING_RIGHT;
        if (index != 0)
            tBounds.right += FIN * 2;
        else
            tBounds.right += FIN;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint paint = getPaint();
        int original = paint.getColor();
        int start = rowStart;

        float height = rows[0] + rowHeight;
        float quarter = rowHeight / 4;
        float width = widths[0];
        float textOffset = (rowHeight - heights[0]) / 2;
        final Path path = new Path();
        path.moveTo(start, rows[0]);
        path.lineTo(start + width, rows[0]);
        path.lineTo(start + width - FIN, quarter);
        path.lineTo(start + width, quarter * 2);
        path.lineTo(start + width - FIN, quarter * 3);
        path.lineTo(start + width, height);
        path.lineTo(start, height);
        path.lineTo(start, rows[0]);

        paint.setColor(colors[0]);
        canvas.drawPath(path, paint);

        paint.setColor(WHITE);
        paint.setShadowLayer(SIZE_SHADOW, 0, 0, BLACK);
        float textStart = height - textOffset;
        canvas.drawText(names[0], start + PADDING_LEFT, textStart, paint);
        paint.clearShadowLayer();

        start += width;

        float lastRow = rows[0];

        for (int i = 1; i < names.length; i++) {
            float rowStart = rows[i];
            if (rowStart > lastRow)
                start = this.rowStart;

            width = widths[i];
            height = rowStart + rowHeight;
            textOffset = (rowHeight - heights[i]) / 2;
            textStart = height - textOffset;
            quarter = rowHeight / 4;

            path.reset();
            path.moveTo(start + FIN, rowStart);
            path.lineTo(start + width, rowStart);
            path.lineTo(start + width - FIN, rowStart + quarter);
            path.lineTo(start + width, rowStart + quarter * 2);
            path.lineTo(start + width - FIN, rowStart + quarter * 3);
            path.lineTo(start + width, height);
            path.lineTo(start + FIN, height);
            path.lineTo(start, rowStart + quarter * 3);
            path.lineTo(start + FIN, rowStart + quarter * 2);
            path.lineTo(start, rowStart + quarter);
            path.lineTo(start + FIN, rowStart);

            paint.setColor(colors[i]);
            canvas.drawPath(path, paint);

            paint.setShadowLayer(SIZE_SHADOW, 0, 0, BLACK);
            paint.setColor(WHITE);
            canvas.drawText(names[i], start + PADDING_LEFT + FIN, textStart, paint);
            paint.clearShadowLayer();

            lastRow = rowStart;
            start += width;
        }

        paint.setColor(original);
    }
}
