package com.github.mobile.android;

import android.app.Activity;
import android.os.Bundle;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

/**
 * Activity that display dialogs
 */
public abstract class DialogFragmentActivity extends RoboSherlockFragmentActivity {

    /**
     * Callback for a dialog finishing and delivering a result
     *
     * @param requestCode
     * @param resultCode
     *            result such as {@link Activity#RESULT_CANCELED} or {@link Activity#RESULT_OK}
     * @param arguments
     */
    public void onDialogResult(int requestCode, int resultCode, Bundle arguments) {

    }
}
