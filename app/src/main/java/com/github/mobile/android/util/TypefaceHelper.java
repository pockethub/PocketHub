package com.github.mobile.android.util;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Helper for dealing with custom typefaces
 */
public class TypefaceHelper {

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
     * Set octocons typeface on given text view
     *
     * @param textView
     * @return text view
     */
    public static TextView setOctocons(TextView textView) {
        textView.setTypeface(getOctocons(textView.getContext()));
        return textView;
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
