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

import static android.app.Activity.RESULT_OK;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

/**
 * Helper to display a single choice dialog
 */
public class SingleChoiceDialogFragment extends DialogFragmentHelper implements OnClickListener {

    /**
     * Arguments key for the selected item
     */
    public static final String ARG_SELECTED = "selected";

    private static final String ARG_CHOICES = "choices";

    private static final String ARG_SELECTED_CHOICE = "selectedChoice";

    private static final String TAG = "single_choice_dialog";

    /**
     * Confirm message and deliver callback to given activity
     *
     * @param activity
     * @param requestCode
     * @param title
     * @param message
     * @param choices
     * @param selectedChoice
     */
    public static void show(final DialogFragmentActivity activity, final int requestCode, final String title,
            final String message, final String[] choices, final int selectedChoice) {
        Bundle arguments = createArguments(title, message, requestCode);
        arguments.putStringArray(ARG_CHOICES, choices);
        arguments.putInt(ARG_SELECTED_CHOICE, selectedChoice);
        show(activity, new SingleChoiceDialogFragment(), arguments, TAG);
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Builder builder = new Builder(getActivity());
        Bundle arguments = getArguments();
        builder.setTitle(getTitle());
        builder.setMessage(getMessage());
        builder.setCancelable(true);
        builder.setOnCancelListener(this);
        int selected = arguments.getInt(ARG_SELECTED_CHOICE);
        if (selected > -1)
            builder.setNeutralButton("Clear", this);
        builder.setSingleChoiceItems(arguments.getStringArray(ARG_CHOICES), selected, this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        switch (which) {
        case BUTTON_NEUTRAL:
            onResult(RESULT_OK);
            break;
        default:
            getArguments().putString(ARG_SELECTED, getArguments().getStringArray(ARG_CHOICES)[which]);
            onResult(RESULT_OK);
            break;
        }
    }
}
