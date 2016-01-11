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

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import com.github.kevinsawicki.wishlist.Toaster;

import org.eclipse.egit.github.core.client.RequestException;

/**
 * Utilities for displaying toast notifications
 */
public class ToastUtils {

    /**
     * Show the given message in a {@link Toast}
     * <p>
     * This method may be called from any thread
     *
     * @param activity
     * @param message
     */
    public static void show(final Activity activity, final String message) {
        Toaster.showLong(activity, message);
    }

    /**
     * Show the message with the given resource id in a {@link Toast}
     * <p>
     * This method may be called from any thread
     *
     * @param activity
     * @param resId
     */
    public static void show(final Activity activity, final int resId) {
        if (activity == null)
            return;

        show(activity, activity.getString(resId));
    }

    /**
     * Show {@link Toast} for throwable
     * <p>
     * This given default message will be used if an message can not be derived
     * from the given {@link Exception}
     * <p>
     * This method may be called from any thread
     *
     * @param activity
     * @param e
     * @param defaultMessage
     */
    public static void show(final Activity activity, final Throwable e,
            final int defaultMessage) {
        if (activity == null)
            return;

        String message;
        if (e instanceof RequestException)
            message = ((RequestException) e).formatErrors();
        else
            message = null;

        if (TextUtils.isEmpty(message))
            message = activity.getString(defaultMessage);

        show(activity, message);
    }
}
