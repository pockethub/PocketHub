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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.mobile.R.string;
import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemView;
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

    private static class UserItemView extends ItemView {

        public final TextView login;

        public final ImageView avatar;

        public final RadioButton selected;

        public UserItemView(final View view) {
            super(view);

            login = (TextView) view.findViewById(id.tv_login);
            avatar = (ImageView) view.findViewById(id.iv_avatar);
            selected = (RadioButton) view.findViewById(id.rb_selected);
        }
    }

    private static class UserListAdapter extends
            ItemListAdapter<User, UserItemView> {

        private final int selected;

        private final AvatarLoader loader;

        public UserListAdapter(LayoutInflater inflater, User[] users,
                int selected, AvatarLoader loader) {
            super(layout.collaborator_item, inflater, users);

            this.selected = selected;
            this.loader = loader;
        }

        @Override
        protected void update(final int position, final UserItemView view,
                final User item) {
            view.login.setText(item.getLogin());
            loader.bind(view.avatar, item);
            view.selected.setChecked(selected == position);
        }

        @Override
        protected UserItemView createView(View view) {
            return new UserItemView(view);
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
