package com.github.mobile.android.repo;

import static android.view.View.GONE;
import android.view.View;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.SearchRepository;

/**
 * View holder for a search repository displayed in a list
 */
public class SearchRepoViewHolder implements ViewHolder<SearchRepository> {

    private final TextView repoName;

    /**
     * Create search repository view holder
     *
     * @param view
     */
    public SearchRepoViewHolder(final View view) {
        repoName = (TextView) view.findViewById(id.tv_repo_name);
        view.findViewById(id.tv_recent_label).setVisibility(GONE);
    }

    @Override
    public void updateViewFor(final SearchRepository repo) {
        repoName.setText(repo.generateId());
    }
}
