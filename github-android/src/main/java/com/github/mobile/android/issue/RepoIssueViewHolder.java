package com.github.mobile.android.issue;

import static android.graphics.Paint.STRIKE_THRU_TEXT_FLAG;
import static android.text.Html.fromHtml;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.mobile.android.util.Time.relativeTimeFor;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mobile.android.R.id;
import com.github.mobile.android.util.AvatarHelper;
import com.madgag.android.listviews.ViewHolder;

import java.util.Arrays;

import org.eclipse.egit.github.core.Issue;

/**
 * View holder for an issue in a repository
 */
public class RepoIssueViewHolder implements ViewHolder<Issue> {

    /**
     * Find the maximum number of digits in the given issue numbers
     *
     * @param issues
     * @return max digits
     */
    public static int computeMaxDigits(Iterable<Issue> issues) {
        int max = 1;
        for (Issue issue : issues)
            max = Math.max(max, (int) Math.log10(issue.getNumber()) + 1);
        return max;
    }

    private final TextView number;

    private final TextView title;

    private final ImageView gravatar;

    private final TextView creation;

    private final TextView comments;

    private final ImageView pullRequestIcon;

    private final AvatarHelper helper;

    private final int flags;

    /**
     * Create view holder
     *
     * @param v
     * @param helper
     * @param maxNumberCount
     */
    public RepoIssueViewHolder(View v, AvatarHelper helper, int maxNumberCount) {
        number = (TextView) v.findViewById(id.tv_issue_number);
        flags = number.getPaintFlags();
        title = (TextView) v.findViewById(id.tv_issue_title);
        gravatar = (ImageView) v.findViewById(id.iv_gravatar);
        creation = (TextView) v.findViewById(id.tv_issue_creation);
        comments = (TextView) v.findViewById(id.tv_issue_comments);
        pullRequestIcon = (ImageView) v.findViewById(id.iv_pull_request);

        this.helper = helper;

        // Set number field to max number size
        Paint paint = new Paint();
        paint.setTypeface(number.getTypeface());
        paint.setTextSize(number.getTextSize());
        char[] text = new char[maxNumberCount + 1];
        Arrays.fill(text, '0');
        text[0] = '#';
        number.getLayoutParams().width = Math.round(paint.measureText(text, 0, text.length));
    }

    @Override
    public void updateViewFor(Issue issue) {
        number.setText("#" + issue.getNumber());
        if (issue.getClosedAt() != null)
            number.setPaintFlags(flags | STRIKE_THRU_TEXT_FLAG);
        else
            number.setPaintFlags(flags);

        helper.bind(gravatar, issue.getUser());

        if (issue.getPullRequest().getHtmlUrl() != null)
            pullRequestIcon.setVisibility(VISIBLE);
        else
            pullRequestIcon.setVisibility(GONE);

        title.setText(issue.getTitle());
        creation.setText(fromHtml("<b>" + issue.getUser().getLogin() + "</b> " + relativeTimeFor(issue.getCreatedAt())));
        comments.setText(Integer.toString(issue.getComments()));
    }
}
