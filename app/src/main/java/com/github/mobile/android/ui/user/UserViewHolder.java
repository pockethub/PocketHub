package com.github.mobile.android.ui.user;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.util.AvatarHelper;
import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.User;

/**
 * Holder for a user view that appears in a list
 */
public class UserViewHolder implements ViewHolder<User> {

    private final ImageView avatarView;

    private final TextView loginText;

    private final AvatarHelper avatarHelper;

    /**
     * Create user view holder
     *
     * @param view
     * @param avatarHelper
     */
    public UserViewHolder(final View view, final AvatarHelper avatarHelper) {
        avatarView = (ImageView) view.findViewById(id.iv_gravatar);
        loginText = (TextView) view.findViewById(id.tv_login);
        this.avatarHelper = avatarHelper;
    }

    @Override
    public void updateViewFor(User user) {
        avatarHelper.bind(avatarView, user);
        loginText.setText(user.getLogin());
    }
}
