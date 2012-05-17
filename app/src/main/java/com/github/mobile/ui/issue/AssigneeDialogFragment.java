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
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.mobile.ui.DialogFragmentActivity;
import com.github.mobile.ui.DialogFragmentHelper;
import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.ui.ItemView;
import com.github.mobile.ui.LightAlertDialog;
import com.github.mobile.util.AvatarLoader;
import com.google.inject.Inject;
import com.viewpagerindicator.R.id;
import com.viewpagerindicator.R.layout;

import java.util.ArrayList;

import org.eclipse.egit.github.core.User;

/**
 * Dialog fragment to select an issue assignee from a list of collaborators
 */
public class AssigneeDialogFragment extends DialogFragmentHelper implements OnClickListener {

    /**
     * Arguments key for the selected item
     */
    public static final String ARG_SELECTED = "selected";

    private static final String ARG_CHOICES = "choices";

    private static final String ARG_SELECTED_CHOICE = "selectedChoice";

    private static final String TAG = "single_choice_dialog";

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

    private static class UserListAdapter extends ItemListAdapter<User, UserItemView> {

        private final int selected;

        private final AvatarLoader loader;

        public UserListAdapter(LayoutInflater inflater, User[] users, int selected, AvatarLoader loader) {
            super(layout.collaborator_item, inflater, users);

            this.selected = selected;
            this.loader = loader;
        }

        @Override
        protected void update(final int position, final UserItemView view, final User item) {
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
            final String message, ArrayList<User> choices, final int selectedChoice) {
        Bundle arguments = createArguments(title, message, requestCode);
        arguments.putSerializable(ARG_CHOICES, choices);
        arguments.putInt(ARG_SELECTED_CHOICE, selectedChoice);
        show(activity, new AssigneeDialogFragment(), arguments, TAG);
    }

    @Inject
    private AvatarLoader loader;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Activity activity = getActivity();
        Bundle arguments = getArguments();

        LightAlertDialog dialog = new LightAlertDialog(activity);
        dialog.setTitle(getTitle());
        dialog.setMessage(getMessage());
        dialog.setCancelable(true);
        dialog.setOnCancelListener(this);

        LayoutInflater inflater = activity.getLayoutInflater();

        ListView view = (ListView) inflater.inflate(layout.list_view, null);

        ArrayList<User> choices = getChoices();
        int selected = arguments.getInt(ARG_SELECTED_CHOICE);
        UserListAdapter adapter = new UserListAdapter(inflater, choices.toArray(new User[choices.size()]), selected,
                loader);
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
