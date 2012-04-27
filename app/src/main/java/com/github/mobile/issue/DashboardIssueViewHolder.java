package com.github.mobile.issue;

import android.view.View;
import android.widget.TextView;

import com.github.mobile.R.id;
import com.github.mobile.util.AvatarHelper;
import com.github.mobile.util.TypefaceHelper;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.egit.github.core.Issue;

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
     * @param numberWidth
     */
    public DashboardIssueViewHolder(View v, AvatarHelper helper, AtomicInteger numberWidth) {
        super(v, helper, numberWidth);
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
