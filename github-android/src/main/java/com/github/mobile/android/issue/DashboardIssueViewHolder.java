package com.github.mobile.android.issue;

import com.github.mobile.android.R.id;

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
     * @param maxNumberCount
     */
    public DashboardIssueViewHolder(View v, Integer maxNumberCount) {
        super(v, maxNumberCount);
        repoText = (TextView) v.findViewById(id.tv_issue_repo_name);
    }

    @Override
    public void updateViewFor(Issue issue) {
        super.updateViewFor(issue);
        String[] segments = issue.getUrl().split("/");
        int length = segments.length;
        if (length >= 4)
            repoText.setText(segments[length - 4] + "/" + segments[length - 3]);
        else
            repoText.setText("");
    }
}
