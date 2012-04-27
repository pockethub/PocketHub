package com.github.mobile.repo;

import android.view.View;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.util.TypefaceHelper;
import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.SearchRepository;

/**
 * View holder for a search repository displayed in a list
 */
public class SearchRepoViewHolder implements ViewHolder<SearchRepository> {

    private final TextView repoIcon;

    private final TextView repoDescription;

    private final TextView repoName;

    /**
     * Create search repository view holder
     *
     * @param view
     */
    public SearchRepoViewHolder(final View view) {
        repoIcon = (TextView) view.findViewById(id.tv_repo_icon);
        TypefaceHelper.setOctocons(repoIcon);
        repoName = (TextView) view.findViewById(id.tv_repo_name);
        repoDescription = (TextView) view.findViewById(id.tv_repo_description);
    }

    @Override
    public void updateViewFor(final SearchRepository repo) {
        if (repo.isFork())
            repoIcon.setText("\uf202");
        else
            repoIcon.setText("\uf201");

        repoName.setText(repo.generateId());
        repoDescription.setText(repo.getDescription());
    }
}
