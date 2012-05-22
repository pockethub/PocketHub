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

import static android.os.Build.VERSION.SDK_INT;
import android.app.ProgressDialog;
import android.content.Context;

import com.github.mobile.R.drawable;

/**
 * Progress dialog in Holo Light theme
 */
public class LightProgressDialog extends ProgressDialog {

    /**
     * Create progress dialog
     *
     * @param context
     * @param resId
     * @return dialog
     */
    public static ProgressDialog create(Context context, int resId) {
        return create(context, context.getResources().getString(resId));
    }

    /**
     * Create progress dialog
     *
     * @param context
     * @param message
     * @return dialog
     */
    public static ProgressDialog create(Context context, CharSequence message) {
        ProgressDialog dialog;
        if (SDK_INT >= 14)
            dialog = new LightProgressDialog(context, message);
        else
            dialog = new ProgressDialog(context);

        dialog.setMessage(message);
        dialog.setIndeterminate(true);
        dialog.setIndeterminateDrawable(context.getResources().getDrawable(drawable.spinner));

        return dialog;
    }

    private LightProgressDialog(Context context, CharSequence message) {
        super(context, THEME_HOLO_LIGHT);

    }
}
