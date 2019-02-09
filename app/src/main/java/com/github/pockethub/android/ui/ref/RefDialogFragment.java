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

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.SingleChoiceDialogFragment;
import com.github.pockethub.android.ui.item.dialog.RefDialogItem;
import com.meisolsson.githubsdk.model.git.GitReference;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * Dialog fragment to select a branch or tag
 */
public class RefDialogFragment extends SingleChoiceDialogFragment {

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
    public static void show(final BaseActivity activity, final int requestCode, final String title,
                            final String message, ArrayList<GitReference> choices,
                            final int selectedChoice) {
        show(activity, requestCode, title, message, choices, selectedChoice,
                new RefDialogFragment());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        int selected = getArguments().getInt(ARG_SELECTED_CHOICE);

        GroupAdapter adapter = new GroupAdapter();
        for (GitReference ref : getChoices()) {
            adapter.add(new RefDialogItem(ref, selected));
        }
        adapter.setOnItemClickListener(this);

        return createDialogBuilder()
                .adapter(adapter, null)
                .negativeText(R.string.cancel)
                .build();
    }

    @SuppressWarnings("unchecked")
    private ArrayList<GitReference> getChoices() {
        return getArguments().getParcelableArrayList(ARG_CHOICES);
    }


    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        super.onItemClick(item, view);
        if (item instanceof RefDialogItem) {
            getArguments().putParcelable(ARG_SELECTED, ((RefDialogItem) item).getGitReference());
            onResult(RESULT_OK);
        }
    }
}
