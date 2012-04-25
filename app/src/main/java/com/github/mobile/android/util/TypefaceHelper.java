package com.github.mobile.android.util;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.Arrays;

/**
 * Helpers for dealing with custom typefaces and measuring text to display
 */
public class TypefaceHelper {

    /**
     * Find the maximum number of digits in the given numbers
     *
     * @param numbers
     * @return max digits
     */
    public static int getMaxDigits(int... numbers) {
        int max = 1;
        for (int number : numbers)
            max = Math.max(max, (int) Math.log10(number) + 1);
        return max;
    }

    /**
     * Get width of number of digits
     *
     * @param view
     * @param numberOfDigits
     * @return number width
     */
    public static int getWidth(TextView view, int numberOfDigits) {
        Paint paint = new Paint();
        paint.setTypeface(view.getTypeface());
        paint.setTextSize(view.getTextSize());
        char[] text = new char[numberOfDigits];
        Arrays.fill(text, '0');
        return Math.round(paint.measureText(text, 0, text.length));
    }

    /**
     * Get octocons typeface
     *
     * @param context
     * @return octocons typeface
     */
    public static Typeface getOctocons(Context context) {
        return getTypeface(context, "octocons-regular-webfont.ttf");
    }

    /**
     * Set octocons typeface on given text view(s)
     *
     * @param textViews
     */
    public static void setOctocons(TextView... textViews) {
        if (textViews == null || textViews.length == 0)
            return;

        Typeface typeface = getOctocons(textViews[0].getContext());
        for (TextView textView : textViews)
            textView.setTypeface(typeface);
    }

    /**
     * Get typeface with name
     *
     * @param context
     * @param name
     * @return typeface
     */
    public static Typeface getTypeface(Context context, String name) {
        return Typeface.createFromAsset(context.getAssets(), name);
    }
}
