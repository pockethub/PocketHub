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

package com.github.pockethub.ui;

import android.content.Context;
import android.support.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;

public class BaseProgressDialog {

    private final Context context;
    /**
     * Progress dialog last displayed
     */
    protected MaterialDialog progress;


    public BaseProgressDialog(Context context) {
        this.context = context;
    }

    /**
     * Dismiss and clear progress dialog field
     */
    protected void dismissProgress() {
        if (progress != null) {
            progress.dismiss();
            progress = null;
        }
    }

    /**
     * Show indeterminate progress dialog with given message
     *
     * @param message
     */
    protected void showIndeterminate(final CharSequence message) {
        dismissProgress();

        progress = new MaterialDialog.Builder(context)
                .content(message)
                .progress(true, 0)
                .build();
        progress.show();
    }

    /**
     * Show indeterminate progress dialog with given message
     *
     * @param resId
     */
    protected void showIndeterminate(@StringRes final int resId) {
        dismissProgress();

        progress = new MaterialDialog.Builder(context)
                .content(resId)
                .progress(true, 0)
                .build();
        progress.show();
    }
}
