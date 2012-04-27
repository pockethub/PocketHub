package com.github.mobile;

import android.os.Bundle;

import com.github.mobile.ui.DialogResultListener;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

/**
 * Activity that display dialogs
 */
public abstract class DialogFragmentActivity extends RoboSherlockFragmentActivity implements DialogResultListener {

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {
        // Intentionally left blank
    }
}
