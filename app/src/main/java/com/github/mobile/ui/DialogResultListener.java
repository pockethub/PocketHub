package com.github.mobile.ui;

import android.app.Activity;
import android.os.Bundle;

/**
 * Listener that dialogs results are delivered too
 */
public interface DialogResultListener {

    /**
     * Callback for a dialog finishing and delivering a result
     *
     * @param requestCode
     * @param resultCode
     *            result such as {@link Activity#RESULT_CANCELED} or {@link Activity#RESULT_OK}
     * @param arguments
     */
    void onDialogResult(int requestCode, int resultCode, Bundle arguments);
}
