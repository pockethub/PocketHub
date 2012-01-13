package com.github.mobile.android.repo;

import android.view.View;
import android.widget.TextView;

import com.github.mobile.android.R;
import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

public class RepoViewHolder implements ViewHolder<Repository> {
    
    private final TextView repoName;
    private final String userLogin;

    public RepoViewHolder(View view, User user) {
        this.userLogin = user.getLogin();
        repoName = (TextView) view.findViewById(R.id.tv_repo_name);
    }
    
    @Override
    public void updateViewFor(Repository repo) {
        repoName.setText(userLogin.equals(repo.getOwner().getLogin())?repo.getName():repo.generateId());
    }
}
