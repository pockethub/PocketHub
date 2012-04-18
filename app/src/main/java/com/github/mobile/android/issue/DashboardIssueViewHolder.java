package com.github.mobile.android.issue;

import com.github.mobile.android.R.id;
import com.github.mobile.android.util.AvatarHelper;
import com.github.mobile.android.util.TypefaceHelper;

import org.eclipse.egit.github.core.Issue;

import android.view.View;
import android.widget.TextView;

/**
 * Dashboard issue view holder
 */
public class DashboardIssueViewHolder extends RepoIssueViewHolder {

    private final TextView repoText;

    /**
     * Create dashboard issue view holder
     *
     * @param v
     * @param helper
     * @param maxNumberCount
     */
    public DashboardIssueViewHolder(View v, AvatarHelper helper, int maxNumberCount) {
        super(v, helper, maxNumberCount);
        repoText = (TextView) v.findViewById(id.tv_issue_repo_name);
        TypefaceHelper.setOctocons((TextView) v.findViewById(id.tv_comment_icon));
    }

    @Override
    public void updateViewFor(final Issue issue) {
        super.updateViewFor(issue);

        String[] segments = issue.getUrl().split("/");
        int length = segments.length;
        if (length >= 4)
            repoText.setText(segments[length - 4] + "/" + segments[length - 3]);
        else
            repoText.setText("");
    }
}
