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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import roboguice.fragment.RoboDialogFragment;

/**
 * Base dialog fragment helper
 */
public abstract class DialogFragmentHelper extends RoboDialogFragment implements
        OnClickListener {

    /**
     * Dialog message
     */
    private static final String ARG_TITLE = "title";

    /**
     * Dialog message
     */
    private static final String ARG_MESSAGE = "message";

    /**
     * Request code
     */
    private static final String ARG_REQUEST_CODE = "requestCode";

    /**
     * Show dialog
     *
     * @param activity
     * @param fragment
     * @param arguments
     * @param tag
     */
    protected static void show(DialogFragmentActivity activity,
            DialogFragmentHelper fragment, Bundle arguments, String tag) {
        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment current = manager.findFragmentByTag(tag);
        if (current != null)
            transaction.remove(current);
        transaction.addToBackStack(null);

        fragment.setArguments(arguments);
        fragment.show(manager, tag);
    }

    /**
     * Create bundle with standard arguments
     *
     * @param title
     * @param message
     * @param requestCode
     * @return bundle
     */
    protected static Bundle createArguments(final String title,
            final String message, final int requestCode) {
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_REQUEST_CODE, requestCode);
        arguments.putString(ARG_TITLE, title);
        arguments.putString(ARG_MESSAGE, message);
        return arguments;
    }

    /**
     * Call back to the activity with the dialog result
     *
     * @param resultCode
     */
    protected void onResult(final int resultCode) {
        final DialogFragmentActivity activity = (DialogFragmentActivity) getActivity();
        if (activity != null) {
            final Bundle arguments = getArguments();
            if (arguments != null)
                activity.onDialogResult(arguments.getInt(ARG_REQUEST_CODE),
                        resultCode, arguments);
        }
    }

    /**
     * Get title
     *
     * @return title
     */
    protected String getTitle() {
        return getArguments().getString(ARG_TITLE);
    }

    /**
     * Get message
     *
     * @return mesage
     */
    protected String getMessage() {
        return getArguments().getString(ARG_MESSAGE);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        onResult(RESULT_CANCELED);
    }

    /**
     * Create default dialog
     *
     * @return dialog
     */
    protected AlertDialog createDialog() {
        final AlertDialog dialog = LightAlertDialog.create(getActivity());
        dialog.setTitle(getTitle());
        dialog.setMessage(getMessage());
        dialog.setCancelable(true);
        dialog.setOnCancelListener(this);
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
    }
}
