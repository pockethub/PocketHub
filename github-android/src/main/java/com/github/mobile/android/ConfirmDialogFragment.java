package com.github.mobile.android;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import roboguice.fragment.RoboDialogFragment;

/**
 * Dialog to display a confirmation
 */
public class ConfirmDialogFragment extends RoboDialogFragment implements OnClickListener {

    private static final String ARG_TITLE = "title";

    private static final String ARG_MESSAGE = "message";

    private static final String ARG_REQUEST_CODE = "requestCode";

    private static final String TAG = "confirm_dialog";

    /**
     * Confirm message and deliver callback to given activity
     *
     * @param activity
     * @param requestCode
     * @param title
     * @param message
     */
    public static void confirm(final DialogFragmentActivity activity, final int requestCode, final String title,
            final String message) {
        FragmentManager manager = activity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment current = manager.findFragmentByTag(TAG);
        if (current != null)
            transaction.remove(current);
        transaction.addToBackStack(null);

        Bundle arguments = new Bundle();
        arguments.putInt(ARG_REQUEST_CODE, requestCode);
        arguments.putString(ARG_TITLE, title);
        arguments.putString(ARG_MESSAGE, message);
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        fragment.setArguments(arguments);
        fragment.show(manager, TAG);
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Builder builder = new Builder(getActivity());
        Bundle arguments = getArguments();
        builder.setTitle(arguments.getString(ARG_TITLE));
        builder.setMessage(arguments.getString(ARG_MESSAGE));
        builder.setPositiveButton(string.yes, this);
        builder.setNegativeButton(string.no, this);
        builder.setCancelable(true);
        builder.setOnCancelListener(this);
        return builder.create();
    }

    private void onResult(int resultCode) {
        ((DialogFragmentActivity) getActivity()).onDialogResult(getArguments().getInt(ARG_REQUEST_CODE), resultCode,
                getArguments());
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        switch (which) {
        case BUTTON_POSITIVE:
            onResult(RESULT_OK);
            break;
        case BUTTON_NEGATIVE:
            onResult(RESULT_CANCELED);
            break;
        default:
            break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        onResult(RESULT_CANCELED);
    }
}
