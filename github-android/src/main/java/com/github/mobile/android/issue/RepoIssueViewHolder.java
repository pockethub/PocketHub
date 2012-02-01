package com.github.mobile.android.issue;

import static android.graphics.Paint.STRIKE_THRU_TEXT_FLAG;
import static android.text.Html.fromHtml;
import static com.github.mobile.android.util.Time.relativeTimeFor;
import android.graphics.Paint;
import android.view.View;
import android.widget.TextView;

import com.github.mobile.android.R.id;
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

    private final TextView creation;

    private final TextView comments;

    private final int flags;

    /**
     * Create view holder
     *
     * @param v
     * @param maxNumberCount
     */
    public RepoIssueViewHolder(View v, Integer maxNumberCount) {
        number = (TextView) v.findViewById(id.tv_issue_number);
        flags = number.getPaintFlags();
        title = (TextView) v.findViewById(id.tv_issue_title);
        creation = (TextView) v.findViewById(id.tv_issue_creation);
        comments = (TextView) v.findViewById(id.tv_issue_comments);

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
    public void updateViewFor(Issue i) {
        number.setText("#" + i.getNumber());
        if (i.getClosedAt() != null)
            number.setPaintFlags(flags | STRIKE_THRU_TEXT_FLAG);
        else
            number.setPaintFlags(flags);
        title.setText(i.getTitle());
        creation.setText(fromHtml("by <b>" + i.getUser().getLogin() + "</b> " + relativeTimeFor(i.getCreatedAt())));
        comments.setText(Integer.toString(i.getComments()));
    }
}
