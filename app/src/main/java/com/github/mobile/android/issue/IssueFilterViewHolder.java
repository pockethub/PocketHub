package com.github.mobile.android.issue;

import android.view.View;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.madgag.android.listviews.ViewHolder;

/**
 * View holder for an {@link IssueFilter}
 */
public class IssueFilterViewHolder implements ViewHolder<IssueFilter> {

    private final TextView repoText;

    private final TextView filterText;

    /**
     * Create holder for view
     *
     * @param view
     */
    public IssueFilterViewHolder(final View view) {
        repoText = (TextView) view.findViewById(id.tv_repo_name);
        filterText = (TextView) view.findViewById(id.tv_filter_summary);
    }

    @Override
    public void updateViewFor(final IssueFilter item) {
        repoText.setText(item.getRepository().generateId());
        filterText.setText(item.toDisplay());
    }
}
