package com.github.mobile.android;

import static android.app.Activity.RESULT_OK;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;

import java.util.Collection;
import java.util.HashSet;

/**
 * Helper to display multiple choice dialogs
 */
public class MultiChoiceDialogFragment extends DialogFragmentHelper implements OnClickListener,
        OnMultiChoiceClickListener {

    /**
     * Arguments key for the selected items
     */
    public static final String ARG_SELECTED = "selected";

    private static final String ARG_SINGLE_CHOICE = "singleChoice";

    private static final String ARG_CHOICES = "choices";

    private static final String ARG_SELECTED_CHOICES = "selectedChoices";

    private static final String TAG = "multi_choice_dialog";

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
    public static void show(final DialogFragmentActivity activity, final int requestCode, final String title,
            final String message, final String[] choices, final boolean[] selectedChoices) {
        Bundle arguments = createArguments(title, message, requestCode);
        arguments.putStringArray(ARG_CHOICES, choices);
        arguments.putBooleanArray(ARG_SELECTED_CHOICES, selectedChoices);
        arguments.putBoolean(ARG_SINGLE_CHOICE, false);
        show(activity, new MultiChoiceDialogFragment(), arguments, TAG);
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Builder builder = new Builder(getActivity());
        Bundle arguments = getArguments();
        builder.setTitle(getTitle());
        builder.setMessage(getMessage());
        builder.setNeutralButton("Clear", this);
        builder.setPositiveButton("Apply", this);
        builder.setCancelable(true);
        builder.setOnCancelListener(this);

        String[] choices = arguments.getStringArray(ARG_CHOICES);
        boolean[] selectedChoices = arguments.getBooleanArray(ARG_SELECTED_CHOICES);
        builder.setMultiChoiceItems(choices, selectedChoices, this);

        HashSet<String> selected = new HashSet<String>();
        if (selectedChoices != null)
            for (int i = 0; i < choices.length; i++)
                if (selectedChoices[i])
                    selected.add(choices[i]);
        arguments.putSerializable(ARG_SELECTED, selected);

        return builder.create();
    }

    @SuppressWarnings("unchecked")
    private Collection<String> getWorkingSelection() {
        return (Collection<String>) getArguments().getSerializable(ARG_SELECTED);
    }

    @Override
    protected void onResult(int resultCode) {
        Collection<String> selected = getWorkingSelection();
        if (selected != null)
            getArguments().putStringArray(ARG_SELECTED, selected.toArray(new String[selected.size()]));
        super.onResult(resultCode);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        switch (which) {
        case BUTTON_NEUTRAL:
            getWorkingSelection().clear();
        case BUTTON_POSITIVE:
            onResult(RESULT_OK);
            break;
        default:
            break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (isChecked)
            getWorkingSelection().add(getArguments().getStringArray(ARG_CHOICES)[which]);
        else
            getWorkingSelection().remove(getArguments().getStringArray(ARG_CHOICES)[which]);
    }
}
