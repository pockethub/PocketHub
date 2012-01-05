package com.github.mobile.android.issue;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.PaintDrawable;

import java.util.Collection;

import org.eclipse.egit.github.core.Label;

/**
 * Custom drawable for labels applied to an issue
 */
public class LabelsDrawable extends PaintDrawable {

    private static final int PADDING_LEFT = 10;

    private static final int PADDING_RIGHT = 10;

    private static final int PADDING_TOP = 10;

    private static final int PADDING_BOTTOM = 10;

    private static final int FIN = 8;

    private final float textSize;

    private final Label[] labels;

    /**
     * Create drawable for labels
     *
     * @param textSize
     * @param labels
     */
    public LabelsDrawable(final float textSize, final Collection<Label> labels) {
        this.textSize = textSize;
        this.labels = labels.toArray(new Label[labels.size()]);
        Rect bounds = new Rect();
        bounds.right = PADDING_LEFT + PADDING_RIGHT;
        for (int i = 0; i < this.labels.length; i++)
            getSize(this.labels[i], i, bounds);
        bounds.bottom += PADDING_BOTTOM;
        setBounds(bounds);
    }

    private void getSize(Label label, int index, Rect out) {
        getSize(label.getName().toUpperCase(), index, out);
    }

    private void getSize(String name, int index, Rect out) {
        Paint p = getPaint();
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextSize(textSize);
        Rect tBounds = new Rect();
        p.getTextBounds(name, 0, name.length(), tBounds);
        float width = tBounds.width() + PADDING_LEFT + PADDING_RIGHT;
        if (index != 0)
            width += FIN * 2;
        else
            width += FIN;
        float height = tBounds.height() + PADDING_TOP + PADDING_BOTTOM;
        out.right += (int) Math.round(Math.ceil(width));
        out.bottom = Math.max(out.bottom, (int) Math.round(Math.ceil(height)));
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint paint = getPaint();
        int original = paint.getColor();
        int start = PADDING_LEFT;
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(textSize);
        Rect tBounds = new Rect();

        Label label = this.labels[0];
        String name = label.getName().toUpperCase();
        tBounds.setEmpty();
        getSize(name, 0, tBounds);
        float width = tBounds.width();
        float height = tBounds.height();
        float quarter = height / 4;
        Path path = new Path();
        path.moveTo(start, 0);
        path.lineTo(start + width, 0);
        path.lineTo(start + width - FIN, quarter);
        path.lineTo(start + width, quarter * 2);
        path.lineTo(start + width - FIN, quarter * 3);
        path.lineTo(start + width, height);
        path.lineTo(start, height);
        path.lineTo(start, 0);
        path.close();
        paint.setColor(Color.parseColor("#" + label.getColor()));
        canvas.drawPath(path, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText(name, start + PADDING_LEFT, height - PADDING_BOTTOM, paint);
        start += (int) Math.round(Math.ceil(width));

        for (int i = 1; i < labels.length; i++) {
            label = labels[i];
            name = label.getName().toUpperCase();
            tBounds.setEmpty();
            getSize(name, i, tBounds);
            width = tBounds.width();
            height = tBounds.height();
            quarter = height / 4;
            path = new Path();
            path.moveTo(start + FIN, 0);
            path.lineTo(start + width, 0);
            path.lineTo(start + width - FIN, quarter);
            path.lineTo(start + width, quarter * 2);
            path.lineTo(start + width - FIN, quarter * 3);
            path.lineTo(start + width, height);
            path.lineTo(start + FIN, height);
            path.lineTo(start, quarter * 3);
            path.lineTo(start + FIN, quarter * 2);
            path.lineTo(start, quarter);
            path.lineTo(start + FIN, 0);
            path.close();
            paint.setColor(Color.parseColor("#" + label.getColor()));
            canvas.drawPath(path, paint);
            paint.setColor(Color.WHITE);
            canvas.drawText(name, start + PADDING_LEFT + FIN, height - PADDING_BOTTOM, paint);
            start += (int) Math.round(Math.ceil(width));
        }

        paint.setColor(original);
    }
}
