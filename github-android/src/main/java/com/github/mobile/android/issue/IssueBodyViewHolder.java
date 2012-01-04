package com.github.mobile.android.issue;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.github.mobile.android.MarkdownViewHolder;
import com.github.mobile.android.R.id;
import com.github.mobile.android.util.HttpImageGetter;

import org.eclipse.egit.github.core.Issue;

/**
 * View holder for an issue's body
 */
public class IssueBodyViewHolder extends MarkdownViewHolder<Issue> {

    private final TextView issueBody;

    /**
     * @param context
     * @param imageGetter
     * @param view
     */
    public IssueBodyViewHolder(Context context, HttpImageGetter imageGetter, View view) {
        super(context, imageGetter);
        issueBody = (TextView) view.findViewById(id.tv_issue_body);
    }

    public void updateViewFor(Issue issue) {
        bindHtml(issueBody, issue.getBodyHtml());
    }
}
