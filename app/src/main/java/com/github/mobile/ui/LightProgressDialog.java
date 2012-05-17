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
package com.github.mobile.ui;

import android.app.ProgressDialog;
import android.content.Context;

import com.github.mobile.R.drawable;

/**
 * Progress dialog in Holo Light theme
 */
public class LightProgressDialog extends ProgressDialog {

    /**
     * Create progress dialog with given message
     *
     * @param context
     * @param resId
     */
    public LightProgressDialog(Context context, int resId) {
        this(context, context.getResources().getString(resId));
    }

    /**
     * Create progress dialog with given message
     *
     * @param context
     * @param message
     */
    public LightProgressDialog(Context context, CharSequence message) {
        super(context, THEME_HOLO_LIGHT);

        setMessage(message);
        setIndeterminate(true);
        setIndeterminateDrawable(context.getResources().getDrawable(drawable.spinner));
    }
}
