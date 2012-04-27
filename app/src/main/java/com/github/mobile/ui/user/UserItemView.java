package com.github.mobile.ui.user;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.ui.ItemView;

/**
 * Item view of a user
 */
public class UserItemView extends ItemView {

    /**
     * Avatar image view
     */
    public final ImageView avatarView;

    /**
     * Login text view
     */
    public final TextView loginText;

    /**
     * @param view
     */
    public UserItemView(final View view) {
        super(view);

        avatarView = (ImageView) view.findViewById(id.iv_gravatar);
        loginText = (TextView) view.findViewById(id.tv_login);
    }
}
