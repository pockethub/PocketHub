package com.github.mobile.android.views;

import android.view.View;
import android.widget.TextView;
import com.madgag.android.listviews.ViewHolder;
import org.eclipse.egit.github.core.PullRequest;

import static com.github.mobile.android.R.id.*;
import static com.github.mobile.android.util.Time.relativeTimeFor;

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
        submission.setText(relativeTimeFor(pr.getCreatedAt()));
        updated.setText(relativeTimeFor(pr.getUpdatedAt()));
        comments.setText(pr.getComments()+" comments");
	}
}
