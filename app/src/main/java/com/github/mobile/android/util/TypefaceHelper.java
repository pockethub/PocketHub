package com.github.mobile.android.util;

import android.content.Context;
import android.graphics.Typeface;

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
