package com.github.mobile.android;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import android.R.string;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.Collection;
import java.util.HashSet;

import roboguice.fragment.RoboDialogFragment;

/**
 * Dialog to display a confirmation
 */
public class ConfirmDialogFragment extends RoboDialogFragment implements OnClickListener, OnMultiChoiceClickListener {

    /**
     * Arguments key for the selected items when showing a multi choice dialog
     */
    public static final String ARG_SELECTED = "selected";

    private static final String ARG_TITLE = "title";

    private static final String ARG_MESSAGE = "message";

    private static final String ARG_REQUEST_CODE = "requestCode";

    private static final String ARG_CHOICES = "choices";

    private static final String ARG_SELECTED_CHOICES = "selectedChoices";

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
        confirm(activity, requestCode, title, message, null, null);
    }

    /**
     * Confirm message and deliver callback to given activity
     *
     * @param activity
     * @param requestCode
     * @param title
     * @param message
     * @param choices
     * @param selectedChoices
     */
    public static void confirm(final DialogFragmentActivity activity, final int requestCode, final String title,
            final String message, final String[] choices, final boolean[] selectedChoices) {
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
        arguments.putStringArray(ARG_CHOICES, choices);
        arguments.putBooleanArray(ARG_SELECTED_CHOICES, selectedChoices);
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        fragment.setArguments(arguments);
        fragment.show(manager, TAG);
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Builder builder = new Builder(getActivity());
        Bundle arguments = getArguments();
        builder.setTitle(arguments.getString(ARG_TITLE));
        builder.setMessage(arguments.getString(ARG_MESSAGE));
        String[] choices = arguments.getStringArray(ARG_CHOICES);
        boolean[] selectedChoices = arguments.getBooleanArray(ARG_SELECTED_CHOICES);
        if (choices != null) {
            builder.setMultiChoiceItems(choices, selectedChoices, this);
            builder.setPositiveButton("Apply", this);
            builder.setNeutralButton("Clear", this);

            HashSet<String> selected = new HashSet<String>();
            if (selectedChoices != null)
                for (int i = 0; i < choices.length; i++)
                    if (selectedChoices[i])
                        selected.add(choices[i]);
            arguments.putSerializable(ARG_SELECTED, selected);
        } else {
            builder.setPositiveButton(string.yes, this);
            builder.setNegativeButton(string.no, this);
        }
        builder.setCancelable(true);
        builder.setOnCancelListener(this);
        return builder.create();
    }

    @SuppressWarnings("unchecked")
    private Collection<String> getWorkingSelection() {
        return (Collection<String>) getArguments().getSerializable(ARG_SELECTED);
    }

    private void onResult(int resultCode) {
        Bundle arguments = getArguments();

        Collection<String> selected = getWorkingSelection();
        if (selected != null)
            arguments.putStringArray(ARG_SELECTED, selected.toArray(new String[selected.size()]));

        ((DialogFragmentActivity) getActivity()).onDialogResult(arguments.getInt(ARG_REQUEST_CODE), resultCode,
                arguments);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        switch (which) {
        case BUTTON_NEUTRAL:
            Collection<String> selected = getWorkingSelection();
            if (selected != null)
                selected.clear();
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

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        Collection<String> selected = getWorkingSelection();
        if (isChecked)
            selected.add(getArguments().getStringArray(ARG_CHOICES)[which]);
        else
            selected.remove(getArguments().getStringArray(ARG_CHOICES)[which]);
    }
}
