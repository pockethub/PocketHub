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
package com.github.mobile.util;

import static android.content.Context.WINDOW_SERVICE;
import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

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
        return ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
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
    @SuppressWarnings("deprecation")
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
}
