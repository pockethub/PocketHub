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
package com.github.pockethub.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import static android.content.Context.WINDOW_SERVICE;
import static android.util.TypedValue.COMPLEX_UNIT_DIP;

/**
 * Helpers for dealing with system services
 */
public class ServiceUtils {

    /**
     * Get default display
     *
     * @param context
     * @return display
     */
    public static Display getDisplay(final Context context) {
        return ((WindowManager) context.getSystemService(WINDOW_SERVICE))
                .getDefaultDisplay();
    }

    /**
     * Get default display
     *
     * @param view
     * @return display
     */
    public static Display getDisplay(final View view) {
        return getDisplay(view.getContext());
    }

    /**
     * Get default display width
     *
     * @param context
     * @return display
     */
    public static int getDisplayWidth(final Context context) {
        return getDisplay(context).getWidth();
    }

    /**
     * Get default display width
     *
     * @param view
     * @return display
     */
    public static int getDisplayWidth(final View view) {
        return getDisplayWidth(view.getContext());
    }

    /**
     * Get pixels from dps
     *
     * @param view
     * @param dp
     * @return pixels
     */
    public static float getPixels(final View view, final int dp) {
        return getPixels(view.getResources(), dp);
    }

    /**
     * Get pixels from dps
     *
     * @param resources
     * @param dp
     * @return pixels
     */
    public static float getPixels(final Resources resources, final int dp) {
        return TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp,
                resources.getDisplayMetrics());
    }

    /**
     * Get pixels from dps
     *
     * @param view
     * @param dp
     * @return pixels
     */
    public static int getIntPixels(final View view, final int dp) {
        return getIntPixels(view.getResources(), dp);
    }

    /**
     * Get pixels from dps
     *
     * @param context
     * @param dp
     * @return pixels
     */
    public static int getIntPixels(final Context context, final int dp) {
        return getIntPixels(context.getResources(), dp);
    }

    /**
     * Get pixels from dps
     *
     * @param resources
     * @param dp
     * @return pixels
     */
    public static int getIntPixels(final Resources resources, final int dp) {
        float pixels = TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp,
                resources.getDisplayMetrics());
        return (int) Math.floor(pixels + 0.5F);
    }
}
