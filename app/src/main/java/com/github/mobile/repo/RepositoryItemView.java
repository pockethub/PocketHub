package com.github.mobile.repo;

import android.view.View;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.ui.ItemView;
import com.github.mobile.util.TypefaceUtils;

/**
 * Item view for a repository
 */
public class RepositoryItemView extends ItemView {

    /**
     * Repository type icon
     */
    public final TextView repoIcon;

    /**
     * Repository name text
     */
    public final TextView repoName;

    /**
     * Recently view label
     */
    public final TextView recentLabel;

    /**
     * @param view
     */
    public RepositoryItemView(final View view) {
        super(view);

        repoIcon = (TextView) view.findViewById(id.tv_repo_icon);
        TypefaceUtils.setOctocons(repoIcon);
        repoName = (TextView) view.findViewById(id.tv_repo_name);
        recentLabel = (TextView) view.findViewById(id.tv_recent_label);
    }
}
