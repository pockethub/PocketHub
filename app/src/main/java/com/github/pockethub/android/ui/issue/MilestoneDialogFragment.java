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
import android.support.annotation.NonNull;
import android.view.View;

import com.github.pockethub.android.R;
import com.github.pockethub.android.ui.BaseActivity;
import com.github.pockethub.android.ui.SingleChoiceDialogFragment;
import com.github.pockethub.android.ui.item.dialog.MilestoneDialogItem;
import com.meisolsson.githubsdk.model.Milestone;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;

import java.util.ArrayList;
import java.util.Collection;

import io.reactivex.Observable;

import static android.app.Activity.RESULT_OK;


/**
 * Dialog fragment to select an issue milestone
 */
public class MilestoneDialogFragment extends SingleChoiceDialogFragment {

    /**
     * Get selected milestone from results bundle
     *
     * @param arguments
     * @return milestone
     */
    public static Milestone getSelected(Bundle arguments) {
        return (Milestone) arguments.getParcelable(ARG_SELECTED);
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
                            final String message, ArrayList<Milestone> choices,
                            final int selectedChoice) {
        show(activity, requestCode, title, message, choices, selectedChoice,
                new MilestoneDialogFragment());
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        int selected = getArguments().getInt(ARG_SELECTED_CHOICE);

        GroupAdapter adapter = new GroupAdapter();
        Collection<MilestoneDialogItem> items = Observable.fromIterable(getChoices())
                .map(ref -> new MilestoneDialogItem(ref, selected))
                .toList()
                .blockingGet();
        adapter.addAll(items);
        adapter.setOnItemClickListener(this);

        return createDialogBuilder()
                .adapter(adapter, null)
                .negativeText(R.string.cancel)
                .neutralText(R.string.clear)
                .onNeutral((dialog, which) -> onResult(RESULT_OK))
                .build();
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Milestone> getChoices() {
        return getArguments().getParcelableArrayList(ARG_CHOICES);
    }

    @Override
    public void onItemClick(@NonNull Item item, @NonNull View view) {
        super.onItemClick(item, view);
        if (item instanceof MilestoneDialogItem) {
            getArguments().putParcelable(ARG_SELECTED, ((MilestoneDialogItem) item).getData());
            onResult(RESULT_OK);
        }
    }
}
