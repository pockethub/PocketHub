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

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import android.R.string;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

/**
 * Helper to display a confirmation dialog
 */
public class ConfirmDialogFragment extends DialogFragmentHelper implements OnClickListener {

    private static final String TAG = "confirm_dialog";

    /**
     * Confirm message and deliver callback to given activity
     *
     * @param activity
     * @param requestCode
     * @param title
     * @param message
     */
    public static void show(final DialogFragmentActivity activity, final int requestCode, final String title,
            final String message) {
        show(activity, requestCode, title, message, null);
    }

    /**
     * Confirm message and deliver callback to given activity
     *
     * @param activity
     * @param requestCode
     * @param title
     * @param message
     * @param bundle
     */
    public static void show(final DialogFragmentActivity activity, final int requestCode, final String title,
            final String message, final Bundle bundle) {
        Bundle arguments = createArguments(title, message, requestCode);
        if (bundle != null)
            arguments.putAll(bundle);
        show(activity, new ConfirmDialogFragment(), arguments, TAG);
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Builder builder = new Builder(getActivity());
        builder.setTitle(getTitle());
        builder.setMessage(getMessage());
        builder.setPositiveButton(string.yes, this);
        builder.setNegativeButton(string.no, this);
        builder.setCancelable(true);
        builder.setOnCancelListener(this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        switch (which) {
        case BUTTON_POSITIVE:
            onResult(RESULT_OK);
            break;
        case BUTTON_NEGATIVE:
            onResult(RESULT_CANCELED);
            break;
        }
    }
}
