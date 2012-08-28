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
package com.github.mobile.ui.issue;

import static android.app.Activity.RESULT_OK;
import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_NEUTRAL;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;
import com.github.mobile.R.string;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.SingleChoiceDialogFragment;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;
import com.viewpagerindicator.R.id;
import com.viewpagerindicator.R.layout;

import java.util.ArrayList;

import org.eclipse.egit.github.core.User;

/**
 * Dialog fragment to select an issue assignee from a list of collaborators
 */
public class AssigneeDialogFragment extends SingleChoiceDialogFragment {

    private static class UserListAdapter extends SingleTypeAdapter<User> {

        private final int selected;

        private final AvatarLoader loader;

        public UserListAdapter(LayoutInflater inflater, User[] users,
                int selected, AvatarLoader loader) {
            super(inflater, layout.collaborator_item);

            this.selected = selected;
            this.loader = loader;
            setItems(users);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        @Override
        protected int[] getChildViewIds() {
            return new int[] { id.tv_login, id.iv_avatar, id.rb_selected };
        }

        @Override
        protected void update(int position, User item) {
            setText(id.tv_login, item.getLogin());
            loader.bind(imageView(id.iv_avatar), item);
            setChecked(id.rb_selected, selected == position);
        }
    }

    /**
     * Get selected user from results bundle
     *
     * @param arguments
     * @return user
     */
    public static User getSelected(Bundle arguments) {
        return (User) arguments.getSerializable(ARG_SELECTED);
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
    public static void show(final DialogFragmentActivity activity,
            final int requestCode, final String title, final String message,
            ArrayList<User> choices, final int selectedChoice) {
        show(activity, requestCode, title, message, choices, selectedChoice,
                new AssigneeDialogFragment());
    }

    @Inject
    private AvatarLoader loader;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Activity activity = getActivity();
        Bundle arguments = getArguments();

        final AlertDialog dialog = createDialog();
        dialog.setButton(BUTTON_NEGATIVE, activity.getString(string.cancel),
                this);
        dialog.setButton(BUTTON_NEUTRAL, activity.getString(string.clear), this);

        LayoutInflater inflater = activity.getLayoutInflater();

        ListView view = (ListView) inflater.inflate(layout.dialog_list_view,
                null);
        view.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                onClick(dialog, position);
            }
        });

        ArrayList<User> choices = getChoices();
        int selected = arguments.getInt(ARG_SELECTED_CHOICE);
        UserListAdapter adapter = new UserListAdapter(inflater,
                choices.toArray(new User[choices.size()]), selected, loader);
        view.setAdapter(adapter);
        if (selected >= 0)
            view.setSelection(selected);
        dialog.setView(view);

        return dialog;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<User> getChoices() {
        return (ArrayList<User>) getArguments().getSerializable(ARG_CHOICES);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        switch (which) {
        case BUTTON_NEGATIVE:
            break;
        case BUTTON_NEUTRAL:
            onResult(RESULT_OK);
            break;
        default:
            getArguments().putSerializable(ARG_SELECTED,
                    getChoices().get(which));
            onResult(RESULT_OK);
        }
    }
}
