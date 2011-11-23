package com.github.mobile.android.views;

import static com.github.mobile.android.R.id.tv_pull_request_comments;
import static com.github.mobile.android.R.id.tv_pull_request_description;
import static com.github.mobile.android.R.id.tv_pull_request_submission;
import static com.github.mobile.android.R.id.tv_pull_request_title;
import static com.github.mobile.android.R.id.tv_pull_request_update_date;
import static com.github.mobile.android.util.Time.relativeTimeFor;
import static org.eclipse.egit.github.core.RepositoryId.createFromUrl;

import android.view.View;
import android.widget.TextView;

import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryId;

public class PullRequestViewHolder implements ViewHolder<PullRequest> {
    private final TextView title, description, submission, updated, comments;

    public PullRequestViewHolder(View v) {
        title = (TextView) v.findViewById(tv_pull_request_title);
        description = (TextView) v.findViewById(tv_pull_request_description);
        submission = (TextView) v.findViewById(tv_pull_request_submission);
        updated = (TextView) v.findViewById(tv_pull_request_update_date);
        comments = (TextView) v.findViewById(tv_pull_request_comments);
    }

    @Override
    public void updateViewFor(PullRequest pr) {
        title.setText(pr.getTitle());
        description.setText(pr.getBody());
        String repo = createFromUrl(pr.getHtmlUrl()).generateId();
        submission.setText(pr.getUser().getLogin()+" submitted to "+repo +" "+relativeTimeFor(pr.getCreatedAt()));
        updated.setText(relativeTimeFor(pr.getUpdatedAt()));
        comments.setText(pr.getComments() + " comments");
    }
}
