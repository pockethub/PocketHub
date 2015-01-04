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

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;

/**
 * Metrics utilities
 */
public class MetricsUtils {

    private static DisplayMetrics dm;

    /**
     * init default phone's DisplayMetrics values
     * DisplayMetrics.widthPixels <br/>
     * DisplayMetrics.heightPixels <br/>
     * DisplayMetrics.densityDpi <br/>
     *
     * @param activity
     */
    public static DisplayMetrics getDisplayMetrics(Activity activity) {
        if (dm == null && activity != null) {
            dm = new DisplayMetrics();
            Display display = activity.getWindowManager().getDefaultDisplay();
            display.getMetrics(dm);
        }
        return dm;
    }

}
