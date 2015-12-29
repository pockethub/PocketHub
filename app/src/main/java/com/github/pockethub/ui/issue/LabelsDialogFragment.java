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
package com.github.pockethub.ui.issue;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alorma.github.sdk.bean.dto.response.Label;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.R;
import com.github.pockethub.ui.DialogFragmentActivity;
import com.github.pockethub.ui.DialogFragmentHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static android.content.DialogInterface.BUTTON_POSITIVE;

/**
 * Dialog fragment to present labels where one or more can be selected
 */
public class LabelsDialogFragment extends DialogFragmentHelper implements
        OnClickListener {

    /**
     * Arguments key for the selected items
     */
    public static final String ARG_SELECTED = "selected";

    private static final String ARG_CHOICES = "choices";

    private static final String ARG_SELECTED_CHOICES = "selectedChoices";

    private static final String TAG = "multi_choice_dialog";

    private static class LabelListAdapter extends SingleTypeAdapter<Label>
            implements OnItemClickListener {

        private final boolean[] selected;

        public LabelListAdapter(LayoutInflater inflater, Label[] labels,
                boolean[] selected) {
            super(inflater, R.layout.label_item);

            this.selected = selected;
            setItems(labels);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            selected[position] = !selected[position];
            notifyDataSetChanged();
        }

        @Override
        protected int[] getChildViewIds() {
            return new int[] { R.id.tv_label_name, R.id.cb_selected };
        }

        @Override
        protected void update(int position, Label item) {
            LabelDrawableSpan.setText(textView(0), item);
            setChecked(1, selected[position]);
        }
    }

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
    public static void show(final DialogFragmentActivity activity,
            final int requestCode, final String title, final String message,
            final ArrayList<Label> choices, final boolean[] selectedChoices) {
        Bundle arguments = createArguments(title, message, requestCode);
        arguments.putParcelableArrayList(ARG_CHOICES, choices);
        arguments.putBooleanArray(ARG_SELECTED_CHOICES, selectedChoices);
        show(activity, new LabelsDialogFragment(), arguments, TAG);
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        Activity activity = getActivity();

        ArrayList<Label> choices = getChoices();
        boolean[] selectedChoices = arguments
                .getBooleanArray(ARG_SELECTED_CHOICES);
        List<String> selected = new ArrayList<>();
        if (selectedChoices != null)
            for (int i = 0; i < choices.size(); i++)
                if (selectedChoices[i])
                    selected.add(choices.get(i).name);
        arguments.putStringArrayList(ARG_SELECTED, (ArrayList<String>) selected);

        LayoutInflater inflater = activity.getLayoutInflater();
        ListView view = (ListView) inflater.inflate(R.layout.dialog_list_view,
                null);
        LabelListAdapter adapter = new LabelListAdapter(inflater,
                choices.toArray(new Label[choices.size()]), selectedChoices);
        view.setAdapter(adapter);
        view.setOnItemClickListener(adapter);

        return new MaterialDialog.Builder(activity)
                .cancelable(true)
                .cancelListener(this)
                .negativeText(R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        LabelsDialogFragment.this.onClick(dialog, BUTTON_NEGATIVE);
                    }
                })
                .neutralText(R.string.clear)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        LabelsDialogFragment.this.onClick(dialog, BUTTON_NEUTRAL);
                    }
                })
                .positiveText(R.string.apply)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        LabelsDialogFragment.this.onClick(dialog, BUTTON_POSITIVE);
                    }
                })
                .title(getTitle())
                .content(getMessage())
                .customView(view, false)
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
        boolean[] selectedChoices = arguments
                .getBooleanArray(ARG_SELECTED_CHOICES);
        ArrayList<Label> choices = getChoices();
        if (selectedChoices != null) {
            for (int i = 0; i < selectedChoices.length; i++)
                if (selectedChoices[i])
                    selected.add(choices.get(i));
        }
        arguments.putParcelableArrayList(ARG_SELECTED, selected);

        super.onResult(resultCode);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        switch (which) {
        case BUTTON_NEUTRAL:
            Arrays.fill(getArguments().getBooleanArray(ARG_SELECTED_CHOICES), false);
        case BUTTON_POSITIVE:
            onResult(RESULT_OK);
        }
    }
}
