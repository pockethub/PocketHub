package com.github.mobile.android.views;

import static android.text.Html.fromHtml;
import static com.github.mobile.android.R.id.tv_issue_comments;
import static com.github.mobile.android.R.id.tv_issue_creation;
import static com.github.mobile.android.R.id.tv_issue_number;
import static com.github.mobile.android.R.id.tv_issue_repo_name;
import static com.github.mobile.android.R.id.tv_issue_title;
import static com.github.mobile.android.util.Time.relativeTimeFor;
import static org.eclipse.egit.github.core.RepositoryId.createFromUrl;
import android.view.View;
import android.widget.TextView;

import com.madgag.android.listviews.ViewHolder;

import org.eclipse.egit.github.core.Issue;

public class IssueViewHolder implements ViewHolder<Issue> {

    public static final String TAG = "IVH";

    private final TextView number, repo, title, creation, comments;

    public IssueViewHolder(View v) {
        number = (TextView) v.findViewById(tv_issue_number);
        number.setWidth((int) number.getPaint().measureText("#00"));
        repo = (TextView) v.findViewById(tv_issue_repo_name);
        title = (TextView) v.findViewById(tv_issue_title);
        creation = (TextView) v.findViewById(tv_issue_creation);
        comments = (TextView) v.findViewById(tv_issue_comments);
    }

    @Override
    public void updateViewFor(Issue i) {
        number.setText("#" + i.getNumber());
        repo.setText(createFromUrl(i.getHtmlUrl()).getName() + " » ");
        title.setText(i.getTitle());
        creation.setText(fromHtml("by <b>" + i.getUser().getLogin() + "</b> " + relativeTimeFor(i.getCreatedAt())));
        comments.setText(i.getComments() + " comments");
    }
}
