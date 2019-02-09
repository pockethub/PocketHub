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
package com.github.pockethub.android.ui.issue;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.DialogFragmentHelper;
import com.github.pockethub.android.ui.item.dialog.LabelDialogItem;
import com.meisolsson.githubsdk.model.Label;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Dialog fragment to present labels where one or more can be selected
 */
public class LabelsDialogFragment extends DialogFragmentHelper implements OnItemClickListener {

    /**
     * Arguments key for the selected items
     */
    public static final String ARG_SELECTED = "selected";

    private static final String ARG_CHOICES = "choices";

    private static final String ARG_SELECTED_CHOICES = "selectedChoices";

    private static final String TAG = "multi_choice_dialog";

    boolean[] selectedChoices;
    private GroupAdapter adapter;

    /**
     * Get selected labels from result bundle
     *
     * @param arguments
     * @return selected labels
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Label> getSelected(Bundle arguments) {
        return arguments.getParcelableArrayList(ARG_SELECTED);
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
    public static void show(final BaseActivity activity, final int requestCode,
                            final String title, final String message,
                            final ArrayList<Label> choices, final boolean[] selectedChoices) {
        Bundle arguments = createArguments(title, message, requestCode);
        arguments.putParcelableArrayList(ARG_CHOICES, choices);
        arguments.putBooleanArray(ARG_SELECTED_CHOICES, selectedChoices);
        show(activity, new LabelsDialogFragment(), arguments, TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        selectedChoices = getArguments().getBooleanArray(ARG_SELECTED_CHOICES);

        ArrayList<Label> choices = getChoices();
        List<String> selected = new ArrayList<>();
        if (selectedChoices != null) {
            for (int i = 0; i < choices.size(); i++) {
                if (selectedChoices[i]) {
                    selected.add(choices.get(i).name());
                }
            }
        }

        adapter = new GroupAdapter();
        for (Label label : getChoices()) {
            adapter.add(new LabelDialogItem(label, selected.contains(label.name())));
        }
        adapter.setOnItemClickListener(this);

        return createDialogBuilder()
                .adapter(adapter, null)
                .negativeText(R.string.cancel)
                .neutralText(R.string.clear)
                .positiveText(R.string.apply)
                .onNeutral((dialog, which) -> {
                    Arrays.fill(getArguments().getBooleanArray(ARG_SELECTED_CHOICES), false);
                    onResult(RESULT_OK);
                })
                .onPositive((dialog, which) -> onResult(RESULT_OK))
                .build();
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Label> getChoices() {
        return getArguments().getParcelableArrayList(ARG_CHOICES);
    }

    @Override
    protected void onResult(int resultCode) {
        Bundle arguments = getArguments();
        ArrayList<Label> selected = new ArrayList<>();
        ArrayList<Label> choices = getChoices();

        if (selectedChoices != null) {
            for (int i = 0; i < selectedChoices.length; i++) {
                if (selectedChoices[i]) {
                    selected.add(choices.get(i));
                }
            }
        }
        arguments.putParcelableArrayList(ARG_SELECTED, selected);

        super.onResult(resultCode);
        dismiss();
    }

    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        if (item instanceof LabelDialogItem) {
            LabelDialogItem labelDialogItem = (LabelDialogItem) item;

            labelDialogItem.toggleSelected();
            selectedChoices[adapter.getAdapterPosition(item)] = labelDialogItem.isSelected();
            item.notifyChanged();
        }
    }
}
