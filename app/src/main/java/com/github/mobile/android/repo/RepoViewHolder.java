package com.github.mobile.android.repo;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import android.view.View;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.util.TypefaceHelper;
import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * View holder for a repository displayed in a list
 */
public class RepoViewHolder implements ViewHolder<Repository> {

    private final TextView repoIcon;

    private final TextView repoName;

    private final TextView recentLabel;

    private final String userLogin;

    private final RecentReposHelper recentIds;

    /**
     * Create repository view holder
     *
     * @param view
     * @param user
     * @param recentIds
     */
    public RepoViewHolder(final View view, final User user, final RecentReposHelper recentIds) {
        this.userLogin = user.getLogin();
        repoIcon = (TextView) view.findViewById(id.tv_repo_icon);
        repoIcon.setTypeface(TypefaceHelper.getOctocons(view.getContext()));
        repoName = (TextView) view.findViewById(id.tv_repo_name);
        recentLabel = (TextView) view.findViewById(id.tv_recent_label);
        this.recentIds = recentIds;
    }

    @Override
    public void updateViewFor(final Repository repo) {
        String id = repo.generateId();

        if (repo.isPrivate())
            repoIcon.setText("\uf200");
        else if (repo.isFork())
            repoIcon.setText("\uf202");
        else
            repoIcon.setText("\uf201");

        recentLabel.setVisibility(recentIds.contains(id) ? VISIBLE : GONE);

        repoName.setText(userLogin.equals(repo.getOwner().getLogin()) ? repo.getName() : id);
    }
}
