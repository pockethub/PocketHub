package com.github.mobile.android.issue;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static android.graphics.Typeface.DEFAULT_BOLD;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.PaintDrawable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;

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

    private final int[] rows;

    private final int rowHeight;

    /**
     * Create drawable for labels
     *
     * @param textSize
     * @param maxWidth
     * @param labels
     */
    public LabelsDrawable(final float textSize, final int maxWidth, final Collection<Label> labels) {
        Label[] sortedLabels = labels.toArray(new Label[labels.size()]);
        Arrays.sort(sortedLabels, new Comparator<Label>() {

            public int compare(Label lhs, Label rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });

        names = new String[sortedLabels.length];
        colors = new int[sortedLabels.length];
        rows = new int[sortedLabels.length];

        for (int i = 0; i < sortedLabels.length; i++) {
            names[i] = sortedLabels[i].getName().toUpperCase(Locale.US);
            colors[i] = Color.parseColor('#' + sortedLabels[i].getColor());
        }

        Paint p = getPaint();
        p.setTypeface(DEFAULT_BOLD);
        p.setTextSize(textSize);

        rowHeight = Math.round(textSize) + PADDING_TOP + PADDING_BOTTOM;
        int rowCount = 0;

        final Rect bounds = new Rect();
        bounds.right = maxWidth;
        int availableRowSpace = maxWidth;
        for (int i = 0; i < names.length; i++) {
            int width = getSize(names[i], i);
            if (availableRowSpace - width >= 0)
                availableRowSpace -= width;
            else {
                rowCount++;
                availableRowSpace = maxWidth;
            }
            rows[i] = rowHeight * rowCount;
            if (rowCount > 0)
                rows[i] += PADDING_BOTTOM;
        }
        bounds.bottom = (rowCount + 1) * rowHeight + rowCount * PADDING_BOTTOM + PADDING_BOTTOM;

        setBounds(bounds);
    }

    private int getSize(final String name, final int index) {
        Rect tBounds = new Rect();
        getPaint().getTextBounds(name, 0, name.length(), tBounds);
        float width = tBounds.width() + PADDING_LEFT + PADDING_RIGHT;
        if (index != 0)
            width += FIN * 2;
        else
            width += FIN;
        return (int) Math.ceil(width);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint paint = getPaint();
        int original = paint.getColor();
        int start = PADDING_LEFT;

        int width = getSize(names[0], 0);
        int height = rowHeight;
        int quarter = rowHeight / 4;
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
        canvas.drawText(names[0], start + PADDING_LEFT, height - PADDING_BOTTOM, paint);
        paint.clearShadowLayer();

        start += width;

        int lastRow = rows[0];

        for (int i = 1; i < names.length; i++) {
            int rowStart = rows[i];
            if (rowStart > lastRow)
                start = PADDING_LEFT;

            width = getSize(names[i], i);
            height = rowStart + rowHeight;
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
            canvas.drawText(names[i], start + PADDING_LEFT + FIN, height - PADDING_BOTTOM, paint);
            paint.clearShadowLayer();

            lastRow = rowStart;
            start += width;
        }

        paint.setColor(original);
    }
}
