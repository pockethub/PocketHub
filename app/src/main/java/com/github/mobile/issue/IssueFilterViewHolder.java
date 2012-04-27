package com.github.mobile.issue;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.util.AvatarUtils;
import com.madgag.android.listviews.ViewHolder;

/**
 * View holder for an {@link IssueFilter}
 */
public class IssueFilterViewHolder implements ViewHolder<IssueFilter> {

    private final AvatarUtils avatarHelper;

    private final ImageView avatarView;

    private final TextView repoText;

    private final TextView filterText;

    /**
     * Create holder for view
     *
     * @param view
     * @param avatarHelper
     */
    public IssueFilterViewHolder(final View view, final AvatarUtils avatarHelper) {
        this.avatarHelper = avatarHelper;
        avatarView = (ImageView) view.findViewById(id.iv_gravatar);
        repoText = (TextView) view.findViewById(id.tv_repo_name);
        filterText = (TextView) view.findViewById(id.tv_filter_summary);
    }

    @Override
    public void updateViewFor(final IssueFilter item) {
        avatarHelper.bind(avatarView, item.getRepository().getOwner());
        repoText.setText(item.getRepository().generateId());
        filterText.setText(item.toDisplay());
    }
}
