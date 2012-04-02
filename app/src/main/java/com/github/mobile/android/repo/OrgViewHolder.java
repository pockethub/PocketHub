package com.github.mobile.android.repo;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.util.AvatarHelper;
import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.User;

/**
 * View holder to display a user/organization
 */
public class OrgViewHolder implements ViewHolder<User> {

    private final AvatarHelper avatarHelper;

    private final TextView nameText;

    private final ImageView avatarView;

    /**
     * Create org view holder
     *
     * @param view
     * @param avatarHelper
     */
    public OrgViewHolder(final View view, final AvatarHelper avatarHelper) {
        this.avatarHelper = avatarHelper;
        nameText = (TextView) view.findViewById(id.tv_org_name);
        avatarView = (ImageView) view.findViewById(id.iv_gravatar);
    }

    @Override
    public void updateViewFor(final User user) {
        nameText.setText(user.getLogin());
        avatarView.setBackgroundDrawable(null);
        avatarHelper.bind(avatarView, user);
    }
}
