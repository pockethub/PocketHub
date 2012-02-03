package com.github.mobile.android.repo;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.util.Avatar;
import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.User;

/**
 * View holder to display a user/organization
 */
public class OrgViewHolder implements ViewHolder<User> {

    private final Context context;

    private final TextView nameText;

    private final ImageView avatarView;

    /**
     * Create org view holder
     *
     * @param view
     * @param context
     */
    public OrgViewHolder(final View view, final Context context) {
        this.context = context;
        nameText = (TextView) view.findViewById(id.tv_org_name);
        avatarView = (ImageView) view.findViewById(id.iv_gravatar);
    }

    @Override
    public void updateViewFor(User user) {
        nameText.setText(user.getLogin());
        avatarView.setBackgroundDrawable(null);
        Avatar.bind(context, avatarView, user);
    }
}
