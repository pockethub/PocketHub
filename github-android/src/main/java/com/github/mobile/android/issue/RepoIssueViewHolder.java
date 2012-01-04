package com.github.mobile.android.issue;

import static android.text.Html.fromHtml;
import static com.github.mobile.android.util.Time.relativeTimeFor;
import static com.madgag.android.listviews.ReflectiveHolderFactory.reflectiveFactoryFor;
import android.graphics.Paint;
import android.view.View;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.madgag.android.listviews.ReflectiveHolderFactory;
import com.madgag.android.listviews.ViewHolder;
import com.madgag.android.listviews.ViewHolderFactory;

import org.eclipse.egit.github.core.Issue;

/**
 * View holder for an issue in a repository
 */
public class RepoIssueViewHolder implements ViewHolder<Issue> {

    /**
     * Factory for creating view holder
     */
    public static final ViewHolderFactory<Issue> FACTORY = reflectiveFactoryFor(RepoIssueViewHolder.class);

    private final TextView number;

    private final TextView title;

    private final TextView creation;

    private final TextView comments;

    /**
     * Create view holder
     *
     * @param v
     */
    public RepoIssueViewHolder(View v) {
        number = (TextView) v.findViewById(id.tv_issue_number);
        title = (TextView) v.findViewById(id.tv_issue_title);
        creation = (TextView) v.findViewById(id.tv_issue_creation);
        comments = (TextView) v.findViewById(id.tv_issue_comments);
    }

    @Override
    public void updateViewFor(Issue i) {
        number.setText("#" + i.getNumber());
        if (i.getClosedAt() != null)
            number.setPaintFlags(number.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        title.setText(i.getTitle());
        creation.setText(fromHtml("by <b>" + i.getUser().getLogin() + "</b> " + relativeTimeFor(i.getCreatedAt())));
        comments.setText(Integer.toString(i.getComments()));
    }

}
