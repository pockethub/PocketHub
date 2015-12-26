/*
 * Copyright (c) 2015 PocketHub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pockethub.ui;

import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Helper to display a single choice dialog
 */
public class SingleChoiceDialogFragment extends DialogFragmentHelper implements
        OnClickListener {

    /**
     * Arguments key for the selected item
     */
    public static final String ARG_SELECTED = "selected";

    /**
     * Choices arguments
     */
    protected static final String ARG_CHOICES = "choices";

    /**
     * Selected choice argument
     */
    protected static final String ARG_SELECTED_CHOICE = "selectedChoice";

    /**
     * Tag
     */
    protected static final String TAG = "single_choice_dialog";

    /**
     * Confirm message and deliver callback to given activity
     *
     * @param activity
     * @param requestCode
     * @param title
     * @param message
     * @param choices
     * @param selectedChoice
     * @param helper
     */
    protected static void show(final DialogFragmentActivity activity,
            final int requestCode, final String title, final String message,
            ArrayList<? extends Parcelable> choices, final int selectedChoice,
            final DialogFragmentHelper helper) {
        Bundle arguments = createArguments(title, message, requestCode);
        arguments.putParcelableArrayList(ARG_CHOICES, choices);
        arguments.putInt(ARG_SELECTED_CHOICE, selectedChoice);
        show(activity, helper, arguments, TAG);
    }
}
