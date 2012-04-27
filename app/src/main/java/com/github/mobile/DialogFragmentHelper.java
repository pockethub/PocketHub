package com.github.mobile;

import static android.app.Activity.RESULT_CANCELED;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import roboguice.fragment.RoboDialogFragment;

/**
 * Base dialog fragment helper
 */
public abstract class DialogFragmentHelper extends RoboDialogFragment {

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
    protected static void show(DialogFragmentActivity activity, DialogFragmentHelper fragment, Bundle arguments,
            String tag) {
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
    protected static Bundle createArguments(final String title, final String message, final int requestCode) {
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
    protected void onResult(int resultCode) {
        ((DialogFragmentActivity) getActivity()).onDialogResult(getArguments().getInt(ARG_REQUEST_CODE), resultCode,
                getArguments());
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
}
