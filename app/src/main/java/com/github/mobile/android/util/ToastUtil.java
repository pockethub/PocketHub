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

package com.github.mobile.android.util;

import static android.widget.Toast.LENGTH_LONG;
import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.widget.Toast;

/**
 * Utilities for displaying toast notifications
 */
public class ToastUtil {

    private static final String TAG = "ToastUtil";

    /**
     * Shows a toast to the user - can be called from any thread, toast will be displayed using the UI-thread.
     * <p>
     * The important thing about the delayed aspect of the UI-thread code used by this method is that it may actually
     * run <em>after</em> the associated activity has been destroyed - so it can not keep a reference to the activity.
     * Calling methods on a destroyed activity may throw exceptions, and keeping a reference to it is technically a
     * short-term memory-leak: http://developer.android.com/resources/articles/avoiding-memory-leaks.html
     *
     * @param activity
     * @param message
     */
    public static void toastOnUiThread(Activity activity, final String message) {
        Log.d(TAG, "Will display toast : " + message);
        final Application application = activity.getApplication();
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(application, message, LENGTH_LONG).show();
            }
        });
    }

    /**
     * Shows a toast to the user - can be called from any thread, toast will be displayed using the UI-thread.
     * <p>
     * The important thing about the delayed aspect of the UI-thread code used by this method is that it may actually
     * run <em>after</em> the associated activity has been destroyed - so it can not keep a reference to the activity.
     * Calling methods on a destroyed activity may throw exceptions, and keeping a reference to it is technically a
     * short-term memory-leak: http://developer.android.com/resources/articles/avoiding-memory-leaks.html
     *
     * @param activity
     * @param resId
     */
    public static void toastOnUiThread(Activity activity, int resId) {
        toastOnUiThread(activity, activity.getString(resId));
    }
}
