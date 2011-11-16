package com.github.mobile.android.views;

import android.view.View;
import android.widget.TextView;
import com.madgag.android.listviews.ViewHolder;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.PullRequest;

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static com.github.mobile.android.R.id.tv_list_item_date;
import static com.github.mobile.android.R.id.tv_list_item_shortdesc;

public class PullRequestViewHolder implements ViewHolder<PullRequest> {
	private final TextView createdAt, title;

	public PullRequestViewHolder(View v) {
		createdAt = (TextView) v.findViewById(tv_list_item_date);
        title = (TextView) v.findViewById(tv_list_item_shortdesc);
	}

	@Override
	public void updateViewFor(PullRequest pr) {
		title.setText(pr.getTitle());
		createdAt.setText(getRelativeTimeSpanString(pr.getCreatedAt().getTime()));
	}
}
