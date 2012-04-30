package com.github.mobile.repo;

import com.github.mobile.R.id;

import android.view.View;
import android.widget.TextView;

/**
 * Item view for a searched for repository
 */
public class SearchRepositoryItemView extends RepositoryItemView {

    /**
     * Repository description text view
     */
    public final TextView repoDescription;

    /**
     * @param view
     */
    public SearchRepositoryItemView(final View view) {
        super(view);

        repoDescription = (TextView) view.findViewById(id.tv_repo_description);
    }
}
