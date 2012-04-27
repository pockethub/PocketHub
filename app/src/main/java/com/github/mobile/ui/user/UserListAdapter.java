package com.github.mobile.ui.user;

import android.view.LayoutInflater;
import android.view.View;

import com.github.mobile.ui.ItemListAdapter;
import com.github.mobile.util.AvatarUtils;
import com.viewpagerindicator.R.layout;

import org.eclipse.egit.github.core.User;

/**
 * List adapter for a list of users
 */
public class UserListAdapter extends ItemListAdapter<User, UserItemView> {

    private final AvatarUtils avatarHelper;

    /**
     * Create user list adapter
     *
     * @param inflater
     * @param elements
     * @param avatarHelper
     */
    public UserListAdapter(final LayoutInflater inflater, final User[] elements, final AvatarUtils avatarHelper) {
        super(layout.user_list_item, inflater, elements);

        this.avatarHelper = avatarHelper;
    }

    /**
     * Create user list adapter
     *
     * @param inflater
     * @param avatarHelper
     */
    public UserListAdapter(final LayoutInflater inflater, final AvatarUtils avatarHelper) {
        this(inflater, null, avatarHelper);
    }

    @Override
    protected void update(final UserItemView view, final User user) {
        avatarHelper.bind(view.avatarView, user);
        view.loginText.setText(user.getLogin());
    }

    @Override
    protected UserItemView createView(final View view) {
        return new UserItemView(view);
    }
}
