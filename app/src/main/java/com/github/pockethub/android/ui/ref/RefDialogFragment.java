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
package com.github.pockethub.android.ui.ref;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.pockethub.android.R;
import com.github.pockethub.android.core.ref.RefUtils;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.SingleChoiceDialogFragment;
import com.meisolsson.githubsdk.model.git.GitReference;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static android.content.DialogInterface.BUTTON_NEGATIVE;

/**
 * Dialog fragment to select a branch or tag
 */
public class RefDialogFragment extends SingleChoiceDialogFragment {

    private static class RefListAdapter extends SingleTypeAdapter<GitReference> {

        private final int selected;

        public RefListAdapter(LayoutInflater inflater, GitReference[] refs,
                int selected) {
            super(inflater, R.layout.ref_item);

            this.selected = selected;
            setItems(refs);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).ref().hashCode();
        }

        @Override
        protected int[] getChildViewIds() {
            return new int[] { R.id.tv_ref_icon, R.id.tv_ref, R.id.rb_selected };
        }

        @Override
        protected void update(int position, GitReference item) {
            if (RefUtils.isTag(item)) {
                setText(0, R.string.icon_tag);
            } else {
                setText(0, R.string.icon_fork);
            }
            setText(1, RefUtils.getName(item));
            setChecked(2, selected == position);
        }
    }

    /**
     * Get selected reference from results bundle
     *
     * @param arguments
     * @return user
     */
    public static GitReference getSelected(Bundle arguments) {
        return (GitReference) arguments.getParcelable(ARG_SELECTED);
    }

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
    public static void show(final BaseActivity activity,
            final int requestCode, final String title, final String message,
            ArrayList<GitReference> choices, final int selectedChoice) {
        show(activity, requestCode, title, message, choices, selectedChoice,
                new RefDialogFragment());
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Activity activity = getActivity();
        Bundle arguments = getArguments();

        final MaterialDialog.Builder dialogBuilder = createDialogBuilder()
                .negativeText(R.string.cancel)
                .onNegative((dialog, which) -> onClick(dialog, BUTTON_NEGATIVE));

        LayoutInflater inflater = activity.getLayoutInflater();

        ListView view = (ListView) inflater.inflate(R.layout.dialog_list_view,
                null);
        view.setOnItemClickListener((parent, view1, position, id) ->
                onClick(getDialog(), position));

        ArrayList<GitReference> choices = getChoices();
        int selected = arguments.getInt(ARG_SELECTED_CHOICE);
        RefListAdapter adapter = new RefListAdapter(inflater,
                choices.toArray(new GitReference[choices.size()]), selected);
        view.setAdapter(adapter);
        if (selected >= 0) {
            view.setSelection(selected);
        }
        dialogBuilder.customView(view, false);

        return dialogBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private ArrayList<GitReference> getChoices() {
        return getArguments().getParcelableArrayList(ARG_CHOICES);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        switch (which) {
        case BUTTON_NEGATIVE:
            break;
        default:
            getArguments().putParcelable(ARG_SELECTED,
                    getChoices().get(which));
            onResult(RESULT_OK);
        }
    }
}
