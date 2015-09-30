package com.github.pockethub.ui;

import android.app.AlertDialog;
import android.content.Context;

public class BaseProgressDialog {

    private final Context context;
    /**
     * Progress dialog last displayed
     */
    protected AlertDialog progress;


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

        progress = LightProgressDialog.create(context, message);
        progress.show();
    }

    /**
     * Show indeterminate progress dialog with given message
     *
     * @param resId
     */
    protected void showIndeterminate(final int resId) {
        dismissProgress();

        progress = LightProgressDialog.create(context, resId);
        progress.show();
    }
}
